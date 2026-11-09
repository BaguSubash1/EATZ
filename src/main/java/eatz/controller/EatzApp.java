package eatz.controller;

import eatz.model.OrderLine;
import eatz.view.ConsoleView;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/** Startup only: builds the three objects, connects them, and starts run(). */
public class EatzApp {

    public static void main(String[] args) {
        OrderLine model = new OrderLine();
        ConsoleView view = new ConsoleView(new Scanner(System.in), System.out);
        EatzController controller = new EatzController(model, view);

        // Ctrl+C prints Goodbye. (run() already prints it on a normal exit)
        AtomicBoolean finished = new AtomicBoolean(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!finished.get()) {
                System.out.println();
                System.out.println("Goodbye.");
            }
        }));

        controller.run();
        finished.set(true);
    }
}
