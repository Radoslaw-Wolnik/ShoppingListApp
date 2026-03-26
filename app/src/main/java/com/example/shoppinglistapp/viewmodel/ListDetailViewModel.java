package com.example.shoppinglistapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.example.shoppinglistapp.MyApp;
import com.example.shoppinglistapp.data.local.entity.Category;
import com.example.shoppinglistapp.data.local.entity.Item;
import com.example.shoppinglistapp.data.local.queryresult.CategoryWithItems;
import com.example.shoppinglistapp.data.local.queryresult.ShoppingListWithAllItems;
import com.example.shoppinglistapp.data.repository.SettingsRepository;
import com.example.shoppinglistapp.data.repository.ShoppingListRepository;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ListDetailViewModel extends ViewModel {

    private final long listId;
    private final ShoppingListRepository repository;
    private final SettingsRepository settingsRepository;
    private final LiveData<ShoppingListWithAllItems> shoppingList;
    private boolean focusMode;
    private boolean categoriesCollapse;
    private boolean showCheckboxes;
    private boolean markedItemsHide;
    private final MutableLiveData<Set<Long>> expandedIds = new MutableLiveData<>(new HashSet<>());
    private boolean initialExpandedSetInitialised = false;

    // observer that triggers the initial set computation when data arrives
    private final Observer<ShoppingListWithAllItems> shoppingListObserver = shoppingListWithAllItems -> {
        if (!initialExpandedSetInitialised && shoppingListWithAllItems != null
                && shoppingListWithAllItems.categories != null) {
            initialExpandedSetInitialised = true;
            computeInitialExpandedSet(shoppingListWithAllItems);
        }
    };

    public ListDetailViewModel(long listId) {
        this.listId = listId;
        repository = MyApp.getDataRepository();
        settingsRepository = MyApp.getSettingsRepository();

        focusMode = settingsRepository.getFocusedMode();
        categoriesCollapse = settingsRepository.getCategoriesCollapse();
        showCheckboxes = settingsRepository.getShowCheckboxes();
        markedItemsHide = settingsRepository.getMarkedItemsHide();

        shoppingList = repository.getShoppingListWithAllItems(listId);
        // Observe the LiveData to initialise the expanded set when data arrives
        shoppingList.observeForever(shoppingListObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        shoppingList.removeObserver(shoppingListObserver);
    }

    private void computeInitialExpandedSet(ShoppingListWithAllItems list) {
        Set<Long> initialSet = new HashSet<>();

        if (focusMode) {
            // Expand only the first category (if it exists)
            if (!list.categories.isEmpty()) {
                initialSet.add(list.categories.get(0).category.getId());
            }
        } else if (!categoriesCollapse) {
            // Expand all categories
            for (CategoryWithItems cat : list.categories) {
                initialSet.add(cat.category.getId());
            }
        }
        // else: both focusedMode false and categoriesCollapse true → empty set (all collapsed)

        expandedIds.setValue(initialSet);
    }

    public void toggleCategoryExpanded(long categoryId) {
        Set<Long> current = new HashSet<>(expandedIds.getValue() != null ? expandedIds.getValue() : new HashSet<>());
        if (current.contains(categoryId)) {
            current.remove(categoryId);
        } else {
            if (focusMode) {
                // Clear all other expanded categories
                current.clear();
            }
            current.add(categoryId);
        }
        expandedIds.setValue(current);
    }

    public LiveData<ShoppingListWithAllItems> getShoppingList() {
        return shoppingList;
    }

    public LiveData<Set<Long>> getExpandedIds() {
        return expandedIds;
    }

    public void updateCategoryName(long categoryId, String newName) {
        repository.updateCategoryName(categoryId, newName);
    }

    public void toggleTaskDone(long taskId, boolean isDone) {
        repository.updateTaskChecked(taskId, isDone);
    }

    public void updateTaskDescription(long taskId, String newDescription) {
        repository.updateTaskDescription(taskId, newDescription);
    }

    public long addNewTask(long categoryId, String description) {
        return repository.insertNewTask(categoryId, description);
    }

    public long addNewCategory(String name) {
        var listId = shoppingList.getValue().shoppingList.getId();
        return repository.insertNewCategory(listId, name);
    }

    // is it better to pass id or the entire object?
    public void deleteCategory(Category category) {
        repository.deleteCategory(category);
    }

    // is it better to pass id or the entire object?
    public void deleteTask(Item item) {
        repository.deleteItem(item);
    }

    public void updateShoppingListTitle(String newTitle) {
        // reuse listId field - repository should handle background threading
        repository.updateShoppingListTitle(listId, newTitle);
    }

    public void toggleFavourite() {
        repository.toggleFavourite(listId);
    }


    public void redoList(){
        repository.resetShoppingList(listId);
    }

    public void copyList(ShoppingListRepository.OnCopyCompleteListener listener){
        repository.copyList(listId, listener);
    }


    public void deleteList() {
        var list = shoppingList.getValue();
        if (list == null || list.shoppingList == null){
            return;
        }
        repository.deleteShoppingList(list.shoppingList);
    }

    // get relevant settings
    public boolean getMarkedItemsHide () {
        return markedItemsHide;
    }

    public boolean getShowCheckboxes () {
        return showCheckboxes;
    }

    public void refreshSettings() {
        // Re-read the current settings from the repository
        focusMode = settingsRepository.getFocusedMode();
        categoriesCollapse = settingsRepository.getCategoriesCollapse();
        showCheckboxes = settingsRepository.getShowCheckboxes();
        markedItemsHide = settingsRepository.getMarkedItemsHide();
    }

}