package entities;


//Customer Lifecycle states to represent each state they are currently in
public enum CustomerState {
    WAITING,
    SEATED,
    ORDERED,
    SERVED,
    READY_TO_PAY,
    PAID,
    LEFT;


    //Returns true if the customer is waiting for food, false otherwise, used for customer satisfaction calculation
    public boolean isWaitingForFood() {
        return this == WAITING || this == ORDERED|| this == SEATED;
    }
}

