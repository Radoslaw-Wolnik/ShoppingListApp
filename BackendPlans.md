# Shopping List App – Android (Java + XML)

## Overview
A fully offline shopping list manager that helps users create multiple shopping lists, organise items by categories, track progress, and mark items as done. The app demonstrates a clean MVVM architecture using modern Android Jetpack components while deliberately using **Java and XML** to build a strong foundation in traditional Android development.

## Features
- **Multiple shopping lists** – each with a title, creation date, and progress indicator.
- **Categorised items** – items are grouped under user‑defined categories within each list.
- **Expandable categories** – collapse/expand categories to see only what you need; category header shows progress (e.g., “Dairy (2/5)”).
- **Mark items as done** – tap an item to toggle its completed state.
- **Add new categories and items** – simple dialogs for quick input.
- **Persistent local storage** – all data saved in a SQLite database via Room.
- **Responsive UI** – built with RecyclerView and Material Design Components.
- **Settings** - show/hide checkboxes (instead draw a line over marked items), hide marked items (sets opacity to 30%), move favourite lists to top, add a default category when creating new lists (with setting the category name), changing the theme (light, dark, auto)

## Tech Stack
| Component             | Technology                                                                 |
|-----------------------|----------------------------------------------------------------------------|
| Language              | Java                                                                       |
| UI                    | XML layouts, Material Design Components                                    |
| Architecture          | MVVM (Model‑View‑ViewModel) + Repository                                   |
| Database              | Room Persistence Library (SQLite)                                          |
| Reactive Programming  | LiveData (observed in UI)                                                  |
| Navigation            | Multiple activities with explicit intents; parent activity declared in manifest for Up navigation |
| Background Threads    | Room handles LiveData queries on background threads; manual DB operations via AsyncTask or Executors (can be extended with RxJava/Coroutines later) |
| View Binding          | View Binding (avoid `findViewById`)                                       |

## Architecture Decisions

### 1. MVVM with Repository
The app follows the official Android architecture guidelines:
- **Model**: Room entities (`ShoppingList`, `Category`, `Item`) and the database.
- **Repository**: A single class that centralises all data operations. It abstracts the data source (Room) from the rest of the app and returns `LiveData` where appropriate.
- **ViewModel**: Holds UI‑related data, survives configuration changes, and communicates with the repository. Two ViewModels are used:
    - `MainViewModel` – for the list of shopping lists (used by `MainActivity`).
    - `ShoppingListDetailViewModel` – for a single shopping list with its categories and items (used by `DetailActivity`).
- **View**: Activities that observe LiveData and update the UI. User actions are passed to the ViewModel.

This separation ensures testability, maintainability, and a clear separation of concerns.

### 2. Why Java + XML?
Modern Android development increasingly favours Kotlin and Jetpack Compose. However, a vast number of existing applications and enterprise projects still rely on Java and XML. Understanding this “older” stack is valuable for maintaining legacy codebases, working in diverse teams, and truly appreciating the problems that Kotlin and Compose solve.

This project intentionally uses Java and XML to:
- Build a solid understanding of fundamental Android concepts (Activities, Lifecycle, RecyclerView adapters, Intents, etc.).
- Learn to work with `LiveData` and `ViewModel` without the syntactic sugar of Kotlin.
- Master XML layout creation and `RecyclerView` adapters – skills still essential in many professional settings.
- Prepare for a second version of the same app written in **Kotlin + Jetpack Compose**, allowing a direct comparison and deeper learning.

### 3. Database Design (Room)
Three tables with foreign key relationships:

```
ShoppingList (id, title, createdAt)
    ↑
Category (id, name, shoppingListId)
    ↑
Item (id, name, isDone, categoryId)
```

- A `ShoppingList` can have many `Category` entries.
- A `Category` can have many `Item` entries.
- `@Relation` annotations in a `ShoppingListWithDetails` POJO load the full hierarchy in one query.

### 4. UI Implementation

**Main Screen (`MainActivity`)**
- Layout contains a `RecyclerView` showing each shopping list’s title, creation date, and progress (computed from item counts).
- Clicking a list starts `DetailActivity` and passes the shopping list ID via an intent extra.

**Detail Screen (`DetailActivity`)**
- Displays the list title and date at the top.
- Below, an **expandable `RecyclerView`** that shows categories and their items.
- A single adapter handles two view types:
    - **Category header** – shows category name and progress, click toggles expand/collapse.
    - **Item row** – shows item name with a checkbox style (tap to toggle done).
- A `Set<Integer>` in the adapter tracks which categories are expanded. When a header is clicked, the adapter updates the underlying flattened list (insert/remove item rows) and notifies the change, allowing smooth animations.
- **Floating Action Button (FAB)** opens a dialog to add a new category or item (with a dropdown to select the category).
- **Up navigation**: The manifest declares `DetailActivity`’s parent as `MainActivity`, so the system provides the Up button in the action bar.

### 5. Navigation (Traditional Activity‑based)
The app uses multiple activities, each representing a distinct screen:
- `MainActivity` – the launcher activity, shows all shopping lists.
- `DetailActivity` – shows a specific list with its categories and items.

Switching between screens is done via explicit intents:
```java
Intent intent = new Intent(MainActivity.this, DetailActivity.class);
intent.putExtra("shopping_list_id", listId);
startActivity(intent);
```

To enable proper Up navigation (the back arrow in the action bar), each activity’s parent is declared in the `AndroidManifest.xml`:
```xml
<activity
    android:name=".DetailActivity"
    android:parentActivityName=".MainActivity" />
```

This approach simplifies the navigation logic and is ideal for small apps with few screens, while still allowing the use of modern architecture components.

### 6. Data Observation
`LiveData` from Room is observed in activities. Any database change (insert, update, delete) automatically triggers a UI update. This makes the app reactive and simple to maintain.

## Project Structure
```
app/
├── src/main/
│   ├── java/com/example/shoppinglist/
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── entity/       # Room Database entities
│   │   │   │   ├── dao/          # Data Access Objects
│   │   │   │   ├── queryresult/  # POJOs for querries and joins
│   │   │   │   └── converter/    # type converters
│   │   │   ├── repository/       # ShoppingListRepository
│   │   │   └── AppDatabse.java   # Room database class
│   │   ├── viewmodel/            # MainViewModel, ShoppingListDetailViewModel
│   │   ├── ui/
│   │   │   ├── main/             # MainActivity & adapter
│   │   │   ├── detail/           # DetailActivity & expandable adapter
│   │   │   └── dialogs/          # AddCategoryDialog, AddItemDialog
│   │   ├── utils/                # Helper classes (e.g., DateFormatter)
│   │   └── MyApp.java            # custom app declaration
│   └── res/
│       ├── layout/               # XML layouts for activities and items
│       ├── menu/                 # Menu resources (if any)
│       └── values/               # Colors, strings, themes
```

## Getting Started
1. Clone the repository.
2. Open the project in **Android Studio** (Arctic Fox or newer).
3. Sync Gradle and let dependencies download.
4. Run on an emulator or physical device (API 21+).

## Future Plans
- Implement drag‑and‑drop to reorder categories/items.
- drag and drop items between categories (move them to diff category)
- when dragging categories collapse all categories to move them easly between
- -
- Mby change the marked items hide to be more explicit? - the difference between hidden items and not hidden is not too big
- Swiping right on item sets it as checked (mby, not sure about it) and swiping left removes it?
- Dark theme better support (now looks ugly).
- Data export/import (binary zip file).
- add different language support
- Save Categories - allows you to import from saved categories to current shopping list (eg tomato pasta category, every time you do it you buy the same things)
- saved categories activity (browse saved categories, add new ones, edit or delete saved categories)

## Fixes
- add a better way to changing name of item/category/list - an EditText and not a pop-up dialog
- apply settings change to the app (use LiveData for settings or sth else)
- add delete item and delete category buttons / options


## Why This Project Matters
This app is not just a shopping list – it’s a carefully crafted learning tool that demonstrates:
- How to structure a real‑world Android app with a clean architecture (MVVM + Repository).
- The role of each Jetpack component (Room, ViewModel, LiveData).
- The power and flexibility of `RecyclerView` (including expandable lists).
- How to work with SQLite via Room without writing raw queries.
- Traditional activity‑based navigation with intents and manifest‑defined parent relationships.

By using Java and XML, it provides a solid foundation for developers who may later transition to Kotlin, while also serving as a reference for maintaining and understanding older Android codebases.

---

*“The only way to go fast is to go well.” – Robert C. Martin*