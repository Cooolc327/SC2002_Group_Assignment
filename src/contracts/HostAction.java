package contracts;

/** The Host may seat one queued customer or wait; food remains the customer's choice. */
public record HostAction(Type type, int customerId, int tableId) {
    public enum Type { SEAT, WAIT }

    public static HostAction seat(int customerId, int tableId) {
        return new HostAction(Type.SEAT, customerId, tableId);
    }

    public static HostAction waitTurn() {
        return new HostAction(Type.WAIT, 0, 0);
    }
}
