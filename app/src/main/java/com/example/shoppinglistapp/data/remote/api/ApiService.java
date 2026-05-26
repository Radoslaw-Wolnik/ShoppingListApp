package com.example.shoppinglistapp.data.remote.api;

import com.example.shoppinglistapp.data.remote.api.models.AddCategoryRequest;
import com.example.shoppinglistapp.data.remote.api.models.AddItemRequest;
import com.example.shoppinglistapp.data.remote.api.models.CreateListRequest;
import com.example.shoppinglistapp.data.remote.api.models.DeviceShoppingListHeaderDto;
import com.example.shoppinglistapp.data.remote.api.models.IdResponse;
import com.example.shoppinglistapp.data.remote.api.models.RegisterDeviceResponse;
import com.example.shoppinglistapp.data.remote.api.models.ShoppingListDto;
import com.example.shoppinglistapp.data.remote.api.models.ToggleItemRequest;
import com.example.shoppinglistapp.data.remote.api.models.UpdateCategoryNameRequest;
import com.example.shoppinglistapp.data.remote.api.models.UpdateItemDescriptionRequest;
import com.example.shoppinglistapp.data.remote.api.models.UpdateTitleRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("api/devices/register")
    Call<RegisterDeviceResponse> registerDevice();

    @GET("api/shopping-lists/")
    Call<List<DeviceShoppingListHeaderDto>> getShoppingLists();

    @GET("api/shopping-lists/{id}")
    Call<ShoppingListDto> getShoppingList(@Path("id") String remoteListId);

    @POST("api/shopping-lists/")
    Call<IdResponse> createShoppingList(@Body CreateListRequest request);

    @PUT("api/shopping-lists/{id}/title")
    Call<Void> updateShoppingListTitle(
            @Path("id") String remoteListId,
            @Body UpdateTitleRequest request);

    @DELETE("api/shopping-lists/{id}")
    Call<Void> deleteShoppingList(@Path("id") String remoteListId);

    @POST("api/shopping-lists/{id}/copy")
    Call<IdResponse> copyShoppingList(@Path("id") String remoteListId);

    @POST("api/shopping-lists/{id}/reset-checked")
    Call<Void> resetCheckedItems(@Path("id") String remoteListId);

    @POST("api/shopping-lists/{listId}/categories")
    Call<IdResponse> addCategory(
            @Path("listId") String remoteListId,
            @Body AddCategoryRequest request);

    @PUT("api/shopping-lists/categories/{categoryId}")
    Call<Void> updateCategory(
            @Path("categoryId") String remoteCategoryId,
            @Body UpdateCategoryNameRequest request);

    @DELETE("api/shopping-lists/categories/{categoryId}")
    Call<Void> deleteCategory(@Path("categoryId") String remoteCategoryId);

    @POST("api/shopping-lists/categories/{categoryId}/items")
    Call<IdResponse> addItem(
            @Path("categoryId") String remoteCategoryId,
            @Body AddItemRequest request);

    @PUT("api/shopping-lists/items/{itemId}")
    Call<Void> updateItem(
            @Path("itemId") String remoteItemId,
            @Body UpdateItemDescriptionRequest request);

    @PUT("api/shopping-lists/items/{itemId}/toggle")
    Call<Void> toggleItem(
            @Path("itemId") String remoteItemId,
            @Body ToggleItemRequest request);

    @DELETE("api/shopping-lists/items/{itemId}")
    Call<Void> deleteItem(@Path("itemId") String remoteItemId);
}
