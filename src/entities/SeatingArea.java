package entities;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class SeatingArea {
    private final List<Table> tables=new ArrayList<>();
    private final WaitingQueue queue=new WaitingQueue();

    //Initialize the seating area with a specified number of tables
    public SeatingArea(int tableCount){
        for (int i=1;i<=tableCount;i++){
            tables.add(new Table(i));
        }
    }

    //-------------------------------Table Management-----------------------------------


    //Add a new customer to the waiting queue
    public void addToQueue(Customer customer){
        queue.add(customer);
    }

    //Remove a customer from the queue
    public boolean isQueued(Customer customer){
        return queue.contains(customer);
    }

    //Check if there is at least one free table in the seating area
    public boolean hasFreeTable(){
        return findFreeTable()!=null;
    }



    //Seat a customer at a free table, removing them from the queue and updating their state
    public Table seat(Customer customer){
        Table table=findFreeTable();
        if (table==null){
            throw new IllegalStateException("No free table available");
        }
        if (!queue.remove(customer)){
            throw new IllegalStateException(customer.describe()+ " is not in the queue");
        }
        table.seat(customer);
        customer.seat();
        return table;
    }


    //Remove a customer from the seating area, either from the queue or from a table
    public void removeCustomer(Customer customer){
        if (queue.remove(customer)){
            return; //Customer was in the queue and has been removed
        }

        for (Table table:tables){
            if (table.getOccupant()==customer){
                table.release();
                return; //Customer was seated at a table and has been released
            }
        }
    }
    




    //-------------------------------Getters-----------------------------------
    public List<Table> getTables(){return Collections.unmodifiableList(tables);}
    public List<Customer> getQueuedCustomers(){return queue.getCustomers();}

    //-------------------------------Customer Seating-----------------------------------

    //Find a free table in the seating area, returning null if none are available
    private Table findFreeTable(){
        for (Table table:tables){
            if (table.isFree()){
                return table;
            }
        }
        return null;
    }
}
