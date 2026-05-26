package com.example.shoppinglistapp.data.remote.api.models;

import java.util.ArrayList;
import java.util.List;

public class CategoryDto {
    public String id;
    public String name;
    public int position;
    public List<ItemDto> items = new ArrayList<>();
}
