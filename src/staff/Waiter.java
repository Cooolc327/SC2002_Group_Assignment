package staff;

import contracts.ActionResult;
import contracts.WaiterAction;
import domain.Restaurant;

public class Waiter {
    private int lastActionTurn = 0;

    public ActionResult execute(WaiterAction action, Restaurant restaurant, int turn) {
        if (lastActionTurn == turn) {
            return ActionResult.rejected("The Waiter has already acted this turn.");
        }
        ActionResult result;
        switch (action.type()) {
            case SEAT:       result = restaurant.seatCustomer(action.customerId(), action.tableId()); break;
            case TAKE_ORDER: result = restaurant.placeOrder(action.customerId()); break;
            case SERVE:      result = restaurant.serveOrder(action.orderId(), turn); break;
            case WAIT:       result = ActionResult.success("Waiter waits this turn."); break;
            default:         result = ActionResult.rejected("Unknown Waiter task.");
        }
        if (result.accepted()) {
            lastActionTurn = turn;   // failed choices don't use up the task
        }
        return result;
    }
    public int getLastActionTurn() { return lastActionTurn; }
}
