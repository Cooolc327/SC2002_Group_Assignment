package contracts;

/** Food is chosen by the customer, so TAKE_ORDER has no menu-item parameter. */
public record WaiterAction(Type type, int customerId, int tableId, int orderId) {
    public enum Type { SEAT, TAKE_ORDER, SERVE, WAIT }

    public static WaiterAction seat(int customerId, int tableId) {
        return new WaiterAction(Type.SEAT, customerId, tableId, 0);
    }

    public static WaiterAction takeOrder(int customerId) {
        return new WaiterAction(Type.TAKE_ORDER, customerId, 0, 0);
    }

    public static WaiterAction serve(int orderId) {
        return new WaiterAction(Type.SERVE, 0, 0, orderId);
    }

    public static WaiterAction waitTurn() {
        return new WaiterAction(Type.WAIT, 0, 0, 0);
    }
}
