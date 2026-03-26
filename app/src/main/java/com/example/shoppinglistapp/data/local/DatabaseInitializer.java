package com.example.shoppinglistapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Room;

import com.example.shoppinglistapp.data.local.dao.CategoryDao;
import com.example.shoppinglistapp.data.local.dao.ItemDao;
import com.example.shoppinglistapp.data.local.dao.ShoppingListDao;
import com.example.shoppinglistapp.data.local.AppDatabase;
import com.example.shoppinglistapp.data.local.entity.Category;
import com.example.shoppinglistapp.data.local.entity.Item;
import com.example.shoppinglistapp.data.local.entity.ShoppingList;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseInitializer {

    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_SEEDED = "database_seeded";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Populates the database with sample data.
     * This method should be called once, e.g., from your Application class or first activity.
     * It runs on a background thread.
     * After running marks the database as seeded.
     */
    public static void populateDatabase(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_SEEDED, false)) {
            // Already seeded, do nothing
            return;
        }

        // Check if database already has data (in case user reinstalled but prefs were cleared)
        // ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            ShoppingListDao shoppingListDao = db.shoppingListDao();

            List<ShoppingList> lists = shoppingListDao.getAllShoppingListsSync();

            if (lists != null && !lists.isEmpty()) {
                // Data already exists, just mark as seeded and return
                prefs.edit().putBoolean(KEY_SEEDED, true).apply();
                return;
            }

            // Perform the actual seeding
            CategoryDao categoryDao = db.categoryDao();
            ItemDao itemDao = db.itemDao();

            // Insert shopping lists
            ShoppingList list1 = new ShoppingList("Weekly Groceries", new Date());
            ShoppingList list2 = new ShoppingList("DIY Supplies", new Date());
            long list1Id = shoppingListDao.insert(list1); // if insert returns long
            long list2Id = shoppingListDao.insert(list2);

            // Insert categories for list1
            Category vegetables = new Category("Vegetables", list1Id);
            Category dairy = new Category("Dairy", list1Id);
            Category bakery = new Category("Bakery", list1Id);
            long vegId = categoryDao.insert(vegetables);
            long dairyId = categoryDao.insert(dairy);
            long bakeryId = categoryDao.insert(bakery);

            // Insert items for Vegetables
            Item carrot = new Item("Carrots", vegId);
            itemDao.insert(carrot);
            carrot.setDone(true);
            itemDao.update(carrot);

            itemDao.insert(new Item("Broccoli", vegId));
            itemDao.insert(new Item("Tomatoes", vegId));

            // Insert items for Dairy
            itemDao.insert(new Item("Milk", dairyId));
            itemDao.insert(new Item("Cheese", dairyId)); // 200g
            itemDao.insert(new Item("Yogurt", dairyId));

            // Insert items for Bakery
            itemDao.insert(new Item("Bread", bakeryId));
            itemDao.insert(new Item("Croissants", bakeryId));

            // Insert categories for list2
            Category tools = new Category("Tools", list2Id);
            Category paint = new Category("Paint", list2Id);
            long toolsId = categoryDao.insert(tools);
            long paintId = categoryDao.insert(paint);

            // Items for Tools
            itemDao.insert(new Item("Hammer", toolsId));
            itemDao.insert(new Item("Screwdriver set", toolsId));
            itemDao.insert(new Item("Nails", toolsId));

            // Items for Paint
            itemDao.insert(new Item("White paint", paintId));
            itemDao.insert(new Item("Brush", paintId));

            // After successful insertion, mark seeded
            prefs.edit().putBoolean(KEY_SEEDED, true).apply();
        });
    }
}