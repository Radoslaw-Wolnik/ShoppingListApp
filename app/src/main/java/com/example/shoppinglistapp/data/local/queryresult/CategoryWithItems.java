package com.example.shoppinglistapp.data.local.queryresult;
import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

import com.example.shoppinglistapp.data.local.entity.Category;
import com.example.shoppinglistapp.data.local.entity.Item;
public class CategoryWithItems {
    @Embedded
    public Category category;

    @Relation(
            parentColumn = "id",
            entityColumn = "category_id",
            entity = Item.class
    )
    public List<Item> items;
}
