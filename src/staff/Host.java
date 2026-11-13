package staff;

import contracts.ActionResult;
import contracts.HostAction;
import domain.Restaurant;

/** An independent seating allowance; rejected actions leave the allowance available. */
public final class Host {
    private int lastActionTurn;

    public ActionResult execute(HostAction action, Restaurant restaurant, int turn) {
        if (!restaurant.isActionTurnOpen(turn)) {
            return ActionResult.rejected("Host seating requires the current action turn");
        }
        if (turn <= lastActionTurn) {
            return ActionResult.rejected("The Host has already acted this turn");
        }
        if (action == null || action.type() == null) {
            return ActionResult.rejected("Choose a valid Host action");
        }
        ActionResult result = switch (action.type()) {
            case SEAT -> restaurant.seatCustomer(action.customerId(), action.tableId());
            case WAIT -> ActionResult.success("Host waits this turn");
        };
        if (result.accepted()) {
            lastActionTurn = turn;
        }
        return result;
    }
}
