package fooditems;

// Percentage discounts on whole cents, shared by combo prices and customer bills
public final class Discount {
    private static final int PERCENT_BASE = 100;

    private Discount() {
    }

    //Take a percentage off an amount, rounded to the nearest cent (half a cent rounds up)
    public static int apply(int cents, int percent) {
        if (cents < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + cents);
        }
        if (percent < 0 || percent > PERCENT_BASE) {
            throw new IllegalArgumentException("Discount must be 0 to 100 percent: " + percent);
        }
        return (int) (((long) cents * (PERCENT_BASE - percent)
                + PERCENT_BASE / 2) / PERCENT_BASE);
    }
}
