package staff;

import contracts.ActionResult;
import contracts.ChefAction;
import contracts.HostAction;
import contracts.StaffGateway;
import contracts.WaiterAction;
import domain.Restaurant;

// Production staff: Host, Waiter, Chef and Cashier each enforce their own per-turn allowance.
public class StaffTeam implements StaffGateway {
    private final Host host = new Host();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();

    @Override
    public ActionResult executeHost(HostAction action, Restaurant restaurant, int turn) {
        return host.execute(action, restaurant, turn);
    }

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
