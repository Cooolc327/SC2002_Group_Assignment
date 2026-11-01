package control;

import config.GameConfig;
import view.GameSummary;

public enum GameResult {
    IN_PROGRESS, VICTORY, DEFEAT;

    public static GameResult evaluate(GameSummary summary) {
        if (summary.turnsUsed() < GameConfig.MAX_TURNS) {
            return IN_PROGRESS;
        }
        boolean won = summary.paidCount() >= GameConfig.MIN_PAID_CUSTOMERS
                && summary.revenueCents() >= GameConfig.MIN_REVENUE_CENTS
                && (long) summary.paidSatisfactionSum()
                   >= (long) GameConfig.MIN_AVG_SATISFACTION * summary.paidCount();
        return won ? VICTORY : DEFEAT;
    }
}
