package eatz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.Map;
import org.junit.Test;

public class OrderValidatorTest {

    private void assertPhoneRejected(String raw) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> OrderValidator.cleanPhone(raw));
        assertEquals("Phone must be 10 digits.", e.getMessage());
    }

    private void assertItemsRejected(String raw, String message) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> OrderValidator.parseItems(raw));
        assertEquals(message, e.getMessage());
    }

    @Test
    public void cleanNameTrimsAndCollapses() {
        assertEquals("Ana Lee", OrderValidator.cleanName("  Ana   Lee "));
    }

    @Test
    public void cleanNameKeepsCodeLikeTextAsPlainText() {
        assertEquals("'; DROP TABLE", OrderValidator.cleanName("'; DROP TABLE"));
    }

    @Test
    public void cleanNameKeepsEmoji() {
        assertEquals("Ana 🌮", OrderValidator.cleanName("Ana 🌮"));
    }

    @Test
    public void cleanNameBlankOrNullThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> OrderValidator.cleanName(" "));
        assertEquals("Name can't be blank.", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> OrderValidator.cleanName(null));
        assertThrows(IllegalArgumentException.class,
                () -> OrderValidator.cleanName("\u0007\u0001"));
    }

    @Test
    public void cleanNameCutsLongNames() {
        String huge = "a".repeat(10000);
        assertEquals(30, OrderValidator.cleanName(huge).length());
    }

    @Test
    public void cleanNameRemovesControlCharacters() {
        assertEquals("Ana", OrderValidator.cleanName("Ana\u0007"));
    }

    @Test
    public void cleanPhoneRemovesFormatting() {
        assertEquals("5555550142", OrderValidator.cleanPhone("(555) 555-0142"));
        assertEquals("5555550142", OrderValidator.cleanPhone("555.555.0142"));
    }

    @Test
    public void cleanPhoneRejectsBadPhones() {
        assertPhoneRejected("555-0142");
        assertPhoneRejected("abcdefghij");
        assertPhoneRejected("");
        assertPhoneRejected(null);
        assertPhoneRejected("555555014x");
    }

    @Test
    public void parseItemsReadsQuantities() {
        assertEquals(Map.of("taco", 2, "churro", 1), OrderValidator.parseItems("2 taco, churro"));
    }

    @Test
    public void parseItemsAddsRepeatsAndIgnoresCase() {
        assertEquals(Map.of("taco", 2), OrderValidator.parseItems("taco, taco"));
        assertEquals(Map.of("taco", 3), OrderValidator.parseItems("2 TACO, Taco"));
    }

    @Test
    public void parseItemsRejectsBadQuantities() {
        String message = "Quantity must be a whole number from 1 to 10.";
        assertItemsRejected("9999 taco", message);
        assertItemsRejected("0 taco", message);
        assertItemsRejected("-1 taco", message);
        assertItemsRejected("99999999999 taco", message);
        assertItemsRejected("2x taco", message);
    }

    @Test
    public void parseItemsRejectsItemsNotOnMenu() {
        assertItemsRejected("pizza",
                "'pizza' isn't on the menu. Menu: taco, burrito, quesadilla, churro, horchata.");
    }

    @Test
    public void parseItemsRejectsBlank() {
        assertItemsRejected("", "Add at least one item.");
        assertItemsRejected("   ", "Add at least one item.");
        assertItemsRejected(null, "Add at least one item.");
        assertItemsRejected(" , ", "Add at least one item.");
    }

    @Test
    public void parseItemsRejectsTooManyItems() {
        assertItemsRejected("10 taco, 10 churro, 1 burrito", "An order can hold at most 20 items.");
    }
}
