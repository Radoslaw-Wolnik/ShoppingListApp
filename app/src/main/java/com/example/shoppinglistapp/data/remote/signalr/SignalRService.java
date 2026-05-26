package com.example.shoppinglistapp.data.remote.signalr;

import android.content.Context;
import android.util.Log;

import com.example.shoppinglistapp.data.remote.BackendUrl;
import com.example.shoppinglistapp.data.remote.api.models.ShoppingListEventDto;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import java.util.HashSet;
import java.util.Set;

public class SignalRService {
    private static final String TAG = "SignalRService";

    private final String hubBaseUrl;
    private HubConnection hubConnection;
    private String currentApiKey;
    private SignalRListener listener;
    private final Set<String> joinedListIds = new HashSet<>();

    public SignalRService(Context context, String backendBaseUrl) {
        this.hubBaseUrl = BackendUrl.normalizeBaseUrl(backendBaseUrl);
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
                () -> {
                    Log.d(TAG, "Connected to shopping list hub");
                    sendJoinedLists();
                },
                error -> Log.w(TAG, "Could not connect to shopping list hub", error));
    }

    public synchronized void stop() {
        if (hubConnection != null && hubConnection.getConnectionState() != HubConnectionState.DISCONNECTED) {
            hubConnection.stop();
        }
    }

    public void joinList(String remoteListId) {
        if (!hasText(remoteListId)) return;
        synchronized (joinedListIds) {
            joinedListIds.add(remoteListId);
        }
        HubConnection connection = hubConnection;
        if (connection != null
                && connection.getConnectionState() == HubConnectionState.CONNECTED
        ) {
            connection.send("JoinList", remoteListId);
        }
    }

    public void leaveList(String remoteListId) {
        if (!hasText(remoteListId)) return;
        synchronized (joinedListIds) {
            joinedListIds.remove(remoteListId);
        }
        HubConnection connection = hubConnection;
        if (connection != null
                && connection.getConnectionState() == HubConnectionState.CONNECTED
        ) {
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
        return BackendUrl.shoppingListHubUrl(hubBaseUrl, apiKey);
    }

    private void sendJoinedLists() {
        HubConnection connection = hubConnection;
        if (connection == null || connection.getConnectionState() != HubConnectionState.CONNECTED) {
            return;
        }

        synchronized (joinedListIds) {
            for (String remoteListId : joinedListIds) {
                connection.send("JoinList", remoteListId);
            }
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
