package fooditems;

import java.util.List;
import java.util.stream.Stream;

public class Menu {
    private final boolean combosEnabled;

    public Menu() {
        this(false);
    }

    public Menu(boolean combosEnabled) {
        this.combosEnabled = combosEnabled;
    }
    private final MenuItem salad = new MenuItem("Salad", 900, 1);
    private final MenuItem burger = new MenuItem("Burger", 1500, 1);
    private final MenuItem pasta = new MenuItem("Pasta", 1800, 2);

    private final List<MenuItem> items = List.of(salad, burger, pasta);

    private final List<ComboMeal> combos = List.of(
        new ComboMeal(salad, burger),
        new ComboMeal(salad, pasta),
        new ComboMeal(burger, pasta)
    );

    public List<MenuItem> getItems(){return items;}
    public List<ComboMeal> getCombos(){return combos;}

    /** Stage 2 retains its original three choices; Stage 3 appends the three combos. */
    public List<MenuItem> getOrderableItems() {
        return combosEnabled ? Stream.concat(items.stream(), combos.stream()).toList() : items;
    }

    //True for a base item or a combo on this menu
    public boolean contains(MenuItem item){
        return items.contains(item) || combos.contains(item);
    }
}
