package com.example.shoppinglistapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.example.shoppinglistapp.data.local.entity.ShoppingList;
import com.example.shoppinglistapp.data.local.queryresult.ShoppingListWithAllItems;
import com.example.shoppinglistapp.data.local.queryresult.ShoppingListWithCount;

import java.util.List;

@Dao
public interface ShoppingListDao {
    @Insert
    long insert(ShoppingList shoppingList);

    @Update
    void update(ShoppingList shoppingList);

    @Delete
    void delete(ShoppingList shoppingList);

    @Query("SELECT * FROM shopping_list ORDER BY created_at DESC")
    LiveData<List<ShoppingList>> getAllShoppingLists();

    // synchronous version used for seeding / background checks:
    @Query("SELECT * FROM shopping_list")
    List<ShoppingList> getAllShoppingListsSync();

    @Query("SELECT *, " +
            "(SELECT COUNT(*) FROM item i " +
            "  JOIN category c ON i.category_id = c.id " +
            "  WHERE c.shopping_list_id = shopping_list.id) AS itemCount, " +
            "(SELECT COUNT(*) FROM item i " +
            "  JOIN category c ON i.category_id = c.id " +
            "  WHERE c.shopping_list_id = shopping_list.id AND i.is_done = 1) AS checkedItemCount " +
            "FROM shopping_list " +
            "ORDER BY created_at DESC")
    LiveData<List<ShoppingListWithCount>> getAllShoppingListsWithCounts();

    @Query("SELECT * FROM shopping_list WHERE id = :id")
    LiveData<ShoppingListWithAllItems> getShoppingListWithAllItemsById(long id);

    @Query("SELECT * FROM shopping_list WHERE id = :id")
    LiveData<ShoppingList> getShoppingListById(int id);

    @Query("UPDATE shopping_list SET title = :newTitle WHERE id = :shoppingListId")
    void updateTitle(long shoppingListId, String newTitle);

    @Query("UPDATE shopping_list SET is_favourite = :isFavourite WHERE id = :shoppingListId")
    void updateFavourite(long shoppingListId, Boolean isFavourite);

    @Query("UPDATE shopping_list SET is_favourite = NOT is_favourite WHERE id = :shoppingListId")
    void toggleFavourite(long shoppingListId);

    @Query("SELECT * FROM shopping_list WHERE id = :id")
    ShoppingListWithAllItems getShoppingListWithAllItemsSync(long id);

    @Query("SELECT * FROM shopping_list WHERE id = :id")
    ShoppingList getShoppingListSync(long id);
}