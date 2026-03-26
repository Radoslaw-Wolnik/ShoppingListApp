package com.example.shoppinglistapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.example.shoppinglistapp.data.local.entity.Category;
import com.example.shoppinglistapp.data.local.queryresult.CategoryWithItems;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(Category category);
    @Update
    void update(Category category);
    @Delete
    void delete(Category category);

    @Query("SELECT * FROM category WHERE shopping_list_id = :shoppingListId ORDER BY id ASC")
    LiveData<List<Category>> getCategoriesByShoppingListId(long shoppingListId);

    @Query("SELECT * FROM category WHERE shopping_list_id = :shoppingListId ORDER BY id ASC")
    LiveData<List<CategoryWithItems>> getCategoriesWithItemsByShoppingListId(long shoppingListId);


    // specific fields update
    @Query("UPDATE category SET name = :newName WHERE id = :categoryId")
    void updateName(long categoryId, String newName);

    @Query("UPDATE category SET position = :newPosition WHERE id = :categoryId")
    void updatePosition(long categoryId, int newPosition);
}
