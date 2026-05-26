package com.example.shoppinglistapp.data.remote.api.models;

import com.google.gson.Gson;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ShoppingListEventDtoTest {
    @Test
    public void shoppingListEventDto_readsBackendSignalREnvelopeAndIgnoresEventSpecificFields() {
        String json = "{"
                + "\"eventType\":\"ItemToggled\","
                + "\"listId\":\"c21bdf9e-626d-44a2-a52f-3ac23d5f43b1\","
                + "\"itemId\":\"6f5c940b-59e1-41cf-a2ff-aa87b36b5a52\","
                + "\"isChecked\":true"
                + "}";

        ShoppingListEventDto event = new Gson().fromJson(json, ShoppingListEventDto.class);

        assertEquals("ItemToggled", event.eventType);
        assertEquals("c21bdf9e-626d-44a2-a52f-3ac23d5f43b1", event.listId);
    }
}
