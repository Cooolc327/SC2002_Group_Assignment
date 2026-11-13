package domain;

/** Activation is permanent; remaining time is derived from the turn, never from UI reads. */
public final class HappyHourPolicy {
    public static final int DURATION_TURNS = 2;
    public static final int SATISFACTION_RECOVERY = 15;
    private Integer activationTurn;

    public boolean hasBeenUsed() {
        return activationTurn != null;
    }

    public void activate(int turn) {
        if (turn < 1 || hasBeenUsed()) {
            throw new IllegalStateException("Happy Hour can be activated once in an active turn");
        }
        activationTurn = turn;
    }

    public int remainingTurns(int turn) {
        if (activationTurn == null || turn < activationTurn) {
            return 0;
        }
        return Math.max(0, DURATION_TURNS - (turn - activationTurn));
    }

    public boolean isActive(int turn) {
        return remainingTurns(turn) > 0;
    }
}
