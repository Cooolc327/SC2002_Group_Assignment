package integration;

import contracts.ActionResult;
import contracts.ChefAction;
import contracts.OrderPort;
import contracts.WaiterAction;
import control.GameController;
import control.GameResult;
import control.TurnPhase;
import domain.Restaurant;
import entities.CriticCustomer;
import entities.Customer;
import entities.CustomerFactory;
import entities.CustomerState;
import entities.RegularCustomer;
import entities.VipCustomer;
import fooditems.Menu;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import orders.BillCalculator;
import orders.Order;
import staff.StaffTeam;
import view.GameView;

/** Stage 2 checks using production customers, orders, staff and CLI. */
public final class Stage2IntegrationTest {
    private static int scenarios;
    private static int assertions;

    private Stage2IntegrationTest() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: Stage2IntegrationTest <replay-directory> <log-directory>");
        }
        Path replays = Path.of(args[0]);
        Path logs = Path.of(args[1]);
        Files.createDirectories(logs);
        factorySchedule();
        pass("seeded six-customer schedule, positive IDs and invalid ID rejection");
        customerBehaviour();
        pass("polymorphic waiting losses, service immunity and Critic preference");
        arrivalSchedule();
        pass("actual mixed arrivals and exact all-WAIT state after 18 turns");
        factoryFailure();
        pass("invalid arrival factories leave the turn unchanged and permit retry");
        String victory = Files.readString(replays.resolve("stage2-victory.txt"));
        String defeat = Files.readString(replays.resolve("stage2-defeat.txt"));
        mixedVictory(victory, logs);
        pass("real mixed-customer victory, VIP payment and final unfinished Critic order");
        criticTiming();
        pass("Critic chooses Pasta and follows READY, SERVED, eating and payment timing");
        for (int units = 0; units <= 2; units++) {
            criticAbandonment(units);
            pass("real Critic abandonment cancels an order with " + units + " preparation units");
        }
        cliReplay("victory", victory, true, 0, logs);
        cliReplay("defeat", defeat, false, 0, logs);
        cliReplay("waiter-retry-victory", "text\n999\n-1\n" + victory, true, 3, logs);
        String[] choices = victory.split("\\R");
        String chefRetry = String.join("\n", choices[0], choices[1], choices[2])
                + "\ntext\n\n2147483648\n999\n-1\n"
                + String.join("\n", java.util.Arrays.copyOfRange(choices, 3, choices.length)) + "\n";
        cliReplay("chef-retry-victory", chefRetry, true, 5, logs);
        cliReplay("bom-victory", "\uFEFF" + victory, true, 0, logs);
        System.out.println("STAGE2_INTEGRATION=PASS scenarios=" + scenarios + " assertions=" + assertions);
    }

    private static void factorySchedule() {
        CustomerFactory factory = new CustomerFactory();
        eq(24369L, factory.getSeed(), "documented default seed");
        List<String> expected = List.of("Regular", "VIP", "Regular", "Regular", "Regular", "Critic");
        for (int id = 1; id <= 6; id++) {
            Customer first = factory.createCustomer(id);
            Customer again = factory.createCustomer(id);
            eq(id, first.getId(), "positive arrival-order ID");
            eq(expected.get(id - 1), first.getTypeName(), "default mixed schedule");
            check(first != again, "each creation returns a fresh customer");
            eq(first.getTypeName(), again.getTypeName(), "schedule lookup does not consume entries");
        }
        for (long seed : new long[] {0, 1, 42, 24369, -7, Long.MAX_VALUE}) {
            CustomerFactory first = new CustomerFactory(seed);
            CustomerFactory second = new CustomerFactory(seed);
            List<String> types = new ArrayList<>();
            for (int id = 1; id <= 6; id++) {
                types.add(first.createCustomer(id).getTypeName());
                eq(types.get(id - 1), second.createCustomer(id).getTypeName(), "same seed reproduces types");
            }
            eq(4L, types.stream().filter("Regular"::equals).count(), "four Regular customers");
            eq(1L, types.stream().filter("VIP"::equals).count(), "one VIP");
            eq(1L, types.stream().filter("Critic"::equals).count(), "one Critic");
        }
        for (int id : new int[] {-1, 0, 7, 16}) {
            throwsType(IllegalArgumentException.class, () -> factory.createCustomer(id));
        }
    }

    private static void customerBehaviour() {
        Menu menu = new Menu();
        List<Customer> customers = List.of(new RegularCustomer(1), new VipCustomer(1), new CriticCustomer(1));
        int[] losses = {8, 5, 12};
        int[] bills = {900, 810, 1800};
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            eq(bills[i], BillCalculator.billFor(customer, customer.chooseItem(menu)), "polymorphic choice and bill");
            customer.applyWaitingPenalty();
            eq(100 - losses[i], customer.getSatisfaction(), "WAITING loss");
            customer.seat();
            customer.applyWaitingPenalty();
            eq(100 - 2 * losses[i], customer.getSatisfaction(), "SEATED loss");
            customer.placeOrder();
            customer.applyWaitingPenalty();
            eq(100 - 3 * losses[i], customer.getSatisfaction(), "ORDERED loss");
            customer.serve(4);
            customer.applyWaitingPenalty();
            eq(100 - 3 * losses[i], customer.getSatisfaction(), "SERVED immunity");
            check(!customer.finishedEating(4), "cannot finish eating on service turn");
            check(customer.finishedEating(5), "can finish eating on later turn");
            customer.applyWaitingPenalty();
            eq(100 - 3 * losses[i], customer.getSatisfaction(), "READY_TO_PAY immunity");
            customer.pay();
            customer.applyWaitingPenalty();
            eq(100 - 3 * losses[i], customer.getSatisfaction(), "PAID immunity");
        }
        for (Customer customer : List.of(new RegularCustomer(1), new VipCustomer(1), new CriticCustomer(1))) {
            for (int turn = 0; turn < 30; turn++) {
                customer.applyWaitingPenalty();
            }
            eq(0, customer.getSatisfaction(), "loss clamps at zero");
            check(customer.isOutOfPatience(), "zero patience while waiting");
        }
        eq("Pasta", new CriticCustomer(1).chooseItem(menu).getName(), "Critic differs from C1 Regular Salad");
        eq(0, new CriticCustomer(1).getBillDiscountPercent(), "Critic pays full price");
        eq(10, new VipCustomer(1).getBillDiscountPercent(), "actual VIP supplies discount");
    }

    private static void arrivalSchedule() {
        List<Integer> requestedIds = new ArrayList<>();
        CustomerFactory factory = new CustomerFactory();
        GameController game = game(id -> {
            requestedIds.add(id);
            return factory.createCustomer(id);
        });
        int arrivals = 0;
        for (int turn = 1; turn <= 18; turn++) {
            GameView start = game.startTurn();
            if (List.of(1, 4, 7, 10, 13, 16).contains(turn)) {
                arrivals++;
                eq(100, customer(start, arrivals).satisfaction(), "new mixed arrival starts at 100");
            }
            eq(arrivals, start.customers().size(), "only six scheduled arrivals");
            accepted(game.submitWaiterAction(WaiterAction.waitTurn()));
            accepted(game.submitChefAction(ChefAction.waitTurn()));
            game.finishTurn();
        }
        eq(List.of(1, 2, 3, 4, 5, 6), requestedIds, "factory receives IDs, never turns or zero-based slots");
        GameView end = game.getView();
        int[] satisfaction = {0, 25, 4, 28, 52, 64};
        for (int id = 1; id <= 6; id++) {
            eq(satisfaction[id - 1], customer(end, id).satisfaction(), "type-specific waiting duration");
        }
        eq(1, end.summary().unhappyDepartures(), "only first Regular abandons");
        eq(0L, end.summary().revenueCents(), "no unpaid revenue");
        check(end.summary().averagePaidSatisfaction().isEmpty(), "no payments means N/A");
        eq(GameResult.DEFEAT, end.result(), "mixed all-WAIT defeat");
        eq(TurnPhase.FINISHED, end.phase(), "always finish turn 18");
    }

    private static void factoryFailure() {
        List<IntFunction<? extends Customer>> invalid = List.of(
                id -> null, id -> new RegularCustomer(id + 1), id -> {
                    Customer customer = new RegularCustomer(id);
                    customer.seat();
                    return customer;
                });
        for (IntFunction<? extends Customer> factory : invalid) {
            GameController game = game(factory);
            GameView before = game.getView();
            throwsType(RuntimeException.class, game::startTurn);
            eq(before, game.getView(), "factory rejection does not partially start a turn");
        }
        AtomicInteger calls = new AtomicInteger();
        GameController retry = game(id -> new RegularCustomer(calls.getAndIncrement() == 0 ? id + 1 : id));
        throwsType(IllegalArgumentException.class, retry::startTurn);
        eq(1, retry.startTurn().turn(), "retry starts original turn");
        eq(1, retry.getView().customers().get(0).id(), "retry retains first customer ID");
    }

    private static void mixedVictory(String replay, Path logs) throws Exception {
        List<GameView> turns = play(new CustomerFactory()::createCustomer);
        GameView end = turns.get(17);
        eq(GameResult.VICTORY, end.result(), "mixed victory");
        eq(5, end.summary().paidCount(), "five customers paid");
        eq(6450L, end.summary().revenueCents(), "$66 less VIP Burger discount $1.50");
        eq(80.4, end.summary().averagePaidSatisfaction().orElseThrow(), "(84+90+76+76+76)/5");
        eq(0, end.summary().unhappyDepartures(), "no abandonment in victory");
        eq(CustomerState.SERVED, customer(turns.get(5), 2).state(), "VIP served turn 6");
        eq(CustomerState.READY_TO_PAY, customer(turns.get(6), 2).state(), "VIP eating completes turn 7");
        eq(CustomerState.PAID, customer(turns.get(7), 2).state(), "VIP pays turn 8");
        eq(1350, turns.get(7).orders().get(1).billCents(), "VIP Burger is $13.50");
        eq(90, turns.get(7).orders().get(1).satisfactionAtPayment(), "VIP payment snapshot");
        eq(CustomerState.ORDERED, customer(end, 6).state(), "last Critic cannot pay by turn 18");
        eq(1, end.orders().get(5).preparedUnits(), "last Pasta has only one preparation unit");
        eq(OrderPort.Status.PREPARING, end.orders().get(5).status(), "last bill is unpaid");
        eq(36L, replay.lines().count(), "fixed victory fixture has two choices per turn");
        StringBuilder trace = new StringBuilder("turn\tpaid\trevenue_cents\tcustomers\torders\n");
        for (GameView view : turns) {
            trace.append(view.turn()).append('\t').append(view.summary().paidCount()).append('\t')
                    .append(view.summary().revenueCents()).append('\t').append(view.customers())
                    .append('\t').append(view.orders()).append('\n');
        }
        Files.writeString(logs.resolve("controller-victory-timeline.tsv"), trace);
    }

    private static void criticTiming() {
        List<GameView> turns = play(id -> id == 1 ? new CriticCustomer(id) : new RegularCustomer(id));
        eq("Pasta", turns.get(1).orders().get(0).itemName(), "real Critic changes the first order");
        eq(OrderPort.Status.PREPARING, turns.get(1).orders().get(0).status(), "one unit is insufficient");
        eq(OrderPort.Status.READY, turns.get(2).orders().get(0).status(), "READY after second unit");
        eq(CustomerState.ORDERED, customer(turns.get(2), 1).state(), "READY is not SERVED");
        eq(CustomerState.SERVED, customer(turns.get(3), 1).state(), "service only on later turn");
        eq(CustomerState.READY_TO_PAY, customer(turns.get(4), 1).state(), "one full turn of eating");
        eq(CustomerState.PAID, customer(turns.get(5), 1).state(), "pay only on later turn");
        eq(1800, turns.get(5).orders().get(0).billCents(), "Critic pays full Pasta price");
        eq(64, turns.get(5).orders().get(0).satisfactionAtPayment(), "three waits at 12 points");
    }

    private static void criticAbandonment(int units) {
        GameController game = game(id -> id == 1 ? new CriticCustomer(id) : new RegularCustomer(id));
        for (int turn = 1; turn <= 18; turn++) {
            game.startTurn();
            WaiterAction waiter = turn == 1 ? WaiterAction.seat(1, 1)
                    : turn == 2 ? WaiterAction.takeOrder(1) : WaiterAction.waitTurn();
            accepted(game.submitWaiterAction(waiter));
            accepted(game.submitChefAction(turn >= 2 && turn < 2 + units
                    ? ChefAction.prepare(1) : ChefAction.waitTurn()));
            GameView end = game.finishTurn();
            if (turn == 9) {
                eq(CustomerState.LEFT, customer(end, 1).state(), "Critic abandons at ninth waiting penalty");
                eq(0, customer(end, 1).satisfaction(), "abandonment clamps to zero");
                eq(OrderPort.Status.CANCELLED, end.orders().get(0).status(), "unfinished order cancelled");
                eq(units, end.orders().get(0).preparedUnits(), "correct preparation state cancelled");
                eq(null, end.tables().get(0).customerId(), "abandonment frees table");
                eq(0L, end.summary().revenueCents(), "cancelled order contributes no revenue");
            }
        }
    }

    private static List<GameView> play(IntFunction<? extends Customer> factory) {
        GameController game = game(factory);
        List<GameView> turns = new ArrayList<>();
        for (int turn = 1; turn <= 18; turn++) {
            GameView start = game.startTurn();
            WaiterAction waiter = start.staff().waiterOptions().stream()
                    .filter(a -> a.type() == WaiterAction.Type.SERVE).findFirst()
                    .orElseGet(() -> start.staff().waiterOptions().stream()
                            .filter(a -> a.type() == WaiterAction.Type.TAKE_ORDER).findFirst()
                            .orElseGet(() -> start.staff().waiterOptions().stream()
                                    .filter(a -> a.type() == WaiterAction.Type.SEAT).findFirst()
                                    .orElse(WaiterAction.waitTurn())));
            accepted(game.submitWaiterAction(waiter));
            accepted(game.submitChefAction(game.getView().staff().chefOptions().stream()
                    .filter(a -> a.type() == ChefAction.Type.PREPARE).findFirst().orElse(ChefAction.waitTurn())));
            GameView end = game.finishTurn();
            eq(turn, end.summary().turnsUsed(), "no early finish");
            turns.add(end);
        }
        return turns;
    }

    private static GameController game(IntFunction<? extends Customer> factory) {
        return new GameController(new Restaurant(Order::new), new StaffTeam(), factory);
    }

    private static void cliReplay(String name, String input, boolean victory,
                                  int invalidMessages, Path logs) throws Exception {
        Path log = logs.resolve("cli-" + name + ".log");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), "Main")
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try (var stdin = process.getOutputStream()) {
            stdin.write(input.getBytes(StandardCharsets.UTF_8));
        }
        if (!process.waitFor(20, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor(5, TimeUnit.SECONDS);
            throw new AssertionError("CLI replay timed out: " + log);
        }
        String output = Files.readString(log);
        eq(0, process.exitValue(), "real Main exits normally");
        check(output.contains("Restaurant Rush (Stage 2)"), "Stage 2 banner");
        check(output.contains("Customer arrival seed: 24369"), "seed is displayed");
        check(output.contains("#2 VIP") && output.contains("#6 Critic"), "real CLI includes both new types");
        eq(18L, output.lines().filter(s -> s.startsWith("================ Turn ")).count(), "18 displayed turns");
        String heading = "================ " + (victory ? "VICTORY" : "DEFEAT") + " ================";
        int result = output.lastIndexOf(heading);
        check(result >= 0, "expected final result");
        String summary = victory
                ? "Dishes served 5 | Paid 5 | Unhappy departures 0 | Revenue $64.50 | Avg satisfaction 80.40 | Turns used 18/18"
                : "Dishes served 0 | Paid 0 | Unhappy departures 1 | Revenue $0.00 | Avg satisfaction N/A | Turns used 18/18";
        check(output.substring(result).contains(summary), "exact independent expected final statistics");
        eq(1L, output.lines().filter(s -> s.startsWith("================ VICTORY")
                || s.startsWith("================ DEFEAT")).count(), "single result after turn 18");
        eq((long) invalidMessages, output.lines().filter(s -> s.contains("Invalid:")).count(), "invalid choices retry");
        if (victory) {
            check(output.contains("Collected $13.50 from customer #2 (satisfaction 90 recorded)"),
                    "real Cashier collects actual VIP bill and satisfaction");
        }
        pass("real mixed Main/CLI " + name);
    }

    private static GameView.CustomerView customer(GameView view, int id) {
        return view.customers().stream().filter(c -> c.id() == id).findFirst().orElseThrow();
    }

    private static void accepted(ActionResult result) {
        check(result.accepted(), result.message());
    }

    private static void eq(Object expected, Object actual, String message) {
        check(java.util.Objects.equals(expected, actual), message + ": expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void throwsType(Class<? extends Throwable> type, Runnable operation) {
        assertions++;
        try {
            operation.run();
        } catch (Throwable error) {
            if (type.isInstance(error)) {
                return;
            }
            throw new AssertionError("Expected " + type.getSimpleName(), error);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static void pass(String name) {
        scenarios++;
        System.out.println("PASS " + name);
    }
}
