package com.example.shoppinglistapp.data.local.queryresult;

import androidx.room.Embedded;
import androidx.room.Ignore;

import com.example.shoppinglistapp.data.local.entity.ShoppingList;

public class ShoppingListWithCount {
    @Embedded
    private ShoppingList shoppingList;
    private int itemCount;
    private int checkedItemCount;

    // No-arg constructor required by Room
    public ShoppingListWithCount() {}

    // Optional constructor for manual creation
    @Ignore
    public ShoppingListWithCount(ShoppingList shoppingList, int itemCount, int checkedItemCount) {
        this.shoppingList = shoppingList;
        this.itemCount = itemCount;
        this.checkedItemCount = checkedItemCount;
    }

    // Getters and setters (Room uses setters or direct field access)
    public ShoppingList getShoppingList() { return shoppingList; }
    public void setShoppingList(ShoppingList shoppingList) { this.shoppingList = shoppingList; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    public int getCheckedItemCount() { return checkedItemCount; }
    public void setCheckedItemCount(int checkedItemCount) { this.checkedItemCount = checkedItemCount; }
}