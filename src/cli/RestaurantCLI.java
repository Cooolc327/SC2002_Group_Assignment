package cli;

import config.GameConfig;
import contracts.ActionResult;
import contracts.ChefAction;
import contracts.WaiterAction;
import control.GameController;
import control.TurnPhase;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import view.GameSummary;
import view.GameView;
import view.GameView.CustomerView;
import view.GameView.MenuItemView;
import view.GameView.OrderView;
import view.GameView.TableView;

// Boundary class: collects the manager's choices and displays GameView snapshots; holds no game rules
public class RestaurantCLI {
    private final GameController controller;
    private final Scanner in;

    public RestaurantCLI(GameController controller, Scanner in) {
        this.controller = controller;
        this.in = in;
    }

    public void run() {
        GameView view = controller.getView();
        printIntro(view);
        while (view.phase() != TurnPhase.FINISHED) {
            view = controller.startTurn();
            System.out.println();
            System.out.println("================ Turn " + view.turn() + " / " + GameConfig.MAX_TURNS + " ================");
            printState(view);

            chooseWaiterAction(view);
            chooseChefAction(controller.getView());   // fresh view: the Chef may prepare an order taken this turn

            view = controller.finishTurn();
            System.out.println();
            System.out.println("Cashier (automatic): " + describePayment(view));
            System.out.println("End of turn " + view.turn() + ": " + describeSummary(view.summary()));
        }
        printResult(view);
    }

    //-------------------------------Manager choices-----------------------------------

    private void chooseWaiterAction(GameView view) {
        List<WaiterAction> options = view.staff().waiterOptions();
        System.out.println();
        System.out.println("Waiter tasks (one per turn):");
        WaiterAction action = pick(options, a -> describeWaiterAction(a, view));
        while (!report(controller.submitWaiterAction(action))) {
            action = pick(options, a -> describeWaiterAction(a, view));
        }
    }

    private void chooseChefAction(GameView view) {
        List<ChefAction> options = view.staff().chefOptions();
        System.out.println();
        System.out.println("Chef tasks (one preparation unit per turn):");
        if (options.size() == 1) {
            System.out.println("  (Chef has no orders to prepare)");
        }
        ChefAction action = pick(options, a -> describeChefAction(a, view));
        while (!report(controller.submitChefAction(action))) {
            action = pick(options, a -> describeChefAction(a, view));
        }
    }

    //Show the options as a numbered list and return the one the manager picks
    private <T> T pick(List<T> options, Function<T, String> describe) {
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  " + i + ") " + describe.apply(options.get(i)));
        }
        while (true) {
            int choice = readInt("Choice: ");
            if (choice >= 0 && choice < options.size()) {
                return options.get(choice);
            }
            System.out.println("  Invalid: choose a number from 0 to " + (options.size() - 1) + ".");
        }
    }

    private boolean report(ActionResult r) {
        System.out.println((r.accepted() ? "  OK: " : "  Invalid: ") + r.message());
        return r.accepted();
    }

    //-------------------------------Display-----------------------------------

    private void printIntro(GameView view) {
        System.out.println("=== Restaurant Rush (Stage 1) ===");
        System.out.println("You are the manager. Each turn: customers arrive -> you choose the Waiter task -> you choose");
        System.out.println("the Chef task -> served customers eat -> the Cashier collects one payment automatically ->");
        System.out.println("everyone not yet served loses satisfaction.");
        System.out.println("Goal after turn " + GameConfig.MAX_TURNS + ": at least " + GameConfig.MIN_PAID_CUSTOMERS
                + " paid customers, revenue at least " + money(GameConfig.MIN_REVENUE_CENTS)
                + ", average satisfaction at least " + GameConfig.MIN_AVG_SATISFACTION + ".");
        System.out.println();
        System.out.println("Menu (customers choose their own food):");
        for (MenuItemView item : view.menu()) {
            System.out.println("  " + item.name() + "  " + money(item.priceCents())
                    + "  " + item.preparationUnits() + " prep unit(s)");
        }
    }

    private void printState(GameView view) {
        System.out.println(describeSummary(view.summary()));
        System.out.println("Waiting queue: " + (view.waitingCustomerIds().isEmpty() ? "(empty)"
                : view.waitingCustomerIds().stream().map(id -> "#" + id).toList()));
        for (TableView t : view.tables()) {
            System.out.println("Table " + t.id() + ": " + (t.customerId() == null ? "free" : "customer #" + t.customerId()));
        }
        System.out.println("Customers:");
        if (view.customers().isEmpty()) {
            System.out.println("  (none yet)");
        }
        for (CustomerView c : view.customers()) {
            System.out.println("  #" + c.id() + " " + c.type() + "  satisfaction " + c.satisfaction()
                    + "  " + describeCustomerState(c));
        }
        System.out.println("Orders:");
        if (view.orders().isEmpty()) {
            System.out.println("  (none yet)");
        }
        for (OrderView o : view.orders()) {
            System.out.println("  Order " + o.id() + " (customer #" + o.customerId() + ") " + o.itemName()
                    + " " + money(o.billCents()) + "  " + o.preparedUnits() + "/" + o.preparationUnits()
                    + " units  " + o.status());
        }
    }

    private void printResult(GameView view) {
        GameSummary s = view.summary();
        System.out.println();
        System.out.println("================ " + view.result() + " ================");
        System.out.println(describeSummary(s));
        System.out.println("  Paid customers " + s.paidCount() + " (need " + GameConfig.MIN_PAID_CUSTOMERS + ")");
        System.out.println("  Revenue " + money(s.revenueCents()) + " (need " + money(GameConfig.MIN_REVENUE_CENTS) + ")");
        System.out.println("  Avg satisfaction " + formatAverage(s) + " (need " + GameConfig.MIN_AVG_SATISFACTION + ")");
    }

    private String describeCustomerState(CustomerView c) {
        switch (c.state()) {
            case WAITING:      return "WAITING (in queue for a table)";
            case SEATED:       return "SEATED (waiting to order)";
            case ORDERED:      return "ORDERED (waiting for food)";
            case SERVED:       return "SERVED (eating, ready to pay next turn)";
            case READY_TO_PAY: return "READY_TO_PAY (Cashier collects automatically)";
            case PAID:         return "PAID";
            case LEFT:         return "LEFT (unhappy, did not pay)";
            default:           return c.state().toString();
        }
    }

    private String describeWaiterAction(WaiterAction a, GameView view) {
        switch (a.type()) {
            case SEAT:       return "Seat customer #" + a.customerId() + " at Table " + a.tableId();
            case TAKE_ORDER: return "Take order from customer #" + a.customerId();
            case SERVE: {
                OrderView o = findOrder(view, a.orderId());
                return "Serve order " + a.orderId() + (o == null ? "" : " (" + o.itemName() + ") to customer #" + o.customerId());
            }
            default:         return "Wait";
        }
    }

    private String describeChefAction(ChefAction a, GameView view) {
        if (a.type() == ChefAction.Type.WAIT) {
            return "Wait";
        }
        OrderView o = findOrder(view, a.orderId());
        return "Prepare order " + a.orderId() + (o == null ? "" : " (" + o.itemName() + " for customer #"
                + o.customerId() + ", " + o.preparedUnits() + "/" + o.preparationUnits() + " units done)");
    }

    //Describe the payment collected this turn, if any
    private String describePayment(GameView view) {
        for (OrderView o : view.orders()) {
            if (o.paidTurn() != null && o.paidTurn() == view.turn()) {
                return "Collected " + money(o.billCents()) + " from customer #" + o.customerId()
                        + " (satisfaction " + o.satisfactionAtPayment() + " recorded), table freed";
            }
        }
        return "No customer was ready to pay this turn";
    }

    private String describeSummary(GameSummary s) {
        return "Dishes served " + s.servedCount() + " | Paid " + s.paidCount()
                + " | Unhappy departures " + s.unhappyDepartures()
                + " | Revenue " + money(s.revenueCents())
                + " | Avg satisfaction " + formatAverage(s)
                + " | Turns used " + s.turnsUsed() + "/" + GameConfig.MAX_TURNS;
    }

    private static String formatAverage(GameSummary s) {
        return s.averagePaidSatisfaction().isPresent()
                ? String.format("%.2f", s.averagePaidSatisfaction().getAsDouble()) : "N/A";
    }

    private static OrderView findOrder(GameView view, int orderId) {
        return view.orders().stream().filter(o -> o.id() == orderId).findFirst().orElse(null);
    }

    private static String money(long cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }

    //-------------------------------Input-----------------------------------

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
