package com.example.shoppinglistapp.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.shoppinglistapp.MyApp;
import com.example.shoppinglistapp.data.local.AppDatabase;
import com.example.shoppinglistapp.data.local.dao.CategoryDao;
import com.example.shoppinglistapp.data.local.dao.ItemDao;
import com.example.shoppinglistapp.data.local.dao.OutboxDao;
import com.example.shoppinglistapp.data.local.dao.ShoppingListDao;
import com.example.shoppinglistapp.data.local.entity.Category;
import com.example.shoppinglistapp.data.local.entity.Item;
import com.example.shoppinglistapp.data.local.entity.OutboxEntity;
import com.example.shoppinglistapp.data.local.entity.ShoppingList;
import com.example.shoppinglistapp.data.local.queryresult.CategoryWithItems;
import com.example.shoppinglistapp.data.local.queryresult.ShoppingListWithAllItems;
import com.example.shoppinglistapp.data.local.queryresult.ShoppingListWithCount;
import com.example.shoppinglistapp.data.sync.SyncManager;
import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShoppingListRepository {
    private final ShoppingListDao shoppingListDao;
    private final CategoryDao categoryDao;
    private final ItemDao itemDao;
    private final OutboxDao outboxDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(); // for background writes
    private final AppDatabase db;
    private final Gson gson = new Gson();

    public ShoppingListRepository(Application application) {
        db = AppDatabase.getInstance(application);
        shoppingListDao = db.shoppingListDao();
        categoryDao = db.categoryDao();
        itemDao = db.itemDao();
        outboxDao = db.outboxDao();
    }

    // --- Read operations (return LiveData from Room, already on background thread) ---

    public LiveData<List<ShoppingListWithCount>> getAllShoppingListsWithCounts() {
        return shoppingListDao.getAllShoppingListsWithCounts(); // LiveData from DAO
    }

    public LiveData<ShoppingListWithAllItems> getShoppingListWithAllItems(long shoppingListId) {
        return shoppingListDao.getShoppingListWithAllItemsById(shoppingListId);
    }

    // --- Write operations (must be on background thread) ---

    public void insertShoppingListWithDefaultCategory(
            ShoppingList newList,
            boolean addDefaultCategory,
            String defaultCategoryName,
            OnInsertCompleteListener listener) {

        executor.execute(() -> {
            final long[] insertedListId = {-1L};
            db.runInTransaction(() -> {
                long listId = shoppingListDao.insert(newList);
                insertedListId[0] = listId;
                enqueue(OutboxEntity.CREATE_LIST, listId, listId, titleData(newList.getTitle()));

                if (addDefaultCategory && defaultCategoryName != null && !defaultCategoryName.trim().isEmpty()) {
                    Category defaultCat = new Category(defaultCategoryName, listId);
                    long categoryId = categoryDao.insert(defaultCat);
                    enqueue(OutboxEntity.CREATE_CATEGORY, listId, categoryId, nameData(defaultCategoryName));
                }
            });

            // Post result back to main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                if (listener != null) listener.onInsertComplete(insertedListId[0]);
            });
            requestSync();
        });
    }

    // Define a callback interface
    public interface OnInsertCompleteListener {
        void onInsertComplete(long id);
    }

    public void updateShoppingList(final ShoppingList shoppingList) {
        executor.execute(() -> {
            shoppingListDao.update(shoppingList);
            enqueue(OutboxEntity.UPDATE_LIST_TITLE, shoppingList.getId(), shoppingList.getId(), titleData(shoppingList.getTitle()));
            requestSync();
        });
    }

    public void deleteShoppingList(final ShoppingList shoppingList) {
        executor.execute(() -> {
            ShoppingList existing = shoppingListDao.getShoppingListSync(shoppingList.getId());
            String remoteId = existing != null ? existing.getRemoteId() : shoppingList.getRemoteId();
            shoppingListDao.delete(shoppingList);
            if (remoteId != null) {
                enqueue(OutboxEntity.DELETE_LIST, shoppingList.getId(), shoppingList.getId(), remoteIdData(remoteId));
            }
            requestSync();
        });
    }

    // Similarly for Category and Item operations
    public void insertCategory(final Category category) {
        executor.execute(() -> {
            long categoryId = categoryDao.insert(category);
            enqueue(OutboxEntity.CREATE_CATEGORY, category.getShoppingListId(), categoryId, nameData(category.getName()));
            requestSync();
        });
    }

    public void updateCategory(final Category category) {
        executor.execute(() -> {
            categoryDao.update(category);
            enqueue(OutboxEntity.UPDATE_CATEGORY_NAME, category.getShoppingListId(), category.getId(), nameData(category.getName()));
            requestSync();
        });
    }

    public void deleteCategory(final Category category) {
        executor.execute(() -> {
            Category existing = categoryDao.getCategorySync(category.getId());
            String remoteId = existing != null ? existing.getRemoteId() : category.getRemoteId();
            long listId = existing != null ? existing.getShoppingListId() : category.getShoppingListId();
            categoryDao.delete(category);
            if (remoteId != null) {
                enqueue(OutboxEntity.DELETE_CATEGORY, listId, category.getId(), remoteIdData(remoteId));
            }
            requestSync();
        });
    }

    public void insertItem(final Item item) {
        executor.execute(() -> {
            long itemId = itemDao.insert(item);
            Category category = categoryDao.getCategorySync(item.getCategoryId());
            long listId = category != null ? category.getShoppingListId() : 0;
            enqueue(OutboxEntity.CREATE_ITEM, listId, itemId, descriptionData(item.getDescription()));
            requestSync();
        });
    }

    public void updateItem(final Item item) {
        executor.execute(() -> {
            itemDao.update(item);
            Category category = categoryDao.getCategorySync(item.getCategoryId());
            long listId = category != null ? category.getShoppingListId() : 0;
            enqueue(OutboxEntity.UPDATE_ITEM_DESCRIPTION, listId, item.getId(), descriptionData(item.getDescription()));
            requestSync();
        });
    }

    public void deleteItem(final Item item) {
        executor.execute(() -> {
            Item existing = itemDao.getItemSync(item.getId());
            String remoteId = existing != null ? existing.getRemoteId() : item.getRemoteId();
            long categoryId = existing != null ? existing.getCategoryId() : item.getCategoryId();
            Category category = categoryDao.getCategorySync(categoryId);
            long listId = category != null ? category.getShoppingListId() : 0;
            itemDao.delete(item);
            if (remoteId != null) {
                enqueue(OutboxEntity.DELETE_ITEM, listId, item.getId(), remoteIdData(remoteId));
            }
            requestSync();
        });
    }

    // Optional: methods that return LiveData for categories/items if needed
    public LiveData<List<Category>> getCategoriesForShoppingList(long shoppingListId) {
        return categoryDao.getCategoriesByShoppingListId(shoppingListId);
    }

    public LiveData<List<Item>> getItemsForCategory(long categoryId) {
        return itemDao.getItemsByCategoryId(categoryId);
    }

    public void updateCategoryName(long categoryId, String newName) {
        executor.execute(() -> {
            Category category = categoryDao.getCategorySync(categoryId);
            if (category == null) return;
            categoryDao.updateName(categoryId, newName);
            enqueue(OutboxEntity.UPDATE_CATEGORY_NAME, category.getShoppingListId(), categoryId, nameData(newName));
            requestSync();
        });
    }

    public void updateTaskDescription(long taskId, String newDescription) {
        executor.execute(() -> {
            Item item = itemDao.getItemSync(taskId);
            if (item == null) return;
            Category category = categoryDao.getCategorySync(item.getCategoryId());
            itemDao.updateDescription(taskId, newDescription);
            enqueue(OutboxEntity.UPDATE_ITEM_DESCRIPTION,
                    category != null ? category.getShoppingListId() : 0,
                    taskId,
                    descriptionData(newDescription));
            requestSync();
        });
    }

    public void updateTaskChecked(long taskId, boolean checked) {
        executor.execute(() -> {
            Item item = itemDao.getItemSync(taskId);
            if (item == null) return;
            Category category = categoryDao.getCategorySync(item.getCategoryId());
            itemDao.updateDone(taskId, checked);
            enqueue(OutboxEntity.TOGGLE_ITEM,
                    category != null ? category.getShoppingListId() : 0,
                    taskId,
                    checkedData(checked));
            requestSync();
        });
    }

    public long insertNewCategory(long listId, String name) {
        Category category = new Category();
        category.setName(name);
        category.setShoppingListId(listId);
        executor.execute(() -> {
            long categoryId = categoryDao.insert(category);
            enqueue(OutboxEntity.CREATE_CATEGORY, listId, categoryId, nameData(name));
            requestSync();
        });
        return -1;
    }

    public long insertNewTask(long categoryId, String description) {
        Item item = new Item();
        item.setCategoryId(categoryId);
        item.setDescription(description);
        item.setDone(false);
        executor.execute(() -> {
            Category category = categoryDao.getCategorySync(categoryId);
            long itemId = itemDao.insert(item);
            enqueue(OutboxEntity.CREATE_ITEM,
                    category != null ? category.getShoppingListId() : 0,
                    itemId,
                    descriptionData(description));
            requestSync();
        });
        return -1;
    }

    private void enqueue(String type, long listId, long objectId, String data) {
        outboxDao.insert(new OutboxEntity(type, listId, objectId, data));
    }

    private String titleData(String title) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        return gson.toJson(data);
    }

    private String nameData(String name) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        return gson.toJson(data);
    }

    private String descriptionData(String description) {
        Map<String, Object> data = new HashMap<>();
        data.put("description", description);
        return gson.toJson(data);
    }

    private String checkedData(boolean isChecked) {
        Map<String, Object> data = new HashMap<>();
        data.put("isChecked", isChecked);
        return gson.toJson(data);
    }

    private String remoteIdData(String remoteId) {
        Map<String, Object> data = new HashMap<>();
        data.put("remoteId", remoteId);
        return gson.toJson(data);
    }

    private String emptyData() {
        return gson.toJson(new HashMap<String, Object>());
    }

    private void requestSync() {
        SyncManager syncManager = MyApp.getSyncManager();
        if (syncManager != null) {
            syncManager.sync();
        }
    }

    // Shutdown executor when done (e.g., in a custom Application class)
    public void shutdown() {
        executor.shutdown();
    }

    public void updateShoppingListTitle(long listId, String newTitle) {
        executor.execute(() -> {
            shoppingListDao.updateTitle(listId, newTitle);
            enqueue(OutboxEntity.UPDATE_LIST_TITLE, listId, listId, titleData(newTitle));
            requestSync();
        });
    }

    public void toggleFavourite(long listId) {
        executor.execute(() -> shoppingListDao.toggleFavourite(listId));
    }

    public void resetShoppingList(long listId) {
        executor.execute(() -> {
            // 1. Fetch all items for the list (e.g., via a custom query)
            List<Item> allItems = itemDao.getItemsByShoppingListId(listId);
            for (Item item : allItems) {
                item.setDone(false);
                itemDao.update(item); // or use a batch update for performance
            }
            // 2. Update the list's createdAt
            ShoppingList list = shoppingListDao.getShoppingListSync(listId);
            if (list != null) {
                list.setCreatedAt(new Date());
                shoppingListDao.update(list);
            }
            enqueue(OutboxEntity.RESET_LIST, listId, listId, emptyData());
            requestSync();
        });
    }

    public void copyList(final long sourceListId, final OnCopyCompleteListener listener) {
        executor.execute(() -> {
            final long[] copiedListId = {-1L};
            db.runInTransaction(() -> {
                // 1. Fetch the source list with all its categories and items
                ShoppingListWithAllItems source = shoppingListDao.getShoppingListWithAllItemsSync(sourceListId);
                if (source == null || source.shoppingList == null) return;

                // 2. Copy the shopping list
                ShoppingList newList = source.shoppingList.copy();
                long newListId = shoppingListDao.insert(newList);
                copiedListId[0] = newListId;
                enqueue(OutboxEntity.CREATE_LIST, newListId, newListId, titleData(newList.getTitle()));

                // 3. Map old category IDs to new category IDs
                Map<Long, Long> categoryIdMap = new HashMap<>();
                for (CategoryWithItems catWithItems : source.categories) {
                    Category newCategory = catWithItems.category.copyForShoppingList(newListId);
                    long newCatId = categoryDao.insert(newCategory);
                    categoryIdMap.put(catWithItems.category.getId(), newCatId);
                    enqueue(OutboxEntity.CREATE_CATEGORY, newListId, newCatId, nameData(newCategory.getName()));
                }

                // 4. Copy items, using the new category IDs
                for (CategoryWithItems catWithItems : source.categories) {
                    long newCatId = categoryIdMap.get(catWithItems.category.getId());
                    for (Item originalItem : catWithItems.items) {
                        Item newItem = originalItem.copyForCategory(newCatId);
                        long newItemId = itemDao.insert(newItem);
                        enqueue(OutboxEntity.CREATE_ITEM, newListId, newItemId, descriptionData(newItem.getDescription()));
                    }
                }
            });
            if (copiedListId[0] == -1L) {
                return;
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                if (listener != null) listener.onCopyComplete(copiedListId[0]);
            });
            requestSync();
        });
    }

    public interface OnCopyCompleteListener {
        void onCopyComplete(long newListId);
    }
}
