package com.example.shoppinglistapp.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.shoppinglistapp.BuildConfig;
import com.example.shoppinglistapp.data.remote.api.ApiClient;
import com.example.shoppinglistapp.data.remote.api.ApiService;
import com.example.shoppinglistapp.data.remote.api.models.*;

import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class RemoteDataSource {
    private static final String PREFS_NAME = "backend_auth";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_DEVICE_ID = "device_id";

    private final ApiService apiService;
    private final SharedPreferences authPreferences;
    private final String baseUrl;

    public RemoteDataSource(Context context) {
        this.authPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.baseUrl = normalizeBaseUrl(BuildConfig.SHOPPING_BACKEND_BASE_URL);
        this.apiService = ApiClient.createService(baseUrl, this::getApiKey);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return authPreferences.getString(KEY_API_KEY, null);
    }

    public String getDeviceId() {
        return authPreferences.getString(KEY_DEVICE_ID, null);
    }

    public synchronized String ensureRegistered() throws IOException {
        String apiKey = getApiKey();
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return apiKey;
        }

        Response<RegisterDeviceResponse> response = apiService.registerDevice().execute();
        RegisterDeviceResponse body = requireBody(response, "register device");
        authPreferences.edit()
                .putString(KEY_API_KEY, body.apiKey)
                .putString(KEY_DEVICE_ID, body.deviceId)
                .apply();
        return body.apiKey;
    }

    public List<DeviceShoppingListHeaderDto> getShoppingLists() throws IOException {
        ensureRegistered();
        return requireBody(apiService.getShoppingLists().execute(), "get shopping lists");
    }

    public ShoppingListDto getShoppingList(String remoteListId) throws IOException {
        ensureRegistered();
        return requireBody(apiService.getShoppingList(remoteListId).execute(), "get shopping list");
    }

    public String createShoppingList(String title) throws IOException {
        ensureRegistered();
        IdResponse response = requireBody(
                apiService.createShoppingList(new CreateListRequest(title)).execute(),
                "create shopping list");
        return response.id;
    }

    public void updateShoppingListTitle(String remoteListId, String title) throws IOException {
        ensureRegistered();
        requireSuccess(apiService.updateShoppingListTitle(remoteListId, new UpdateTitleRequest(title)).execute(),
                "update shopping list title");
    }

    public void deleteShoppingList(String remoteListId) throws IOException {
        ensureRegistered();
        requireSuccess(apiService.deleteShoppingList(remoteListId).execute(), "delete shopping list");
    }

    public String copyShoppingList(String remoteListId) throws IOException {
        ensureRegistered();
        IdResponse response = requireBody(apiService.copyShoppingList(remoteListId).execute(), "copy shopping list");
        return response.id;
    }

    public void resetCheckedItems(String remoteListId) throws IOException {
        ensureRegistered();
        requireSuccess(apiService.resetCheckedItems(remoteListId).execute(), "reset checked items");
    }

    public String addCategory(String remoteListId, String name) throws IOException {
        ensureRegistered();
        IdResponse response = requireBody(
                apiService.addCategory(remoteListId, new AddCategoryRequest(name)).execute(),
                "add category");
        return response.id;
    }

    public void updateCategory(String remoteCategoryId, String name) throws IOException {
        ensureRegistered();
        requireSuccess(apiService.updateCategory(remoteCategoryId, new UpdateCategoryNameRequest(name)).execute(),
                "update category");
    }

    public void deleteCategory(String remoteCategoryId) throws IOException {
        ensureRegistered();
        requireSuccess(apiService.deleteCategory(remoteCategoryId).execute(), "delete category");
    }

    public String addItem(String remoteCategoryId, String description) throws IOException {
        ensureRegistered();
        IdResponse response = requireBody(
                apiService.addItem(remoteCategoryId, new AddItemRequest(description)).execute(),
                "add item");
        return response.id;
    }

    public void updateItem(String remoteItemId, String description) throws IOException {
        ensureRegistered();
        requireSuccess(apiService.updateItem(remoteItemId, new UpdateItemDescriptionRequest(description)).execute(),
                "update item");
    }

    public void toggleItem(String remoteItemId, boolean isChecked) throws IOException {
        ensureRegistered();
        requireSuccess(apiService.toggleItem(remoteItemId, new ToggleItemRequest(isChecked)).execute(),
                "toggle item");
    }

    public void deleteItem(String remoteItemId) throws IOException {
        ensureRegistered();
        requireSuccess(apiService.deleteItem(remoteItemId).execute(), "delete item");
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return "http://10.0.2.2:5295/";
        }
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    private static void requireSuccess(Response<?> response, String action) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Failed to " + action + ": HTTP " + response.code());
        }
    }

    private static <T> T requireBody(Response<T> response, String action) throws IOException {
        requireSuccess(response, action);
        T body = response.body();
        if (body == null) {
            throw new IOException("Failed to " + action + ": empty response body");
        }
        return body;
    }
}
