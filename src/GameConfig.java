import java.util.Set;

public final class GameConfig{
    //Game configuration constants
    public static final int MAX_TURNS=18;
    public static final int TABLE_COUNT=2;
    public static final Set<Integer> ARRIVAL_TURNS = Set.of(1, 4, 7, 10, 13, 16);

    //Minimum requirements to win the game
    public static final int MIN_PAID_CUSTOMERS = 4;
    public static final int MIN_REVENUE_CENTS = 4500;
    public static final int MIN_AVG_SATISFACTION = 60;
}