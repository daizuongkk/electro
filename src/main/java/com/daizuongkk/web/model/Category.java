package com.daizuongkk.web.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public enum Category {

    DIEN_THOAI("Điện Thoại"), LAPTOP("Máy Tính"), PHU_KIEN("Phụ Kiện"), TABLET("Máy Tính Bảng"), KHAC("Khác");

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public static Map<String, String> getAlls() {
        Map<String, String> categories = new LinkedHashMap<>();
        for (Category category : Category.values()) {
            categories.put(category.toString(), category.name);
        }
        return categories;
    }


    public static String getNameByCode(String category) {
        for (Category ctgr : Category.values()) {
            if (ctgr.toString().equals(category)) {
                return ctgr.getName();
            }

        }
        return category;
    }


}
