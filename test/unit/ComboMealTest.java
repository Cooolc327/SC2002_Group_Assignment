package unit;

import contracts.OrderPort;
import entities.RegularCustomer;
import fooditems.ComboMeal;
import fooditems.Menu;
import fooditems.MenuItem;
import java.util.List;
import orders.Order;

// Tests for ComboMeal and the combos on the menu
public final class ComboMealTest {
    private static int scenarios;
    private static int assertions;

    private ComboMealTest() {
    }

    public static void main(String[] args) {
        run("menu offers a combo of each pair of base items", ComboMealTest::menuCombos);
        run("brief example: Salad + Pasta is $24.30 and 3 units", ComboMealTest::briefExample);
        run("combo price rounds to the nearest cent", ComboMealTest::priceRounding);
        run("menu contains its own items and combos only", ComboMealTest::menuContains);
        run("combo works wherever a MenuItem does", ComboMealTest::usedAsMenuItem);
        run("three-unit combo is READY after its third unit", ComboMealTest::orderPreparation);
        System.out.println("COMBO_MEAL=PASS scenarios=" + scenarios + " assertions=" + assertions);
    }

    //-------------------------------Scenarios-----------------------------------

    private static void menuCombos() {
        Menu menu = new Menu();
        List<MenuItem> items = menu.getItems();
        List<ComboMeal> combos = menu.getCombos();
        eq(3, items.size(), "base menu still has three items");
        eq(3, combos.size(), "number of combos");
        checkCombo(combos.get(0), "Salad + Burger Combo", 2160, 2, items.get(0), items.get(1));
        checkCombo(combos.get(1), "Salad + Pasta Combo", 2430, 3, items.get(0), items.get(2));
        checkCombo(combos.get(2), "Burger + Pasta Combo", 2970, 3, items.get(1), items.get(2));
        throwsType(UnsupportedOperationException.class, () -> combos.remove(0));
        throwsType(UnsupportedOperationException.class,
                () -> combos.get(0).getComponents().set(0, items.get(2)));
    }

    private static void briefExample() {
        Menu menu = new Menu();
        ComboMeal combo = new ComboMeal(menu.getItems().get(0), menu.getItems().get(2));
        eq(2430, combo.getPriceCents(), "($9 + $18) x 0.9");
        eq(3, combo.getPrepUnits(), "1 + 2 units");
        eq("$24.30", combo.getFormattedPrice(), "formatted price");
    }

    private static void priceRounding() {
        MenuItem free = new MenuItem("Water", 0, 1);
        eq(95, new ComboMeal(new MenuItem("A", 105, 1), free).getPriceCents(), "94.5 cents rounds up");
        eq(95, new ComboMeal(new MenuItem("B", 106, 1), free).getPriceCents(), "95.4 cents rounds down");
        eq(94, new ComboMeal(new MenuItem("C", 104, 1), free).getPriceCents(), "93.6 cents rounds up");
    }

    private static void menuContains() {
        Menu menu = new Menu();
        MenuItem salad = menu.getItems().get(0);
        MenuItem pasta = menu.getItems().get(2);
        check(menu.contains(salad), "base item");
        check(menu.contains(menu.getCombos().get(1)), "combo");
        check(!menu.contains(new MenuItem("Salad", 900, 1)), "lookalike item from elsewhere");
        check(!menu.contains(new ComboMeal(salad, pasta)), "lookalike combo from elsewhere");
        check(!menu.contains(new Menu().getItems().get(0)), "item from another menu");
    }

    private static void usedAsMenuItem() {
        MenuItem item = new Menu().getCombos().get(2);
        eq("Burger + Pasta Combo", item.getName(), "name");
        eq(2970, item.getPriceCents(), "price");
        eq(3, item.prepUnits(), "prepUnits()");
        eq(3, item.getPrepUnits(), "getPrepUnits()");
    }

    private static void orderPreparation() {
        ComboMeal combo = new Menu().getCombos().get(1);
        Order order = new Order(1, new RegularCustomer(1), combo, combo.getPriceCents());
        eq(2430, order.getBillCents(), "bill");
        order.advanceOneUnit(2);
        eq(OrderPort.Status.PREPARING, order.getStatus(), "after unit 1");
        order.advanceOneUnit(3);
        eq(OrderPort.Status.PREPARING, order.getStatus(), "after unit 2");
        eq(null, order.getReadyTurn(), "no ready turn before the last unit");
        order.advanceOneUnit(4);
        eq(OrderPort.Status.READY, order.getStatus(), "after unit 3");
        eq(3, order.getPreparedUnits(), "prepared units");
        eq(4, order.getReadyTurn(), "ready turn");
        throwsType(IllegalStateException.class, () -> order.advanceOneUnit(5));
        throwsType(IllegalStateException.class, () -> order.markServed(4));
        order.markServed(5);
        order.markPaid(7, 80);
        eq(OrderPort.Status.PAID, order.getStatus(), "paid");
        eq(2430, order.getBillCents(), "bill unchanged by payment");
    }

    //-------------------------------Helpers-----------------------------------

    private static void checkCombo(ComboMeal combo, String name, int priceCents, int prepUnits,
            MenuItem first, MenuItem second) {
        eq(name, combo.getName(), "combo name");
        eq(priceCents, combo.getPriceCents(), name + " price");
        eq(prepUnits, combo.getPrepUnits(), name + " preparation units");
        check(combo.getComponents().get(0) == first && combo.getComponents().get(1) == second,
                name + " is built from the menu's own items");
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
