package staff;

import contracts.ActionResult;
import contracts.ChefAction;
import domain.Restaurant;

public class Chef {
    private int lastActionTurn = 0;

    public ActionResult execute(ChefAction action, Restaurant restaurant, int turn) {
        if (lastActionTurn == turn) {
            return ActionResult.rejected("The Chef has already acted this turn.");
        }
        ActionResult result = action.type() == ChefAction.Type.WAIT
                ? ActionResult.success("Chef waits this turn.")
                : restaurant.advancePreparation(action.orderId(), turn);
        if (result.accepted()) {
            lastActionTurn = turn;   // failed choices don't use up the task
        }
        return result;
    }
    public int getLastActionTurn() { return lastActionTurn; }
}
