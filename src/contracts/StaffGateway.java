package contracts;

import domain.Restaurant;

/**
 * Member D adapts Host, Waiter, Chef and Cashier here. Staff enforce their own per-turn
 * allowances and call Restaurant for coordinated domain changes.
 * A failed action must not consume staff capacity.
 */
public interface StaffGateway {
    /** Older Stage 1/2 test staff need not provide a Host. Production Stage 3 staff override this. */
    default ActionResult executeHost(HostAction action, Restaurant restaurant, int turn) {
        return ActionResult.rejected("This staff team does not provide a Host");
    }

    ActionResult executeWaiter(WaiterAction action, Restaurant restaurant, int turn);
    ActionResult executeChef(ChefAction action, Restaurant restaurant, int turn);
    ActionResult processPayment(Restaurant restaurant, int turn);
}
