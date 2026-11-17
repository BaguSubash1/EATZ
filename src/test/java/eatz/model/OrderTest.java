package eatz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class OrderTest {
    private static final String PHONE = "5555550142";
    private final Instant t = Instant.parse("2026-11-09T12:00:00Z");

    @Test
    public void constructorKeepsFields() {
        Order order = new Order(1, "Ana", PHONE, Map.of("taco", 2), t);
        assertEquals(1, order.id);
        assertEquals("Ana", order.customerName);
        assertEquals(PHONE, order.phone);
        assertEquals(Map.of("taco", 2), order.items);
        assertEquals(t, order.placedAt);
        assertEquals(OrderStatus.WAITING, order.getStatus());
    }

    @Test
    public void constructorRejectsBadFields() {
        Map<String, Integer> ok = Map.of("taco", 2);
        assertThrows(IllegalArgumentException.class, () -> new Order(1, "Ana", "555", ok, t));
        assertThrows(IllegalArgumentException.class, () -> new Order(1, " ", PHONE, ok, t));
        assertThrows(IllegalArgumentException.class, () -> new Order(0, "Ana", PHONE, ok, t));
        assertThrows(IllegalArgumentException.class,
                () -> new Order(1, "Ana", PHONE, Map.of(), t));
        assertThrows(IllegalArgumentException.class,
                () -> new Order(1, "Ana", PHONE, Map.of("taco", 0), t));
        assertThrows(IllegalArgumentException.class, () -> new Order(1, null, PHONE, ok, t));
        assertThrows(IllegalArgumentException.class, () -> new Order(1, "Ana", null, ok, t));
        assertThrows(IllegalArgumentException.class, () -> new Order(1, "Ana", PHONE, null, t));
        assertThrows(IllegalArgumentException.class, () -> new Order(1, "Ana", PHONE, ok, null));
    }

    @Test
    public void constructorRejectsCapsEvenWithoutController() {
        assertThrows(IllegalArgumentException.class,
                () -> new Order(1, "Ana", PHONE, Map.of("taco", 9999), t));
    }

    @Test
    public void itemsAreUnmodifiableAndCopied() {
        Map<String, Integer> original = new HashMap<>();
        original.put("taco", 2);
        Order order = new Order(1, "Ana", PHONE, original, t);
        assertThrows(UnsupportedOperationException.class, () -> order.items.put("churro", 1));
        original.put("churro", 1);
        assertEquals(Map.of("taco", 2), order.items);
    }

    @Test
    public void getStatusAfterSetStatus() {
        Order order = new Order(1, "Ana", PHONE, Map.of("taco", 2), t);
        order.setStatus(OrderStatus.READY);
        assertEquals(OrderStatus.READY, order.getStatus());
    }
}
