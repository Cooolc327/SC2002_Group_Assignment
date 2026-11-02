package staff;
public class WaiterAction {
    public enum Type { SEAT, TAKE_ORDER, SERVE, SKIP }
    private final Type type;
    private final int targetId;   // customer id, or order id for SERVE
    private final int tableId;
    private WaiterAction(Type type, int targetId, int tableId) {
        this.type = type; this.targetId = targetId; this.tableId = tableId;
    }
    public static WaiterAction seat(int customerId, int tableId) { return new WaiterAction(Type.SEAT, customerId, tableId); }
    public static WaiterAction takeOrder(int customerId)         { return new WaiterAction(Type.TAKE_ORDER, customerId, 0); }
    public static WaiterAction serve(int orderId)                { return new WaiterAction(Type.SERVE, orderId, 0); }
    public static WaiterAction skip()                            { return new WaiterAction(Type.SKIP, 0, 0); }
    public Type getType() { return type; }
    public int getTargetId() { return targetId; }
    public int getTableId() { return tableId; }
}
