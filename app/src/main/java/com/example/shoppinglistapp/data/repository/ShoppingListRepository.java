package com.example.shoppinglistapp.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.shoppinglistapp.data.local.AppDatabase;
import com.example.shoppinglistapp.data.local.dao.CategoryDao;
import com.example.shoppinglistapp.data.local.dao.ItemDao;
import com.example.shoppinglistapp.data.local.dao.ShoppingListDao;
import com.example.shoppinglistapp.data.local.entity.Category;
import com.example.shoppinglistapp.data.local.entity.Item;
import com.example.shoppinglistapp.data.local.entity.ShoppingList;
import com.example.shoppinglistapp.data.local.queryresult.CategoryWithItems;
import com.example.shoppinglistapp.data.local.queryresult.ShoppingListWithAllItems;
import com.example.shoppinglistapp.data.local.queryresult.ShoppingListWithCount;

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
    private final ExecutorService executor = Executors.newSingleThreadExecutor(); // for background writes
    private final AppDatabase db;

    public ShoppingListRepository(Application application) {
        db = AppDatabase.getInstance(application);
        shoppingListDao = db.shoppingListDao();
        categoryDao = db.categoryDao();
        itemDao = db.itemDao();
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
            // Insert the shopping list
            long listId = shoppingListDao.insert(newList);

            // If needed, insert the default category (synchronously, same thread)
            if (addDefaultCategory && defaultCategoryName != null && !defaultCategoryName.trim().isEmpty()) {
                Category defaultCat = new Category(defaultCategoryName, listId);
                categoryDao.insert(defaultCat); // synchronous insert
            }

            // Post result back to main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                if (listener != null) listener.onInsertComplete(listId);
            });
        });
    }

    // Define a callback interface
    public interface OnInsertCompleteListener {
        void onInsertComplete(long id);
    }

    public void updateShoppingList(final ShoppingList shoppingList) {
        executor.execute(() -> shoppingListDao.update(shoppingList));
    }

    public void deleteShoppingList(final ShoppingList shoppingList) {
        executor.execute(() -> shoppingListDao.delete(shoppingList));
    }

    // Similarly for Category and Item operations
    public void insertCategory(final Category category) {
        executor.execute(() -> categoryDao.insert(category));
    }

    public void updateCategory(final Category category) {
        executor.execute(() -> categoryDao.update(category));
    }

    public void deleteCategory(final Category category) {
        executor.execute(() -> categoryDao.delete(category));
    }

    public void insertItem(final Item item) {
        executor.execute(() -> itemDao.insert(item));
    }

    public void updateItem(final Item item) {
        executor.execute(() -> itemDao.update(item));
    }

    public void deleteItem(final Item item) {
        executor.execute(() -> itemDao.delete(item));
    }

    // Optional: methods that return LiveData for categories/items if needed
    public LiveData<List<Category>> getCategoriesForShoppingList(long shoppingListId) {
        return categoryDao.getCategoriesByShoppingListId(shoppingListId);
    }

    public LiveData<List<Item>> getItemsForCategory(long categoryId) {
        return itemDao.getItemsByCategoryId(categoryId);
    }

    public void updateCategoryName(long categoryId, String newName) {
        executor.execute(() -> categoryDao.updateName(categoryId, newName));
    }

    public void updateTaskDescription(long taskId, String newDescription) {
        executor.execute(() -> itemDao.updateDescription(taskId, newDescription));
    }

    public void updateTaskChecked(long taskId, boolean checked) {
        executor.execute(() -> itemDao.updateDone(taskId, checked));
    }

    public long insertNewCategory(long listId, String name) {
        Category category = new Category();
        category.setName(name);
        category.setShoppingListId(listId);
        executor.execute(() -> categoryDao.insert(category));
        return category.getId();
    }

    public long insertNewTask(long categoryId, String description) {
        Item item = new Item();
        item.setCategoryId(categoryId);
        item.setDescription(description);
        item.setDone(false);
        executor.execute(() -> itemDao.insert(item));
        return item.getId();
    }

    // Shutdown executor when done (e.g., in a custom Application class)
    public void shutdown() {
        executor.shutdown();
    }

    public void updateShoppingListTitle(long listId, String newTitle) {
        executor.execute(() -> shoppingListDao.updateTitle(listId, newTitle));
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
        });
    }

    public void copyList(final long sourceListId, final OnCopyCompleteListener listener) {
        executor.execute(() -> {
            db.runInTransaction(() -> {
                // 1. Fetch the source list with all its categories and items
                ShoppingListWithAllItems source = shoppingListDao.getShoppingListWithAllItemsSync(sourceListId);
                if (source == null || source.shoppingList == null) return;

                // 2. Copy the shopping list
                ShoppingList newList = source.shoppingList.copy();
                long newListId = shoppingListDao.insert(newList);

                // 3. Map old category IDs to new category IDs
                Map<Long, Long> categoryIdMap = new HashMap<>();
                for (CategoryWithItems catWithItems : source.categories) {
                    Category newCategory = catWithItems.category.copyForShoppingList(newListId);
                    long newCatId = categoryDao.insert(newCategory);
                    categoryIdMap.put(catWithItems.category.getId(), newCatId);
                }

                // 4. Copy items, using the new category IDs
                for (CategoryWithItems catWithItems : source.categories) {
                    long newCatId = categoryIdMap.get(catWithItems.category.getId());
                    for (Item originalItem : catWithItems.items) {
                        Item newItem = originalItem.copyForCategory(newCatId);
                        itemDao.insert(newItem);
                    }
                }

                // 5. Post result back to main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (listener != null) listener.onCopyComplete(newListId);
                });
            });
        });
    }

    public interface OnCopyCompleteListener {
        void onCopyComplete(long newListId);
    }
}
