package contracts;

public record ChefAction(Type type, int orderId) {
    public enum Type { PREPARE, WAIT }

    public static ChefAction prepare(int orderId) {
        return new ChefAction(Type.PREPARE, orderId);
    }

    public static ChefAction waitTurn() {
        return new ChefAction(Type.WAIT, 0);
    }
}
