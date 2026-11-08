package control;

import config.GameConfig;
import contracts.ActionResult;
import contracts.ChefAction;
import contracts.StaffGateway;
import contracts.WaiterAction;
import domain.Restaurant;
import entities.Customer;
import entities.CustomerState;
import entities.RegularCustomer;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import view.GameView;
import view.GameView.StaffView;

/** Shared turn coordinator; customer behaviour is supplied by the customer objects. */
public final class GameController {
    private final Restaurant restaurant;
    private final StaffGateway staff;
    private final IntFunction<? extends Customer> customerFactory;
    private int currentTurn;
    private int nextCustomerId = 1;
    private TurnPhase phase = TurnPhase.NOT_STARTED;
    private GameResult result = GameResult.IN_PROGRESS;
    private ActionResult lastPaymentResult = ActionResult.success("No payment processed yet");

    public GameController(Restaurant restaurant, StaffGateway staff) {
        this(restaurant, staff, RegularCustomer::new);
    }

    public GameController(Restaurant restaurant, StaffGateway staff,
                          IntFunction<? extends Customer> customerFactory) {
        this.restaurant = Objects.requireNonNull(restaurant, "restaurant");
        this.staff = Objects.requireNonNull(staff, "staff");
        this.customerFactory = Objects.requireNonNull(customerFactory, "customerFactory");
    }

    public GameView startTurn() {
        if (phase != TurnPhase.NOT_STARTED && phase != TurnPhase.TURN_ENDED) {
            throw new IllegalStateException("The previous turn must end before starting another");
        }
        int nextTurn = currentTurn + 1;
        Customer arrival = null;
        if (GameConfig.ARRIVAL_TURNS.contains(nextTurn)) {
            arrival = Objects.requireNonNull(customerFactory.apply(nextCustomerId), "arrival");
            if (arrival.getId() != nextCustomerId || arrival.getState() != CustomerState.WAITING
                    || arrival.getSatisfaction() != Customer.MAX_SATISFACTION) {
                throw new IllegalArgumentException("Factory must return the requested new customer at 100");
            }
        }
        restaurant.beginTurn(nextTurn);
        currentTurn = nextTurn;
        if (arrival != null) {
            restaurant.registerArrival(arrival);
            nextCustomerId++;
        }
        phase = TurnPhase.AWAIT_WAITER;
        return getView();
    }

    public ActionResult submitWaiterAction(WaiterAction action) {
        if (phase != TurnPhase.AWAIT_WAITER) {
            return ActionResult.rejected("The game is not awaiting a Waiter action");
        }
        if (action == null || action.type() == null) {
            return ActionResult.rejected("Choose a valid Waiter action");
        }
        ActionResult outcome = Objects.requireNonNull(
                staff.executeWaiter(action, restaurant, currentTurn), "Waiter result");
        if (outcome.accepted()) {
            phase = TurnPhase.AWAIT_CHEF;
        }
        return outcome;
    }

    public ActionResult submitChefAction(ChefAction action) {
        if (phase != TurnPhase.AWAIT_CHEF) {
            return ActionResult.rejected("The game is not awaiting a Chef action");
        }
        if (action == null || action.type() == null) {
            return ActionResult.rejected("Choose a valid Chef action");
        }
        ActionResult outcome = Objects.requireNonNull(
                staff.executeChef(action, restaurant, currentTurn), "Chef result");
        if (outcome.accepted()) {
            phase = TurnPhase.AUTO;
        }
        return outcome;
    }

    public GameView finishTurn() {
        if (phase != TurnPhase.AUTO) {
            throw new IllegalStateException("Both staff choices must complete before ending the turn");
        }
        restaurant.advanceEating(currentTurn);
        lastPaymentResult = Objects.requireNonNull(
                staff.processPayment(restaurant, currentTurn), "Cashier result");
        restaurant.applyWaitingPenalty(currentTurn);
        restaurant.completeTurn(currentTurn);
        result = GameResult.evaluate(restaurant.getSummary());
        phase = currentTurn == GameConfig.MAX_TURNS ? TurnPhase.FINISHED : TurnPhase.TURN_ENDED;
        return getView();
    }

    public GameView getView() {
        boolean waiterAvailable = phase == TurnPhase.AWAIT_WAITER;
        boolean chefAvailable = phase == TurnPhase.AWAIT_CHEF;
        StaffView staffView = new StaffView(waiterAvailable, chefAvailable,
                phase == TurnPhase.AUTO,
                waiterAvailable ? restaurant.getWaiterOptions(currentTurn) : List.of(),
                chefAvailable ? restaurant.getChefOptions() : List.of());
        return new GameView(currentTurn, phase, restaurant.getCustomerViews(),
                restaurant.getWaitingCustomerIds(), restaurant.getTableViews(),
                restaurant.getOrderViews(), restaurant.getMenuViews(), staffView,
                restaurant.getSummary(), result);
    }

    public ActionResult getLastPaymentResult() {
        return lastPaymentResult;
    }
}
