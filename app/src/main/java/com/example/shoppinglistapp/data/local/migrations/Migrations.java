package com.example.shoppinglistapp.data.local.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migrations {

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE category ADD COLUMN position INTEGER NOT NULL DEFAULT -1"
            );

            database.execSQL(
                    "ALTER TABLE item ADD COLUMN position INTEGER NOT NULL DEFAULT -1"
            );

            database.execSQL(
                    "ALTER TABLE shopping_list ADD COLUMN is_favourite INTEGER NOT NULL DEFAULT 0"
            );
            database.execSQL(
                    "CREATE INDEX index_shopping_list_is_favourite ON shopping_list(is_favourite)"
            );
        }
    };
}