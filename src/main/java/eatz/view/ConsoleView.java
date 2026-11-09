package eatz.view;

import eatz.model.OrderInfo;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Prints menus, prompts, the line and the simulated texts, and reads raw lines.
 * It makes no decisions. Its only model type is the read-only OrderInfo.
 * Everything is printed with println/print of plain strings, never as a format string.
 */
public class ConsoleView {
    private final Scanner in;
    private final PrintStream out;

    /**
     * @param in  where typed lines come from
     * @param out where output goes
     */
    public ConsoleView(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    /** Prints the main menu. */
    public void showMenu() {
        out.println();
        out.println("=== EATZ ===");
        out.println("1) Add order");
        out.println("2) Mark ready");
        out.println("3) Cancel order");
        out.println("4) View line");
        out.println("5) Remake order");
    }

    /**
     * Prints a prompt and reads one line.
     *
     * @param prompt the text to show
     * @return the raw line
     * @throws NoSuchElementException at end of input
     */
    public String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        return in.nextLine();
    }

    /**
     * Asks a yes/no question until the answer is y or n.
     *
     * @param prompt the question
     * @return true for y
     */
    public boolean confirm(String prompt) {
        while (true) {
            String answer = readLine(prompt + " ").trim().toLowerCase();
            if (answer.equals("y")) {
                return true;
            }
            if (answer.equals("n")) {
                return false;
            }
            out.println("Please type y or n.");
        }
    }

    /** Prints an error message it was given. */
    public void showError(String message) {
        out.println("Error: " + message);
    }

    /** Prints a plain message. */
    public void showMessage(String message) {
        out.println(message);
    }

    /** Prints the whole line, front first. */
    public void showLine(List<OrderInfo> line) {
        if (line.isEmpty()) {
            out.println("The line is empty.");
            return;
        }
        out.println("--- The line ---");
        for (OrderInfo info : line) {
            out.println("#" + info.position() + "  order " + info.id() + "  " + info.name()
                    + "  " + formatPhone(info.phone()) + "  " + formatItems(info.items())
                    + "  (about " + info.waitMinutes() + " min)");
        }
    }

    /** Prints the simulated text telling a customer their place in line. */
    public void showPositionText(OrderInfo info) {
        out.println("Order " + info.id() + " for " + info.name() + ".");
        out.println("[TEXT to " + formatPhone(info.phone()) + "] You're #" + info.position()
                + ", about " + info.waitMinutes() + " min away.");
    }

    /** Prints the simulated text telling a customer their order is ready. */
    public void showReadyText(OrderInfo info) {
        out.println("[TEXT to " + formatPhone(info.phone()) + "] " + info.name()
                + ", your order " + info.id() + " is ready!");
    }

    /** Prints the simulated text telling a customer their order was cancelled. */
    public void showCancelText(OrderInfo info) {
        out.println("[TEXT to " + formatPhone(info.phone()) + "] " + info.name()
                + ", your order " + info.id() + " was cancelled.");
    }

    private String formatPhone(String digits) {
        if (digits.length() != 10) {
            return digits;
        }
        return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
    }

    private String formatItems(Map<String, Integer> items) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(entry.getValue()).append(" ").append(entry.getKey());
        }
        return sb.toString();
    }
}
