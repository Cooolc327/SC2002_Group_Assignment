package entities;


//Base class for all customers
public abstract class Customer {
    
    public static final int MAX_SATISFACTION = 100;

    private final int id;
    private int satisfaction=MAX_SATISFACTION;
    private CustomerState state = CustomerState.WAITING;
}
