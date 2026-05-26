package com.example.shoppinglistapp.data.remote.api.models;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListDto {
    public String id;
    public String title;
    public DeviceInfoDto owner;
    public List<DeviceInfoDto> editors = new ArrayList<>();
    public List<CategoryDto> categories = new ArrayList<>();
    public String updatedAt;
}
