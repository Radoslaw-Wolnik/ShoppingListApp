package com.example.shoppinglistapp.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class SettingsRepository {
    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_THEME_MODE = "theme_mode"; // -1 : auto | 1 : light | 0 : dark
    private static final String KEY_SHOW_CHECKBOXES = "show_checkboxes"; // default true
    private static final String KEY_MOVE_FAVOURITES_TO_TOP = "move_favourites_to_top"; // default false
    private static final String KEY_CATEGORIES_COLLAPSE = "categories_collapse"; // default false
    private static final String KEY_MARKED_ITEMS_HIDE = "marked_items_hide"; // default false
    private static final String KEY_ADD_DEFAULT_CATEGORY = "add_default_category"; // default false
    private static final String KEY_DEFAULT_CATEGORY_NAME = "default_category_name"; // default "Default"
    private static final String KEY_FOCUSED_MODE = "focused_mode"; // default false
    private static final String KEY_FIRST_RUN = "first_run"; // default true

    private final SharedPreferences prefs;

    public SettingsRepository(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Theme mode
    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    // Show checkboxes
    public boolean getShowCheckboxes() {
        return prefs.getBoolean(KEY_SHOW_CHECKBOXES, true);  // default true
    }
    public void setShowCheckboxes(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_CHECKBOXES, show).apply();
    }

    // Move favourites to top
    public boolean getMoveFavouritesToTop() {
        return prefs.getBoolean(KEY_MOVE_FAVOURITES_TO_TOP, true);
    }

    public void setMoveFavouritesToTop(boolean move) {
        prefs.edit().putBoolean(KEY_MOVE_FAVOURITES_TO_TOP, move).apply();
    }

    // Collapse all categories when opening a list
    public boolean getCategoriesCollapse() {
        return prefs.getBoolean(KEY_CATEGORIES_COLLAPSE, false);
    }
    public void setCategoriesCollapse(boolean collapse) {
        prefs.edit().putBoolean(KEY_CATEGORIES_COLLAPSE, collapse).apply();
    }

    // Hide marked items to bottom or set higher opacity
    public boolean getMarkedItemsHide() {
        return prefs.getBoolean(KEY_MARKED_ITEMS_HIDE, false);
    }
    public void setMarkedItemsHide(boolean hide) {
        prefs.edit().putBoolean(KEY_MARKED_ITEMS_HIDE, hide).apply();
    }

    // Add default category
    public boolean getAddDefaultCategory() {
        return prefs.getBoolean(KEY_ADD_DEFAULT_CATEGORY, false);
    }
    public void setAddDefaultCategory(boolean add) {
        prefs.edit().putBoolean(KEY_ADD_DEFAULT_CATEGORY, add).apply();
    }

    // Default category name
    public String getDefaultCategoryName() {
        return prefs.getString(KEY_DEFAULT_CATEGORY_NAME, "Default");
    }
    public void setDefaultCategoryName(String name) {
        prefs.edit().putString(KEY_DEFAULT_CATEGORY_NAME, name).apply();
    }

    // Focused mode
    public boolean getFocusedMode() {
        return prefs.getBoolean(KEY_FOCUSED_MODE, false);
    }
    public void setFocusedMode(boolean focused) {
        prefs.edit().putBoolean(KEY_FOCUSED_MODE, focused).apply();
    }

    // First-run helper
    public boolean isFirstRun() {
        return prefs.getBoolean(KEY_FIRST_RUN, true);
    }

    public void setFirstRunDone() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();
    }
}
