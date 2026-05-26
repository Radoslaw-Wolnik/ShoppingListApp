package com.example.shoppinglistapp.ui.listdetails.expandablelist.util;


import com.example.shoppinglistapp.data.local.queryresult.CategoryWithItems;
import com.example.shoppinglistapp.data.local.entity.Item;
import com.example.shoppinglistapp.ui.listdetails.expandablelist.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class FlatListBuilder {
    public static List<ListItem> buildFlatList(
            List<CategoryWithItems> categories,
            Set<Long> expandedCategoryIds
    ) {
        List<ListItem> flatList = new ArrayList<>();
        List<CategoryWithItems> sortedCategories = new ArrayList<>(categories);
        sortedCategories.sort(Comparator
                .comparingInt((CategoryWithItems categoryWithItems) -> categoryWithItems.category.getPosition())
                .thenComparingLong(categoryWithItems -> categoryWithItems.category.getId()));

        for (CategoryWithItems categoryWithItems : sortedCategories) {
            var category = categoryWithItems.category;
            var expanded = expandedCategoryIds.contains(category.getId());
            List<Item> sortedItems = new ArrayList<>(categoryWithItems.items);
            sortedItems.sort(Comparator
                    .comparingInt(Item::getPosition)
                    .thenComparingLong(Item::getId));

            // Compute progress
            int total = sortedItems.size();
            int completed = 0;
            for (var task : sortedItems) {
                if (task.isDone()) completed++;
            }
            flatList.add(new CategoryItem(category.getId(), category.getName(), expanded, completed, total));

            if (expanded) {
                // Add tasks
                for (var task : sortedItems) {
                    flatList.add(new TaskItem(task.id, task.getDescription(), task.isDone()));
                }
                // Add placeholder
                flatList.add(new PlaceholderTaskItem(category.getId()));
            }
        }

        // Add placeholder
        flatList.add(new PlaceholderCategoryItem());

        return flatList;
    }
}
