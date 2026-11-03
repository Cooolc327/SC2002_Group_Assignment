package fooditems;
public class MenuItem {
    private final String name;
    private final int priceCents;
    private final int prepUnits;

    public MenuItem(String name, int priceCents, int prepUnits) {
        this.name = name;
        this.priceCents = priceCents;
        this.prepUnits = prepUnits;
    }
    

    //Getters
    public String getName(){
        return this.name;
    }

    public int getPriceCents(){
        return this.priceCents;
    }

    public int prepUnits(){
        return this.prepUnits;
    }

    //To show Formatted Price in dollars
    public String getFormattedPrice(){
        return String.format("$%d.%02d", priceCents / 100, priceCents % 100);
    }

    public int getPrepUnits(){
        return this.prepUnits;
    }
    
}
