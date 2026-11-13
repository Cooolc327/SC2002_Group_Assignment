package integration;

import contracts.ActionResult;
import contracts.ChefAction;
import contracts.HostAction;
import contracts.WaiterAction;
import control.GameController;
import control.GameResult;
import control.TurnPhase;
import domain.HappyHourPolicy;
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
import orders.Order;
import staff.Host;
import staff.StaffTeam;
import staff.Waiter;
import view.GameView;

/** Production-flow checks with expected values fixed from the assignment's rules. */
public final class Stage3IntegrationTest {
    private static int scenarios;
    private static int assertions;

    private Stage3IntegrationTest() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException("Expected replay directory, log directory, optional --write-replays");
        }
        Path replays = Path.of(args[0]);
        Path logs = Path.of(args[1]);
        Files.createDirectories(logs);
        run("menu modes and polymorphic combo choices", Stage3IntegrationTest::menuChoices);
        run("start-of-turn and staff phase guards are atomic", Stage3IntegrationTest::phaseGuards);
        run("Host and Waiter have independent allowances", Stage3IntegrationTest::hostAllowances);
        run("Host permits same-turn ordering; serving and payment keep their delays", Stage3IntegrationTest::serviceTiming);
        run("Happy Hour lasts exactly two turns and cannot restart", Stage3IntegrationTest::policyClock);
        run("recovery eligibility, cap and invalid input", Stage3IntegrationTest::recoveryStates);
        run("recovery occurs once at each active turn start", Stage3IntegrationTest::recoveryTiming);
        run("activation discounts new orders and preserves existing bills", Stage3IntegrationTest::lockedBills);
        run("real combo preparation, serving and locked payment", Stage3IntegrationTest::comboPayment);
        run("VIP combo discounts remain locked through real payment", Stage3IntegrationTest::vipComboPayment);
        run("unserved combos cancel and release tables at every preparation state", Stage3IntegrationTest::comboAbandonment);
        run("Happy Hour trades revenue for recorded satisfaction", Stage3IntegrationTest::happyHourTradeoff);
        run("Stage 3 snapshots are detached and immutable", Stage3IntegrationTest::snapshots);
        if (args.length == 3) {
            eq("--write-replays", args[2], "explicit replay-writing option");
            Files.writeString(replays.resolve("stage3-victory.txt"), recordReplay(true));
            Files.writeString(replays.resolve("stage3-defeat.txt"), recordReplay(false));
        }
        String victory = Files.readString(replays.resolve("stage3-victory.txt"));
        String defeat = Files.readString(replays.resolve("stage3-defeat.txt"));
        checkCli("victory", victory, true, logs);
        checkCli("defeat", defeat, false, logs, "--stage3");
        checkCli("bom-victory", "\uFEFF" + victory, true, logs);
        List<String> choices = victory.lines().toList();
        StringBuilder invalid = new StringBuilder();
        for (int i = 0; i < choices.size(); i++) {
            if (i < 4) {
                invalid.append("not-a-number\n99\n");
            }
            invalid.append(choices.get(i)).append('\n');
        }
        checkCli("invalid-input-victory", invalid.toString(), true, logs);
        String eof = cli("", "--stage3");
        check(eof.contains("Input ended. Exiting game."), "EOF exits clearly");
        check(!eof.contains("================ VICTORY"), "EOF is not presented as a finished game");
        pass("CLI handles input ending during the start-of-turn choice");
        System.out.println("STAGE3_INTEGRATION=PASS scenarios=" + scenarios + " assertions=" + assertions);
    }

    private static void menuChoices() {
        Menu basic = new Menu();
        Menu extended = new Menu(true);
        eq(3, basic.getOrderableItems().size(), "Stage 2 menu");
        eq(6, extended.getOrderableItems().size(), "Stage 3 menu");
        eq("Salad + Burger Combo", new RegularCustomer(4).chooseItem(extended).getName(), "Regular combo choice");
        eq("Salad + Pasta Combo", new VipCustomer(5).chooseItem(extended).getName(), "VIP combo choice");
        eq("Salad + Pasta Combo", new CriticCustomer(6).chooseItem(extended).getName(), "Critic maximum prep and first tie");
        eq("Pasta", new CriticCustomer(6).chooseItem(basic).getName(), "earlier Critic behaviour");
        throwsType(UnsupportedOperationException.class, () -> extended.getOrderableItems().clear());
    }

    private static void phaseGuards() {
        Restaurant restaurant = new Restaurant(Order::new, true);
        GameController game = new GameController(restaurant, new StaffTeam());
        rejected(game.submitTurnStartDecision(true));
        game.startTurn();
        GameView before = game.getView();
        eq(TurnPhase.AWAIT_TURN_START, before.phase(), "opening phase");
        rejected(restaurant.seatCustomer(1, 1));
        rejected(game.submitHostAction(HostAction.seat(1, 1)));
        rejected(game.submitWaiterAction(WaiterAction.waitTurn()));
        rejected(game.submitChefAction(ChefAction.waitTurn()));
        eq(before, game.getView(), "early actions change nothing");
        throwsType(IllegalStateException.class, game::finishTurn);
        accepted(game.submitTurnStartDecision(false));
        before = game.getView();
        rejected(game.submitHostAction(null));
        rejected(game.submitHostAction(new HostAction(null, 1, 1)));
        rejected(game.submitHostAction(HostAction.seat(1, 99)));
        eq(before, game.getView(), "invalid Host choices are atomic");
        accepted(game.submitHostAction(HostAction.seat(1, 1)));
        rejected(game.submitHostAction(HostAction.waitTurn()));
        rejected(game.submitTurnStartDecision(true));
        eq(TurnPhase.AWAIT_WAITER, game.getView().phase(), "Host finishes only its own phase");
    }

    private static void hostAllowances() {
        Restaurant restaurant = new Restaurant(Order::new, true);
        restaurant.beginTurn(1);
        restaurant.registerArrival(new RegularCustomer(1));
        restaurant.registerArrival(new RegularCustomer(2)); // explicit domain fixture, not the base arrival schedule
        accepted(restaurant.resolveTurnStart(false));
        Host host = new Host();
        Waiter waiter = new Waiter();
        rejected(host.execute(HostAction.seat(1, 99), restaurant, 1));
        accepted(host.execute(HostAction.seat(1, 1), restaurant, 1));
        rejected(host.execute(HostAction.seat(2, 2), restaurant, 1));
        rejected(waiter.execute(WaiterAction.seat(2, 1), restaurant, 1));
        accepted(waiter.execute(WaiterAction.seat(2, 2), restaurant, 1));
        eq(List.of(), restaurant.getWaitingCustomerIds(), "both customers removed from queue");
        eq(1, restaurant.getTableViews().get(0).customerId(), "Host table");
        eq(2, restaurant.getTableViews().get(1).customerId(), "Waiter table");
        restaurant.advanceEating(1);
        restaurant.applyWaitingPenalty(1);
        restaurant.completeTurn(1);
        rejected(host.execute(HostAction.waitTurn(), restaurant, 2));
        restaurant.beginTurn(2);
        accepted(restaurant.resolveTurnStart(false));
        accepted(host.execute(HostAction.waitTurn(), restaurant, 2));
        rejected(host.execute(HostAction.waitTurn(), restaurant, 2));
    }

    private static void serviceTiming() {
        GameController game = game();
        driveTurn(game, true, 0, null);
        eq("READY", game.getView().orders().get(0).status().name(), "Chef finishes Salad in turn 1");
        eq(null, game.getView().orders().get(0).servedTurn(), "not served in its ready turn");
        driveTurn(game, true, 0, null);
        eq(CustomerState.SERVED, customer(game.getView(), 1).state(), "served on turn 2");
        eq(0, game.getView().summary().paidCount(), "no immediate payment");
        driveTurn(game, true, 0, null);
        eq(CustomerState.READY_TO_PAY, customer(game.getView(), 1).state(), "eats on turn 3");
        eq(0, game.getView().summary().paidCount(), "cannot pay when first ready");
        driveTurn(game, true, 0, null);
        eq(CustomerState.PAID, customer(game.getView(), 1).state(), "pays on turn 4");
        eq(900L, game.getView().summary().revenueCents(), "Salad payment");
    }

    private static void policyClock() {
        HappyHourPolicy policy = new HappyHourPolicy();
        eq(0, policy.remainingTurns(1), "initially inactive");
        policy.activate(4);
        eq(0, policy.remainingTurns(3), "not active before activation");
        eq(2, policy.remainingTurns(4), "activation turn included");
        eq(1, policy.remainingTurns(5), "next turn included");
        eq(0, policy.remainingTurns(6), "expired");
        throwsType(IllegalStateException.class, () -> policy.activate(6));
        throwsType(IllegalStateException.class, () -> new HappyHourPolicy().activate(0));
        GameController game = game();
        driveTurn(game, false, 1, null);
        game.startTurn();
        GameView before = game.getView();
        rejected(game.submitTurnStartDecision(true));
        eq(before, game.getView(), "used activation changes neither phase nor satisfaction");
        accepted(game.submitTurnStartDecision(false));
        eq(1, game.getView().happyHour().remainingTurns(), "ongoing policy after rejecting a restart");
    }

    private static void recoveryStates() {
        for (CustomerState state : CustomerState.values()) {
            Customer customer = new RegularCustomer(1);
            for (int i = 0; i < 5; i++) {
                customer.applyWaitingPenalty();
            }
            if (state == CustomerState.LEFT) {
                while (customer.getSatisfaction() > 0) {
                    customer.applyWaitingPenalty();
                }
                customer.abandon();
            } else if (state != CustomerState.WAITING) {
                customer.seat();
                if (state != CustomerState.SEATED) {
                    customer.placeOrder();
                    if (state != CustomerState.ORDERED) {
                        customer.serve(1);
                        if (state != CustomerState.SERVED) {
                            customer.finishedEating(2);
                            if (state == CustomerState.PAID) {
                                customer.pay();
                            }
                        }
                    }
                }
            }
            int before = customer.getSatisfaction();
            customer.recoverWaitingSatisfaction(15);
            eq(state.isWaitingForFood() ? before + 15 : before, customer.getSatisfaction(), "recovery for " + state);
            customer.recoverWaitingSatisfaction(Integer.MAX_VALUE);
            eq(state.isWaitingForFood() ? 100 : before, customer.getSatisfaction(), "cap/exclusion for " + state);
            int after = customer.getSatisfaction();
            throwsType(IllegalArgumentException.class, () -> customer.recoverWaitingSatisfaction(-1));
            eq(after, customer.getSatisfaction(), "negative recovery is atomic");
        }
    }

    private static void recoveryTiming() {
        GameController game = game();
        driveTurn(game, false, 0, null);
        eq(92, customer(game.getView(), 1).satisfaction(), "first waiting loss");
        game.startTurn();
        accepted(game.submitTurnStartDecision(true));
        eq(100, customer(game.getView(), 1).satisfaction(), "first capped recovery");
        GameView before = game.getView();
        rejected(game.submitTurnStartDecision(false));
        eq(before, game.getView(), "cannot recover twice in one turn");
        completeWaitingTurn(game);
        game.startTurn();
        accepted(game.submitTurnStartDecision(false));
        eq(100, customer(game.getView(), 1).satisfaction(), "second recovery");
        completeWaitingTurn(game);
        game.startTurn();
        accepted(game.submitTurnStartDecision(false));
        eq(0, game.getView().happyHour().remainingTurns(), "third turn inactive");
        eq(92, customer(game.getView(), 1).satisfaction(), "no recovery after expiry");
    }

    private static void lockedBills() {
        GameController game = game();
        while (game.getView().turn() < 8) {
            driveTurn(game, true, 4, null);
            if (game.getView().turn() == 4) {
                eq(900, order(game.getView(), 1).billCents(), "pre-existing Salad unchanged");
                eq(1080, order(game.getView(), 2).billCents(), "brief VIP Burger example");
            }
        }
        eq(0, game.getView().happyHour().remainingTurns(), "expired before inspecting payment");
        eq(1080, order(game.getView(), 2).billCents(), "discount remains locked after expiry");
        eq(7, order(game.getView(), 2).paidTurn(), "VIP pays after policy expiry");
        eq(100, order(game.getView(), 2).satisfactionAtPayment(), "VIP waiting recovery recorded on payment");
    }

    private static void comboPayment() {
        GameController game = game();
        while (game.getView().phase() != TurnPhase.FINISHED) {
            driveTurn(game, true, 4, null);
            if (game.getView().turn() == 10) {
                eq("Salad + Burger Combo", order(game.getView(), 4).itemName(), "actual customer combo order");
                eq(2, order(game.getView(), 4).preparationUnits(), "summed prep units");
                eq(2160, order(game.getView(), 4).billCents(), "price outside Happy Hour");
            }
            if (game.getView().turn() < 18) {
                check(game.getView().phase() != TurnPhase.FINISHED, "never finishes early");
            }
        }
        GameView view = game.getView();
        eq(18, view.summary().turnsUsed(), "all turns completed");
        eq(6, view.customers().size(), "six arrivals");
        eq(2, view.tables().size(), "two tables");
        eq(5, view.summary().paidCount(), "at most five pay in this run");
        eq(8370L, view.summary().revenueCents(), "manual expected total: 900+1080+1800+2160+2430");
        eq(436, view.summary().paidSatisfactionSum(), "manual expected total: 92+100+84+84+76");
        eq(GameResult.VICTORY, view.result(), "all three targets met");
        eq(2160, order(view, 4).billCents(), "combo payment remains locked");
        eq(14, order(view, 4).paidTurn(), "two-unit combo payment timing");
        eq(18, order(view, 5).paidTurn(), "three-unit combo payment timing");
        eq("Salad + Pasta Combo", order(view, 6).itemName(), "Critic's real combo preference");
        eq(null, order(view, 6).paidTurn(), "turn-16 arrival cannot pay by turn 18");
        throwsType(IllegalStateException.class, game::startTurn);
    }

    private static void snapshots() {
        GameController game = game();
        GameView opening = game.startTurn();
        accepted(game.submitTurnStartDecision(true));
        GameView host = game.getView();
        throwsType(UnsupportedOperationException.class, () -> host.staff().hostOptions().clear());
        throwsType(UnsupportedOperationException.class, () -> host.menu().clear());
        accepted(game.submitHostAction(HostAction.seat(1, 1)));
        eq(CustomerState.WAITING, customer(host, 1).state(), "old customer view unchanged");
        eq(0, opening.happyHour().remainingTurns(), "old policy view unchanged");
        eq(2, game.getView().happyHour().remainingTurns(), "current policy view");
        check(opening.happyHour().canActivate(), "old activation availability detached");
    }

    private static void vipComboPayment() {
        // All-VIP fixture isolates combo/VIP/Happy Hour composition; the default still uses the required mixed seed.
        GameController game = new GameController(new Restaurant(Order::new, true), new StaffTeam(), VipCustomer::new);
        while (game.getView().phase() != TurnPhase.FINISHED) {
            driveTurn(game, true, 13, null);
        }
        GameView.OrderView order = order(game.getView(), 5);
        eq("Salad + Pasta Combo", order.itemName(), "VIP chooses the documented combo");
        eq(1750, order.billCents(), "2430 cents -> VIP 2187 -> Happy Hour 1750");
        eq(3, order.preparationUnits(), "combo prep units");
        eq(18, order.paidTurn(), "payment occurs after policy expiry");
        eq(0, game.getView().happyHour().remainingTurns(), "policy expired");
    }

    private static void comboAbandonment() {
        for (int preparedUnits : List.of(0, 1, 2)) {
            Restaurant restaurant = new Restaurant(Order::new, true);
            for (int turn = 1; turn <= 13; turn++) {
                restaurant.beginTurn(turn);
                if (turn == 1) { restaurant.registerArrival(new RegularCustomer(4)); }
                accepted(restaurant.resolveTurnStart(false));
                if (turn == 1) {
                    accepted(restaurant.seatCustomer(4, 1));
                    accepted(restaurant.placeOrder(4));
                }
                if (turn <= preparedUnits) {
                    accepted(restaurant.advancePreparation(1, turn));
                }
                if (turn == 2) {
                    String expected = preparedUnits == 0 ? "PLACED" : preparedUnits == 1 ? "PREPARING" : "READY";
                    eq(expected, restaurant.getOrderViews().get(0).status().name(), "abandonment starting state");
                }
                restaurant.advanceEating(turn);
                restaurant.applyWaitingPenalty(turn);
                restaurant.completeTurn(turn);
            }
            eq("CANCELLED", restaurant.getOrderViews().get(0).status().name(), "combo order cancelled");
            eq(CustomerState.LEFT, restaurant.getCustomerViews().get(0).state(), "customer leaves");
            eq(null, restaurant.getTableViews().get(0).customerId(), "table released");
            eq(0L, restaurant.getSummary().revenueCents(), "unpaid combo adds no revenue");
        }
    }

    private static void happyHourTradeoff() {
        GameController game = game();
        while (game.getView().phase() != TurnPhase.FINISHED) { driveTurn(game, true, 0, null); }
        eq(8640L, game.getView().summary().revenueCents(), "same service choices without discount");
        eq(431, game.getView().summary().paidSatisfactionSum(), "VIP pays at 95 instead of recovered 100");
        eq(5, game.getView().summary().paidCount(), "same throughput");
        eq(GameResult.VICTORY, game.getView().result(), "both strategies meet the targets");
    }

    private static GameController game() {
        CustomerFactory factory = new CustomerFactory();
        return new GameController(new Restaurant(Order::new, true), new StaffTeam(), factory::createCustomer);
    }

    private static void completeWaitingTurn(GameController game) {
        accepted(game.submitHostAction(HostAction.waitTurn()));
        accepted(game.submitWaiterAction(WaiterAction.waitTurn()));
        accepted(game.submitChefAction(ChefAction.waitTurn()));
        game.finishTurn();
    }

    private static String recordReplay(boolean winning) {
        GameController game = game();
        List<String> choices = new ArrayList<>();
        while (game.getView().phase() != TurnPhase.FINISHED) {
            driveTurn(game, winning, winning ? 4 : 0, choices);
        }
        return String.join("\n", choices) + "\n";
    }

    private static void driveTurn(GameController game, boolean winning, int activateTurn, List<String> choices) {
        GameView view = game.startTurn();
        boolean activate = view.turn() == activateTurn;
        if (choices != null && view.happyHour().canActivate()) {
            choices.add(activate ? "1" : "0");
        }
        accepted(game.submitTurnStartDecision(activate));
        List<HostAction> hosts = game.getView().staff().hostOptions();
        int hostChoice = winning && hosts.size() > 1 ? 1 : 0;
        if (choices != null) { choices.add(Integer.toString(hostChoice)); }
        accepted(game.submitHostAction(hosts.get(hostChoice)));
        List<WaiterAction> waiters = game.getView().staff().waiterOptions();
        int waiterChoice = 0;
        if (winning) {
            for (WaiterAction.Type type : List.of(WaiterAction.Type.SERVE, WaiterAction.Type.TAKE_ORDER, WaiterAction.Type.SEAT)) {
                for (int i = 1; i < waiters.size(); i++) {
                    if (waiters.get(i).type() == type) { waiterChoice = i; break; }
                }
                if (waiterChoice != 0) { break; }
            }
        }
        if (choices != null) { choices.add(Integer.toString(waiterChoice)); }
        accepted(game.submitWaiterAction(waiters.get(waiterChoice)));
        List<ChefAction> chefs = game.getView().staff().chefOptions();
        int chefChoice = winning && chefs.size() > 1 ? 1 : 0;
        if (choices != null) { choices.add(Integer.toString(chefChoice)); }
        accepted(game.submitChefAction(chefs.get(chefChoice)));
        game.finishTurn();
    }

    private static GameView.CustomerView customer(GameView view, int id) {
        return view.customers().stream().filter(c -> c.id() == id).findFirst().orElseThrow();
    }

    private static GameView.OrderView order(GameView view, int customerId) {
        return view.orders().stream().filter(o -> o.customerId() == customerId).findFirst().orElseThrow();
    }

    private static void checkCli(String name, String input, boolean victory, Path logs, String... args) throws Exception {
        String output = cli(input, args);
        Files.writeString(logs.resolve("cli-" + name + ".log"), output);
        check(output.contains("Restaurant Rush (Stage 3)"), "Stage 3 banner");
        eq(18L, output.lines().filter(s -> s.startsWith("================ Turn ")).count(), "real CLI all turns");
        check(output.contains(victory ? "================ VICTORY" : "================ DEFEAT"), "CLI result");
        check(output.contains(victory ? "Revenue $83.70" : "Revenue $0.00"), "CLI revenue");
        check(output.contains(victory ? "Avg satisfaction 87.20" : "Avg satisfaction N/A"), "CLI average");
        check(output.contains(victory ? "Paid customers 5 (need 4)" : "Paid customers 0 (need 4)"), "CLI paid count");
        eq(victory ? 4 : 18, output.split("Happy Hour choice:", -1).length - 1
                - (name.equals("invalid-input-victory") ? 2 : 0), "activation prompts disappear once used");
        check(output.contains("Host tasks"), "Host choice shown");
        if (victory) {
            check(output.contains("Happy Hour remaining: 2 turn(s)"), "activation duration displayed");
            check(output.contains("Happy Hour remaining: 1 turn(s)"), "next turn duration displayed");
            check(output.contains("Happy Hour remaining: 0 turn(s) (activation used)"), "expiry displayed");
            check(output.contains("ordered Burger (order 2, $10.80)"), "real VIP Happy Hour bill");
            check(output.contains("Salad + Burger Combo"), "actual combo displayed");
        }
        pass("real default Main/CLI " + name);
    }

    private static String cli(String input, String... args) throws Exception {
        List<String> command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), "Main"));
        command.addAll(List.of(args));
        Path outputFile = Files.createTempFile("stage3-cli-", ".log");
        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(outputFile.toFile()).start();
        try {
            try (var stdin = process.getOutputStream()) {
                stdin.write(input.getBytes(StandardCharsets.UTF_8));
            }
            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished) { process.destroyForcibly(); }
            check(finished, "CLI exits within timeout");
            eq(0, process.exitValue(), "CLI exits normally");
            return Files.readString(outputFile, StandardCharsets.UTF_8);
        } finally {
            if (process.isAlive()) { process.destroyForcibly(); }
            Files.deleteIfExists(outputFile);
        }
    }

    private static void accepted(ActionResult result) { check(result.accepted(), result.message()); }
    private static void rejected(ActionResult result) { check(!result.accepted(), "expected rejection: " + result.message()); }
    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) { throw new AssertionError(message); }
    }
    private static void eq(Object expected, Object actual, String message) {
        check(java.util.Objects.equals(expected, actual), message + ": expected " + expected + ", got " + actual);
    }
    private static void throwsType(Class<? extends Throwable> type, Runnable action) {
        assertions++;
        try { action.run(); } catch (Throwable error) {
            if (type.isInstance(error)) { return; }
            throw new AssertionError("Expected " + type.getSimpleName(), error);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
    private static void run(String name, Runnable action) { action.run(); pass(name); }
    private static void pass(String name) { scenarios++; System.out.println("PASS " + name); }
}
