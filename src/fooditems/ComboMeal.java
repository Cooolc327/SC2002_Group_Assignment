package fooditems;

import java.util.List;

// Two menu items sold together: preparation units add up, price is 90% of the pair
public class ComboMeal extends MenuItem {
    public static final int COMBO_DISCOUNT_PERCENT = 10;

    private final List<MenuItem> components;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName() + " Combo",
                Discount.apply(first.getPriceCents() + second.getPriceCents(), COMBO_DISCOUNT_PERCENT),
                first.getPrepUnits() + second.getPrepUnits());
        this.components = List.of(first, second);
    }

    public List<MenuItem> getComponents(){
        return components;
    }
}
