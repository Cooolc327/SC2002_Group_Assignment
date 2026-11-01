package view;

import java.util.OptionalDouble;

/** All amounts are integer cents; the average is based only on payment records. */
public record GameSummary(int servedCount, int paidCount, int unhappyDepartures,
                          long revenueCents, int paidSatisfactionSum, int turnsUsed) {
    public OptionalDouble averagePaidSatisfaction() {
        return paidCount == 0 ? OptionalDouble.empty()
                : OptionalDouble.of((double) paidSatisfactionSum / paidCount);
    }
}
