package com.example.shoppinglistapp.viewmodel.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.shoppinglistapp.viewmodel.ListDetailViewModel;

public class ListDetailViewModelFactory implements ViewModelProvider.Factory {
    private final long listId;

    public ListDetailViewModelFactory( long listId) {
        this.listId = listId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ListDetailViewModel.class)) {
            return (T) new ListDetailViewModel(listId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}