package com.example.shoppinglistapp.data.remote;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BackendUrlTest {
    @Test
    public void defaultBaseUrl_pointsToDockerBackendOnAndroidEmulator() {
        assertEquals("http://10.0.2.2:8080/", BackendUrl.DEFAULT_BASE_URL);
    }

    @Test
    public void normalizeBaseUrl_usesDefaultWhenBlank() {
        assertEquals(BackendUrl.DEFAULT_BASE_URL, BackendUrl.normalizeBaseUrl("  "));
    }

    @Test
    public void normalizeBaseUrl_addsTrailingSlashAndTrims() {
        assertEquals("http://example.test:5295/", BackendUrl.normalizeBaseUrl(" http://example.test:5295 "));
    }

    @Test
    public void shoppingListHubUrl_buildsEncodedSignalRUrl() {
        String url = BackendUrl.shoppingListHubUrl("http://example.test", "abc+123/=");

        assertEquals("http://example.test/hub/shoppingLists?apiKey=abc%2B123%2F%3D", url);
    }
}
