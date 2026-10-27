package fooditems;

import java.util.List;

public class Menu {
    private final List<MenuItem> items = List.of(
        new MenuItem("Salad", 900, 1),
        new MenuItem("Burger", 1500, 1),
        new MenuItem("Pasta", 1800, 2)
    );

    public List<MenuItem> getItems(){return items;}
}


