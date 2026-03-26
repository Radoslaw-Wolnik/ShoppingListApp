package com.example.shoppinglistapp.ui.listdetails.expandablelist.util;


import com.example.shoppinglistapp.data.local.queryresult.CategoryWithItems;
import com.example.shoppinglistapp.ui.listdetails.expandablelist.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FlatListBuilder {
    public static List<ListItem> buildFlatList(
            List<CategoryWithItems> categories,
            Set<Long> expandedCategoryIds
    ) {
        List<ListItem> flatList = new ArrayList<>();
        for (CategoryWithItems categoryWithItems : categories) {
            var category = categoryWithItems.category;
            var expanded = expandedCategoryIds.contains(category.getId());

            // Compute progress
            int total = categoryWithItems.items.size();
            int completed = 0;
            for (var task : categoryWithItems.items) {
                if (task.isDone()) completed++;
            }
            flatList.add(new CategoryItem(category.getId(), category.getName(), expanded, completed, total));

            if (expanded) {
                // Add tasks
                for (var task : categoryWithItems.items) {
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