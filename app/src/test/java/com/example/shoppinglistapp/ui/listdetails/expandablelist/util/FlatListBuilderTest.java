package com.example.shoppinglistapp.ui.listdetails.expandablelist.util;

import com.example.shoppinglistapp.data.local.entity.Category;
import com.example.shoppinglistapp.data.local.entity.Item;
import com.example.shoppinglistapp.data.local.queryresult.CategoryWithItems;
import com.example.shoppinglistapp.ui.listdetails.expandablelist.model.CategoryItem;
import com.example.shoppinglistapp.ui.listdetails.expandablelist.model.ListItem;
import com.example.shoppinglistapp.ui.listdetails.expandablelist.model.TaskItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FlatListBuilderTest {
    @Test
    public void buildFlatList_ordersCategoriesAndItemsByRemotePositionThenLocalId() {
        CategoryWithItems laterCategory = categoryWithItems(20, "Later", 2,
                item(200, "Second item", 2),
                item(100, "First item", 1));
        CategoryWithItems firstCategory = categoryWithItems(10, "First", 1,
                item(300, "Only item", 0));

        List<ListItem> items = FlatListBuilder.buildFlatList(
                Arrays.asList(laterCategory, firstCategory),
                new HashSet<>(Arrays.asList(10L, 20L)));

        assertCategory(items.get(0), 10, "First");
        assertTask(items.get(1), 300, "Only item");
        assertCategory(items.get(3), 20, "Later");
        assertTask(items.get(4), 100, "First item");
        assertTask(items.get(5), 200, "Second item");
    }

    private static CategoryWithItems categoryWithItems(long id, String name, int position, Item... items) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setPosition(position);

        CategoryWithItems categoryWithItems = new CategoryWithItems();
        categoryWithItems.category = category;
        categoryWithItems.items = Arrays.asList(items);
        return categoryWithItems;
    }

    private static Item item(long id, String description, int position) {
        Item item = new Item();
        item.setId(id);
        item.setDescription(description);
        item.setPosition(position);
        return item;
    }

    private static void assertCategory(ListItem item, long id, String name) {
        assertTrue(item instanceof CategoryItem);
        CategoryItem categoryItem = (CategoryItem) item;
        assertEquals(id, categoryItem.getCategoryId());
        assertEquals(name, categoryItem.getName());
    }

    private static void assertTask(ListItem item, long id, String description) {
        assertTrue(item instanceof TaskItem);
        TaskItem taskItem = (TaskItem) item;
        assertEquals(id, taskItem.getTaskId());
        assertEquals(description, taskItem.getDescription());
    }
}
