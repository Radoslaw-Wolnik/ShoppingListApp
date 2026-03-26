package com.example.shoppinglistapp.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.example.shoppinglistapp.R;
import com.example.shoppinglistapp.databinding.ActivitySettingsBinding;
import com.example.shoppinglistapp.viewmodel.SettingsViewModel;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private SettingsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // bind the toolbar to the activity
        setSupportActionBar(binding.topAppBar);

        // Enable the Up button (back arrow) in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // ----- Theme Toggle Group -----
        binding.themeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                int mode;
                if (checkedId == R.id.lightThemeBtn) {
                    mode = AppCompatDelegate.MODE_NIGHT_NO;
                } else if (checkedId == R.id.darkThemeBtn) {
                    mode = AppCompatDelegate.MODE_NIGHT_YES;
                } else {
                    mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                }
                viewModel.setThemeMode(mode);
            }
        });

        // Observe theme mode to set initial checked button
        viewModel.getThemeMode().observe(this, mode -> {
            int checkedId;
            if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
                checkedId = R.id.lightThemeBtn;
            } else if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
                checkedId = R.id.darkThemeBtn;
            } else {
                checkedId = R.id.autoThemeBtn;
            }
            binding.themeToggleGroup.check(checkedId);
        });

        // ----- Show Checkboxes -----
        binding.setCheckboxes.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setShowCheckboxes(isChecked));
        viewModel.getShowCheckboxes().observe(this, isChecked ->
                binding.setCheckboxes.setChecked(isChecked));

        // ----- Move Favourites to Top -----
        binding.setFavourites.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setMoveFavouritesToTop(isChecked));
        viewModel.getMoveFavouritesToTop().observe(this, isChecked ->
                binding.setFavourites.setChecked(isChecked));

        // ----- Collapse Categories -----
        binding.setCategoriesCollapse.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setCategoriesCollapse(isChecked));
        viewModel.getCategoriesCollapse().observe(this, isChecked ->
                binding.setCategoriesCollapse.setChecked(isChecked));

        // ----- Marked Items Hide -----
        binding.setMarkedItemsHide.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setMarkedItemsHide(isChecked));
        viewModel.getMarkedItemsHide().observe(this, isChecked ->
                binding.setMarkedItemsHide.setChecked(isChecked));

        // ----- Focused Mode -----
        binding.setFocusedMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setFocusedMode(isChecked));
        viewModel.getFocusedMode().observe(this, isChecked ->
                binding.setFocusedMode.setChecked(isChecked));

        // ----- Add Default Category -----
        binding.setAddDefaultCategory.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setAddDefaultCategory(isChecked));

        viewModel.getAddDefaultCategory().observe(this, isChecked -> {
            binding.setAddDefaultCategory.setChecked(isChecked);
            // Show/hide the default category name UI
            int visibility = isChecked ? View.VISIBLE : View.GONE;
            binding.textDefaultCategoryName.setVisibility(visibility);
            binding.editDefaultCategoryName.setVisibility(visibility);
        });

        // ----- Default Category Name EditText -----
        EditText editDefaultCategoryName = binding.editDefaultCategoryName;

        // Update text when saved value changes (but don't overwrite while user is typing)
        viewModel.getDefaultCategoryName().observe(this, savedName -> {
            if (!editDefaultCategoryName.hasFocus()) {
                editDefaultCategoryName.setText(savedName);
            }
        });

        // Save on "Done" IME action
        editDefaultCategoryName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String newName = editDefaultCategoryName.getText().toString().trim();
                if (!newName.isEmpty()) {
                    viewModel.setDefaultCategoryName(newName);
                } else {
                    viewModel.setDefaultCategoryName("Default"); // fallback
                }
                editDefaultCategoryName.clearFocus();
                hideKeyboard();
                return true;
            }
            return false;
        });

        // Add back press callback
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if the EditText is currently focused
                if (binding.editDefaultCategoryName.hasFocus()) {
                    // Revert to last saved value
                    String savedName = viewModel.getDefaultCategoryName().getValue();
                    binding.editDefaultCategoryName.setText(savedName);
                    binding.editDefaultCategoryName.clearFocus();
                    hideKeyboard();
                    // Do NOT finish the activity yet; we just cancelled editing
                    return;
                }
                // If we get here, no special handling needed – proceed with default back action
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}