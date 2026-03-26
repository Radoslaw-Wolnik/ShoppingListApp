package com.example.shoppinglistapp.data.local.queryresult;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

import com.example.shoppinglistapp.data.local.entity.Category;
import com.example.shoppinglistapp.data.local.entity.ShoppingList;

public class ShoppingListWithAllItems {
    @Embedded
    public ShoppingList shoppingList;

    @Relation(
            parentColumn = "id",
            entityColumn = "shopping_list_id",
            entity = Category.class
    )
    public List<CategoryWithItems> categories;

}
