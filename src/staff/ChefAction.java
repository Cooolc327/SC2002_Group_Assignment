public class ChefAction {
    public enum Type { PREPARE, SKIP }
    private final Type type;
    private final int orderId;
    private ChefAction(Type type, int orderId) { this.type = type; this.orderId = orderId; }
    public static ChefAction prepare(int orderId) { return new ChefAction(Type.PREPARE, orderId); }
    public static ChefAction skip()               { return new ChefAction(Type.SKIP, 0); }
    public Type getType() { return type; }
    public int getOrderId() { return orderId; }
}
