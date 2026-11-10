package eatz.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import eatz.model.OrderLine;
import eatz.view.ConsoleView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.Test;

/** Scripted runs of the whole program (controller + view + model) using a fake keyboard. */
public class EatzControllerTest {
    private OrderLine model;

    private String run(String typed) {
        model = new OrderLine();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ConsoleView view = new ConsoleView(new Scanner(typed), new PrintStream(bytes));
        new EatzController(model, view).run();
        return bytes.toString();
    }

    private int count(String text, String part) {
        int count = 0;
        int at = text.indexOf(part);
        while (at >= 0) {
            count++;
            at = text.indexOf(part, at + 1);
        }
        return count;
    }

    @Test
    public void constructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new EatzController(null, null));
    }

    @Test
    public void parseMenuChoiceAndParseId() {
        EatzController c = new EatzController(new OrderLine(),
                new ConsoleView(new Scanner(""), new PrintStream(new ByteArrayOutputStream())));
        assertEquals(3, c.parseMenuChoice(" 3 "));
        assertThrows(IllegalArgumentException.class, () -> c.parseMenuChoice("6"));
        assertThrows(IllegalArgumentException.class, () -> c.parseMenuChoice("x"));
        assertThrows(IllegalArgumentException.class, () -> c.parseMenuChoice(""));
        assertEquals(12, c.parseId("12"));
        assertThrows(IllegalArgumentException.class, () -> c.parseId("abc"));
    }

    @Test
    public void happyPathAddViewReady() {
        String out = run("1\nAna\n555-555-0142\n2 taco, churro\n4\n2\n4\n");
        assertTrue(out.contains("[TEXT to 555-555-0142] You're #1, about 3 min away."));
        assertTrue(out.contains("order 1  Ana"));
        assertTrue(out.contains("your order 1 is ready!"));
        assertTrue(out.contains("The line is empty."));
        assertTrue(out.endsWith("Goodbye." + System.lineSeparator()));
    }

    @Test
    public void badInputScriptShowsEachErrorOnce() {
        String out = run("x\n"            // bad menu choice
                + "1\nAna\n555\n5555550142\npizza\ntaco\n"  // bad phone, bad item
                + "2\n"                    // mark ready on empty line... after add, so serves Ana
                + "2\n"                    // now empty
                + "3\nabc\n99\n"           // bad id, unknown id
                + "5\n99\n");              // remake unknown id; then end of input
        assertEquals(1, count(out, "Please choose a number from 1 to 5."));
        assertEquals(1, count(out, "Phone must be 10 digits."));
        assertEquals(1, count(out, "isn't on the menu"));
        assertEquals(1, count(out, "Nobody's waiting."));
        assertEquals(1, count(out, "Order ID must be a whole number."));
        assertEquals(1, count(out, "No such active order."));
        assertEquals(1, count(out, "No such order to remake."));
        assertEquals(1, count(out, "Goodbye."));
    }

    @Test
    public void markReadyOnEmptyLine() {
        String out = run("2\n");
        assertTrue(out.contains("Nobody's waiting."));
        assertTrue(out.contains("Goodbye."));
    }

    @Test
    public void duplicateOrderAsksFirst() {
        String order = "1\nAna\n5555550142\ntaco\n";
        String out = run(order + order + "maybe\nn\n" + order + "y\n");
        // asked 3 times: "maybe" (re-asked), "n", then "y" on the third order
        assertEquals(3, count(out, "Duplicate of the last order."));
        assertTrue(out.contains("Please type y or n."));
        assertTrue(out.contains("Order not added."));
        assertEquals(2, model.size());
    }

    @Test
    public void phoneCapIsShownAndLineUnchanged() {
        String order = "1\nAna\n5555550142\n%d taco\n";
        String out = run(String.format(order, 1) + String.format(order, 2)
                + String.format(order, 3) + String.format(order, 4));
        assertTrue(out.contains("That phone number already has 3 orders in line."));
        assertEquals(3, model.size());
    }

    @Test
    public void cancelAndRemakeWork() {
        String out = run("1\nAna\n5555550142\ntaco\n1\nBen\n5555550143\nchurro\n"
                + "2\n5\n1\n3\n2\n4\n");
        assertTrue(out.contains("You're #1, about 3 min away."));
        assertTrue(out.contains("your order 2 was cancelled."));
        assertEquals(1, model.size());
    }

    @Test
    public void weirdNamesAreShownAsPlainText() {
        String out = run("1\n'; DROP TABLE %s %d\n5555550142\ntaco\n");
        assertTrue(out.contains("'; DROP TABLE %s %d"));
        assertEquals(1, model.size());
    }
}
