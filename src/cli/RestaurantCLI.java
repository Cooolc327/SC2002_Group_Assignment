import java.util.Scanner;

public class RestaurantCLI {
    private final GameController controller;
    private final Scanner in;

    public RestaurantCLI(GameController controller, Scanner in) {
        this.controller = controller;
        this.in = in;
    }

    public void run() {
        System.out.println("=== Restaurant Rush (Stage 1) ===");
        while (!controller.isGameOver()) {
            controller.startTurn();
            System.out.println();
            System.out.println("------------ Turn " + controller.getCurrentTurn()
                    + " / " + GameConfig.MAX_TURNS + " ------------");
            printEvents();
            System.out.println(controller.describeState());
            chooseWaiterAction();
            chooseChefAction();
            controller.finishTurn();
            printEvents();
            System.out.println("Results: " + controller.describeSummary());
        }
        System.out.println();
        System.out.println("*** " + controller.getOutcome() + " ***");
    }

    private void chooseWaiterAction() {
        while (true) {
            System.out.println("Waiter: 1) Seat customer  2) Take order  3) Serve order  0) Skip");
            int choice = readInt("Choice: ");
            WaiterAction action;
            switch (choice) {
                case 1: {
                    int customerId = readInt("  Customer id: ");
                    int tableId = readInt("  Table id: ");
                    action = WaiterAction.seat(customerId, tableId);
                    break;
                }
                case 2: action = WaiterAction.takeOrder(readInt("  Customer id: ")); break;
                case 3: action = WaiterAction.serve(readInt("  Order id: ")); break;
                case 0: action = WaiterAction.skip(); break;
                default:
                    System.out.println("  Invalid: choose 0, 1, 2 or 3.");
                    continue;
            }
            ActionResult r = controller.submitWaiterAction(action);
            System.out.println((r.isSuccess() ? "  OK: " : "  Invalid: ") + r.getMessage());
            if (r.isSuccess()) return;
        }
    }

    private void chooseChefAction() {
        while (true) {
            System.out.println("Chef: 1) Prepare order  0) Skip");
            int choice = readInt("Choice: ");
            ChefAction action;
            if (choice == 1)      action = ChefAction.prepare(readInt("  Order id: "));
            else if (choice == 0) action = ChefAction.skip();
            else { System.out.println("  Invalid: choose 0 or 1."); continue; }
            ActionResult r = controller.submitChefAction(action);
            System.out.println((r.isSuccess() ? "  OK: " : "  Invalid: ") + r.getMessage());
            if (r.isSuccess()) return;
        }
    }

    private void printEvents() {
        for (String e : controller.takeEvents()) {
            System.out.println("* " + e);
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!in.hasNextLine()) {
                System.out.println("\nInput ended. Exiting game.");
                System.exit(0);
            }
            String line = in.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  Invalid: please enter a whole number.");
            }
        }
    }
}