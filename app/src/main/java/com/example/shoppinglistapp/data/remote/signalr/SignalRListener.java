package com.example.shoppinglistapp.data.remote.signalr;

import com.example.shoppinglistapp.data.remote.api.models.ShoppingListEventDto;

public interface SignalRListener {
    void onShoppingListEvent(ShoppingListEventDto event);
}
