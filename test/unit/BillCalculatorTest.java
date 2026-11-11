package unit;

import contracts.ActionResult;
import domain.Restaurant;
import entities.CriticCustomer;
import entities.Customer;
import entities.RegularCustomer;
import entities.VipCustomer;
import fooditems.ComboMeal;
import fooditems.Discount;
import fooditems.Menu;
import fooditems.MenuItem;
import java.util.ArrayList;
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
        run("Happy Hour takes 20 percent off for every customer type", BillCalculatorTest::happyHour);
        run("VIP discount then Happy Hour on combos", BillCalculatorTest::comboBills);
        run("rounding each step matches rounding once at the end", BillCalculatorTest::roundingOrder);
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
        Customer discounted = new VipCustomer(1);
        eq(810, BillCalculator.billFor(discounted, items.get(0)), "Salad 10% off");
        eq(1350, BillCalculator.billFor(discounted, items.get(1)), "Burger 10% off");
        eq(1620, BillCalculator.billFor(discounted, items.get(2)), "Pasta 10% off");
        eq(1500, items.get(1).getPriceCents(), "menu price unchanged by billing");
    }

    private static void rounding() {
        eq(900, Discount.apply(900, 0), "no discount");
        eq(0, Discount.apply(900, 100), "full discount");
        eq(0, Discount.apply(0, 10), "zero amount");
        eq(5, Discount.apply(6, 10), "5.4 cents rounds down");
        eq(5, Discount.apply(5, 10), "4.5 cents rounds up");
        eq(14, Discount.apply(15, 10), "13.5 cents rounds up");
        eq(10, Discount.apply(11, 10), "9.9 cents rounds up");
        eq(Integer.MAX_VALUE, Discount.apply(Integer.MAX_VALUE, 0),
                "large valid amounts do not overflow");
    }

    private static void invalidInput() {
        throwsType(IllegalArgumentException.class, () -> Discount.apply(-1, 10));
        throwsType(IllegalArgumentException.class, () -> Discount.apply(900, -1));
        throwsType(IllegalArgumentException.class, () -> Discount.apply(900, 101));
    }

    //Customer 1 chooses Salad, so a 10% discount gives an $8.10 bill on the real Order
    private static void orderLocksBill() {
        Restaurant restaurant = new Restaurant(Order::new);
        restaurant.beginTurn(1);
        restaurant.registerArrival(new VipCustomer(1));
        check(restaurant.seatCustomer(1, 1).accepted(), "seat customer 1");
        ActionResult ordered = restaurant.placeOrder(1);
        check(ordered.accepted(), ordered.message());
        check(ordered.message().contains("Salad") && ordered.message().contains("$8.10"),
                "order message shows the bill: " + ordered.message());
        OrderView order = restaurant.getOrderViews().get(0);
        eq(810, order.billCents(), "bill stored on the order");
    }

    private static void happyHour() {
        List<MenuItem> items = new Menu().getItems();
        MenuItem burger = items.get(1);
        Customer regular = new RegularCustomer(1);
        Customer vip = new VipCustomer(2);
        Customer critic = new CriticCustomer(3);
        eq(1080, BillCalculator.billFor(vip, burger, true), "brief example: VIP Burger");
        eq(1200, BillCalculator.billFor(regular, burger, true), "Regular Burger");
        eq(1200, BillCalculator.billFor(critic, burger, true), "Critic Burger");
        eq(648, BillCalculator.billFor(vip, items.get(0), true), "VIP Salad");
        eq(1296, BillCalculator.billFor(vip, items.get(2), true), "VIP Pasta");
        eq(1350, BillCalculator.billFor(vip, burger, false), "VIP Burger outside Happy Hour");
        eq(BillCalculator.billFor(vip, burger, false), BillCalculator.billFor(vip, burger),
                "two-argument bill means no Happy Hour");
    }

    private static void comboBills() {
        List<ComboMeal> combos = new Menu().getCombos();
        Customer regular = new RegularCustomer(1);
        Customer vip = new VipCustomer(2);
        int[] vipBills = {1944, 2187, 2673};
        int[] regularHappyHour = {1728, 1944, 2376};
        int[] vipHappyHour = {1555, 1750, 2138};
        for (int i = 0; i < combos.size(); i++) {
            ComboMeal combo = combos.get(i);
            eq(vipBills[i], BillCalculator.billFor(vip, combo), "VIP " + combo.getName());
            eq(regularHappyHour[i], BillCalculator.billFor(regular, combo, true),
                    "Regular " + combo.getName() + " in Happy Hour");
            eq(vipHappyHour[i], BillCalculator.billFor(vip, combo, true),
                    "VIP " + combo.getName() + " in Happy Hour");
        }
    }

    private static void roundingOrder() {
        Menu menu = new Menu();
        List<MenuItem> everything = new ArrayList<>(menu.getItems());
        everything.addAll(menu.getCombos());
        Customer vip = new VipCustomer(1);
        for (MenuItem item : everything) {
            int roundedOnce = (item.getPriceCents() * 90 * 80 + 5000) / 10000;
            eq(roundedOnce, BillCalculator.billFor(vip, item, true), item.getName());
        }
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

}
