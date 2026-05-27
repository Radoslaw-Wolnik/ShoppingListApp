# Shopping List App

Android shopping list app built with Java, XML views, Room, LiveData, WorkManager, Retrofit, and SignalR.

The app is local-first. Room is the source of truth for the UI, so lists stay usable while the device is offline. Sync runs around that local model: the app keeps backend GUIDs next to local Room ids, queues local edits in an outbox, and replays them when the backend is reachable again.

The backend lives in a separate repository: [Radoslaw-Wolnik/ShoppingListBackend](https://github.com/Radoslaw-Wolnik/ShoppingListBackend).

## Features

- Create and manage multiple shopping lists.
- Group items into categories.
- Expand and collapse categories.
- Add, rename, check, reset, copy, and delete list content.
- Keep local data in Room.
- Sync lists, categories, and items with the backend.
- Retry offline edits through a local outbox table.
- Register the device with the backend automatically.
- Receive realtime list updates through SignalR while a list is open.

## Setup

The recommended local setup is to run the backend with Docker and run the Android app from Android Studio.

### 1. Start the backend

Clone and start the backend repository:

```powershell
git clone https://github.com/Radoslaw-Wolnik/ShoppingListBackend.git
cd ShoppingListBackend
copy .env.example .env
docker compose up --build
```

The Docker setup exposes the API on your host machine at:

```text
http://localhost:8080/
```

For the Android emulator, the same service is available through Android's host alias:

```text
http://10.0.2.2:8080/
```

That emulator URL is the app's default debug backend URL, so no extra Android configuration is needed for the Docker setup.

### 2. Run the Android app

Open this repository in Android Studio and run the `app` configuration on an emulator.

From the command line, you can also build or install it with Gradle:

```powershell
.\gradlew.bat compileDebugJavaWithJavac
.\gradlew.bat installDebug
```

### Using a different backend URL

If you are running the backend somewhere else, pass `shoppingBackendBaseUrl` when building the app:

```powershell
.\gradlew.bat installDebug -PshoppingBackendBaseUrl=http://10.0.2.2:5295/
```

You can also put the property in your local `gradle.properties`:

```properties
shoppingBackendBaseUrl=http://10.0.2.2:5295/
```

For a physical Android device, use your computer's LAN IP instead of `10.0.2.2`:

```properties
shoppingBackendBaseUrl=http://192.168.1.50:8080/
```

Make sure your firewall allows the backend port you choose.

## Backend Connection

The app talks to the backend through:

- `POST /api/devices/register` for first-time device registration.
- `X-API-Key` on authenticated HTTP requests.
- `/api/shopping-lists` REST endpoints for list, category, and item changes.
- `/hub/shoppingLists?apiKey=...` for SignalR updates.

No manual API key setup is needed. The app stores the returned device id and API key in `backend_auth` SharedPreferences.

## Sync Model

Room keeps local `long` ids for fast UI work. The backend owns `Guid` ids for synced data. To bridge the two:

- `shopping_list`, `category`, and `item` rows have nullable `remote_id` values.
- Local edits are written to Room first.
- Backend work is queued in the `outbox` table.
- `SyncManager` uploads missing remote ids, replays the outbox, then downloads the latest backend state.
- `WorkManager` retries sync periodically while the network is connected.
- Detail screens join the list SignalR group, and incoming events trigger a refresh sync.

Client-only settings, such as favourites and display preferences, stay local.

## Development Commands

Compile the app:

```powershell
.\gradlew.bat compileDebugJavaWithJavac
```

Run unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

## Project Structure

```text
app/src/main/java/com/example/shoppinglistapp/
  data/local/       Room database, entities, DAOs, migrations
  data/remote/      Retrofit backend client and SignalR client
  data/repository/  App repository used by ViewModels
  data/sync/        Outbox sync and WorkManager worker
  ui/               Activities and RecyclerView adapters
  viewmodel/        Main, detail, and settings ViewModels
```

## Notes

- The development backend uses cleartext HTTP, so the debug app allows cleartext traffic.
- The emulator must be able to reach the backend URL before sync can succeed.
- Existing local rows without `remote_id` are uploaded during sync.
- If the backend is unavailable, local edits stay in Room and the outbox until a later sync succeeds.
