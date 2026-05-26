package com.example.shoppinglistapp.data.remote;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class BackendUrl {
    public static final String DEFAULT_BASE_URL = "http://10.0.2.2:5295/";

    private BackendUrl() {
    }

    public static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return DEFAULT_BASE_URL;
        }

        String trimmed = baseUrl.trim();
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }

    public static String shoppingListHubUrl(String baseUrl, String apiKey) {
        return normalizeBaseUrl(baseUrl) + "hub/shoppingLists?apiKey=" + urlEncode(apiKey);
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
