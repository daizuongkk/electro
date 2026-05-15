package com.daizuongkk.web.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

public final class FlashUtils {
    private static final String PREFIX = "__flash_";

    private FlashUtils() {
    }

    public static void put(HttpServletRequest request, String name, Object value) {
        if (value == null) {
            return;
        }
        request.getSession().setAttribute(PREFIX + name, value);
    }

    public static void putAll(HttpServletRequest request, Map<String, ?> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        values.forEach((name, value) -> put(request, name, value));
    }

    public static void consume(HttpServletRequest request, String... names) {
        HttpSession session = request.getSession(false);
        if (session == null || names == null) {
            return;
        }

        for (String name : names) {
            String key = PREFIX + name;
            Object value = session.getAttribute(key);
            if (value != null) {
                request.setAttribute(name, value);
                session.removeAttribute(key);
            }
        }
    }
}
