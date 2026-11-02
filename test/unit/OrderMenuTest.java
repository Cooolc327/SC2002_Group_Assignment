package unit;

import contracts.OrderPort;
import entities.RegularCustomer;
import fooditems.Menu;
import fooditems.MenuItem;
import java.util.List;
import orders.Order;

// Tests for the base menu and the order lifecycle
public final class OrderMenuTest {
    private static final int SALAD = 0;
    private static final int BURGER = 1;
    private static final int PASTA = 2;
    private static final List<MenuItem> ITEMS = new Menu().getItems();

    private static int scenarios;
    private static int assertions;

    private OrderMenuTest() {
    }

    public static void main(String[] args) {
        run("menu holds the three base items", OrderMenuTest::baseMenu);
        run("new order starts PLACED with its bill", OrderMenuTest::newOrderState);
        run("one-unit Salad goes straight to READY", OrderMenuTest::saladPreparation);
        run("two-unit Pasta goes PREPARING then READY", OrderMenuTest::pastaPreparation);
        run("dish is served on a later turn than READY", OrderMenuTest::serving);
        run("payment needs SERVED and records satisfaction", OrderMenuTest::payment);
        run("only unserved orders can be cancelled", OrderMenuTest::cancelling);
        System.out.println("ORDER_MENU=PASS scenarios=" + scenarios + " assertions=" + assertions);
    }

    //-------------------------------Scenarios-----------------------------------

    private static void baseMenu() {
        List<MenuItem> items = new Menu().getItems();
        eq(3, items.size(), "menu size");
        checkItem(items.get(SALAD), "Salad", 900, 1);
        checkItem(items.get(BURGER), "Burger", 1500, 1);
        checkItem(items.get(PASTA), "Pasta", 1800, 2);
        throwsType(UnsupportedOperationException.class, () -> items.add(items.get(SALAD)));
    }

    private static void newOrderState() {
        Order order = newOrder(BURGER);
        eq(1, order.getId(), "order id");
        eq(1, order.getCustomerId(), "customer id");
        eq("Burger", order.getItem().getName(), "item");
        eq(1500, order.getBillCents(), "bill");
        eq(OrderPort.Status.PLACED, order.getStatus(), "status");
        eq(0, order.getPreparedUnits(), "prepared units");
        eq(null, order.getReadyTurn(), "ready turn");
        eq(null, order.getServedTurn(), "served turn");
        eq(null, order.getPaidTurn(), "paid turn");
        eq(null, order.getSatisfactionAtPayment(), "satisfaction at payment");
    }

    private static void saladPreparation() {
        Order order = newOrder(SALAD);
        order.advanceOneUnit(2);
        eq(OrderPort.Status.READY, order.getStatus(), "Salad after one unit");
        eq(1, order.getPreparedUnits(), "prepared units");
        eq(2, order.getReadyTurn(), "ready turn");
        rejectedWithoutChange(order, "prepare a READY order", () -> order.advanceOneUnit(3));
    }

    private static void pastaPreparation() {
        Order order = newOrder(PASTA);
        order.advanceOneUnit(2);
        eq(OrderPort.Status.PREPARING, order.getStatus(), "Pasta after unit 1");
        eq(1, order.getPreparedUnits(), "prepared units after unit 1");
        eq(null, order.getReadyTurn(), "no ready turn after unit 1");
        order.advanceOneUnit(3);
        eq(OrderPort.Status.READY, order.getStatus(), "Pasta after unit 2");
        eq(2, order.getPreparedUnits(), "prepared units after unit 2");
        eq(3, order.getReadyTurn(), "ready turn");
        rejectedWithoutChange(order, "prepare a third unit", () -> order.advanceOneUnit(4));
    }

    private static void serving() {
        Order order = newOrder(SALAD);
        rejectedWithoutChange(order, "serve before READY", () -> order.markServed(2));
        order.advanceOneUnit(2);
        rejectedWithoutChange(order, "serve on the READY turn", () -> order.markServed(2));
        order.markServed(3);
        eq(OrderPort.Status.SERVED, order.getStatus(), "status");
        eq(3, order.getServedTurn(), "served turn");
        rejectedWithoutChange(order, "serve twice", () -> order.markServed(4));
        rejectedWithoutChange(order, "prepare a SERVED order", () -> order.advanceOneUnit(4));
    }

    private static void payment() {
        Order placed = newOrder(SALAD);
        rejectedWithoutChange(placed, "pay before SERVED", () -> placed.markPaid(3, 80));
        Order order = servedOrder();
        order.markPaid(5, 76);
        eq(OrderPort.Status.PAID, order.getStatus(), "status");
        eq(5, order.getPaidTurn(), "paid turn");
        eq(76, order.getSatisfactionAtPayment(), "satisfaction at payment");
        eq(900, order.getBillCents(), "bill unchanged by payment");
        rejectedWithoutChange(order, "pay twice", () -> order.markPaid(6, 50));
    }

    private static void cancelling() {
        Order placed = newOrder(PASTA);
        placed.cancel();
        eq(OrderPort.Status.CANCELLED, placed.getStatus(), "cancel PLACED");

        Order preparing = newOrder(PASTA);
        preparing.advanceOneUnit(2);
        preparing.cancel();
        eq(OrderPort.Status.CANCELLED, preparing.getStatus(), "cancel PREPARING");

        Order ready = newOrder(SALAD);
        ready.advanceOneUnit(2);
        ready.cancel();
        eq(OrderPort.Status.CANCELLED, ready.getStatus(), "cancel READY");
        rejectedWithoutChange(ready, "cancel twice", ready::cancel);
        rejectedWithoutChange(ready, "prepare a CANCELLED order", () -> ready.advanceOneUnit(3));
        rejectedWithoutChange(ready, "serve a CANCELLED order", () -> ready.markServed(3));

        Order served = servedOrder();
        rejectedWithoutChange(served, "cancel after serving", served::cancel);
        served.markPaid(5, 76);
        rejectedWithoutChange(served, "cancel after payment", served::cancel);
    }

    //-------------------------------Fixtures-----------------------------------

    private static Order newOrder(int itemIndex) {
        MenuItem item = ITEMS.get(itemIndex);
        return new Order(1, new RegularCustomer(1), item, item.getPriceCents());
    }

    //Salad made on turn 2 and served on turn 3
    private static Order servedOrder() {
        Order order = newOrder(SALAD);
        order.advanceOneUnit(2);
        order.markServed(3);
        return order;
    }

    //-------------------------------Helpers-----------------------------------

    private static void checkItem(MenuItem item, String name, int priceCents, int prepUnits) {
        eq(name, item.getName(), "item name");
        eq(priceCents, item.getPriceCents(), name + " price");
        eq(prepUnits, item.prepUnits(), name + " preparation units");
    }

    //A rejected change must throw and leave every field as it was
    private static void rejectedWithoutChange(Order order, String change, Runnable operation) {
        String before = snapshot(order);
        throwsType(IllegalStateException.class, operation);
        eq(before, snapshot(order), change + " left the order unchanged");
    }

    private static String snapshot(Order order) {
        return order.getStatus() + " " + order.getItem().getName() + " " + order.getBillCents()
                + " " + order.getPreparedUnits() + " " + order.getReadyTurn()
                + " " + order.getServedTurn() + " " + order.getPaidTurn()
                + " " + order.getSatisfactionAtPayment();
    }

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
