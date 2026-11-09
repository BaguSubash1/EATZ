package eatz;

import eatz.model.OrderInfo;
import eatz.model.OrderLine;
import eatz.model.OrderValidator;

/**
 * A tiny driver that exercises the model with no view and no controller.
 * (This class prints; the model classes never do.)
 */
public class ModelDriver {

    public static void main(String[] args) {
        OrderLine line = new OrderLine();

        System.out.println("Add three orders:");
        line.addOrder("Ana", "(555) 555-0142", OrderValidator.parseItems("2 taco, churro"));
        line.addOrder("Ben", "555-555-0199", OrderValidator.parseItems("burrito"));
        line.addOrder("Cy", "555.555.0123", OrderValidator.parseItems("3 horchata"));
        print(line);

        System.out.println("Cancel order 2:");
        line.cancel(2);
        print(line);

        System.out.println("Mark ready (order 1), then remake it:");
        line.markReady();
        print(line);
        line.remake(1);
        print(line);

        System.out.println("Bad input is rejected and the line is unchanged:");
        try {
            line.cancel(99);
        } catch (IllegalArgumentException e) {
            System.out.println("  cancel(99): " + e.getMessage());
        }
        try {
            OrderValidator.parseItems("pizza");
        } catch (IllegalArgumentException e) {
            System.out.println("  parseItems(pizza): " + e.getMessage());
        }
        System.out.println("  size is still " + line.size());
    }

    private static void print(OrderLine line) {
        for (OrderInfo info : line.getLine()) {
            System.out.println("  #" + info.position() + " order " + info.id() + " " + info.name()
                    + " " + info.items() + " wait " + info.waitMinutes() + " min");
        }
        if (line.isEmpty()) {
            System.out.println("  (empty)");
        }
    }
}
