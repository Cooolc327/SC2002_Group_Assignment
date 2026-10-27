package entities;

//Imports
import fooditems.Menu;
import fooditems.MenuItem;

import java.util.List;

//Base class for all customers
public abstract class Customer {
    
    public static final int MAX_SATISFACTION = 100;

    private final int id;
    private int satisfaction=MAX_SATISFACTION;
    private CustomerState state = CustomerState.WAITING;
    private int servedTurn;
    private int readyToPayTurn;

    protected Customer(int id){
        this.id=id;
    }

    //-------------------Abstraction functions for subclasses (Regular,VIP,Host)---------------------------
    protected abstract int getSatistfactionLossPerTurn();
    public abstract String getTypeName();

    //-----------------------------------Food serving rule-------------------------------------------------
   
    //Choose a menu item for the customer based on their id and the menu provided
    public MenuItem chooseItem(Menu menu){
        List<MenuItem> items = menu.getItems();
        return items.get((id-1) % items.size());
    }


    //-------------------------------------Satisfaction management------------------------------------
    
    //End of turn update for the customer, applying penalties if necessary
    public void applyWaitingPenalty(){
        if (!state.isWaitingForFood()){
            return; //No penalty if the customer is not waiting for food
        }

        satisfaction=Math.max(0,satisfaction-getSatistfactionLossPerTurn()); //Update satisfaction, ensuring it doesn't go below 0
    }

    //Patience check to determine if the customer is still willing to wait
    public boolean isOutOfPatience(){
        return state.isWaitingForFood()&&satisfaction==0;
    }


    //------------------------------State Transitions-----------------------------------
    
    //Seat the customer, transitioning them to the SEATED state
    public void seat(){
        require(CustomerState.WAITING,"Seated");
        state=CustomerState.SEATED;
    }

    //Take the customer's order, transitioning them to the ORDERED state
    public void placeOrder(){
        require(CustomerState.SEATED,"have an order taken");
        state=CustomerState.ORDERED;
    }

    //Serve the customer, transitioning them to the SERVED state and recording the turn they were served
    public void serve(int currentTurn){
        require(CustomerState.ORDERED,"be served");
        state=CustomerState.SERVED;
        servedTurn=currentTurn;
    }

    //Check if the customer has finished eating, if so transition to READY_TO_PAY state and record the turn they are ready to pay
    public boolean finishedEating(int currentTurn){
        if (state == CustomerState.SERVED && currentTurn>servedTurn){
            state=CustomerState.READY_TO_PAY;
            readyToPayTurn=currentTurn;
            return true;
        }
        return false;
    }

    //Make the customer pay, transitioning them to the PAID state
    public void pay(){
        require(CustomerState.READY_TO_PAY,"pay");
        state=CustomerState.PAID;
    }

    //Makes the customer leave once satisfaction drops to 0
    public void abandon(){
        if (!state.isWaitingForFood()){
            throw new IllegalStateException(describe()+" cannot abandon in state "+state);
        }

        state=CustomerState.LEFT;
    }


    //------------------------------Getters--------------------------------------------
    public int getId(){return id;}
    public int getSatisfaction(){return satisfaction;}
    public CustomerState getState(){return state;}
    public int getReadyToPayTurn(){return readyToPayTurn;} // To be used by cashier for oldest customer in queue first
    
    //Describe the customer with their type and id
    public String describe(){
        return getTypeName()+" #"+ id;    
    }

    //Describe the customer with their type, id, satisfaction and state
    @Override
    public String toString(){
        return describe()+"[Satisfaction: "+satisfaction+", State: "+state+"]";
    }


    //Check if expected state matches current state, if not throw an exception with the action that was attempted
    private void require(CustomerState expected, String action){
        if (state!=expected){
            throw new IllegalStateException("Cannot perform action '"+action+"' when customer is in state "+state);
        }
    }
}
