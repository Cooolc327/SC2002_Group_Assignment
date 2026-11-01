package control;

import config.GameConfig;
import contracts.ActionResult;
import contracts.ChefAction;
import contracts.StaffGateway;
import contracts.WaiterAction;
import domain.Restaurant;
import entities.RegularCustomer;
import java.util.List;
import java.util.Objects;
import view.GameView;
import view.GameView.StaffView;

/** Stage 1 turn coordinator, independent of a console UI or production staff classes. */
public final class GameController {
    private final Restaurant restaurant;
    private final StaffGateway staff;
    private int currentTurn;
    private int nextCustomerId = 1;
    private TurnPhase phase = TurnPhase.NOT_STARTED;
    private GameResult result = GameResult.IN_PROGRESS;
    private ActionResult lastPaymentResult = ActionResult.success("No payment processed yet");

    public GameController(Restaurant restaurant, StaffGateway staff) {
        this.restaurant = Objects.requireNonNull(restaurant, "restaurant");
        this.staff = Objects.requireNonNull(staff, "staff");
    }

    public GameView startTurn() {
        if (phase != TurnPhase.NOT_STARTED && phase != TurnPhase.TURN_ENDED) {
            throw new IllegalStateException("The previous turn must end before starting another");
        }
        restaurant.beginTurn(currentTurn + 1);
        currentTurn++;
        if (GameConfig.ARRIVAL_TURNS.contains(currentTurn)) {
            restaurant.registerArrival(new RegularCustomer(nextCustomerId++));
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
