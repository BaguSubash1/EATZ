package eatz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import eatz.model.LinkedDeque.Node;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;

public class LinkedDequeTest {
    private LinkedDeque<String> deque;

    @Before
    public void setUp() {
        deque = new LinkedDeque<>();
    }

    @Test
    public void newDequeIsEmpty() {
        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
        assertThrows(NoSuchElementException.class, () -> deque.dequeue());
    }

    @Test
    public void enqueueKeepsOrder() {
        deque.enqueue("A");
        deque.enqueue("B");
        assertEquals(2, deque.size());
        assertEquals(List.of("A", "B"), deque.toList());
        assertEquals("A", deque.peek());
    }

    @Test
    public void enqueueNullThrows() {
        deque.enqueue("A");
        deque.enqueue("B");
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> deque.enqueue(null));
        assertEquals("Item cannot be null.", e.getMessage());
        assertEquals(2, deque.size());
    }

    @Test
    public void enqueueFrontPutsItemFirst() {
        deque.enqueue("A");
        deque.enqueue("B");
        deque.enqueueFront("X");
        assertEquals(List.of("X", "A", "B"), deque.toList());
        assertEquals(3, deque.size());
    }

    @Test
    public void enqueueFrontIntoEmpty() {
        deque.enqueueFront("X");
        assertEquals("X", deque.peek());
        assertEquals(List.of("X"), deque.toList());
        deque.enqueue("A");
        assertEquals(List.of("X", "A"), deque.toList());
    }

    @Test
    public void enqueueFrontNullThrows() {
        deque.enqueue("A");
        assertThrows(IllegalArgumentException.class, () -> deque.enqueueFront(null));
        assertEquals(List.of("A"), deque.toList());
    }

    @Test
    public void dequeueReturnsFront() {
        deque.enqueue("A");
        deque.enqueue("B");
        deque.enqueue("C");
        assertEquals("A", deque.dequeue());
        assertEquals(List.of("B", "C"), deque.toList());
        assertEquals(2, deque.size());
    }

    @Test
    public void dequeueOnlyItemEmptiesDeque() {
        deque.enqueue("A");
        assertEquals("A", deque.dequeue());
        assertTrue(deque.isEmpty());
        assertThrows(NoSuchElementException.class, () -> deque.peek());
        // still usable afterwards
        deque.enqueue("B");
        assertEquals(List.of("B"), deque.toList());
    }

    @Test
    public void dequeueEmptyThrows() {
        assertThrows(NoSuchElementException.class, () -> deque.dequeue());
    }

    @Test
    public void peekDoesNotRemove() {
        deque.enqueue("A");
        deque.enqueue("B");
        assertEquals("A", deque.peek());
        assertEquals(2, deque.size());
    }

    @Test
    public void peekEmptyThrows() {
        assertThrows(NoSuchElementException.class, () -> deque.peek());
    }

    @Test
    public void isEmptyAndSize() {
        deque.enqueue("A");
        assertFalse(deque.isEmpty());
        deque.dequeue();
        assertTrue(deque.isEmpty());
        deque.enqueue("A");
        deque.enqueue("B");
        assertEquals(2, deque.size());
        assertThrows(IllegalArgumentException.class, () -> deque.enqueue(null));
        assertEquals(2, deque.size());
    }

    @Test
    public void removeMiddle() {
        deque.enqueue("A");
        Node<String> b = deque.enqueue("B");
        deque.enqueue("C");
        deque.remove(b);
        assertEquals(List.of("A", "C"), deque.toList());
        assertEquals(2, deque.size());
        assertEquals("A", deque.peek());
    }

    @Test
    public void removeFirst() {
        Node<String> a = deque.enqueue("A");
        deque.enqueue("B");
        deque.enqueue("C");
        deque.remove(a);
        assertEquals(List.of("B", "C"), deque.toList());
        assertEquals("B", deque.peek());
        assertEquals(2, deque.size());
    }

    @Test
    public void removeLast() {
        deque.enqueue("A");
        deque.enqueue("B");
        Node<String> c = deque.enqueue("C");
        deque.remove(c);
        assertEquals(List.of("A", "B"), deque.toList());
        // the tail moved, so a new enqueue lands after B
        deque.enqueue("D");
        assertEquals(List.of("A", "B", "D"), deque.toList());
    }

    @Test
    public void removeOnlyNode() {
        Node<String> a = deque.enqueue("A");
        deque.remove(a);
        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
        assertTrue(deque.toList().isEmpty());
    }

    @Test
    public void removeNullThrows() {
        deque.enqueue("A");
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> deque.remove(null));
        assertEquals("Node cannot be null.", e.getMessage());
        assertEquals(List.of("A"), deque.toList());
    }

    @Test
    public void toListIsACopy() {
        deque.enqueue("A");
        deque.enqueue("B");
        List<String> list = deque.toList();
        list.add("Z");
        list.remove("A");
        assertEquals(List.of("A", "B"), deque.toList());
    }

    @Test
    public void toListEmpty() {
        assertTrue(deque.toList().isEmpty());
    }

    @Test
    public void nodeGetData() {
        Node<String> a = deque.enqueue("A");
        assertEquals("A", a.getData());
        deque.remove(a);
        assertEquals("A", a.getData());
    }
}
