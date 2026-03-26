package com.example.shoppinglistapp.ui.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.shoppinglistapp.R;

import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.shoppinglistapp.databinding.ActivityMainBinding;
import com.example.shoppinglistapp.ui.listdetails.DetailActivity;
import com.example.shoppinglistapp.ui.settings.SettingsActivity;
import com.example.shoppinglistapp.viewmodel.MainViewModel;
import com.example.shoppinglistapp.viewmodel.SettingsViewModel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private SettingsViewModel settingsViewModel;
    private ShoppingListAdapter adapter;
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install the splash screen and get the controller
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setTitle("Home");

        // Inflate and set content view using view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        // Set up RecyclerView
        adapter = new ShoppingListAdapter(shoppingList -> {
            // Navigate to detail activity when an item is clicked
            Log.d("MainActivity", "Item clicked, id = " + shoppingList.getId());
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("shopping_list_id", shoppingList.getId());
            startActivity(intent);
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // Obtain ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Keep splash screen visible until data is loaded
        splashScreen.setKeepOnScreenCondition(() -> !isDataLoaded);

        // Observe LiveData from ViewModel
        viewModel.getSortedShoppingLists().observe(this, shoppingLists -> {
            // Update the adapter with the latest data
            adapter.setShoppingLists(shoppingLists);
            // Mark data as loaded (only once)
            if (!isDataLoaded) {
                isDataLoaded = true;
            }
        });

        // FAB click: create and insert a new shopping list
        binding.fabAdd.setOnClickListener(v -> {

            viewModel.addNewShoppingList(id -> {
                // Now we have the correct ID
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("shopping_list_id", id);
                startActivity(intent);
            });
        });

        // settings click
        binding.topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        binding.settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}