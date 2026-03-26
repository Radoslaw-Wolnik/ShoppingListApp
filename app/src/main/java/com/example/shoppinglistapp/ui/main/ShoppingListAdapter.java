package com.example.shoppinglistapp.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppinglistapp.R;
import com.example.shoppinglistapp.data.local.entity.ShoppingList;
import com.example.shoppinglistapp.data.local.queryresult.ShoppingListWithCount;
import com.example.shoppinglistapp.databinding.ItemShoppingListBinding;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private List<ShoppingListWithCount> shoppingLists = new ArrayList<>();
    private final OnItemClickListener listener;

    // constructor
    public ShoppingListAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    // --- RecyclerViewer methods ---
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShoppingListBinding binding = ItemShoppingListBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingListWithCount current = shoppingLists.get(position);
        holder.bind(current, listener);
    }

    @Override
    public int getItemCount() {
        return shoppingLists.size();
    }

    public void setShoppingLists(List<ShoppingListWithCount> lists) {
        this.shoppingLists = lists;
        notifyDataSetChanged();
    }

    // interface for click listener
    public interface OnItemClickListener {
        void onItemClick(ShoppingList shoppingList); // we pass the embedded ShoppingList
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemShoppingListBinding binding;

        public ViewHolder(@NonNull ItemShoppingListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // what happens on the viewHolder bind
        public void bind(final ShoppingListWithCount item, final OnItemClickListener listener) {
            ShoppingList list = item.getShoppingList();
            binding.titleTextView.setText(list.getTitle());

            String countText = item.getCheckedItemCount()  + " / " + item.getItemCount();
            binding.countTextView.setText(countText);

            binding.dateTextView.setText(android.text.format.DateFormat.format("dd/MM/yyyy", list.getCreatedAt()));
            binding.favouriteIcon.setVisibility(list.isFavourite() ? View.VISIBLE : View.GONE);


            itemView.setOnClickListener(v -> listener.onItemClick(list));
        }
    }
}
