package com.example.shoppinglistapp.data.remote;

import com.example.shoppinglistapp.data.remote.api.ApiClient;
import com.example.shoppinglistapp.data.remote.api.ApiService;

import org.junit.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RemoteDataSourceTest {
    @Test
    public void getShoppingLists_reRegistersAndRetriesAfterStoredApiKeyIsRejected() throws Exception {
        MockWebServer server = new MockWebServer();
        FakeSharedPreferences preferences = new FakeSharedPreferences();
        preferences.edit()
                .putString("api_key", "stale-key")
                .putString("device_id", "old-device")
                .commit();

        try {
            server.enqueue(new MockResponse().setResponseCode(401));
            server.enqueue(jsonResponse("{\"apiKey\":\"fresh-key\",\"deviceId\":\"new-device\"}"));
            server.enqueue(jsonResponse("[]"));

            RemoteDataSource remoteDataSource = createRemoteDataSource(server, preferences);

            remoteDataSource.getShoppingLists();

            RecordedRequest staleRequest = server.takeRequest();
            RecordedRequest registerRequest = server.takeRequest();
            RecordedRequest retryRequest = server.takeRequest();

            assertEquals("/api/shopping-lists/", staleRequest.getPath());
            assertEquals("stale-key", staleRequest.getHeader("X-API-Key"));
            assertEquals("/api/devices/register", registerRequest.getPath());
            assertNull(registerRequest.getHeader("X-API-Key"));
            assertEquals("/api/shopping-lists/", retryRequest.getPath());
            assertEquals("fresh-key", retryRequest.getHeader("X-API-Key"));
            assertEquals("fresh-key", preferences.getString("api_key", null));
            assertEquals("new-device", preferences.getString("device_id", null));
        } finally {
            server.shutdown();
        }
    }

    @Test
    public void deleteItem_treatsMissingRemoteRowAsAlreadyDeleted() throws Exception {
        MockWebServer server = new MockWebServer();
        FakeSharedPreferences preferences = new FakeSharedPreferences();
        preferences.edit()
                .putString("api_key", "fresh-key")
                .putString("device_id", "device")
                .commit();

        try {
            server.enqueue(new MockResponse().setResponseCode(404));

            RemoteDataSource remoteDataSource = createRemoteDataSource(server, preferences);

            remoteDataSource.deleteItem("6f5c940b-59e1-41cf-a2ff-aa87b36b5a52");

            RecordedRequest request = server.takeRequest();
            assertEquals("/api/shopping-lists/items/6f5c940b-59e1-41cf-a2ff-aa87b36b5a52", request.getPath());
            assertEquals("DELETE", request.getMethod());
        } finally {
            server.shutdown();
        }
    }

    private static RemoteDataSource createRemoteDataSource(
            MockWebServer server,
            FakeSharedPreferences preferences) {
        ApiService apiService = ApiClient.createService(
                server.url("/").toString(),
                () -> preferences.getString("api_key", null));
        return new RemoteDataSource(apiService, preferences, server.url("/").toString());
    }

    private static MockResponse jsonResponse(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
