package com.example.shoppinglistapp.data.remote.api.models;

public class ToggleItemRequest {
    public final boolean isChecked;

    public ToggleItemRequest(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
