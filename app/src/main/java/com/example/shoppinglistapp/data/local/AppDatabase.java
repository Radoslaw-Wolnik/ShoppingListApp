package com.example.shoppinglistapp.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.shoppinglistapp.data.local.converter.Converters;
import com.example.shoppinglistapp.data.local.dao.CategoryDao;
import com.example.shoppinglistapp.data.local.dao.ItemDao;
import com.example.shoppinglistapp.data.local.dao.ShoppingListDao;
import com.example.shoppinglistapp.data.local.entity.Category;
import com.example.shoppinglistapp.data.local.entity.Item;
import com.example.shoppinglistapp.data.local.entity.ShoppingList;
import com.example.shoppinglistapp.data.local.migrations.Migrations;

@Database(entities = {ShoppingList.class, Category.class, Item.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract ShoppingListDao shoppingListDao();
    public abstract CategoryDao categoryDao();
    public abstract ItemDao itemDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "shopping_list.db")
                            .addMigrations(Migrations.MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}