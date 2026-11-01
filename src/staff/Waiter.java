public class Waiter {
    private int lastActionTurn = 0;

    public ActionResult execute(WaiterAction action, Restaurant restaurant, int turn) {
        if (action.getType() == WaiterAction.Type.SKIP) {
            return ActionResult.ok("Waiter waits this turn.");
        }
        if (lastActionTurn == turn) {
            return ActionResult.fail("The Waiter has already completed a task this turn.");
        }
        ActionResult result;
        switch (action.getType()) {
            case SEAT:       result = restaurant.seatCustomer(action.getTargetId(), action.getTableId()); break;
            case TAKE_ORDER: result = restaurant.placeOrder(action.getTargetId()); break;
            case SERVE:      result = restaurant.serveOrder(action.getTargetId(), turn); break;
            default:         result = ActionResult.fail("Unknown Waiter task.");
        }
        if (result.isSuccess()) {
            lastActionTurn = turn;   // failed choices don't use up the task
        }
        return result;
    }
    public int getLastActionTurn() { return lastActionTurn; }
}
