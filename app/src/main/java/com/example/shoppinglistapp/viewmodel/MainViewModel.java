package com.example.shoppinglistapp.viewmodel;

import com.example.shoppinglistapp.MyApp;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.shoppinglistapp.data.local.entity.ShoppingList;
import com.example.shoppinglistapp.data.local.queryresult.ShoppingListWithCount;
import com.example.shoppinglistapp.data.repository.SettingsRepository;
import com.example.shoppinglistapp.data.repository.ShoppingListRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainViewModel extends ViewModel {
    private final ShoppingListRepository repository;
    private final SettingsRepository settingsRepository;
    private final LiveData<List<ShoppingListWithCount>> allShoppingLists;

    public MainViewModel() {
        repository = MyApp.getDataRepository();
        settingsRepository = MyApp.getSettingsRepository();
        allShoppingLists = repository.getAllShoppingListsWithCounts();
    }

    public LiveData<List<ShoppingListWithCount>> getAllShoppingLists() {
        return allShoppingLists;
    }

    public void addNewShoppingList(ShoppingListRepository.OnInsertCompleteListener listener) {
        ShoppingList newList = new ShoppingList("New List", new Date());
        repository.insertShoppingListWithDefaultCategory(newList, settingsRepository.getAddDefaultCategory(), settingsRepository.getDefaultCategoryName(), listener);
    }

    // New method that returns sorted list based on current preference
    public LiveData<List<ShoppingListWithCount>> getSortedShoppingLists() {
        // Transform the original LiveData to apply sorting
        return Transformations.map(allShoppingLists, lists -> {
            if (settingsRepository.getMoveFavouritesToTop()) {
                // Sort: favourites first (assuming ShoppingListWithCount has a method isFavourite())
                List<ShoppingListWithCount> sorted = new ArrayList<>(lists);
                sorted.sort((a, b) -> Boolean.compare(
                        b.getShoppingList().isFavourite(),
                        a.getShoppingList().isFavourite()
                ));
                return sorted;
            } else {
                return lists; // original order
            }
        });
    }

    public boolean getMoveFavouritesToTop() {
        return settingsRepository.getMoveFavouritesToTop();
    }
}
