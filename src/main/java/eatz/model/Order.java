package eatz.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** One ticket: id, customer name, phone, items, time placed, and status. */
public class Order {
    public final int id;
    public final String customerName;
    public final String phone;
    public final Map<String, Integer> items;
    public final Instant placedAt;
    private OrderStatus status;

    /**
     * Builds a ticket with status WAITING. Re-checks every field.
     *
     * @param id           the order id (1 or more)
     * @param customerName the cleaned name
     * @param phone        exactly 10 digits
     * @param items        item name to quantity
     * @param placedAt     when the order was placed
     */
    public Order(int id, String customerName, String phone, Map<String, Integer> items,
                 Instant placedAt) {
        if (id < 1) {
            throw new IllegalArgumentException("Order id must be 1 or more.");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Name can't be blank.");
        }
        if (customerName.length() > OrderValidator.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Name is too long.");
        }
        if (phone == null || !phone.matches("[0-9]{" + OrderValidator.PHONE_DIGITS + "}")) {
            throw new IllegalArgumentException("Phone must be 10 digits.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Add at least one item.");
        }
        if (placedAt == null) {
            throw new IllegalArgumentException("Time placed cannot be null.");
        }
        int total = 0;
        Map<String, Integer> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            String name = entry.getKey();
            Integer qty = entry.getValue();
            if (name == null || qty == null) {
                throw new IllegalArgumentException("Items cannot contain null.");
            }
            if (qty < 1 || qty > OrderValidator.MAX_PER_ITEM) {
                throw new IllegalArgumentException("Quantity must be a whole number from 1 to 10.");
            }
            total += qty;
            copy.put(name, qty);
        }
        if (total > OrderValidator.MAX_PER_ORDER) {
            throw new IllegalArgumentException("An order can hold at most 20 items.");
        }
        this.id = id;
        this.customerName = customerName;
        this.phone = phone;
        this.items = Collections.unmodifiableMap(copy);
        this.placedAt = placedAt;
        this.status = OrderStatus.WAITING;
    }

    /** @return WAITING, READY or CANCELLED */
    public OrderStatus getStatus() {
        return status;
    }

    /** Only OrderLine (same package) changes the status. */
    void setStatus(OrderStatus status) {
        this.status = status;
    }
}
