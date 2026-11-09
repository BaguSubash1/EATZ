package eatz.model;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The Queue: a generic doubly linked deque. It knows nothing about orders or food.
 *
 * @param <T> the type of item stored
 */
public class LinkedDeque<T> {

    /**
     * Holds one item plus prev and next links. Outside code can hold a node as a
     * handle, but only LinkedDeque can touch the links.
     *
     * @param <T> the type of item stored
     */
    public static class Node<T> {
        private final T data;
        private Node<T> prev;
        private Node<T> next;

        private Node(T data) {
            this.data = data;
        }

        /** @return the stored item */
        public T getData() {
            return data;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /** Creates an empty deque. */
    public LinkedDeque() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Adds an item at the back.
     *
     * @param item the item (not null)
     * @return the node holding the item
     */
    public Node<T> enqueue(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        Node<T> node = new Node<>(item);
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        size++;
        return node;
    }

    /**
     * Adds an item at the front.
     *
     * @param item the item (not null)
     * @return the node holding the item
     */
    public Node<T> enqueueFront(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        Node<T> node = new Node<>(item);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            head.prev = node;
            node.next = head;
            head = node;
        }
        size++;
        return node;
    }

    /**
     * Removes and returns the front item.
     *
     * @return the front item
     */
    public T dequeue() {
        if (head == null) {
            throw new NoSuchElementException("The deque is empty.");
        }
        Node<T> old = head;
        head = old.next;
        if (head == null) {
            tail = null;
        } else {
            head.prev = null;
        }
        old.next = null;
        size--;
        return old.data;
    }

    /**
     * Returns the front item without removing it.
     *
     * @return the front item
     */
    public T peek() {
        if (head == null) {
            throw new NoSuchElementException("The deque is empty.");
        }
        return head.data;
    }

    /** @return true when no items are stored */
    public boolean isEmpty() {
        return size == 0;
    }

    /** @return the number of items stored */
    public int size() {
        return size;
    }

    /**
     * Unlinks a node from anywhere in the deque. The node must currently be in this deque.
     *
     * @param node the node to unlink
     */
    public void remove(Node<T> node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null.");
        }
        if (node.prev == null) {
            head = node.next;
        } else {
            node.prev.next = node.next;
        }
        if (node.next == null) {
            tail = node.prev;
        } else {
            node.next.prev = node.prev;
        }
        node.prev = null;
        node.next = null;
        size--;
    }

    /** @return a copy of the items, front to back */
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        Node<T> current = head;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }
}
