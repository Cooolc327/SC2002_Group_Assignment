package entities;

import fooditems.Menu;
import fooditems.MenuItem;

public class CriticCustomer extends Customer {

    private static final int LOSS_PER_TURN=12; //Critic customers lose 12 satisfaction points per turn while waiting for food

    public CriticCustomer(int id) {
        super(id);
    }

    @Override
    protected int getSatistfactionLossPerTurn() {
        return LOSS_PER_TURN;
    }

    @Override
    public String getTypeName() {
        return "Critic";
    }

    @Override
    public MenuItem chooseItem(Menu menu){
        MenuItem best = menu.getItems().get(0);

        for (MenuItem item : menu.getItems()){
            if (item.getPrepUnits() > best.getPrepUnits()){
                best = item;
            }
        }

        return best;

    }

}
