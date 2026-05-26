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

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE shopping_list ADD COLUMN remote_id TEXT");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_shopping_list_remote_id ON shopping_list(remote_id)");

            database.execSQL("ALTER TABLE category ADD COLUMN remote_id TEXT");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_category_remote_id ON category(remote_id)");

            database.execSQL("ALTER TABLE item ADD COLUMN remote_id TEXT");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_item_remote_id ON item(remote_id)");

            database.execSQL("CREATE TABLE IF NOT EXISTS outbox (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "type TEXT, " +
                    "list_id INTEGER NOT NULL, " +
                    "object_id INTEGER NOT NULL, " +
                    "data TEXT, " +
                    "created_at INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_outbox_list_id ON outbox(list_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_outbox_created_at ON outbox(created_at)");
        }
    };
}
