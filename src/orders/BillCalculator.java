package orders;

import entities.Customer;
import fooditems.Discount;
import fooditems.MenuItem;

// Works out a customer's bill in integer cents
public final class BillCalculator {
    private BillCalculator() {
    }

    //-------------------------------Billing-----------------------------------

    //Menu price less the customer's own discount
    public static int billFor(Customer customer, MenuItem item) {
        return Discount.apply(item.getPriceCents(), customer.getBillDiscountPercent());
    }
}
