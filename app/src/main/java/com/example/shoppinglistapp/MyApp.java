package com.example.shoppinglistapp;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.shoppinglistapp.data.local.AppDatabase;
import com.example.shoppinglistapp.data.local.DatabaseInitializer;
import com.example.shoppinglistapp.data.repository.SettingsRepository;
import com.example.shoppinglistapp.data.repository.ShoppingListRepository;


public class MyApp extends Application {
    private static AppDatabase database;
    private static ShoppingListRepository dataRepository;
    private static SettingsRepository settingsRepository;


    @Override
    public void onCreate() {
        super.onCreate();
        // database = Room.databaseBuilder(this, AppDatabase.class, "shopping_list.db").build();
        database = AppDatabase.getInstance(this);
        dataRepository = new ShoppingListRepository(this);
        settingsRepository = new SettingsRepository(this);

        // Set default settings on first run
        if (settingsRepository.isFirstRun()) {
            // Set your desired defaults (they will overwrite any existing values)
            settingsRepository.setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            settingsRepository.setShowCheckboxes(true);
            settingsRepository.setMoveFavouritesToTop(true);
            settingsRepository.setCategoriesCollapse(false);
            settingsRepository.setMarkedItemsHide(false);
            settingsRepository.setAddDefaultCategory(false);
            settingsRepository.setDefaultCategoryName("Default");
            settingsRepository.setFocusedMode(false);
            // Mark first run as done
            settingsRepository.setFirstRunDone();

            // Trigger the one‑time database population (runs on background thread)
            DatabaseInitializer.populateDatabase(this);
        }



        // Apply saved theme mode before any activity is created
        int savedMode = settingsRepository.getThemeMode();
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }

    public static AppDatabase getDatabase() {
        return database;
    }
    public static ShoppingListRepository getDataRepository() {
        return dataRepository;
    }
    public static SettingsRepository getSettingsRepository() {
        return settingsRepository;
    }

}