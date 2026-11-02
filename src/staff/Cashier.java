package staff;

import contracts.ActionResult;
import domain.Restaurant;
import java.util.OptionalInt;

public class Cashier {
    private int lastActionTurn = 0;

    // Automatic step: collects from the oldest READY_TO_PAY customer, at most once per turn
    public ActionResult processPayment(Restaurant restaurant, int turn) {
        if (lastActionTurn == turn) {
            return ActionResult.rejected("The Cashier has already collected a payment this turn.");
        }
        OptionalInt next = restaurant.findOldestPayableCustomer(turn);
        if (next.isEmpty()) {
            return ActionResult.success("Cashier: no customer is ready to pay.");
        }
        ActionResult result = restaurant.settlePayment(next.getAsInt(), turn);
        if (result.accepted()) {
            lastActionTurn = turn;
        }
        return result;
    }
    public int getLastActionTurn() { return lastActionTurn; }
}
