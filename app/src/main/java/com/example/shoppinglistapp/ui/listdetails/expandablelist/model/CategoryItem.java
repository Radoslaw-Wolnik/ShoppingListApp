package com.example.shoppinglistapp.ui.listdetails.expandablelist.model;

public class CategoryItem extends ListItem {
    private final long categoryId;
    private final String name;
    private final boolean isExpanded;
    private final int completed;
    private final int total;

    // Constructor

    public CategoryItem(long categoryId, String name, boolean isExpanded, int completed, int total) {
        this.categoryId = categoryId;
        this.name = name;
        this.isExpanded = isExpanded;
        this.completed = completed;
        this.total = total;
    }

    @Override
    public long getId() {
        return categoryId;
    }

    // Getters
    public long getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public boolean isExpanded() { return isExpanded; }
    public int getProgress() { return total == 0 ? -1 : (completed * 100 / total); }

}