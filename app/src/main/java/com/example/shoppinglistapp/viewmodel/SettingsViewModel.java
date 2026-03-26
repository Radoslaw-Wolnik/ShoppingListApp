package com.example.shoppinglistapp.viewmodel;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.shoppinglistapp.MyApp;
import com.example.shoppinglistapp.data.repository.SettingsRepository;

public class SettingsViewModel extends ViewModel {
    private final SettingsRepository repository;
    private final MutableLiveData<Integer> themeMode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showCheckboxes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> moveFavouritesToTop = new MutableLiveData<>();
    private final MutableLiveData<Boolean> categoriesCollapse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> markedItemsHide = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addDefaultCategory = new MutableLiveData<>();
    private final MutableLiveData<String> defaultCategoryName = new MutableLiveData<>();
    private final MutableLiveData<Boolean> focusedMode = new MutableLiveData<>();


    public SettingsViewModel() {
        repository = MyApp.getSettingsRepository();

        themeMode.setValue(repository.getThemeMode());
        showCheckboxes.setValue(repository.getShowCheckboxes());
        moveFavouritesToTop.setValue(repository.getMoveFavouritesToTop());
        categoriesCollapse.setValue(repository.getCategoriesCollapse());
        markedItemsHide.setValue(repository.getMarkedItemsHide());
        addDefaultCategory.setValue(repository.getAddDefaultCategory());
        defaultCategoryName.setValue(repository.getDefaultCategoryName());
        focusedMode.setValue(repository.getFocusedMode());
    }

    public LiveData<Integer> getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(int mode) {
        repository.setThemeMode(mode);
        themeMode.setValue(mode); // Update LiveData so observers see it
        // Also apply the theme globally
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public LiveData<Boolean> getShowCheckboxes() {
        return showCheckboxes;
    }

    public void setShowCheckboxes(boolean show) {
        repository.setShowCheckboxes(show);
        showCheckboxes.setValue(show);
    }

    public LiveData<Boolean> getMoveFavouritesToTop() {
        return moveFavouritesToTop;
    }

    public void setMoveFavouritesToTop(boolean move) {
        repository.setMoveFavouritesToTop(move);
        moveFavouritesToTop.setValue(move);
    }


    public LiveData<Boolean> getCategoriesCollapse() {
        return categoriesCollapse;
    }

    public void setCategoriesCollapse(boolean collapse) {
        repository.setCategoriesCollapse(collapse);
        categoriesCollapse.setValue(collapse);
        if (focusedMode.getValue() == null)
            return;
        if (focusedMode.getValue() == true && collapse) {
            focusedMode.setValue(false);
            repository.setFocusedMode(false);
        }
    }

    public LiveData<Boolean> getMarkedItemsHide() {
        return markedItemsHide;
    }

    public void setMarkedItemsHide(boolean hide) {
        repository.setMarkedItemsHide(hide);
        markedItemsHide.setValue(hide);
    }

    public LiveData<Boolean> getAddDefaultCategory() {
        return addDefaultCategory;
    }

    public void setAddDefaultCategory(boolean add) {
        repository.setAddDefaultCategory(add);
        addDefaultCategory.setValue(add);
    }

    public LiveData<String> getDefaultCategoryName() {
        return defaultCategoryName;
    }

    public void setDefaultCategoryName(String name) {
        repository.setDefaultCategoryName(name);
        defaultCategoryName.setValue(name);
    }

    public LiveData<Boolean> getFocusedMode() {
        return focusedMode;
    }

    public void setFocusedMode(boolean focused) {
        repository.setFocusedMode(focused);
        focusedMode.setValue(focused);
        if (categoriesCollapse.getValue() == null ) {
            return;
        }
        if (categoriesCollapse.getValue() == true && focused) {
            categoriesCollapse.setValue(false);
            repository.setCategoriesCollapse(false);
        }

    }
}