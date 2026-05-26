package com.example.shoppinglistapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

@Entity(tableName = "outbox")
public class OutboxEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "type")
    public String type;  // "ADD_ITEM", "TOGGLE_ITEM", "UPDATE_ITEM_NAME", "DELETE_ITEM", "UPDATE_ITEM_POSITION",
                         // "ADD_CATEGORY", "UPDATE_CATEGORY_NAME", "DELETE_CATEGORY", "UPDATE_CATEGORY_POSITION",
                         // "UPDATE_LIST_NAME"
                         // "UPDATE_LIST_OWNERS", "UPDATE_LIST_EDITORS"


    @ColumnInfo(name = "list_id", index = true)
    public long listId;   // which shopping list (if applicable)

    @ColumnInfo(name = "object_id")
    public long objectId; // optional category or item ID

    @ColumnInfo(name = "data")
    public String data;     // JSON string with additional data (e.g., description, new state)

    @ColumnInfo(name = "created_at", index = true)
    public long createdAt;

    public OutboxEntity() {}

    @Ignore
    public OutboxEntity(String type, long listId, long objectId, String data) {
        this.type = type;
        this.listId = listId;
        this.objectId = objectId;
        this.data = data;
        this.createdAt = System.currentTimeMillis();
    }
}
