package com.example.shoppinglistapp.ui.listdetails.expandablelist.model;

public class PlaceholderTaskItem extends ListItem {
    private final long parentCategoryId;
    private static final long PLACEHOLDER_ID_OFFSET = 1_000_000_000_000L;

    public PlaceholderTaskItem(long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    @Override
    public long getId() {
        return PLACEHOLDER_ID_OFFSET + parentCategoryId;
    }

    public long getParentCategoryId() {
        return parentCategoryId;
    }
}