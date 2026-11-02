package integration;

import contracts.ActionResult;
import contracts.ChefAction;
import contracts.OrderPort;
import contracts.StaffGateway;
import contracts.WaiterAction;
import control.GameController;
import control.GameResult;
import control.TurnPhase;
import domain.Restaurant;
import entities.Customer;
import entities.CustomerState;
import entities.RegularCustomer;
import entities.SeatingArea;
import fooditems.MenuItem;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import orders.Order;
import view.GameSummary;
import view.GameView;

/**
 * Executable member-A tests. --production-orders uses the real Order.
 * RecordingStaff remains a test double for call-order assertions; the separate
 * Stage1SmokeTest exercises the real StaffTeam, Main and CLI.
 */
public final class MemberACoreTest {
    private static boolean productionOrders;
    private static int scenarios;
    private static int assertions;

    private MemberACoreTest() {}

    public static void main(String[] args) {
        if (args.length > 1 || (args.length == 1 && !args[0].equals("--production-orders"))) {
            throw new IllegalArgumentException("Usage: MemberACoreTest [--production-orders]");
        }
        productionOrders = args.length == 1;
        run("18 turns, arrival schedule and defeat", MemberACoreTest::idleDefeat);
        run("phase limits and invalid-action retries", MemberACoreTest::invalidRetries);
        run("ordering, serving, eating and payment timing", MemberACoreTest::serviceTiming);
        run("two-unit Pasta timeline", MemberACoreTest::pastaTimeline);
        run("reproducible victory with current menu rule", MemberACoreTest::victoryReplay);
        run("abandonment cancels PLACED orders", () -> abandonment(0));
        run("abandonment cancels PREPARING orders", () -> abandonment(1));
        run("abandonment cancels READY orders", () -> abandonment(2));
        run("payment FIFO and one payment per turn", () -> paymentPriority(false));
        run("payment ties use customer id", () -> paymentPriority(true));
        run("snapshots are detached and immutable", MemberACoreTest::immutableSnapshots);
        run("order factory failure leaves no partial order", MemberACoreTest::factoryFailure);
        run("seating validation is atomic", MemberACoreTest::seatingValidation);
        run("victory boundaries", MemberACoreTest::resultBoundaries);
        System.out.println("MEMBER_A_CORE=PASS scenarios=" + scenarios + " assertions=" + assertions
                + " orders=" + (productionOrders ? "production" : "fake"));
    }

    private static void idleDefeat() {
        Fixture f = new Fixture();
        eq(0, f.game.getView().turn(), "initial turn");
        eq(TurnPhase.NOT_STARTED, f.game.getView().phase(), "initial phase");
        eq(2, f.game.getView().tables().size(), "two tables");
        check(f.game.getView().summary().averagePaidSatisfaction().isEmpty(), "initial N/A");
        int arrivals = 0;
        for (int turn = 1; turn <= 18; turn++) {
            GameView start = f.game.startTurn();
            if (List.of(1, 4, 7, 10, 13, 16).contains(turn)) {
                arrivals++;
                eq(100, customer(start, arrivals).satisfaction(), "arrival satisfaction");
            }
            eq(arrivals, start.customers().size(), "scheduled arrivals");
            chooseAndFinish(f, WaiterAction.waitTurn(), ChefAction.waitTurn());
        }
        GameView end = f.game.getView();
        eq(6, end.customers().size(), "six arrivals total");
        eq(2, end.summary().unhappyDepartures(), "C1 and C2 abandon");
        eq(0, end.summary().paidCount(), "no payments");
        eq(0L, end.summary().revenueCents(), "no unpaid revenue");
        check(end.summary().averagePaidSatisfaction().isEmpty(), "no payments means N/A");
        eq(18, end.summary().turnsUsed(), "all turns complete");
        eq(GameResult.DEFEAT, end.result(), "defeat");
        eq(TurnPhase.FINISHED, end.phase(), "finished");
        eq(18, f.staff.cashierCalls, "one automatic payment pass per turn");
        throwsType(IllegalStateException.class, f.game::startTurn);
        throwsType(IllegalStateException.class, f.game::finishTurn);
        check(!f.game.submitWaiterAction(WaiterAction.waitTurn()).accepted(), "no action after end");
    }

    private static void invalidRetries() {
        Fixture f = new Fixture();
        throwsType(IllegalStateException.class, f.game::finishTurn);
        f.game.startTurn();
        GameView before = f.game.getView();
        check(!f.game.submitChefAction(ChefAction.waitTurn()).accepted(), "chef cannot act first");
        eq(0, f.staff.chefCalls, "wrong-phase action never reaches staff");
        check(!f.game.submitWaiterAction(null).accepted(), "null waiter choice rejected");
        check(!f.game.submitWaiterAction(new WaiterAction(null, 0, 0, 0)).accepted(), "null action type");
        check(!f.game.submitWaiterAction(WaiterAction.seat(99, 1)).accepted(), "unknown customer");
        check(!f.game.submitWaiterAction(WaiterAction.seat(1, 99)).accepted(), "unknown table");
        eq(before, f.game.getView(), "invalid choices change nothing");
        accepted(f.game.submitWaiterAction(WaiterAction.seat(1, 2)));
        eq(1, f.staff.waiterAccepted, "only successful choice consumes task");
        check(!f.game.submitWaiterAction(WaiterAction.waitTurn()).accepted(), "one waiter task");
        check(!f.game.submitChefAction(null).accepted(), "null chef choice rejected");
        check(!f.game.submitChefAction(ChefAction.prepare(99)).accepted(), "unknown order");
        eq(TurnPhase.AWAIT_CHEF, f.game.getView().phase(), "chef may retry");
        eq(100, customer(f.game.getView(), 1).satisfaction(), "retry has no penalty");
        accepted(f.game.submitChefAction(ChefAction.waitTurn()));
        check(!f.game.submitChefAction(ChefAction.waitTurn()).accepted(), "one chef task");
        f.game.finishTurn();
        eq(92, customer(f.game.getView(), 1).satisfaction(), "one end-turn penalty");
        GameView end = f.game.getView();
        throwsType(IllegalStateException.class, f.game::finishTurn);
        eq(end, f.game.getView(), "duplicate finish changes nothing");
    }

    private static void serviceTiming() {
        Fixture f = new Fixture();
        f.turn(WaiterAction.seat(1, 1), ChefAction.waitTurn());
        f.game.startTurn();
        accepted(f.game.submitWaiterAction(WaiterAction.takeOrder(1)));
        check(f.game.getView().staff().chefOptions().contains(ChefAction.prepare(1)),
                "chef can select an order taken this turn");
        accepted(f.game.submitChefAction(ChefAction.prepare(1)));
        eq(OrderPort.Status.READY, order(f.game.getView(), 1).status(), "one-unit dish READY");
        check(!f.restaurant.serveOrder(1, 2).accepted(), "no same-turn serving");
        eq(0L, f.game.getView().summary().revenueCents(), "READY is unpaid");
        f.game.finishTurn();
        eq(84, customer(f.game.getView(), 1).satisfaction(), "unserved penalty");
        GameView third = f.game.startTurn();
        check(third.staff().waiterOptions().contains(WaiterAction.serve(1)), "serve on next turn");
        chooseAndFinish(f, WaiterAction.serve(1), ChefAction.waitTurn());
        eq(CustomerState.SERVED, customer(f.game.getView(), 1).state(), "not eaten immediately");
        eq(84, customer(f.game.getView(), 1).satisfaction(), "served customer loses nothing");
        f.turn(WaiterAction.waitTurn(), ChefAction.waitTurn());
        eq(CustomerState.READY_TO_PAY, customer(f.game.getView(), 1).state(), "one full turn eating");
        eq(0, f.game.getView().summary().paidCount(), "no same-turn payment");
        f.game.startTurn();
        check(!f.game.submitWaiterAction(WaiterAction.seat(2, 1)).accepted(),
                "table remains occupied until automatic payment later this turn");
        chooseAndFinish(f, WaiterAction.seat(2, 2), ChefAction.waitTurn());
        GameView fifth = f.game.getView();
        eq(CustomerState.PAID, customer(fifth, 1).state(), "payment on turn five");
        eq(900L, fifth.summary().revenueCents(), "C1 chooses Salad in current code");
        eq(84, order(fifth, 1).satisfactionAtPayment(), "payment snapshot");
        eq(null, fifth.tables().get(0).customerId(), "payment releases table");
        eq(5, order(fifth, 1).paidTurn(), "paid turn recorded");
        eq(List.of("waiter:1", "chef:1", "cashier:1"),
                f.staff.events.subList(0, 3), "staff execution order");
    }

    private static void pastaTimeline() {
        Restaurant r = new Restaurant(MemberACoreTest::createTestOrder);
        r.beginTurn(1);
        r.registerArrival(new RegularCustomer(3)); // id 3 deterministically chooses Pasta.
        accepted(r.seatCustomer(3, 1));
        closeDomain(r, 1);
        r.beginTurn(2);
        accepted(r.placeOrder(3));
        accepted(r.advancePreparation(1, 2));
        eq(OrderPort.Status.PREPARING, r.getOrderViews().get(0).status(), "Pasta first unit");
        closeDomain(r, 2);
        r.beginTurn(3);
        accepted(r.advancePreparation(1, 3));
        check(!r.serveOrder(1, 3).accepted(), "Pasta cannot be served when just completed");
        closeDomain(r, 3);
        eq(76, r.getCustomerViews().get(0).satisfaction(), "three waiting penalties");
        r.beginTurn(4);
        accepted(r.serveOrder(1, 4));
        closeDomain(r, 4);
        r.beginTurn(5);
        r.advanceEating(5);
        check(!r.settlePayment(3, 5).accepted(), "new READY_TO_PAY cannot pay");
        closeDomain(r, 5);
        r.beginTurn(6);
        accepted(r.settlePayment(3, 6));
        closeDomain(r, 6);
        eq(1800L, r.getSummary().revenueCents(), "Pasta payment");
        eq(76, r.getOrderViews().get(0).satisfactionAtPayment(), "Pasta satisfaction");
        eq(null, r.getTableViews().get(0).customerId(), "Pasta table freed");
    }

    private static void victoryReplay() {
        Fixture f = new Fixture();
        WaiterAction[] actions = {
            WaiterAction.seat(1, 1), WaiterAction.takeOrder(1), WaiterAction.serve(1),
            WaiterAction.seat(2, 2), WaiterAction.takeOrder(2), WaiterAction.serve(2),
            WaiterAction.seat(3, 1), WaiterAction.takeOrder(3), WaiterAction.waitTurn(),
            WaiterAction.serve(3), WaiterAction.seat(4, 2), WaiterAction.takeOrder(4),
            WaiterAction.serve(4), WaiterAction.seat(5, 1), WaiterAction.takeOrder(5),
            WaiterAction.serve(5), WaiterAction.seat(6, 2), WaiterAction.takeOrder(6)
        };
        int[] preparation = {0, 1, 0, 0, 2, 0, 0, 3, 3, 0, 0, 4, 0, 0, 5, 0, 0, 6};
        for (int i = 0; i < actions.length; i++) {
            f.turn(actions[i], preparation[i] == 0 ? ChefAction.waitTurn()
                    : ChefAction.prepare(preparation[i]));
            if (i == 14) {
                eq(4, f.game.getView().summary().paidCount(), "four paid by turn fifteen");
                eq(5100L, f.game.getView().summary().revenueCents(), "early revenue target met");
                eq(GameResult.IN_PROGRESS, f.game.getView().result(), "no early victory");
            }
        }
        GameView view = f.game.getView();
        eq(GameResult.VICTORY, view.result(), "victory");
        eq(5, view.summary().paidCount(), "five paid");
        eq(5, view.summary().servedCount(), "five served");
        eq(6600L, view.summary().revenueCents(), "reproducible revenue");
        eq(396, view.summary().paidSatisfactionSum(), "payment satisfaction total");
        eq(79.2, view.summary().averagePaidSatisfaction().orElseThrow(), "average");
        eq(0, view.summary().unhappyDepartures(), "no abandonment");
        eq(OrderPort.Status.PREPARING, order(view, 6).status(), "late C6 cannot finish");
        eq(1, order(view, 6).preparedUnits(), "only first Pasta unit by turn eighteen");
    }

    private static void abandonment(int preparationUnits) {
        Restaurant r = new Restaurant(MemberACoreTest::createTestOrder);
        for (int turn = 1; turn <= 13; turn++) {
            r.beginTurn(turn);
            if (turn == 1) {
                r.registerArrival(new RegularCustomer(3));
                accepted(r.seatCustomer(3, 1));
            }
            if (turn == 2) {
                accepted(r.placeOrder(3));
                if (preparationUnits > 0) {
                    accepted(r.advancePreparation(1, turn));
                }
            }
            if (turn == 3 && preparationUnits == 2) {
                accepted(r.advancePreparation(1, turn));
            }
            closeDomain(r, turn);
        }
        eq(CustomerState.LEFT, r.getCustomerViews().get(0).state(), "abandoned at zero");
        eq(0, r.getCustomerViews().get(0).satisfaction(), "satisfaction floor");
        eq(OrderPort.Status.CANCELLED, r.getOrderViews().get(0).status(), "unfinished order cancelled");
        eq(null, r.getTableViews().get(0).customerId(), "abandonment frees table");
        eq(0L, r.getSummary().revenueCents(), "cancelled order contributes no revenue");
        eq(1, r.getSummary().unhappyDepartures(), "departure count");
        r.beginTurn(14);
        check(!r.advancePreparation(1, 14).accepted(), "cancelled order cannot be prepared");
        check(!r.serveOrder(1, 14).accepted(), "cancelled order cannot be served");
    }

    private static void paymentPriority(boolean tied) {
        // Domain fixture builds two eligible customers; staff quotas are tested above.
        Restaurant r = new Restaurant(MemberACoreTest::createTestOrder);
        for (int turn = 1; turn <= 4; turn++) {
            r.beginTurn(turn);
            if (turn == 1) {
                r.registerArrival(new RegularCustomer(2)); // reverse insertion order
                r.registerArrival(new RegularCustomer(1));
                accepted(r.seatCustomer(2, 1));
                accepted(r.seatCustomer(1, 2));
            } else if (turn == 2) {
                accepted(r.placeOrder(2)); // order 1 = customer 2
                accepted(r.placeOrder(1)); // order 2 = customer 1
                accepted(r.advancePreparation(1, turn));
                accepted(r.advancePreparation(2, turn));
            } else if (turn == 3) {
                accepted(r.serveOrder(1, turn));
                if (tied) {
                    accepted(r.serveOrder(2, turn));
                }
            } else if (!tied) {
                accepted(r.serveOrder(2, turn));
            }
            closeDomain(r, turn);
        }
        r.beginTurn(5);
        r.advanceEating(5);
        int first = tied ? 1 : 2;
        int second = tied ? 2 : 1;
        eq(first, r.findOldestPayableCustomer(5).orElseThrow(), "oldest eligible first");
        check(!r.settlePayment(second, 5).accepted(), "wrong customer rejected");
        accepted(r.settlePayment(first, 5));
        check(!r.settlePayment(second, 5).accepted(), "one payment per turn");
        closeDomain(r, 5);
        r.beginTurn(6);
        accepted(r.settlePayment(second, 6));
        check(!r.settlePayment(first, 6).accepted(), "paid customer cannot pay twice");
        closeDomain(r, 6);
        eq(2400L, r.getSummary().revenueCents(), "exactly two bills");
        eq(2, r.getSummary().paidCount(), "two customers paid");
    }

    private static void immutableSnapshots() {
        Fixture f = new Fixture();
        GameView before = f.game.startTurn();
        throwsType(UnsupportedOperationException.class, () -> before.customers().clear());
        throwsType(UnsupportedOperationException.class, () -> before.tables().clear());
        throwsType(UnsupportedOperationException.class, () -> before.waitingCustomerIds().clear());
        throwsType(UnsupportedOperationException.class, () -> before.menu().clear());
        throwsType(UnsupportedOperationException.class, () -> before.staff().waiterOptions().clear());
        eq(before, f.game.getView(), "reading never advances a turn");
        chooseAndFinish(f, WaiterAction.seat(1, 1), ChefAction.waitTurn());
        eq(CustomerState.WAITING, customer(before, 1).state(), "old snapshot retains state");
        eq(100, customer(before, 1).satisfaction(), "old snapshot retains satisfaction");
        eq(List.of(1), before.waitingCustomerIds(), "old queue detached");
        eq(null, before.tables().get(0).customerId(), "old table detached");
        f.game.startTurn();
        accepted(f.game.submitWaiterAction(WaiterAction.takeOrder(1)));
        accepted(f.game.submitChefAction(ChefAction.prepare(1)));
        GameView ready = f.game.getView();
        throwsType(UnsupportedOperationException.class, () -> ready.orders().clear());
        f.game.finishTurn();
        f.turn(WaiterAction.serve(1), ChefAction.waitTurn());
        eq(OrderPort.Status.READY, order(ready, 1).status(), "old order snapshot detached");
    }

    private static void factoryFailure() {
        boolean[] invalid = {true};
        Restaurant r = new Restaurant((id, c, item, bill) ->
                createTestOrder(id, c, item, invalid[0] ? bill + 1 : bill));
        r.beginTurn(1);
        r.registerArrival(new RegularCustomer(1));
        accepted(r.seatCustomer(1, 1));
        throwsType(IllegalStateException.class, () -> r.placeOrder(1));
        eq(0, r.getOrderViews().size(), "bad factory adds no order");
        eq(CustomerState.SEATED, r.getCustomerViews().get(0).state(), "customer unchanged");
        invalid[0] = false;
        accepted(r.placeOrder(1));
        eq(1, r.getOrderViews().get(0).id(), "failed factory consumes no order id");
    }

    private static void seatingValidation() {
        SeatingArea seating = new SeatingArea(2);
        Customer c = new RegularCustomer(1);
        seating.addToQueue(c);
        throwsType(IllegalArgumentException.class, () -> seating.seat(c, 99));
        check(seating.isQueued(c), "invalid table preserves queue");
        check(seating.getTables().get(0).isFree(), "invalid table preserves occupancy");
        c.seat(); // intentionally inconsistent input must not partially mutate seating.
        throwsType(IllegalStateException.class, () -> seating.seat(c, 1));
        check(seating.isQueued(c), "invalid customer state preserves queue");
        check(seating.getTables().get(0).isFree(), "invalid state preserves table");
        SeatingArea normal = new SeatingArea(2);
        Customer other = new RegularCustomer(2);
        normal.addToQueue(other);
        eq(1, normal.seat(other).getId(), "original automatic seating API remains available");
        normal.removeCustomer(other);
        check(normal.getTables().get(0).isFree(), "seated customer can be removed");
    }

    private static void resultBoundaries() {
        eq(GameResult.VICTORY, GameResult.evaluate(new GameSummary(4, 4, 0, 4500, 240, 18)),
                "all thresholds inclusive");
        eq(GameResult.DEFEAT, GameResult.evaluate(new GameSummary(4, 3, 0, 4500, 240, 18)),
                "paid count required");
        eq(GameResult.DEFEAT, GameResult.evaluate(new GameSummary(4, 4, 0, 4499, 240, 18)),
                "revenue required");
        eq(GameResult.DEFEAT, GameResult.evaluate(new GameSummary(4, 4, 0, 4500, 239, 18)),
                "satisfaction required");
        eq(GameResult.IN_PROGRESS, GameResult.evaluate(new GameSummary(4, 4, 0, 4500, 240, 17)),
                "no early ending");
        eq(GameResult.DEFEAT, GameResult.evaluate(new GameSummary(0, 0, 0, 0, 0, 18)),
                "zero payments");
    }

    private static void closeDomain(Restaurant r, int turn) {
        r.advanceEating(turn);
        r.applyWaitingPenalty(turn);
        throwsType(IllegalStateException.class, () -> r.applyWaitingPenalty(turn));
        r.completeTurn(turn);
    }

    private static GameView.CustomerView customer(GameView view, int id) {
        return view.customers().stream().filter(c -> c.id() == id).findFirst().orElseThrow();
    }

    private static GameView.OrderView order(GameView view, int id) {
        return view.orders().stream().filter(o -> o.id() == id).findFirst().orElseThrow();
    }

    private static void chooseAndFinish(Fixture f, WaiterAction waiter, ChefAction chef) {
        accepted(f.game.submitWaiterAction(waiter));
        accepted(f.game.submitChefAction(chef));
        f.game.finishTurn();
    }

    private static void accepted(ActionResult result) {
        check(result.accepted(), result.message());
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void eq(Object expected, Object actual, String message) {
        check(java.util.Objects.equals(expected, actual),
                message + ": expected " + expected + ", got " + actual);
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

    private static void run(String name, Runnable scenario) {
        scenario.run();
        scenarios++;
        System.out.println("PASS " + name);
    }

    private static OrderPort createTestOrder(int id, Customer customer, MenuItem item, int bill) {
        return productionOrders ? new Order(id, customer, item, bill)
                : new FakeOrder(id, customer, item, bill);
    }

    private static final class Fixture {
        final Restaurant restaurant = new Restaurant(MemberACoreTest::createTestOrder);
        final RecordingStaff staff = new RecordingStaff();
        final GameController game = new GameController(restaurant, staff);

        void turn(WaiterAction waiter, ChefAction chef) {
            game.startTurn();
            chooseAndFinish(this, waiter, chef);
        }
    }

    /** Test-only adapter stands in for member D's staff. */
    private static final class RecordingStaff implements StaffGateway {
        final List<String> events = new ArrayList<>();
        int chefCalls;
        int cashierCalls;
        int waiterAccepted;

        @Override
        public ActionResult executeWaiter(WaiterAction action, Restaurant r, int turn) {
            events.add("waiter:" + turn);
            ActionResult result = switch (action.type()) {
                case SEAT -> r.seatCustomer(action.customerId(), action.tableId());
                case TAKE_ORDER -> r.placeOrder(action.customerId());
                case SERVE -> r.serveOrder(action.orderId(), turn);
                case WAIT -> ActionResult.success("Waiter waits");
            };
            if (result.accepted()) {
                waiterAccepted++;
            }
            return result;
        }

        @Override
        public ActionResult executeChef(ChefAction action, Restaurant r, int turn) {
            events.add("chef:" + turn);
            chefCalls++;
            return action.type() == ChefAction.Type.WAIT ? ActionResult.success("Chef waits")
                    : r.advancePreparation(action.orderId(), turn);
        }

        @Override
        public ActionResult processPayment(Restaurant r, int turn) {
            events.add("cashier:" + turn);
            cashierCalls++;
            OptionalInt oldest = r.findOldestPayableCustomer(turn);
            return oldest.isPresent() ? r.settlePayment(oldest.getAsInt(), turn)
                    : ActionResult.success("No eligible payment");
        }
    }

    /** Deterministic order substitute selected by the default isolated core run. */
    private static final class FakeOrder implements OrderPort {
        private final int id;
        private final int customerId;
        private final MenuItem item;
        private final int bill;
        private int prepared;
        private Status status = Status.PLACED;
        private Integer readyTurn;
        private Integer servedTurn;
        private Integer paidTurn;
        private Integer satisfactionAtPayment;

        FakeOrder(int id, Customer customer, MenuItem item, int bill) {
            this.id = id;
            this.customerId = customer.getId();
            this.item = item;
            this.bill = bill;
        }

        @Override public int getId() { return id; }
        @Override public int getCustomerId() { return customerId; }
        @Override public MenuItem getItem() { return item; }
        @Override public int getBillCents() { return bill; }
        @Override public int getPreparedUnits() { return prepared; }
        @Override public Status getStatus() { return status; }
        @Override public Integer getReadyTurn() { return readyTurn; }
        @Override public Integer getServedTurn() { return servedTurn; }
        @Override public Integer getPaidTurn() { return paidTurn; }
        @Override public Integer getSatisfactionAtPayment() { return satisfactionAtPayment; }

        @Override
        public void advanceOneUnit(int turn) {
            if (status != Status.PLACED && status != Status.PREPARING) {
                throw new IllegalStateException("Order cannot be prepared");
            }
            prepared++;
            status = prepared == item.prepUnits() ? Status.READY : Status.PREPARING;
            if (status == Status.READY) {
                readyTurn = turn;
            }
        }

        @Override
        public void markServed(int turn) {
            if (status != Status.READY || readyTurn == null || readyTurn >= turn) {
                throw new IllegalStateException("Order cannot be served");
            }
            status = Status.SERVED;
            servedTurn = turn;
        }

        @Override
        public void markPaid(int turn, int satisfaction) {
            if (status != Status.SERVED) {
                throw new IllegalStateException("Order cannot be paid");
            }
            status = Status.PAID;
            paidTurn = turn;
            satisfactionAtPayment = satisfaction;
        }

        @Override
        public void cancel() {
            if (status == Status.SERVED || status == Status.PAID || status == Status.CANCELLED) {
                throw new IllegalStateException("Order cannot be cancelled");
            }
            status = Status.CANCELLED;
        }
    }
}
