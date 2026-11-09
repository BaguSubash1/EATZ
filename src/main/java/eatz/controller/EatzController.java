package eatz.controller;

import eatz.model.OrderInfo;
import eatz.model.OrderLine;
import eatz.model.OrderValidator;
import eatz.view.ConsoleView;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Runs the menu loop, passes typed text to the model, hands the answers to the view,
 * and turns model errors into messages. The only class that touches both sides.
 */
public class EatzController {
    private final OrderLine model;
    private final ConsoleView view;

    /**
     * @param model the line
     * @param view  the console
     */
    public EatzController(OrderLine model, ConsoleView view) {
        if (model == null || view == null) {
            throw new IllegalArgumentException("Model and view cannot be null.");
        }
        this.model = model;
        this.view = view;
    }

    /** Runs the menu loop until end of input. */
    public void run() {
        try {
            while (true) {
                view.showMenu();
                int choice;
                try {
                    choice = parseMenuChoice(view.readLine("Choose 1-5: "));
                } catch (IllegalArgumentException e) {
                    view.showError(e.getMessage());
                    continue;
                }
                switch (choice) {
                    case 1 -> handleAdd();
                    case 2 -> handleReady();
                    case 3 -> handleCancel();
                    case 4 -> handleViewLine();
                    default -> handleRemake();
                }
            }
        } catch (NoSuchElementException e) {
            // end of input (Ctrl+D)
            view.showMessage("Goodbye.");
        }
    }

    int parseMenuChoice(String raw) {
        try {
            int choice = Integer.parseInt(raw.trim());
            if (choice >= 1 && choice <= 5) {
                return choice;
            }
        } catch (NumberFormatException e) {
            // fall through to the message below
        }
        throw new IllegalArgumentException("Please choose a number from 1 to 5.");
    }

    int parseId(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Order ID must be a whole number.");
        }
    }

    private <T> T promptUntilValid(String prompt, Function<String, T> parser) {
        while (true) {
            try {
                return parser.apply(view.readLine(prompt));
            } catch (IllegalArgumentException e) {
                view.showError(e.getMessage());
            }
        }
    }

    private void handleAdd() {
        String name = promptUntilValid("Customer name: ", OrderValidator::cleanName);
        String phone = promptUntilValid("Phone: ", OrderValidator::cleanPhone);
        Map<String, Integer> items = promptUntilValid(
                "Items (e.g. 2 taco, churro): ", OrderValidator::parseItems);
        try {
            if (model.isDuplicateOfLast(name, phone, items)
                    && !view.confirm("Duplicate of the last order. Add anyway? (y/n)")) {
                view.showMessage("Order not added.");
                return;
            }
            OrderInfo info = model.addOrder(name, phone, items);
            view.showPositionText(info);
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }

    private void handleReady() {
        try {
            view.showReadyText(model.markReady());
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }

    private void handleCancel() {
        int id = promptUntilValid("Order ID to cancel: ", this::parseId);
        try {
            view.showCancelText(model.cancel(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }

    private void handleViewLine() {
        view.showLine(model.getLine());
    }

    private void handleRemake() {
        int id = promptUntilValid("Order ID to remake: ", this::parseId);
        try {
            view.showPositionText(model.remake(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }
}
