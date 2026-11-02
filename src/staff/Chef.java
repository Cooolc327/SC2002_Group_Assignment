package staff;
public class Chef {
    private int lastActionTurn = 0;

    public ActionResult execute(ChefAction action, Restaurant restaurant, int turn) {
        if (lastActionTurn == turn) {
            return ActionResult.fail("The Chef has already acted this turn.");
        }
        if (action.getType() == ChefAction.Type.SKIP) {
            lastActionTurn = turn;
            return ActionResult.ok("Chef waits this turn.");
        }
        ActionResult result = restaurant.advancePreparation(action.getOrderId(), turn);
        if (result.isSuccess()) {
            lastActionTurn = turn;
        }
        return result;
    }
    public int getLastActionTurn() { return lastActionTurn; }
}
