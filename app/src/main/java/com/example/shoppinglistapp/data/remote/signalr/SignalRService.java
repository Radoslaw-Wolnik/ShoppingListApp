package com.example.shoppinglistapp.data.remote.signalr;

import android.content.Context;
import android.util.Log;

import com.example.shoppinglistapp.data.remote.api.models.ShoppingListEventDto;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SignalRService {
    private static final String TAG = "SignalRService";

    private final Context context;
    private final String hubBaseUrl;
    private HubConnection hubConnection;
    private String currentApiKey;
    private SignalRListener listener;

    public SignalRService(Context context, String backendBaseUrl) {
        this.context = context.getApplicationContext();
        this.hubBaseUrl = normalizeBaseUrl(backendBaseUrl) + "hub/shoppingLists";
    }

    public synchronized void start(String apiKey) {
        if (!hasText(apiKey)) return;
        if (hubConnection != null && hubConnection.getConnectionState() != HubConnectionState.DISCONNECTED) {
            return;
        }

        currentApiKey = apiKey;
        hubConnection = HubConnectionBuilder.create(buildHubUrl(apiKey)).build();
        registerHandlers(hubConnection);
        hubConnection.start().subscribe(
                () -> Log.d(TAG, "Connected to shopping list hub"),
                error -> Log.w(TAG, "Could not connect to shopping list hub", error));
    }

    public synchronized void stop() {
        if (hubConnection != null && hubConnection.getConnectionState() != HubConnectionState.DISCONNECTED) {
            hubConnection.stop();
        }
    }

    public void joinList(String remoteListId) {
        HubConnection connection = hubConnection;
        if (connection != null
                && connection.getConnectionState() == HubConnectionState.CONNECTED
                && hasText(remoteListId)) {
            connection.send("JoinList", remoteListId);
        }
    }

    public void leaveList(String remoteListId) {
        HubConnection connection = hubConnection;
        if (connection != null
                && connection.getConnectionState() == HubConnectionState.CONNECTED
                && hasText(remoteListId)) {
            connection.send("LeaveList", remoteListId);
        }
    }

    public void setListener(SignalRListener listener) {
        this.listener = listener;
    }

    private void registerHandlers(HubConnection connection) {
        connection.on("ShoppingListEvent", event -> {
            if (listener != null) {
                listener.onShoppingListEvent(event);
            }
        }, ShoppingListEventDto.class);
    }

    private String buildHubUrl(String apiKey) {
        return hubBaseUrl + "?apiKey=" + urlEncode(apiKey);
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return "http://10.0.2.2:5295/";
        }
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
