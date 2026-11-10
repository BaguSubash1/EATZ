package eatz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.Map;
import org.junit.Test;

public class OrderInfoTest {
    private static final String PHONE = "5555550142";

    @Test
    public void accessorsReturnValues() {
        OrderInfo info = new OrderInfo(3, "Ana", PHONE, Map.of("taco", 2), 4, 12);
        assertEquals(3, info.id());
        assertEquals("Ana", info.name());
        assertEquals(PHONE, info.phone());
        assertEquals(Map.of("taco", 2), info.items());
        assertEquals(4, info.position());
        assertEquals(12, info.waitMinutes());
    }

    @Test
    public void badValuesThrow() {
        Map<String, Integer> items = Map.of("taco", 2);
        assertThrows(IllegalArgumentException.class,
                () -> new OrderInfo(3, "Ana", PHONE, items, -1, 12));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderInfo(3, "Ana", PHONE, items, 4, -1));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderInfo(3, null, PHONE, items, 4, 12));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderInfo(3, "Ana", null, items, 4, 12));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderInfo(3, "Ana", PHONE, null, 4, 12));
    }

    @Test
    public void itemsAreUnmodifiable() {
        OrderInfo info = new OrderInfo(3, "Ana", PHONE, Map.of("taco", 2), 4, 12);
        assertThrows(UnsupportedOperationException.class, () -> info.items().put("churro", 1));
    }
}
