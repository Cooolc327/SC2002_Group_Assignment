package domain;

import config.GameConfig;
import contracts.ActionResult;
import contracts.OrderPort;
import contracts.ChefAction;
import contracts.HostAction;
import contracts.WaiterAction;
import entities.Customer;
import entities.CustomerState;
import entities.SeatingArea;
import entities.Table;
import fooditems.Menu;
import fooditems.MenuItem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import orders.BillCalculator;
import view.GameSummary;
import view.GameView.CustomerView;
import view.GameView.MenuItemView;
import view.GameView.OrderView;
import view.GameView.TableView;

/** Coordinates cross-entity changes; staff allowances belong to member D. */
public final class Restaurant {
    private final boolean stage3Enabled;
    private final HappyHourPolicy happyHour = new HappyHourPolicy();
    private final Menu menu;
    private final SeatingArea seatingArea;
    private final OrderPort.Factory orderFactory;
    private final Map<Integer, Customer> customers = new LinkedHashMap<>();
    private final Map<Integer, OrderPort> orders = new LinkedHashMap<>();
    private int currentTurn;
    private int completedTurns;
    private int lastPenaltyTurn;
    private int lastPaymentTurn;
    private int nextOrderId = 1;
    private int lastTurnStartPolicyTurn;

    public Restaurant(OrderPort.Factory orderFactory) {
        this(orderFactory, false);
    }

    public Restaurant(OrderPort.Factory orderFactory, boolean stage3Enabled) {
        this.stage3Enabled = stage3Enabled;
        this.menu = new Menu(stage3Enabled);
        this.seatingArea = new SeatingArea(GameConfig.TABLE_COUNT);
        this.orderFactory = Objects.requireNonNull(orderFactory, "orderFactory");
    }

    public void beginTurn(int turn) {
        if (turn != currentTurn + 1 || currentTurn != completedTurns
                || turn > GameConfig.MAX_TURNS) {
            throw new IllegalStateException("Complete the previous turn before starting the next");
        }
        currentTurn = turn;
    }

    public void registerArrival(Customer customer) {
        requireOpenTurn(currentTurn);
        Objects.requireNonNull(customer, "customer");
        if (customer.getId() < 1 || customers.containsKey(customer.getId())
                || customer.getState() != CustomerState.WAITING
                || customer.getSatisfaction() != Customer.MAX_SATISFACTION) {
            throw new IllegalArgumentException("Arrival must be a new customer at satisfaction 100");
        }
        customers.put(customer.getId(), customer);
        seatingArea.addToQueue(customer);
    }

    public ActionResult seatCustomer(int customerId, int tableId) {
        if (!isActionTurnOpen(currentTurn)) {
            return ActionResult.rejected("No active turn");
        }
        Customer customer = customers.get(customerId);
        if (customer == null || customer.getState() != CustomerState.WAITING
                || !seatingArea.isQueued(customer)) {
            return ActionResult.rejected("Customer " + customerId + " is not waiting in the queue");
        }
        Table table = seatingArea.getTables().stream()
                .filter(t -> t.getId() == tableId).findFirst().orElse(null);
        if (table == null || !table.isFree()) {
            return ActionResult.rejected("Table " + tableId + " does not exist or is occupied");
        }
        seatingArea.seat(customer, tableId);
        return ActionResult.success("Seated customer " + customerId + " at table " + tableId);
    }

    public ActionResult placeOrder(int customerId) {
        if (!isActionTurnOpen(currentTurn)) {
            return ActionResult.rejected("No active turn");
        }
        Customer customer = customers.get(customerId);
        if (customer == null || customer.getState() != CustomerState.SEATED
                || !hasTable(customer) || orderFor(customerId) != null) {
            return ActionResult.rejected("Customer " + customerId + " must be seated without an order");
        }
        MenuItem item = customer.chooseItem(menu);
        if (!menu.getOrderableItems().contains(item)) {
            throw new IllegalStateException("Customer chose an item outside the menu");
        }
        // The bill includes the customer's discount and is fixed when ordering.
        int billCents = BillCalculator.billFor(customer, item,
                stage3Enabled && happyHour.isActive(currentTurn));
        OrderPort order = orderFactory.create(nextOrderId, customer, item, billCents);
        validateNewOrder(order, customerId, item, billCents);
        customer.placeOrder();
        orders.put(nextOrderId++, order);
        return ActionResult.success("Customer " + customerId + " ordered " + item.getName()
                + " (order " + order.getId() + ", " + money(billCents) + ")");
    }

    public ActionResult advancePreparation(int orderId, int turn) {
        if (!isActionTurnOpen(turn)) {
            return ActionResult.rejected("Preparation requires the current active turn");
        }
        OrderPort order = orders.get(orderId);
        if (order == null || (order.getStatus() != OrderPort.Status.PLACED
                && order.getStatus() != OrderPort.Status.PREPARING)) {
            return ActionResult.rejected("Order " + orderId + " cannot be prepared");
        }
        order.advanceOneUnit(turn);
        return ActionResult.success("Advanced order " + orderId + " by one preparation unit");
    }

    public ActionResult serveOrder(int orderId, int turn) {
        if (!isActionTurnOpen(turn)) {
            return ActionResult.rejected("Serving requires the current active turn");
        }
        OrderPort order = orders.get(orderId);
        if (order == null || order.getStatus() != OrderPort.Status.READY) {
            return ActionResult.rejected("Order " + orderId + " is not READY");
        }
        if (order.getReadyTurn() == null || order.getReadyTurn() >= turn) {
            return ActionResult.rejected("A dish completed this turn can be served next turn");
        }
        Customer customer = customers.get(order.getCustomerId());
        if (customer == null || customer.getState() != CustomerState.ORDERED || !hasTable(customer)) {
            return ActionResult.rejected("The customer is not seated waiting for this order");
        }
        order.markServed(turn);
        customer.serve(turn);
        return ActionResult.success("Served order " + orderId);
    }

    public void advanceEating(int turn) {
        requireOpenTurn(turn);
        for (Customer customer : customers.values()) {
            customer.finishedEating(turn);
        }
    }

    /** Only customers ready on an earlier turn qualify; ties use customer id. */
    public OptionalInt findOldestPayableCustomer(int turn) {
        if (!isOpenTurn(turn) || lastPaymentTurn == turn) {
            return OptionalInt.empty();
        }
        return customers.values().stream()
                .filter(c -> c.getState() == CustomerState.READY_TO_PAY
                        && c.getReadyToPayTurn() < turn)
                .min(Comparator.comparingInt(Customer::getReadyToPayTurn)
                        .thenComparingInt(Customer::getId))
                .map(c -> OptionalInt.of(c.getId())).orElseGet(OptionalInt::empty);
    }

    public ActionResult settlePayment(int customerId, int turn) {
        if (!isOpenTurn(turn)) {
            return ActionResult.rejected("Payment requires the current active turn");
        }
        OptionalInt oldest = findOldestPayableCustomer(turn);
        if (oldest.isEmpty() || oldest.getAsInt() != customerId) {
            return ActionResult.rejected("Only the oldest eligible customer may pay, once per turn");
        }
        Customer customer = customers.get(customerId);
        OrderPort order = orderFor(customerId);
        if (order == null || order.getStatus() != OrderPort.Status.SERVED || !hasTable(customer)) {
            return ActionResult.rejected("Payment requires a served order and an occupied table");
        }
        order.markPaid(turn, customer.getSatisfaction());
        customer.pay();
        seatingArea.removeCustomer(customer);
        lastPaymentTurn = turn;
        return ActionResult.success("Collected payment from customer " + customerId);
    }

    public void applyWaitingPenalty(int turn) {
        requireOpenTurn(turn);
        for (Customer customer : customers.values()) {
            customer.applyWaitingPenalty();
            if (customer.isOutOfPatience()) {
                OrderPort order = orderFor(customer.getId());
                if (order != null) {
                    order.cancel();
                }
                customer.abandon();
                seatingArea.removeCustomer(customer);
            }
        }
        lastPenaltyTurn = turn;
    }

    public void completeTurn(int turn) {
        if (turn != currentTurn || turn != completedTurns + 1 || lastPenaltyTurn != turn) {
            throw new IllegalStateException("Complete a turn only after its waiting penalty");
        }
        completedTurns = turn;
    }

    public GameSummary getSummary() {
        int served = 0;
        int paid = 0;
        int satisfactionSum = 0;
        long revenue = 0;
        for (OrderPort order : orders.values()) {
            if (order.getServedTurn() != null) {
                served++;
            }
            if (order.getStatus() == OrderPort.Status.PAID) {
                if (order.getSatisfactionAtPayment() == null) {
                    throw new IllegalStateException("A paid order needs its satisfaction snapshot");
                }
                paid++;
                revenue += order.getBillCents();
                satisfactionSum += order.getSatisfactionAtPayment();
            }
        }
        int departures = (int) customers.values().stream()
                .filter(c -> c.getState() == CustomerState.LEFT).count();
        return new GameSummary(served, paid, departures, revenue, satisfactionSum, completedTurns);
    }

    public List<CustomerView> getCustomerViews() {
        return customers.values().stream().map(c -> new CustomerView(c.getId(), c.getTypeName(),
                c.getSatisfaction(), c.getState(),
                c.getReadyToPayTurn() == 0 ? null : c.getReadyToPayTurn())).toList();
    }

    public List<Integer> getWaitingCustomerIds() {
        return seatingArea.getQueuedCustomers().stream().map(Customer::getId).toList();
    }

    public List<TableView> getTableViews() {
        return seatingArea.getTables().stream().map(t -> new TableView(t.getId(),
                t.isFree() ? null : t.getOccupant().getId())).toList();
    }

    public List<OrderView> getOrderViews() {
        return orders.values().stream().map(o -> new OrderView(o.getId(), o.getCustomerId(),
                o.getItem().getName(), o.getBillCents(), o.getPreparedUnits(),
                o.getItem().prepUnits(), o.getStatus(), o.getReadyTurn(), o.getServedTurn(),
                o.getPaidTurn(), o.getSatisfactionAtPayment())).toList();
    }

    public List<MenuItemView> getMenuViews() {
        return menu.getOrderableItems().stream().map(i -> new MenuItemView(i.getName(),
                i.getPriceCents(), i.prepUnits())).toList();
    }

    public List<WaiterAction> getWaiterOptions(int turn) {
        List<WaiterAction> options = new ArrayList<>();
        options.add(WaiterAction.waitTurn());
        for (Customer customer : seatingArea.getQueuedCustomers()) {
            for (Table table : seatingArea.getTables()) {
                if (table.isFree()) {
                    options.add(WaiterAction.seat(customer.getId(), table.getId()));
                }
            }
        }
        for (Customer customer : customers.values()) {
            if (customer.getState() == CustomerState.SEATED && hasTable(customer)) {
                options.add(WaiterAction.takeOrder(customer.getId()));
            }
        }
        for (OrderPort order : orders.values()) {
            if (order.getStatus() == OrderPort.Status.READY && order.getReadyTurn() != null
                    && order.getReadyTurn() < turn) {
                options.add(WaiterAction.serve(order.getId()));
            }
        }
        return List.copyOf(options);
    }

    public List<HostAction> getHostOptions() {
        List<HostAction> options = new ArrayList<>();
        options.add(HostAction.waitTurn());
        for (WaiterAction action : getWaiterOptions(currentTurn)) {
            if (action.type() == WaiterAction.Type.SEAT) {
                options.add(HostAction.seat(action.customerId(), action.tableId()));
            }
        }
        return List.copyOf(options);
    }

    /** Resolve once, before any staff action. A rejected activation performs no recovery. */
    public ActionResult resolveTurnStart(boolean activateHappyHour) {
        if (!stage3Enabled || !isOpenTurn(currentTurn) || lastTurnStartPolicyTurn == currentTurn) {
            return ActionResult.rejected("Resolve Happy Hour only at the start of a Stage 3 turn");
        }
        if (activateHappyHour && happyHour.hasBeenUsed()) {
            return ActionResult.rejected("Happy Hour has already been used this game");
        }
        if (activateHappyHour) {
            happyHour.activate(currentTurn);
        }
        if (happyHour.isActive(currentTurn)) {
            for (Customer customer : customers.values()) {
                customer.recoverWaitingSatisfaction(HappyHourPolicy.SATISFACTION_RECOVERY);
            }
        }
        lastTurnStartPolicyTurn = currentTurn;
        return ActionResult.success(happyHour.isActive(currentTurn)
                ? "Happy Hour active: waiting customers recover 15 satisfaction (maximum 100)"
                : "Turn start complete; Happy Hour is inactive");
    }

    public boolean isStage3Enabled() {
        return stage3Enabled;
    }

    public boolean hasUsedHappyHour() {
        return happyHour.hasBeenUsed();
    }

    public int getHappyHourRemainingTurns() {
        return happyHour.remainingTurns(currentTurn);
    }

    public boolean isActionTurnOpen(int turn) {
        return isOpenTurn(turn) && (!stage3Enabled || lastTurnStartPolicyTurn == turn);
    }

    public List<ChefAction> getChefOptions() {
        List<ChefAction> options = new ArrayList<>();
        options.add(ChefAction.waitTurn());
        for (OrderPort order : orders.values()) {
            if (order.getStatus() == OrderPort.Status.PLACED
                    || order.getStatus() == OrderPort.Status.PREPARING) {
                options.add(ChefAction.prepare(order.getId()));
            }
        }
        return List.copyOf(options);
    }

    private boolean hasTable(Customer customer) {
        return seatingArea.getTables().stream().anyMatch(t -> t.getOccupant() == customer);
    }

    private OrderPort orderFor(int customerId) {
        return orders.values().stream().filter(o -> o.getCustomerId() == customerId)
                .findFirst().orElse(null);
    }

    private static String money(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }

    private boolean isOpenTurn(int turn) {
        return turn > 0 && turn == currentTurn && completedTurns < turn && lastPenaltyTurn != turn;
    }

    private void requireOpenTurn(int turn) {
        if (!isOpenTurn(turn)) {
            throw new IllegalStateException("Operation requires the current unfinished turn");
        }
    }

    private void validateNewOrder(OrderPort order, int customerId, MenuItem item, int billCents) {
        if (order == null || order.getId() != nextOrderId || order.getCustomerId() != customerId
                || order.getItem() != item || order.getBillCents() != billCents
                || order.getStatus() != OrderPort.Status.PLACED || order.getPreparedUnits() != 0
                || order.getReadyTurn() != null || order.getServedTurn() != null
                || order.getPaidTurn() != null || order.getSatisfactionAtPayment() != null) {
            throw new IllegalStateException("Order factory did not return the requested fresh order");
        }
    }
}
