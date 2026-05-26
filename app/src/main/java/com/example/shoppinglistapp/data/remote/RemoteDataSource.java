package com.example.shoppinglistapp.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.shoppinglistapp.BuildConfig;
import com.example.shoppinglistapp.data.remote.api.ApiClient;
import com.example.shoppinglistapp.data.remote.api.ApiService;
import com.example.shoppinglistapp.data.remote.api.models.*;

import retrofit2.Call;
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

    RemoteDataSource(ApiService apiService, SharedPreferences authPreferences, String baseUrl) {
        this.apiService = apiService;
        this.authPreferences = authPreferences;
        this.baseUrl = normalizeBaseUrl(baseUrl);
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
        return executeForBody(apiService::getShoppingLists, "get shopping lists");
    }

    public ShoppingListDto getShoppingList(String remoteListId) throws IOException {
        return executeForBody(() -> apiService.getShoppingList(remoteListId), "get shopping list");
    }

    public String createShoppingList(String title) throws IOException {
        IdResponse response = requireBody(
                executeWithRegisteredDevice(() -> apiService.createShoppingList(new CreateListRequest(title))),
                "create shopping list");
        return response.id;
    }

    public void updateShoppingListTitle(String remoteListId, String title) throws IOException {
        executeForSuccess(() -> apiService.updateShoppingListTitle(remoteListId, new UpdateTitleRequest(title)),
                "update shopping list title");
    }

    public void deleteShoppingList(String remoteListId) throws IOException {
        executeForIdempotentDelete(() -> apiService.deleteShoppingList(remoteListId), "delete shopping list");
    }

    public String copyShoppingList(String remoteListId) throws IOException {
        IdResponse response = executeForBody(() -> apiService.copyShoppingList(remoteListId), "copy shopping list");
        return response.id;
    }

    public void resetCheckedItems(String remoteListId) throws IOException {
        executeForSuccess(() -> apiService.resetCheckedItems(remoteListId), "reset checked items");
    }

    public String addCategory(String remoteListId, String name) throws IOException {
        IdResponse response = requireBody(
                executeWithRegisteredDevice(() -> apiService.addCategory(remoteListId, new AddCategoryRequest(name))),
                "add category");
        return response.id;
    }

    public void updateCategory(String remoteCategoryId, String name) throws IOException {
        executeForSuccess(() -> apiService.updateCategory(remoteCategoryId, new UpdateCategoryNameRequest(name)),
                "update category");
    }

    public void deleteCategory(String remoteCategoryId) throws IOException {
        executeForIdempotentDelete(() -> apiService.deleteCategory(remoteCategoryId), "delete category");
    }

    public String addItem(String remoteCategoryId, String description) throws IOException {
        IdResponse response = requireBody(
                executeWithRegisteredDevice(() -> apiService.addItem(remoteCategoryId, new AddItemRequest(description))),
                "add item");
        return response.id;
    }

    public void updateItem(String remoteItemId, String description) throws IOException {
        executeForSuccess(() -> apiService.updateItem(remoteItemId, new UpdateItemDescriptionRequest(description)),
                "update item");
    }

    public void toggleItem(String remoteItemId, boolean isChecked) throws IOException {
        executeForSuccess(() -> apiService.toggleItem(remoteItemId, new ToggleItemRequest(isChecked)),
                "toggle item");
    }

    public void deleteItem(String remoteItemId) throws IOException {
        executeForIdempotentDelete(() -> apiService.deleteItem(remoteItemId), "delete item");
    }

    private static String normalizeBaseUrl(String baseUrl) {
        return BackendUrl.normalizeBaseUrl(baseUrl);
    }

    private <T> T executeForBody(CallFactory<T> callFactory, String action) throws IOException {
        return requireBody(executeWithRegisteredDevice(callFactory), action);
    }

    private <T> void executeForSuccess(CallFactory<T> callFactory, String action) throws IOException {
        requireSuccess(executeWithRegisteredDevice(callFactory), action);
    }

    private <T> void executeForIdempotentDelete(CallFactory<T> callFactory, String action) throws IOException {
        Response<T> response = executeWithRegisteredDevice(callFactory);
        if (response.code() == 404) {
            return;
        }
        requireSuccess(response, action);
    }

    private <T> Response<T> executeWithRegisteredDevice(CallFactory<T> callFactory) throws IOException {
        ensureRegistered();
        Response<T> response = callFactory.create().execute();
        if (response.code() != 401) {
            return response;
        }

        clearRegistration();
        ensureRegistered();
        return callFactory.create().execute();
    }

    private synchronized void clearRegistration() {
        authPreferences.edit()
                .remove(KEY_API_KEY)
                .remove(KEY_DEVICE_ID)
                .apply();
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

    private interface CallFactory<T> {
        Call<T> create();
    }
}
