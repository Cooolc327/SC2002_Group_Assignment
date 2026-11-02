package staff;

import contracts.ActionResult;
import contracts.ChefAction;
import contracts.StaffGateway;
import contracts.WaiterAction;
import domain.Restaurant;

// Base-setting staff: one Waiter, one Chef and one Cashier, each enforcing its own per-turn allowance
public class StaffTeam implements StaffGateway {
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();

    @Override
    public ActionResult executeWaiter(WaiterAction action, Restaurant restaurant, int turn) {
        return waiter.execute(action, restaurant, turn);
    }

    @Override
    public ActionResult executeChef(ChefAction action, Restaurant restaurant, int turn) {
        return chef.execute(action, restaurant, turn);
    }

    @Override
    public ActionResult processPayment(Restaurant restaurant, int turn) {
        return cashier.processPayment(restaurant, turn);
    }
}
