package contracts;

import domain.Restaurant;

/**
 * Member D adapts Waiter, Chef and Cashier here. Staff enforce their own per-turn
 * allowances and call Restaurant for coordinated domain changes.
 * A failed action must not consume staff capacity.
 */
public interface StaffGateway {
    ActionResult executeWaiter(WaiterAction action, Restaurant restaurant, int turn);
    ActionResult executeChef(ChefAction action, Restaurant restaurant, int turn);
    ActionResult processPayment(Restaurant restaurant, int turn);
}
