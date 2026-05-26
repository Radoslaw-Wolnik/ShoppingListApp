# Shopping List App

Android shopping list app built with Java, XML views, Room, LiveData, and a .NET backend.

The app is still local-first: Room is the UI source of truth, so the screens stay responsive and edits work while offline. Backend sync runs around that local model by storing backend GUIDs beside local Room ids and replaying queued local edits when the network is available.

## What It Does

- Create and manage multiple shopping lists.
- Group items into categories.
- Expand and collapse categories.
- Add, rename, check, reset, copy, and delete list content.
- Keep local data in Room.
- Register this device with the backend automatically.
- Sync lists, categories, and items with the backend.
- Retry offline edits through a local outbox table.
- Listen for realtime backend updates with SignalR while a list is open.

## Backend Connection

The backend lives at:

```text
C:\Users\rados\Github\ShoppingListBackend
```

The Android app connects to the backend using:

- `POST /api/devices/register` on first sync.
- `X-API-Key` on authenticated HTTP requests.
- `/api/shopping-lists` REST endpoints for durable list/category/item changes.
- `/hub/shoppingLists?apiKey=...` for SignalR updates.

No manual API key setup is needed. The app stores the returned device id and API key in `backend_auth` SharedPreferences.

## Sync Design

Room keeps local `long` ids for the UI. The backend owns `Guid` ids. To bridge that:

- `shopping_list`, `category`, and `item` now have a nullable `remote_id`.
- Local edits are written to Room first.
- Backend work is queued in the `outbox` table.
- `SyncManager` uploads missing remote ids, replays the outbox, then downloads the hydrated backend lists.
- `WorkManager` retries sync periodically when the network is connected.
- Detail screens join the list SignalR group, and incoming events trigger a refresh sync.

Client-only settings, such as favourites and display preferences, stay local.

## Run With Backend

### Option 1: Local .NET API, default Android config

The Android app defaults to:

```text
http://10.0.2.2:5295/
```

`10.0.2.2` is the Android emulator alias for your host machine, and `5295` is the backend's local HTTP launch profile.

Start PostgreSQL and Redis, then run the API:

```powershell
cd C:\Users\rados\Github\ShoppingListBackend
copy .env.example .env
docker compose up -d db redis

$env:ConnectionStrings__DefaultConnection="Host=localhost;Port=5432;Database=ShoppingListApp;Username=ShoppingListApp;Password=please-change-this-local-development-password"
$env:ConnectionStrings__SignalRRedis="localhost:6379"
dotnet run --project src\ShoppingListBackend.Api --launch-profile http
```

Then open this Android project in Android Studio and run the app on an emulator.

### Option 2: Full Docker backend

Docker exposes the API on host port `8080`:

```powershell
cd C:\Users\rados\Github\ShoppingListBackend
copy .env.example .env
docker compose up --build
```

For the Android emulator, set the app backend URL to:

```properties
shoppingBackendBaseUrl=http://10.0.2.2:8080/
```

You can put that in this app's `gradle.properties`, or pass it for a build:

```powershell
.\gradlew.bat installDebug -PshoppingBackendBaseUrl=http://10.0.2.2:8080/
```

### Physical Device

Use your computer's LAN IP instead of `10.0.2.2`, for example:

```properties
shoppingBackendBaseUrl=http://192.168.1.50:5295/
```

Make sure Windows Firewall allows the backend port.

## Android Build

Compile the app:

```powershell
cd C:\Users\rados\StudioProjects\ShoppingListApp
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
- If the backend is unavailable, local edits remain in Room and the outbox until a later sync succeeds.
