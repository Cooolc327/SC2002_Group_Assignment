package entities;


//Table Base Class
public class Table{

    //Each table has a unique id and can have one occupant (customer) at a time
    private final int id;
    private Customer occupant;

    public Table(int id){
        this.id=id;
    }

    //-------------------------------Occupancy Management-----------------------------------

    //Check if the table is empty (no occupant)
    public boolean isFree(){
        return occupant==null;
    }

    //Seat a customer at the table, ensuring the table is free and the customer is in the correct state
    public void seat(Customer customer){
        if (!isFree()){
            throw new IllegalStateException("Table "+ id + " is already occupied");
        }

        occupant=customer;
    }

    //Releases a customer from the table, ensuring the table is currently occupied
    public void release(){
        if (isFree()){
            throw new IllegalStateException("Table "+ id + " is already free");
        }
        occupant=null;
    }

    //-------------------------------Getters-----------------------------------

    public int getId() {return id;}
    public Customer getOccupant() {return occupant;}

    @Override
    public String toString() {
        return "Table #" + id + (isFree() ? " is free" : " is occupied by " + occupant.describe());
    }
}