package eatz.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A read-only snapshot of one order. Position 0 means not in line.
 */
public record OrderInfo(int id, String name, String phone, Map<String, Integer> items,
                        int position, int waitMinutes) {

    public OrderInfo {
        if (name == null || phone == null || items == null) {
            throw new IllegalArgumentException("OrderInfo fields cannot be null.");
        }
        if (position < 0 || waitMinutes < 0) {
            throw new IllegalArgumentException("Position and wait cannot be negative.");
        }
        items = Collections.unmodifiableMap(new LinkedHashMap<>(items));
    }
}
