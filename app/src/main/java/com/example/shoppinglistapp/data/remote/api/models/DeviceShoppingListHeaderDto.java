package com.example.shoppinglistapp.data.remote.api.models;

import java.util.ArrayList;
import java.util.List;

public class DeviceShoppingListHeaderDto {
    public String id;
    public String title;
    public DeviceInfoDto owner;
    public List<DeviceInfoDto> editors = new ArrayList<>();
    public String updatedAt;
}
