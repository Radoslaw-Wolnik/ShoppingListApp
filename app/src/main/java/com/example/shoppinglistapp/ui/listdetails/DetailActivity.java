package com.example.shoppinglistapp.ui.listdetails;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.shoppinglistapp.R;
import com.example.shoppinglistapp.data.local.queryresult.CategoryWithItems;
import com.example.shoppinglistapp.data.local.queryresult.ShoppingListWithAllItems;
import com.example.shoppinglistapp.databinding.ActivityDetailBinding;
import com.example.shoppinglistapp.ui.listdetails.expandablelist.adapter.ExpandableShoppingListAdapter;
import com.example.shoppinglistapp.ui.listdetails.expandablelist.util.FlatListBuilder;
import com.example.shoppinglistapp.viewmodel.ListDetailViewModel;
import com.example.shoppinglistapp.viewmodel.factory.ListDetailViewModelFactory;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private MaterialToolbar toolbar;
    private ListDetailViewModel viewModel;
    private ExpandableShoppingListAdapter adapter;

    private boolean isEditing = false;
    private EditText activeEditor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = binding.topAppBar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // disable default title
        }


        long listId = getIntent().getLongExtra("shopping_list_id", -1);
        if (listId == -1) {
            finish();
            return;
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                android.util.Log.d("DETAIL_EDIT", "the back button has been pressed");
                if (isEditing) {
                    exitEditMode();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Setup adapter with listener
        adapter = new ExpandableShoppingListAdapter(new ExpandableShoppingListAdapter.OnItemClickListener() {
            @Override
            public void onCategoryToggle(long categoryId) {
                viewModel.toggleCategoryExpanded(categoryId);
            }

            @Override
            public void onCategoryNameEdit(long categoryId, String currentName) {
                showEditCategoryNameDialog(categoryId, currentName);
            }

            @Override
            public void onTaskClick(long taskId, boolean isDone) {
                viewModel.toggleTaskDone(taskId, isDone);
            }

            @Override
            public void onTaskTextEdit(long taskId, String currentText) {
                showEditTaskDescriptionDialog(taskId, currentText);
            }

            @Override
            public void onNewItemEntered(long categoryId, String text) {
                viewModel.addNewTask(categoryId, text);
            }

            @Override
            public void onNewCategoryEntered(String text) {
                viewModel.addNewCategory(text);
            }

            @Override
            public void onStartEditing(EditText editor) {
                activeEditor = editor;
                isEditing = true;
                android.util.Log.d("DETAIL_EDIT", "enterEditMode: " + editor);

                binding.editModeOverlay.setVisibility(View.VISIBLE);
            }
        });

        binding.editModeOverlay.setOnClickListener(v -> exitEditMode());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // ViewModel
        ListDetailViewModelFactory factory = new ListDetailViewModelFactory(listId);
        viewModel = new ViewModelProvider(this, factory).get(ListDetailViewModel.class);

        adapter.setShowCheckboxes(viewModel.getShowCheckboxes());
        adapter.setHideMarkedItems(viewModel.getMarkedItemsHide());

        // Set click listener on the custom title TextView
        binding.titleTextView.setOnClickListener(v -> {
            ShoppingListWithAllItems currentList = viewModel.getShoppingList().getValue();
            if (currentList != null && currentList.shoppingList != null) {
                showEditListNameDialog(currentList.shoppingList.getTitle());
            }
        });

        // set click listener for the menu items
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.favourite_star) {
                viewModel.toggleFavourite();
                return true;
            }
            else if (item.getItemId() == R.id.action_redo) {
                viewModel.redoList();
                return true;
            }
            else if (item.getItemId() == R.id.action_copy) {
                viewModel.copyList(newListId -> {
                    Toast.makeText(this, "List copied successfully", Toast.LENGTH_SHORT).show();
                });
                return true;
                }
            else if (item.getItemId() == R.id.action_delete) {
                viewModel.deleteList();
                finish();
                return true;
            }
            return false;
        });



        // Observe data
        viewModel.getShoppingList().observe(this, listWithItems -> {
            Log.d("DetailActivityInfo", "shoppingList updated");
            updateList();
            updateTitle(listWithItems);   // we'll update the custom TextView here
            invalidateOptionsMenu();  // this triggers onPrepareOptionsMenu
        });
        viewModel.getExpandedIds().observe(this, expandedIds -> updateList());

        // set the back button in the top app bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh settings from repository (in case they changed in SettingsActivity)
        viewModel.refreshSettings();
        // Update the adapter with the new settings
        adapter.setShowCheckboxes(viewModel.getShowCheckboxes());
        adapter.setHideMarkedItems(viewModel.getMarkedItemsHide());
    }


    private void exitEditMode() {
        android.util.Log.d("DETAIL_EDIT", "exitEditMode called. activeEditor=" + activeEditor);

        isEditing = false;

        binding.editModeOverlay.setVisibility(View.GONE);

        if (activeEditor != null) {
            activeEditor.clearFocus();
            activeEditor.setText("");
            activeEditor.setCursorVisible(false); // optional, cleaner UX
            activeEditor = null;
        }

        binding.getRoot().requestFocus();

        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        }
    }


    private void updateList() {
        Log.d("DetailActivityInfo", "shoppingList updateList method runed");
        ShoppingListWithAllItems listWithItems = viewModel.getShoppingList().getValue();
        if (listWithItems == null) return; // or show an empty state

        List<CategoryWithItems> categories = listWithItems.categories;

        Set<Long> expanded = viewModel.getExpandedIds().getValue();
        if (categories != null && expanded != null) {
            adapter.submitList(FlatListBuilder.buildFlatList(categories, expanded));
        }
    }

    private void updateTitle(ShoppingListWithAllItems listWithItems) {
        if (listWithItems == null || listWithItems.shoppingList == null) return;
        String title = listWithItems.shoppingList.getTitle();
        binding.titleTextView.setText(title);  // always update the custom TextView
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem favItem = menu.findItem(R.id.favourite_star);
        if (favItem != null) {
            ShoppingListWithAllItems list = viewModel.getShoppingList().getValue();
            boolean isFav = (list != null && list.shoppingList != null) ? list.shoppingList.isFavourite() : false;
            favItem.setIcon(isFav ? R.drawable.star_marked : R.drawable.star_unmrked);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // MENU: inflate a menu with a rename action
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu); // see menu XML below
        return true;
    }

    // --- Dialogs for user input ---

    private void showEditListNameDialog(String currentName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit shopping list title");

        EditText input = new EditText(this);
        input.setText(currentName);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                viewModel.updateShoppingListTitle(newName);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditCategoryNameDialog(long categoryId, String currentName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit category name");

        EditText input = new EditText(this);
        input.setText(currentName);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                viewModel.updateCategoryName(categoryId, newName);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditTaskDescriptionDialog(long taskId, String currentText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit item description");

        EditText input = new EditText(this);
        input.setText(currentText);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newText = input.getText().toString().trim();
            if (!newText.isEmpty()) {
                viewModel.updateTaskDescription(taskId, newText);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
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