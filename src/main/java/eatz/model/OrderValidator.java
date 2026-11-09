package eatz.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Cleans and checks raw typed text against the menu and the caps. */
public class OrderValidator {
    public static final List<String> MENU =
            List.of("taco", "burrito", "quesadilla", "churro", "horchata");
    public static final int MAX_PER_ITEM = 10;
    public static final int MAX_PER_ORDER = 20;
    public static final int MAX_NAME_LENGTH = 30;
    public static final int PHONE_DIGITS = 10;

    private OrderValidator() {
    }

    /**
     * Collapses spaces, removes control characters, trims, cuts to 30 characters.
     *
     * @param raw the typed name
     * @return the clean name
     */
    public static String cleanName(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Name can't be blank.");
        }
        String name = raw.replaceAll("\\s+", " ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isISOControl(c)) {
                sb.append(c);
            }
        }
        name = sb.toString().replaceAll("\\s+", " ").trim();
        if (name.length() > MAX_NAME_LENGTH) {
            name = name.substring(0, MAX_NAME_LENGTH);
            // do not leave half an emoji at the end
            if (Character.isHighSurrogate(name.charAt(name.length() - 1))) {
                name = name.substring(0, name.length() - 1);
            }
            name = name.trim();
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name can't be blank.");
        }
        return name;
    }

    /**
     * Removes spaces, dashes, dots and parentheses and checks for 10 digits.
     *
     * @param raw the typed phone
     * @return the 10 digits
     */
    public static String cleanPhone(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Phone must be 10 digits.");
        }
        String phone = raw.replaceAll("[\\s\\-.()]", "");
        if (!phone.matches("[0-9]{" + PHONE_DIGITS + "}")) {
            throw new IllegalArgumentException("Phone must be 10 digits.");
        }
        return phone;
    }

    /**
     * Reads entries like "2 taco, churro" (quantity optional, default 1).
     *
     * @param raw the typed item list
     * @return item name to quantity, repeats added together
     */
    public static Map<String, Integer> parseItems(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Add at least one item.");
        }
        Map<String, Integer> items = new LinkedHashMap<>();
        int total = 0;
        for (String entry : raw.split(",")) {
            entry = entry.trim().replaceAll("\\s+", " ").toLowerCase();
            if (entry.isEmpty()) {
                continue;
            }
            int quantity = 1;
            String name = entry;
            int space = entry.indexOf(' ');
            if (space > 0) {
                String first = entry.substring(0, space);
                // an entry that starts like a number is "quantity name"
                if (first.matches("[-+]?[0-9].*")) {
                    quantity = parseQuantity(first);
                    name = entry.substring(space + 1);
                }
            } else if (entry.matches("[-+]?[0-9].*")) {
                // a number with no item name
                parseQuantity(entry);
            }
            if (!MENU.contains(name)) {
                throw new IllegalArgumentException("'" + name + "' isn't on the menu. Menu: "
                        + String.join(", ", MENU) + ".");
            }
            int combined = items.getOrDefault(name, 0) + quantity;
            if (combined > MAX_PER_ITEM) {
                throw quantityError();
            }
            items.put(name, combined);
            total += quantity;
        }
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Add at least one item.");
        }
        if (total > MAX_PER_ORDER) {
            throw new IllegalArgumentException("An order can hold at most 20 items.");
        }
        return items;
    }

    private static int parseQuantity(String text) {
        try {
            int quantity = Integer.parseInt(text);
            if (quantity < 1 || quantity > MAX_PER_ITEM) {
                throw quantityError();
            }
            return quantity;
        } catch (NumberFormatException e) {
            throw quantityError();
        }
    }

    private static IllegalArgumentException quantityError() {
        return new IllegalArgumentException("Quantity must be a whole number from 1 to 10.");
    }
}
