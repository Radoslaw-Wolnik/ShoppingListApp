package com.example.shoppinglistapp.data.remote.api;

import com.example.shoppinglistapp.data.remote.api.models.DeviceShoppingListHeaderDto;

import org.junit.Test;

import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApiClientTest {
    @Test
    public void createService_sendsApiKeyHeaderWhenAvailable() throws Exception {
        MockWebServer server = new MockWebServer();
        try {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("[]"));

            ApiService service = ApiClient.createService(server.url("/").toString(), () -> "test-api-key");

            Response<List<DeviceShoppingListHeaderDto>> response = service.getShoppingLists().execute();

            RecordedRequest request = server.takeRequest();
            assertTrue(response.isSuccessful());
            assertEquals("/api/shopping-lists/", request.getPath());
            assertEquals("test-api-key", request.getHeader("X-API-Key"));
        } finally {
            server.shutdown();
        }
    }

    @Test
    public void createService_omitsApiKeyHeaderWhenMissing() throws Exception {
        MockWebServer server = new MockWebServer();
        try {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("[]"));

            ApiService service = ApiClient.createService(server.url("/").toString(), () -> " ");

            Response<List<DeviceShoppingListHeaderDto>> response = service.getShoppingLists().execute();

            RecordedRequest request = server.takeRequest();
            assertTrue(response.isSuccessful());
            assertEquals(null, request.getHeader("X-API-Key"));
        } finally {
            server.shutdown();
        }
    }
}
