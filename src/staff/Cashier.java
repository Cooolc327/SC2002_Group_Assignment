package staff;

import entities.Customer;

public class Cashier {
    private int lastActionTurn = 0;

    public ActionResult processPayment(Restaurant restaurant, int turn) {
        if (lastActionTurn == turn) {
            return ActionResult.fail("The Cashier has already collected a payment this turn.");
        }
        Customer next = restaurant.nextCustomerReadyToPay();
        if (next == null) {
            return ActionResult.ok("Cashier: no customer is ready to pay.");
        }
        ActionResult result = restaurant.settlePayment(next.getId(), turn);
        if (result.isSuccess()) {
            lastActionTurn = turn;
        }
        return result;
    }
    public int getLastActionTurn() { return lastActionTurn; }
}
