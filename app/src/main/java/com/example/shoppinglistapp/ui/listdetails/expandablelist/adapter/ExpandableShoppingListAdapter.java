package com.example.shoppinglistapp.ui.listdetails.expandablelist.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shoppinglistapp.R;
import com.example.shoppinglistapp.databinding.ItemCategoryBinding;
import com.example.shoppinglistapp.databinding.ItemItemBinding;
import com.example.shoppinglistapp.databinding.ItemPlaceholderCategoryBinding;
import com.example.shoppinglistapp.databinding.ItemPlaceholderTaskBinding;
import com.example.shoppinglistapp.ui.listdetails.expandablelist.model.*;

public class ExpandableShoppingListAdapter extends ListAdapter<ListItem, RecyclerView.ViewHolder> {

    public interface OnItemClickListener {
        void onCategoryToggle(long categoryId);
        void onCategoryNameEdit(long categoryId, String currentName);
        void onTaskClick(long taskId, boolean isDone);
        void onTaskTextEdit(long taskId, String currentText);
        void onNewItemEntered(long categoryId, String text);
        void onNewCategoryEntered(String text);
        void onStartEditing(EditText editor);
    }

    private final OnItemClickListener listener;
    private boolean showCheckboxes = true;
    private boolean hideMarkedItems = false;

    public ExpandableShoppingListAdapter(OnItemClickListener listener) {
        super(new ListItemDiffCallback());
        this.listener = listener;
    }

    public void setShowCheckboxes(boolean show) {
        if (this.showCheckboxes != show) {
            this.showCheckboxes = show;
            notifyDataSetChanged();  // rebind all items
        }
    }

    public void setHideMarkedItems(boolean hide) {
        if (this.hideMarkedItems != hide) {
            this.hideMarkedItems = hide;
            notifyDataSetChanged();  // rebind all items
        }
    }

    @Override
    public int getItemViewType(int position) {
        ListItem item = getItem(position);
        if (item instanceof CategoryItem) return VIEW_TYPE_CATEGORY;
        if (item instanceof TaskItem) return VIEW_TYPE_TASK;
        if (item instanceof PlaceholderTaskItem) return VIEW_TYPE_PLACEHOLDER_ITEM;
        if (item instanceof PlaceholderCategoryItem) return VIEW_TYPE_PLACEHOLDER_CATEGORY;
        throw new IllegalArgumentException("Unknown item type");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_CATEGORY) {
            ItemCategoryBinding binding = ItemCategoryBinding.inflate(inflater, parent, false);
            return new CategoryViewHolder(binding, listener);
        } else if (viewType == VIEW_TYPE_TASK) {
            ItemItemBinding binding = ItemItemBinding.inflate(inflater, parent, false);
            return new TaskViewHolder(binding, listener);
        } else if (viewType == VIEW_TYPE_PLACEHOLDER_ITEM) {
            ItemPlaceholderTaskBinding binding = ItemPlaceholderTaskBinding.inflate(inflater, parent, false);
            return new PlaceholderTaskViewHolder(binding, listener);
        } else if (viewType == VIEW_TYPE_PLACEHOLDER_CATEGORY) {
            ItemPlaceholderCategoryBinding binding = ItemPlaceholderCategoryBinding.inflate(inflater, parent, false);
            return new PlaceholderCategoryViewHolder(binding, listener);
        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = getItem(position);
        if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).bind((CategoryItem) item);
        } else if (holder instanceof TaskViewHolder) {
            ((TaskViewHolder) holder).bind((TaskItem) item);
        } else if (holder instanceof PlaceholderTaskViewHolder) {
            ((PlaceholderTaskViewHolder) holder).bind((PlaceholderTaskItem) item);
        } else if (holder instanceof PlaceholderCategoryViewHolder) {
            ((PlaceholderCategoryViewHolder) holder).bind((PlaceholderCategoryItem) item);
        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
    }

    // clean up listeners to avoid memory leaks
    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof PlaceholderTaskViewHolder) {
            ((PlaceholderTaskViewHolder) holder).binding.nameEditText.setOnEditorActionListener(null);
        } else if (holder instanceof PlaceholderCategoryViewHolder) {
            ((PlaceholderCategoryViewHolder) holder).binding.categoryNameEditText.setOnEditorActionListener(null);
        }
    }


    // ViewHolders
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;
        private final OnItemClickListener listener;
        private CategoryItem currentItem;

        CategoryViewHolder(ItemCategoryBinding binding, OnItemClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;

            binding.getRoot().setOnClickListener(v -> {
                if (currentItem != null) {
                    listener.onCategoryToggle(currentItem.getCategoryId());
                }
            });

            binding.categoryName.setOnClickListener(v -> {
                if (currentItem != null) {
                    listener.onCategoryNameEdit(currentItem.getCategoryId(), currentItem.getName());
                }
            });
        }

        void bind(CategoryItem item) {
            currentItem = item;
            binding.categoryName.setText(item.getName());
            binding.categoryProgress.setText(item.getProgress() == -1 ? ""
                    : item.getProgress() + "%");
            binding.expandIcon.setImageResource(item.isExpanded()
                    ? R.drawable.outline_arrow_drop_up_24
                    : R.drawable.outline_arrow_drop_down_24);
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemItemBinding binding;
        private final OnItemClickListener listener;
        private TaskItem currentItem;

        TaskViewHolder(ItemItemBinding binding, OnItemClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;

            // Whole item click toggles done (excluding text clicks)
            binding.getRoot().setOnClickListener(v -> {
                if (currentItem != null) {
                    listener.onTaskClick(currentItem.getTaskId(), !currentItem.isDone());
                }
            });

            // Checkbox toggles done as well (but avoid double trigger by stopping propagation)
            binding.checkBox.setOnClickListener(v -> {
                if (currentItem != null) {
                    listener.onTaskClick(currentItem.getTaskId(), binding.checkBox.isChecked());
                }
            });

            binding.nameTextView.setOnClickListener(v -> {
                if (currentItem != null) {
                    listener.onTaskTextEdit(currentItem.getTaskId(), currentItem.getDescription());
                }
            });
        }

        void bind(TaskItem item) {
            currentItem = item;

            // ----- Checkbox visibility -----
            if (showCheckboxes) {
                binding.checkBox.setVisibility(View.VISIBLE);
                binding.crossedOverLineImageView.setVisibility(View.GONE);
                binding.checkBox.setChecked(item.isDone());
            } else {
                binding.checkBox.setVisibility(View.GONE);
                // Show the crossed line only when the task is done
                if (item.isDone()) {
                    binding.crossedOverLineImageView.setVisibility(View.VISIBLE);
                } else {
                    binding.crossedOverLineImageView.setVisibility(View.GONE);
                }
            }

            // ----- Opacity for marked items (when hideMarkedItems is true) -----
            if (hideMarkedItems && item.isDone()) {
                //binding.getRoot().setAlpha(0.3f);   // 30% opacity
                binding.itemContainer.setAlpha(0.3f);
            } else {
                binding.itemContainer.setAlpha(1.0f);
                // binding.getRoot().setAlpha(1.0f);
            }

            binding.nameTextView.setText(item.getDescription());
        }
    }

    static class PlaceholderTaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemPlaceholderTaskBinding binding;
        private final OnItemClickListener listener;
        private long categoryId;



        PlaceholderTaskViewHolder(ItemPlaceholderTaskBinding binding, OnItemClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;

            binding.nameEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    listener.onStartEditing(binding.nameEditText);
                }
            });

            // No root click listener – the EditText handles focus
            binding.nameEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = binding.nameEditText.getText().toString().trim();
                    if (!text.isEmpty()) {
                        listener.onNewItemEntered(categoryId, text);
                        binding.nameEditText.setText(""); // clear for next item
                    }
                    return true;
                }
                return false;
            });
        }

        void bind(PlaceholderTaskItem item) {
            this.categoryId = item.getParentCategoryId();
            // No data binding needed, just handle click
        }
    }

    static class PlaceholderCategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemPlaceholderCategoryBinding binding;
        private final OnItemClickListener listener;

        PlaceholderCategoryViewHolder(ItemPlaceholderCategoryBinding binding, OnItemClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;

            binding.categoryNameEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    listener.onStartEditing(binding.categoryNameEditText);
                }
            });

            binding.categoryNameEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = binding.categoryNameEditText.getText().toString().trim();
                    if (!text.isEmpty()) {
                        listener.onNewCategoryEntered(text);
                        binding.categoryNameEditText.setText(""); // clear for next item
                    }
                    return true;
                }
                return false;
            });
        }

        void bind(PlaceholderCategoryItem item) {
            // No data needed
        }
    }


    private static final int VIEW_TYPE_CATEGORY = 0;
    private static final int VIEW_TYPE_TASK = 1;
    private static final int VIEW_TYPE_PLACEHOLDER_ITEM = 2;
    private static final int VIEW_TYPE_PLACEHOLDER_CATEGORY = 3;
}