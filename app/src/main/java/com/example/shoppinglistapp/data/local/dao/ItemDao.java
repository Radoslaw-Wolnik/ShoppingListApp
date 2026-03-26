package com.example.shoppinglistapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.example.shoppinglistapp.data.local.entity.Item;

import java.util.List;

@Dao
public interface ItemDao {
    @Insert
    long insert(Item item);
    @Update
    void update(Item item);
    @Delete
    void delete(Item item);
    @Query("SELECT * FROM item WHERE category_id = :CategoryId ORDER BY ID ASC")
    LiveData<List<Item>> getItemsByCategoryId(long CategoryId);

    @Query("SELECT i.* FROM item i " +
            "INNER JOIN category c ON i.category_id = c.id " +
            "WHERE c.shopping_list_id = :shoppingListId")
    List<Item> getItemsByShoppingListId(long shoppingListId);

    // specific fields update
    @Query("UPDATE item SET description = :newDescription WHERE id = :itemId")
    void updateDescription(long itemId, String newDescription);

    @Query("UPDATE item SET is_done = :newDone WHERE id = :itemId")
    void updateDone(long itemId, boolean newDone);

    @Query("UPDATE item SET position = :newPosition WHERE id = :itemId")
    void updatePosition(long itemId, int newPosition);
}
