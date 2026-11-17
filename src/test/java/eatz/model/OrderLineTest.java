package eatz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class OrderLineTest {
    private static final String PHONE = "5555550142";
    private OrderLine line;

    @Before
    public void setUp() {
        line = new OrderLine();
    }

    /** Adds n orders, each from a different phone, so the per-phone cap is not hit. */
    private void addOrders(int n) {
        for (int i = 1; i <= n; i++) {
            line.addOrder("Cust" + i, "555555010" + i, Map.of("taco", 1));
        }
    }

    private List<Integer> ids() {
        List<Integer> ids = new ArrayList<>();
        for (OrderInfo info : line.getLine()) {
            ids.add(info.id());
        }
        return ids;
    }

    @Test
    public void newLineIsEmpty() {
        assertEquals(0, line.size());
        assertTrue(line.isEmpty());
        assertTrue(line.getLine().isEmpty());
        assertEquals(1, line.addOrder("Ana", PHONE, Map.of("taco", 2)).id());
    }

    @Test
    public void isDuplicateOfLast() {
        assertFalse(line.isDuplicateOfLast("Ana", PHONE, Map.of("taco", 2)));
        line.addOrder("Ana", PHONE, Map.of("taco", 2));
        assertTrue(line.isDuplicateOfLast("Ana", PHONE, Map.of("taco", 2)));
        assertFalse(line.isDuplicateOfLast("Ana", PHONE, Map.of("churro", 1)));
        assertThrows(IllegalArgumentException.class,
                () -> line.isDuplicateOfLast(null, PHONE, Map.of("taco", 2)));
    }

    @Test
    public void addOrderAssignsIdsPositionsAndWaits() {
        OrderInfo first = line.addOrder("Ana", PHONE, Map.of("taco", 2));
        assertEquals(1, first.id());
        assertEquals(1, first.position());
        assertEquals(3, first.waitMinutes());
        OrderInfo second = line.addOrder("Ben", "5555550199", Map.of("churro", 1));
        assertEquals(2, second.id());
        assertEquals(2, second.position());
        assertEquals(6, second.waitMinutes());
        assertEquals(2, line.size());
    }

    @Test
    public void addOrderRejectsBadInputAndLeavesLineUnchanged() {
        assertThrows(IllegalArgumentException.class,
                () -> line.addOrder("Ana", "555", Map.of("taco", 2)));
        assertThrows(IllegalArgumentException.class,
                () -> line.addOrder(" ", PHONE, Map.of("taco", 2)));
        assertThrows(IllegalArgumentException.class,
                () -> line.addOrder("Ana", PHONE, Map.of("taco", 0)));
        assertThrows(IllegalArgumentException.class,
                () -> line.addOrder("Ana", PHONE, null));
        assertEquals(0, line.size());
        // a rejected add does not use up an id
        assertEquals(1, line.addOrder("Ana", PHONE, Map.of("taco", 2)).id());
    }

    @Test
    public void addOrderCapsOrdersPerPhone() {
        line.addOrder("Ana", PHONE, Map.of("taco", 1));
        line.addOrder("Ana", PHONE, Map.of("taco", 2));
        line.addOrder("Ana", PHONE, Map.of("taco", 3));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> line.addOrder("Ana", PHONE, Map.of("taco", 4)));
        assertEquals("That phone number already has 3 orders in line.", e.getMessage());
        assertEquals(3, line.size());
    }

    @Test
    public void phoneCapFreesUpWhenAnOrderIsServed() {
        line.addOrder("Ana", PHONE, Map.of("taco", 1));
        line.addOrder("Ana", PHONE, Map.of("taco", 2));
        line.addOrder("Ana", PHONE, Map.of("taco", 3));
        line.markReady();
        OrderInfo added = line.addOrder("Ana", PHONE, Map.of("taco", 4));
        assertEquals(3, added.position());
        assertEquals(3, line.size());
    }

    @Test
    public void markReadyServesFrontAndMovesOthersUp() {
        addOrders(3);
        OrderInfo served = line.markReady();
        assertEquals(1, served.id());
        assertEquals(0, served.position());
        List<OrderInfo> rest = line.getLine();
        assertEquals(List.of(2, 3), ids());
        assertEquals(1, rest.get(0).position());
        assertEquals(3, rest.get(0).waitMinutes());
        assertEquals(2, rest.get(1).position());
        assertEquals(6, rest.get(1).waitMinutes());
        assertEquals(1, line.remake(1).id());
    }

    @Test
    public void markReadyOnEmptyLineThrows() {
        IllegalStateException e =
                assertThrows(IllegalStateException.class, () -> line.markReady());
        assertEquals("Nobody's waiting.", e.getMessage());
        line.addOrder("Ana", PHONE, Map.of("taco", 2));
        line.markReady();
        assertThrows(IllegalStateException.class, () -> line.markReady());
        assertEquals(0, line.size());
    }

    @Test
    public void cancelMiddleFirstAndLast() {
        addOrders(3);
        assertEquals(2, line.cancel(2).id());
        assertEquals(List.of(1, 3), ids());
        assertEquals(2, line.getLine().get(1).position());

        line.cancel(1);
        assertEquals(List.of(3), ids());
        line.cancel(3);
        assertTrue(line.isEmpty());
    }

    @Test
    public void cancelRejectsUnknownCancelledAndServedOrders() {
        addOrders(3);
        line.markReady(); // serves order 1
        line.cancel(2);
        for (int id : new int[] {99, 2, 1}) {
            IllegalArgumentException e =
                    assertThrows(IllegalArgumentException.class, () -> line.cancel(id));
            assertEquals("No such active order.", e.getMessage());
        }
        assertEquals(List.of(3), ids());
    }

    @Test
    public void remakeServedOrderGoesToFront() {
        addOrders(2);
        line.markReady();
        OrderInfo remade = line.remake(1);
        assertEquals(1, remade.position());
        assertEquals(List.of(1, 2), ids());
    }

    @Test
    public void remadeOrderCanBeCancelled() {
        addOrders(2);
        line.markReady();
        line.remake(1);
        line.cancel(1);
        assertEquals(List.of(2), ids());
    }

    @Test
    public void remakeRejectsUnknownCancelledAndWaitingOrders() {
        addOrders(3);
        line.cancel(2);
        for (int id : new int[] {99, 2, 1}) {
            IllegalArgumentException e =
                    assertThrows(IllegalArgumentException.class, () -> line.remake(id));
            assertEquals("No such order to remake.", e.getMessage());
        }
        assertEquals(List.of(1, 3), ids());
    }

    @Test
    public void remakeIsNotBlockedByPhoneCap() {
        line.addOrder("Ana", PHONE, Map.of("taco", 1));
        line.markReady();
        line.addOrder("Ana", PHONE, Map.of("taco", 2));
        line.addOrder("Ana", PHONE, Map.of("taco", 3));
        line.addOrder("Ana", PHONE, Map.of("taco", 4));
        assertEquals(1, line.remake(1).id());
        assertEquals(4, line.size());
    }

    @Test
    public void getLineListsPositionsAndWaits() {
        addOrders(2);
        List<OrderInfo> infos = line.getLine();
        assertEquals(2, infos.size());
        assertEquals(1, infos.get(0).position());
        assertEquals(3, infos.get(0).waitMinutes());
        assertEquals(2, infos.get(1).position());
        assertEquals(6, infos.get(1).waitMinutes());
    }

    @Test
    public void getLineIsUnmodifiable() {
        assertTrue(line.getLine().isEmpty());
        addOrders(1);
        List<OrderInfo> infos = line.getLine();
        assertThrows(UnsupportedOperationException.class, () -> infos.remove(0));
    }

    @Test
    public void sizeAndIsEmpty() {
        assertTrue(line.isEmpty());
        addOrders(2);
        assertEquals(2, line.size());
        assertFalse(line.isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> line.addOrder("X", "555", Map.of("taco", 1)));
        assertEquals(2, line.size());
    }
}
