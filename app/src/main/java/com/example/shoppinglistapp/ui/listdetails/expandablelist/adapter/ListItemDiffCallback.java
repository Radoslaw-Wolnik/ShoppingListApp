package com.example.shoppinglistapp.ui.listdetails.expandablelist.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import com.example.shoppinglistapp.ui.listdetails.expandablelist.model.*;


public class ListItemDiffCallback extends DiffUtil.ItemCallback<ListItem> {
    @Override
    public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
        if (oldItem.getClass() != newItem.getClass()) {
            return false;
        }
        return oldItem.getId() == newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
        if (oldItem instanceof PlaceholderTaskItem && newItem instanceof PlaceholderTaskItem) {
            return true;
        }
        if (oldItem instanceof CategoryItem && newItem instanceof CategoryItem) {
            CategoryItem oldCat = (CategoryItem) oldItem;
            CategoryItem newCat = (CategoryItem) newItem;
            return oldCat.getName().equals(newCat.getName()) &&
                    oldCat.isExpanded() == newCat.isExpanded() &&
                    oldCat.getProgress() == newCat.getProgress();
        }
        if (oldItem instanceof TaskItem && newItem instanceof TaskItem) {
            TaskItem oldTask = (TaskItem) oldItem;
            TaskItem newTask = (TaskItem) newItem;
            return oldTask.getDescription().equals(newTask.getDescription()) &&
                    oldTask.isDone() == newTask.isDone();
        }
        return false;
    }

    @Override
    public Object getChangePayload(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
        return null; // You can implement partial updates if needed
    }
}