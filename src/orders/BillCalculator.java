package orders;

import entities.Customer;
import fooditems.MenuItem;

// Works out a customer's bill in integer cents
public final class BillCalculator {
    private static final int PERCENT_BASE = 100;

    private BillCalculator() {
    }

    //-------------------------------Billing-----------------------------------

    //Menu price less the customer's own discount
    public static int billFor(Customer customer, MenuItem item) {
        return applyDiscount(item.getPriceCents(), customer.getBillDiscountPercent());
    }

    //Take a percentage off an amount, rounded to the nearest cent (half a cent rounds up)
    public static int applyDiscount(int cents, int discountPercent) {
        if (cents < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + cents);
        }
        if (discountPercent < 0 || discountPercent > PERCENT_BASE) {
            throw new IllegalArgumentException("Discount must be 0 to 100 percent: " + discountPercent);
        }
        return (int) (((long) cents * (PERCENT_BASE - discountPercent)
                + PERCENT_BASE / 2) / PERCENT_BASE);
    }
}
