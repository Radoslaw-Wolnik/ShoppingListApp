package com.example.shoppinglistapp.data.sync;

import android.content.Context;
import android.util.Log;

import com.example.shoppinglistapp.data.local.AppDatabase;
import com.example.shoppinglistapp.data.local.entity.Category;
import com.example.shoppinglistapp.data.local.entity.Item;
import com.example.shoppinglistapp.data.local.entity.OutboxEntity;
import com.example.shoppinglistapp.data.local.entity.ShoppingList;
import com.example.shoppinglistapp.data.remote.RemoteDataSource;
import com.example.shoppinglistapp.data.remote.api.models.CategoryDto;
import com.example.shoppinglistapp.data.remote.api.models.DeviceShoppingListHeaderDto;
import com.example.shoppinglistapp.data.remote.api.models.ItemDto;
import com.example.shoppinglistapp.data.remote.api.models.ShoppingListDto;
import com.example.shoppinglistapp.data.remote.signalr.SignalRService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncManager {
    private static final String TAG = "SyncManager";

    private final Context context;
    private final AppDatabase db;
    private final RemoteDataSource remoteDataSource;
    private final SignalRService signalRService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();
    private final Object syncLock = new Object();

    public SyncManager(Context context, RemoteDataSource remoteDataSource, SignalRService signalRService) {
        this.context = context.getApplicationContext();
        this.db = AppDatabase.getInstance(context);
        this.remoteDataSource = remoteDataSource;
        this.signalRService = signalRService;
    }

    public void sync() {
        executor.execute(this::syncBlocking);
    }

    public boolean syncBlocking() {
        synchronized (syncLock) {
            if (!ConnectivityHelper.isNetworkAvailable(context)) {
                return false;
            }

            try {
                String apiKey = remoteDataSource.ensureRegistered();
                signalRService.start(apiKey);
                uploadLocalTreesWithoutRemoteIds();
                uploadPendingOperations();
                downloadRemoteLists();
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Sync failed", e);
                return false;
            }
        }
    }

    private void uploadLocalTreesWithoutRemoteIds() throws IOException {
        List<ShoppingList> lists = db.shoppingListDao().getAllShoppingListsSync();
        for (ShoppingList list : lists) {
            ensureListRemoteId(list.getId());

            List<Category> categories = db.categoryDao().getCategoriesByShoppingListIdSync(list.getId());
            for (Category category : categories) {
                ensureCategoryRemoteId(category.getId());

                List<Item> items = db.itemDao().getItemsByCategoryIdSync(category.getId());
                for (Item item : items) {
                    ensureItemRemoteId(item.getId());
                }
            }
        }
    }

    private void uploadPendingOperations() throws IOException {
        List<OutboxEntity> pending = db.outboxDao().getAllPending();
        for (OutboxEntity operation : pending) {
            boolean success = uploadOperation(operation);
            if (success) {
                db.outboxDao().delete(operation);
            } else {
                return;
            }
        }
    }

    private boolean uploadOperation(OutboxEntity operation) throws IOException {
        switch (operation.type) {
            case OutboxEntity.CREATE_LIST:
                return ensureListRemoteId(operation.objectId) != null || !listExists(operation.objectId);
            case OutboxEntity.UPDATE_LIST_TITLE:
                return uploadListTitle(operation.objectId);
            case OutboxEntity.DELETE_LIST:
                return deleteRemoteList(operation);
            case OutboxEntity.RESET_LIST:
                return resetRemoteList(operation.objectId);
            case OutboxEntity.CREATE_CATEGORY:
                return ensureCategoryRemoteId(operation.objectId) != null || !categoryExists(operation.objectId);
            case OutboxEntity.UPDATE_CATEGORY_NAME:
                return uploadCategoryName(operation.objectId);
            case OutboxEntity.DELETE_CATEGORY:
                return deleteRemoteCategory(operation);
            case OutboxEntity.CREATE_ITEM:
                return ensureItemRemoteId(operation.objectId) != null || !itemExists(operation.objectId);
            case OutboxEntity.UPDATE_ITEM_DESCRIPTION:
                return uploadItemDescription(operation.objectId);
            case OutboxEntity.TOGGLE_ITEM:
                return uploadItemChecked(operation.objectId);
            case OutboxEntity.DELETE_ITEM:
                return deleteRemoteItem(operation);
            default:
                Log.w(TAG, "Dropping unknown sync operation: " + operation.type);
                return true;
        }
    }

    private String ensureListRemoteId(long listId) throws IOException {
        ShoppingList list = db.shoppingListDao().getShoppingListSync(listId);
        if (list == null) return null;
        if (hasText(list.getRemoteId())) return list.getRemoteId();

        String remoteId = remoteDataSource.createShoppingList(nonEmpty(list.getTitle(), "New List"));
        db.shoppingListDao().updateRemoteId(listId, remoteId);
        return remoteId;
    }

    private String ensureCategoryRemoteId(long categoryId) throws IOException {
        Category category = db.categoryDao().getCategorySync(categoryId);
        if (category == null) return null;
        if (hasText(category.getRemoteId())) return category.getRemoteId();

        String remoteListId = ensureListRemoteId(category.getShoppingListId());
        if (!hasText(remoteListId)) return null;

        String remoteId = remoteDataSource.addCategory(remoteListId, nonEmpty(category.getName(), "Category"));
        db.categoryDao().updateRemoteId(categoryId, remoteId);
        return remoteId;
    }

    private String ensureItemRemoteId(long itemId) throws IOException {
        Item item = db.itemDao().getItemSync(itemId);
        if (item == null) return null;
        if (hasText(item.getRemoteId())) return item.getRemoteId();

        String remoteCategoryId = ensureCategoryRemoteId(item.getCategoryId());
        if (!hasText(remoteCategoryId)) return null;

        String remoteId = remoteDataSource.addItem(remoteCategoryId, nonEmpty(item.getDescription(), "Item"));
        db.itemDao().updateRemoteId(itemId, remoteId);
        if (item.isDone()) {
            remoteDataSource.toggleItem(remoteId, true);
        }
        return remoteId;
    }

    private boolean uploadListTitle(long listId) throws IOException {
        ShoppingList list = db.shoppingListDao().getShoppingListSync(listId);
        if (list == null) return true;

        String remoteId = ensureListRemoteId(listId);
        if (!hasText(remoteId)) return false;

        remoteDataSource.updateShoppingListTitle(remoteId, nonEmpty(list.getTitle(), "New List"));
        return true;
    }

    private boolean resetRemoteList(long listId) throws IOException {
        ShoppingList list = db.shoppingListDao().getShoppingListSync(listId);
        if (list == null) return true;

        String remoteId = ensureListRemoteId(listId);
        if (!hasText(remoteId)) return false;

        remoteDataSource.resetCheckedItems(remoteId);
        return true;
    }

    private boolean uploadCategoryName(long categoryId) throws IOException {
        Category category = db.categoryDao().getCategorySync(categoryId);
        if (category == null) return true;

        String remoteId = ensureCategoryRemoteId(categoryId);
        if (!hasText(remoteId)) return false;

        remoteDataSource.updateCategory(remoteId, nonEmpty(category.getName(), "Category"));
        return true;
    }

    private boolean uploadItemDescription(long itemId) throws IOException {
        Item item = db.itemDao().getItemSync(itemId);
        if (item == null) return true;

        String remoteId = ensureItemRemoteId(itemId);
        if (!hasText(remoteId)) return false;

        remoteDataSource.updateItem(remoteId, nonEmpty(item.getDescription(), "Item"));
        return true;
    }

    private boolean uploadItemChecked(long itemId) throws IOException {
        Item item = db.itemDao().getItemSync(itemId);
        if (item == null) return true;

        String remoteId = ensureItemRemoteId(itemId);
        if (!hasText(remoteId)) return false;

        remoteDataSource.toggleItem(remoteId, item.isDone());
        return true;
    }

    private boolean deleteRemoteList(OutboxEntity operation) throws IOException {
        String remoteId = readDataString(operation.data, "remoteId");
        if (hasText(remoteId)) {
            remoteDataSource.deleteShoppingList(remoteId);
        }
        return true;
    }

    private boolean deleteRemoteCategory(OutboxEntity operation) throws IOException {
        String remoteId = readDataString(operation.data, "remoteId");
        if (hasText(remoteId)) {
            remoteDataSource.deleteCategory(remoteId);
        }
        return true;
    }

    private boolean deleteRemoteItem(OutboxEntity operation) throws IOException {
        String remoteId = readDataString(operation.data, "remoteId");
        if (hasText(remoteId)) {
            remoteDataSource.deleteItem(remoteId);
        }
        return true;
    }

    private void downloadRemoteLists() throws IOException {
        List<DeviceShoppingListHeaderDto> remoteHeaders = remoteDataSource.getShoppingLists();
        Set<String> remoteListIds = new HashSet<>();

        for (DeviceShoppingListHeaderDto header : remoteHeaders) {
            if (!hasText(header.id)) continue;
            remoteListIds.add(header.id);
            ShoppingListDto dto = remoteDataSource.getShoppingList(header.id);
            db.runInTransaction(() -> applyRemoteList(dto));
        }

        removeListsMissingFromBackend(remoteListIds);
    }

    private void applyRemoteList(ShoppingListDto dto) {
        if (dto == null || !hasText(dto.id)) return;

        ShoppingList localList = db.shoppingListDao().getShoppingListByRemoteId(dto.id);
        long localListId;
        if (localList == null) {
            ShoppingList newList = new ShoppingList(nonEmpty(dto.title, "New List"), new Date());
            newList.setRemoteId(dto.id);
            localListId = db.shoppingListDao().insert(newList);
        } else {
            localListId = localList.getId();
            localList.setTitle(nonEmpty(dto.title, "New List"));
            db.shoppingListDao().update(localList);
        }

        Set<String> remoteCategoryIds = new HashSet<>();
        if (dto.categories != null) {
            for (CategoryDto categoryDto : dto.categories) {
                if (categoryDto == null || !hasText(categoryDto.id)) continue;
                remoteCategoryIds.add(categoryDto.id);
                long localCategoryId = upsertRemoteCategory(localListId, categoryDto);
                applyRemoteItems(localCategoryId, categoryDto);
            }
        }

        removeCategoriesMissingFromRemote(localListId, remoteCategoryIds);
    }

    private long upsertRemoteCategory(long localListId, CategoryDto dto) {
        Category category = db.categoryDao().getCategoryByRemoteId(dto.id);
        if (category == null) {
            Category newCategory = new Category(nonEmpty(dto.name, "Category"), localListId);
            newCategory.setPosition(dto.position);
            newCategory.setRemoteId(dto.id);
            return db.categoryDao().insert(newCategory);
        }

        category.setName(nonEmpty(dto.name, "Category"));
        category.setPosition(dto.position);
        category.setShoppingListId(localListId);
        db.categoryDao().update(category);
        return category.getId();
    }

    private void applyRemoteItems(long localCategoryId, CategoryDto categoryDto) {
        Set<String> remoteItemIds = new HashSet<>();

        if (categoryDto.items != null) {
            for (ItemDto itemDto : categoryDto.items) {
                if (itemDto == null || !hasText(itemDto.id)) continue;
                remoteItemIds.add(itemDto.id);
                upsertRemoteItem(localCategoryId, itemDto);
            }
        }

        removeItemsMissingFromRemote(localCategoryId, remoteItemIds);
    }

    private void upsertRemoteItem(long localCategoryId, ItemDto dto) {
        Item item = db.itemDao().getItemByRemoteId(dto.id);
        if (item == null) {
            Item newItem = new Item(nonEmpty(dto.description, "Item"), localCategoryId);
            newItem.setDone(dto.isChecked);
            newItem.setPosition(dto.position);
            newItem.setRemoteId(dto.id);
            db.itemDao().insert(newItem);
            return;
        }

        item.setDescription(nonEmpty(dto.description, "Item"));
        item.setDone(dto.isChecked);
        item.setPosition(dto.position);
        item.setCategoryId(localCategoryId);
        db.itemDao().update(item);
    }

    private void removeListsMissingFromBackend(Set<String> remoteListIds) {
        List<ShoppingList> localLists = db.shoppingListDao().getAllShoppingListsSync();
        for (ShoppingList local : localLists) {
            String remoteId = local.getRemoteId();
            if (hasText(remoteId) && !remoteListIds.contains(remoteId)) {
                db.shoppingListDao().delete(local);
            }
        }
    }

    private void removeCategoriesMissingFromRemote(long localListId, Set<String> remoteCategoryIds) {
        List<Category> localCategories = db.categoryDao().getCategoriesByShoppingListIdSync(localListId);
        for (Category local : localCategories) {
            String remoteId = local.getRemoteId();
            if (hasText(remoteId) && !remoteCategoryIds.contains(remoteId)) {
                db.categoryDao().delete(local);
            }
        }
    }

    private void removeItemsMissingFromRemote(long localCategoryId, Set<String> remoteItemIds) {
        List<Item> localItems = db.itemDao().getItemsByCategoryIdSync(localCategoryId);
        for (Item local : localItems) {
            String remoteId = local.getRemoteId();
            if (hasText(remoteId) && !remoteItemIds.contains(remoteId)) {
                db.itemDao().delete(local);
            }
        }
    }

    private boolean listExists(long listId) {
        return db.shoppingListDao().getShoppingListSync(listId) != null;
    }

    private boolean categoryExists(long categoryId) {
        return db.categoryDao().getCategorySync(categoryId) != null;
    }

    private boolean itemExists(long itemId) {
        return db.itemDao().getItemSync(itemId) != null;
    }

    private String readDataString(String json, String key) {
        if (json == null || json.trim().isEmpty()) return null;
        JsonObject object = gson.fromJson(json, JsonObject.class);
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) return null;
        return object.get(key).getAsString();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String nonEmpty(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }
}
