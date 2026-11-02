package integration;

import contracts.ActionResult;
import contracts.ChefAction;
import contracts.WaiterAction;
import control.GameController;
import control.TurnPhase;
import domain.Restaurant;
import entities.CustomerState;
import entities.RegularCustomer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import orders.Order;
import staff.Cashier;
import staff.Chef;
import staff.StaffTeam;
import staff.Waiter;
import view.GameView;

/** A's development smoke checks; E performs and records the independent acceptance. */
public final class Stage1SmokeTest {
    private static int scenarios;
    private static int assertions;

    private Stage1SmokeTest() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: Stage1SmokeTest <replay-directory> <log-directory>");
        }
        Path replays = Path.of(args[0]);
        Path logs = Path.of(args[1]);
        Files.createDirectories(logs);
        singleCustomerTiming();
        pass("real Order and StaffTeam timing, retries and table release");
        staffAllowances();
        pass("production staff enforce per-turn allowances");
        String victory = Files.readString(replays.resolve("stage1-victory.txt"));
        String defeat = Files.readString(replays.resolve("stage1-defeat.txt"));
        cliReplay("victory", victory, "VICTORY",
                "Dishes served 5 | Paid 5 | Unhappy departures 0 | Revenue $66.00"
                + " | Avg satisfaction 79.20 | Turns used 18/18", 0, logs);
        cliReplay("defeat", defeat, "DEFEAT",
                "Dishes served 0 | Paid 0 | Unhappy departures 2 | Revenue $0.00"
                + " | Avg satisfaction N/A | Turns used 18/18", 0, logs);
        cliReplay("invalid-input-victory", "not-a-number\n999\n-1\n" + victory, "VICTORY",
                "Dishes served 5 | Paid 5 | Unhappy departures 0 | Revenue $66.00"
                + " | Avg satisfaction 79.20 | Turns used 18/18", 3, logs);
        System.out.println("STAGE1_SMOKE=PASS scenarios=" + scenarios + " assertions=" + assertions);
    }

    private static void singleCustomerTiming() {
        Restaurant restaurant = new Restaurant(Order::new);
        GameController game = new GameController(restaurant, new StaffTeam());
        game.startTurn();
        GameView before = game.getView();
        check(!game.submitWaiterAction(WaiterAction.seat(99, 1)).accepted(), "Reject invalid seating");
        check(game.getView().equals(before), "Invalid seating does not mutate the view");
        accepted(game.submitWaiterAction(WaiterAction.seat(1, 1)));
        accepted(game.submitChefAction(ChefAction.waitTurn()));
        check(customer(game.finishTurn(), 1).satisfaction() == 92, "First waiting penalty");

        game.startTurn();
        accepted(game.submitWaiterAction(WaiterAction.takeOrder(1)));
        check(!game.submitChefAction(ChefAction.prepare(99)).accepted(), "Chef may retry invalid order");
        check(game.getView().phase() == TurnPhase.AWAIT_CHEF, "Invalid preparation retains phase");
        accepted(game.submitChefAction(ChefAction.prepare(1)));
        check(game.getView().orders().get(0).readyTurn() == 2, "New order prepared this turn");
        check(!restaurant.serveOrder(1, 2).accepted(), "No same-turn serving");
        check(customer(game.finishTurn(), 1).satisfaction() == 84, "Second waiting penalty");

        game.startTurn();
        accepted(game.submitWaiterAction(WaiterAction.serve(1)));
        accepted(game.submitChefAction(ChefAction.waitTurn()));
        GameView third = game.finishTurn();
        check(customer(third, 1).state() == CustomerState.SERVED, "Not eaten immediately");
        check(customer(third, 1).satisfaction() == 84, "Served customer loses no satisfaction");

        game.startTurn();
        accepted(game.submitWaiterAction(WaiterAction.waitTurn()));
        accepted(game.submitChefAction(ChefAction.waitTurn()));
        GameView fourth = game.finishTurn();
        check(customer(fourth, 1).state() == CustomerState.READY_TO_PAY, "Eating finishes next turn");
        check(fourth.summary().paidCount() == 0, "No payment on ready-to-pay turn");

        game.startTurn();
        check(!game.submitWaiterAction(WaiterAction.seat(2, 1)).accepted(),
                "Table remains occupied until the later payment step");
        accepted(game.submitWaiterAction(WaiterAction.seat(2, 2)));
        accepted(game.submitChefAction(ChefAction.waitTurn()));
        GameView fifth = game.finishTurn();
        check(fifth.summary().paidCount() == 1 && fifth.summary().revenueCents() == 900,
                "Actual Cashier collects Salad bill on turn five");
        check(fifth.orders().get(0).satisfactionAtPayment() == 84, "Payment snapshot");
        check(fifth.tables().get(0).customerId() == null, "Payment releases table");
    }

    private static void staffAllowances() {
        // Domain fixture: the controller's normal arrival schedule is checked by the core suite.
        Restaurant restaurant = new Restaurant(Order::new);
        restaurant.beginTurn(1);
        restaurant.registerArrival(new RegularCustomer(1));
        restaurant.registerArrival(new RegularCustomer(2));
        Waiter waiter = new Waiter();
        check(!waiter.execute(WaiterAction.seat(99, 1), restaurant, 1).accepted(), "Invalid waiter task");
        check(waiter.getLastActionTurn() == 0, "Invalid task consumes no waiter allowance");
        accepted(waiter.execute(WaiterAction.seat(1, 1), restaurant, 1));
        check(!waiter.execute(WaiterAction.seat(2, 2), restaurant, 1).accepted(), "One waiter task");
        accepted(restaurant.placeOrder(1));
        Chef chef = new Chef();
        check(!chef.execute(ChefAction.prepare(99), restaurant, 1).accepted(), "Invalid chef task");
        check(chef.getLastActionTurn() == 0, "Invalid task consumes no chef allowance");
        accepted(chef.execute(ChefAction.prepare(1), restaurant, 1));
        check(!chef.execute(ChefAction.waitTurn(), restaurant, 1).accepted(), "One chef task");
        closeDomain(restaurant, 1);

        Cashier cashier = new Cashier();
        restaurant.beginTurn(2);
        accepted(restaurant.serveOrder(1, 2));
        accepted(cashier.processPayment(restaurant, 2));
        check(restaurant.getSummary().paidCount() == 0, "No payment when served");
        closeDomain(restaurant, 2);
        restaurant.beginTurn(3);
        restaurant.advanceEating(3);
        accepted(cashier.processPayment(restaurant, 3));
        check(restaurant.getSummary().paidCount() == 0, "No same-turn ready-to-pay collection");
        closeDomain(restaurant, 3);
        restaurant.beginTurn(4);
        accepted(cashier.processPayment(restaurant, 4));
        check(cashier.getLastActionTurn() == 4, "Cashier records successful turn");
        check(!cashier.processPayment(restaurant, 4).accepted(), "One cashier payment");
        check(restaurant.getSummary().paidCount() == 1, "No duplicate payment");
        closeDomain(restaurant, 4);
    }

    private static void closeDomain(Restaurant restaurant, int turn) {
        restaurant.advanceEating(turn);
        restaurant.applyWaitingPenalty(turn);
        restaurant.completeTurn(turn);
    }

    private static void cliReplay(String name, String input, String result, String summary,
                                  int invalidMessages, Path logs) throws Exception {
        Path log = logs.resolve("cli-" + name + ".log");
        Path java = Path.of(System.getProperty("java.home"), "bin", "java");
        Process process = new ProcessBuilder(java.toString(), "-cp",
                System.getProperty("java.class.path"), "Main")
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try (var stdin = process.getOutputStream()) {
            stdin.write(input.getBytes(StandardCharsets.UTF_8));
        }
        if (!process.waitFor(20, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor(5, TimeUnit.SECONDS);
            throw new AssertionError("CLI replay timed out: " + log);
        }
        String output = Files.readString(log);
        check(process.exitValue() == 0, "CLI exits successfully: " + log);
        check(output.lines().filter(line -> line.startsWith("================ Turn ")).count() == 18,
                "CLI runs all 18 turns: " + log);
        String heading = "================ " + result + " ================";
        int finalResult = output.lastIndexOf(heading);
        check(finalResult >= 0, "CLI expected result: " + log);
        check(output.substring(finalResult).contains(summary), "CLI final statistics: " + log);
        check(output.lines().filter(line -> line.startsWith("================ VICTORY")
                || line.startsWith("================ DEFEAT")).count() == 1, "One final result: " + log);
        check(count(output, "Invalid:") == invalidMessages, "Expected input retries: " + log);
        pass("real Main/CLI " + name + " (log " + log.getFileName() + ")");
    }

    private static long count(String text, String fragment) {
        long count = 0;
        int position = 0;
        while ((position = text.indexOf(fragment, position)) >= 0) {
            count++;
            position += fragment.length();
        }
        return count;
    }

    private static GameView.CustomerView customer(GameView view, int id) {
        return view.customers().stream().filter(customer -> customer.id() == id)
                .findFirst().orElseThrow();
    }

    private static void accepted(ActionResult result) {
        check(result.accepted(), result.message());
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }

    private static void pass(String scenario) {
        scenarios++;
        System.out.println("PASS " + scenario);
    }
}
