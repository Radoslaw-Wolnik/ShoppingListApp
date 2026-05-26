package com.example.shoppinglistapp;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.shoppinglistapp.data.local.AppDatabase;
import com.example.shoppinglistapp.data.local.DatabaseInitializer;
import com.example.shoppinglistapp.data.remote.RemoteDataSource;
import com.example.shoppinglistapp.data.remote.signalr.SignalRService;
import com.example.shoppinglistapp.data.repository.SettingsRepository;
import com.example.shoppinglistapp.data.repository.ShoppingListRepository;
import com.example.shoppinglistapp.data.sync.SyncManager;
import com.example.shoppinglistapp.data.sync.SyncWorker;

import java.util.concurrent.TimeUnit;


public class MyApp extends Application {
    private static AppDatabase database;
    private static ShoppingListRepository dataRepository;
    private static SettingsRepository settingsRepository;
    private static RemoteDataSource remoteDataSource;
    private static SignalRService signalRService;
    private static SyncManager syncManager;


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

        remoteDataSource = new RemoteDataSource(this);
        signalRService = new SignalRService(this, remoteDataSource.getBaseUrl());
        syncManager = new SyncManager(this, remoteDataSource, signalRService);
        signalRService.setListener(event -> syncManager.sync());

        // Schedule periodic sync with WorkManager
        setupWorkManager();
        syncManager.sync();

        // Apply saved theme mode before any activity is created
        int savedMode = settingsRepository.getThemeMode();
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }

    private void setupWorkManager() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest syncWork = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("sync", ExistingPeriodicWorkPolicy.KEEP, syncWork);
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
    public static RemoteDataSource getRemoteDataSource() { return remoteDataSource; }
    public static SignalRService getSignalRService() { return signalRService; }
    public static SyncManager getSyncManager() { return syncManager; }

}
