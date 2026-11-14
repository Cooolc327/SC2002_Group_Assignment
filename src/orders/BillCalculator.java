package orders;

import entities.Customer;
import fooditems.Discount;
import fooditems.MenuItem;

// Works out a customer's bill in integer cents
public final class BillCalculator {
    public static final int HAPPY_HOUR_DISCOUNT_PERCENT = 20;

    private BillCalculator() {
    }

    //-------------------------------Billing-----------------------------------

    //Menu price less the customer's own discount, outside Happy Hour
    public static int billFor(Customer customer, MenuItem item) {
        return billFor(customer, item, false);
    }

    //Customer discount first, then Happy Hour. Each step rounds to the nearest cent
    public static int billFor(Customer customer, MenuItem item, boolean happyHourActive) {
        int billCents = Discount.apply(item.getPriceCents(), customer.getBillDiscountPercent());
        if (happyHourActive) {
            billCents = Discount.apply(billCents, HAPPY_HOUR_DISCOUNT_PERCENT);
        }
        return billCents;
    }
}
