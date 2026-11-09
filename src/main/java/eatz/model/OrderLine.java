package eatz.model;

import eatz.model.LinkedDeque.Node;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns the Queue and every rule about the line. This is the one class the controller calls.
 */
public class OrderLine {
    public static final int MAX_ACTIVE_PER_PHONE = 3;
    public static final int MINUTES_PER_ORDER = 3;

    private final LinkedDeque<Order> waiting = new LinkedDeque<>();
    private final Map<Integer, Node<Order>> nodeById = new HashMap<>();
    private final Map<Integer, Order> ordersById = new HashMap<>();
    private int nextId = 1;
    private Order lastOrder = null;

    /** Creates an empty line; the first id is 1. */
    public OrderLine() {
    }

    /**
     * True if all three values equal the most recently created order's.
     *
     * @param name  the clean name
     * @param phone the clean phone
     * @param items the items
     * @return whether it duplicates the last order
     */
    public boolean isDuplicateOfLast(String name, String phone, Map<String, Integer> items) {
        if (name == null || phone == null || items == null) {
            throw new IllegalArgumentException("Order fields cannot be null.");
        }
        if (lastOrder == null) {
            return false;
        }
        return lastOrder.customerName.equals(name)
                && lastOrder.phone.equals(phone)
                && lastOrder.items.equals(items);
    }

    /**
     * Adds an order at the back of the line.
     *
     * @param name  the name
     * @param phone the phone
     * @param items the items
     * @return a snapshot with position and wait
     */
    public OrderInfo addOrder(String name, String phone, Map<String, Integer> items) {
        if (name == null || phone == null || items == null) {
            throw new IllegalArgumentException("Order fields cannot be null.");
        }
        String cleanName = OrderValidator.cleanName(name);
        String cleanPhone = OrderValidator.cleanPhone(phone);

        int count = 0;
        for (Order order : waiting.toList()) {
            if (order.phone.equals(cleanPhone)) {
                count++;
            }
        }
        if (count >= MAX_ACTIVE_PER_PHONE) {
            throw new IllegalArgumentException("That phone number already has 3 orders in line.");
        }

        // The Order constructor checks the items; nothing changes if it throws.
        Order order = new Order(nextId, cleanName, cleanPhone, items, Instant.now());
        nextId++;
        Node<Order> node = waiting.enqueue(order);
        nodeById.put(order.id, node);
        ordersById.put(order.id, order);
        lastOrder = order;
        return makeInfo(order, waiting.size());
    }

    /**
     * Serves the front order.
     *
     * @return a snapshot with position 0
     */
    public OrderInfo markReady() {
        if (waiting.isEmpty()) {
            throw new IllegalStateException("Nobody's waiting.");
        }
        Order order = waiting.dequeue();
        nodeById.remove(order.id);
        order.setStatus(OrderStatus.READY);
        return makeInfo(order, 0);
    }

    /**
     * Cancels a waiting order, wherever it is in the line.
     *
     * @param id the order id
     * @return a snapshot with position 0
     */
    public OrderInfo cancel(int id) {
        Node<Order> node = nodeById.get(id);
        if (node == null) {
            throw new IllegalArgumentException("No such active order.");
        }
        waiting.remove(node);
        nodeById.remove(id);
        Order order = node.getData();
        order.setStatus(OrderStatus.CANCELLED);
        return makeInfo(order, 0);
    }

    /**
     * Puts a served order back at the front of the line.
     *
     * @param id the order id
     * @return a snapshot with position 1
     */
    public OrderInfo remake(int id) {
        Order order = ordersById.get(id);
        if (order == null || order.getStatus() != OrderStatus.READY) {
            throw new IllegalArgumentException("No such order to remake.");
        }
        order.setStatus(OrderStatus.WAITING);
        Node<Order> node = waiting.enqueueFront(order);
        nodeById.put(id, node);
        return makeInfo(order, 1);
    }

    /** @return snapshots front to back with positions 1..n */
    public List<OrderInfo> getLine() {
        List<OrderInfo> line = new ArrayList<>();
        int position = 1;
        for (Order order : waiting.toList()) {
            line.add(makeInfo(order, position));
            position++;
        }
        return Collections.unmodifiableList(line);
    }

    /** @return the number of waiting orders */
    public int size() {
        return waiting.size();
    }

    /** @return true when nobody is waiting */
    public boolean isEmpty() {
        return waiting.isEmpty();
    }

    private OrderInfo makeInfo(Order order, int position) {
        return new OrderInfo(order.id, order.customerName, order.phone, order.items,
                position, position * MINUTES_PER_ORDER);
    }
}
