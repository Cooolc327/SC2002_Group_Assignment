package unit;

import contracts.ActionResult;
import domain.Restaurant;
import entities.Customer;
import entities.RegularCustomer;
import fooditems.Menu;
import fooditems.MenuItem;
import java.util.List;
import orders.BillCalculator;
import orders.Order;
import view.GameView.OrderView;

// Price calculation tests for BillCalculator
public final class BillCalculatorTest {
    private static int scenarios;
    private static int assertions;

    private BillCalculatorTest() {
    }

    public static void main(String[] args) {
        run("customer without a discount pays the menu price", BillCalculatorTest::fullPrice);
        run("10 percent customer discount on every menu item", BillCalculatorTest::customerDiscount);
        run("discounts round to the nearest cent", BillCalculatorTest::rounding);
        run("invalid amounts and discounts are rejected", BillCalculatorTest::invalidInput);
        run("taking an order locks the discounted bill", BillCalculatorTest::orderLocksBill);
        System.out.println("BILL_CALCULATOR=PASS scenarios=" + scenarios + " assertions=" + assertions);
    }

    //-------------------------------Scenarios-----------------------------------

    private static void fullPrice() {
        List<MenuItem> items = new Menu().getItems();
        Customer regular = new RegularCustomer(1);
        eq(0, regular.getBillDiscountPercent(), "regular discount");
        eq("Salad", items.get(0).getName(), "first menu item");
        eq(900, BillCalculator.billFor(regular, items.get(0)), "Salad bill");
        eq("Burger", items.get(1).getName(), "second menu item");
        eq(1500, BillCalculator.billFor(regular, items.get(1)), "Burger bill");
        eq("Pasta", items.get(2).getName(), "third menu item");
        eq(1800, BillCalculator.billFor(regular, items.get(2)), "Pasta bill");
    }

    private static void customerDiscount() {
        List<MenuItem> items = new Menu().getItems();
        Customer discounted = new DiscountedCustomer(1);
        eq(810, BillCalculator.billFor(discounted, items.get(0)), "Salad 10% off");
        eq(1350, BillCalculator.billFor(discounted, items.get(1)), "Burger 10% off");
        eq(1620, BillCalculator.billFor(discounted, items.get(2)), "Pasta 10% off");
        eq(1500, items.get(1).getPriceCents(), "menu price unchanged by billing");
    }

    private static void rounding() {
        eq(900, BillCalculator.applyDiscount(900, 0), "no discount");
        eq(0, BillCalculator.applyDiscount(900, 100), "full discount");
        eq(0, BillCalculator.applyDiscount(0, 10), "zero amount");
        eq(5, BillCalculator.applyDiscount(6, 10), "5.4 cents rounds down");
        eq(5, BillCalculator.applyDiscount(5, 10), "4.5 cents rounds up");
        eq(14, BillCalculator.applyDiscount(15, 10), "13.5 cents rounds up");
        eq(10, BillCalculator.applyDiscount(11, 10), "9.9 cents rounds up");
    }

    private static void invalidInput() {
        throwsType(IllegalArgumentException.class, () -> BillCalculator.applyDiscount(-1, 10));
        throwsType(IllegalArgumentException.class, () -> BillCalculator.applyDiscount(900, -1));
        throwsType(IllegalArgumentException.class, () -> BillCalculator.applyDiscount(900, 101));
    }

    //Customer 1 chooses Salad, so a 10% discount gives an $8.10 bill on the real Order
    private static void orderLocksBill() {
        Restaurant restaurant = new Restaurant(Order::new);
        restaurant.beginTurn(1);
        restaurant.registerArrival(new DiscountedCustomer(1));
        check(restaurant.seatCustomer(1, 1).accepted(), "seat customer 1");
        ActionResult ordered = restaurant.placeOrder(1);
        check(ordered.accepted(), ordered.message());
        check(ordered.message().contains("Salad") && ordered.message().contains("$8.10"),
                "order message shows the bill: " + ordered.message());
        OrderView order = restaurant.getOrderViews().get(0);
        eq(810, order.billCents(), "bill stored on the order");
    }

    //-------------------------------Helpers-----------------------------------

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void eq(Object expected, Object actual, String message) {
        check(java.util.Objects.equals(expected, actual),
                message + ": expected " + expected + ", got " + actual);
    }

    private static void throwsType(Class<? extends Throwable> type, Runnable operation) {
        assertions++;
        try {
            operation.run();
        } catch (Throwable error) {
            if (type.isInstance(error)) {
                return;
            }
            throw new AssertionError("Expected " + type.getSimpleName(), error);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static void run(String name, Runnable scenario) {
        scenario.run();
        scenarios++;
        System.out.println("PASS " + name);
    }

    //Test-only customer with a 10% discount, standing in until VIPCustomer exists
    private static final class DiscountedCustomer extends Customer {
        DiscountedCustomer(int id) {
            super(id);
        }

        @Override
        protected int getSatistfactionLossPerTurn() {
            return 0;
        }

        @Override
        public String getTypeName() {
            return "DiscountTest";
        }

        @Override
        public int getBillDiscountPercent() {
            return 10;
        }
    }
}
