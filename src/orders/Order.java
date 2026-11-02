package orders;

import contracts.OrderPort;
import entities.Customer;
import fooditems.MenuItem;

// One customer's order: tracks preparation progress, status and the turn of each milestone
public class Order implements OrderPort {
    private final int id;
    private final int customerId;
    private final MenuItem item;
    private final int billCents;   // locked when the order is taken
    private int preparedUnits;
    private Status status = Status.PLACED;
    private Integer readyTurn;
    private Integer servedTurn;
    private Integer paidTurn;
    private Integer satisfactionAtPayment;

    public Order(int id, Customer customer, MenuItem item, int billCents) {
        this.id = id;
        this.customerId = customer.getId();
        this.item = item;
        this.billCents = billCents;
    }

    //-------------------------------State Transitions-----------------------------------

    //Chef completes one preparation unit; the order becomes READY after its last unit
    @Override
    public void advanceOneUnit(int turn) {
        if (status != Status.PLACED && status != Status.PREPARING) {
            throw new IllegalStateException("Order " + id + " cannot be prepared in state " + status);
        }
        preparedUnits++;
        if (preparedUnits == item.prepUnits()) {
            status = Status.READY;
            readyTurn = turn;
        } else {
            status = Status.PREPARING;
        }
    }

    //Only a dish that was already READY on an earlier turn can be served
    @Override
    public void markServed(int turn) {
        if (status != Status.READY || readyTurn == null || readyTurn >= turn) {
            throw new IllegalStateException("Order " + id + " cannot be served this turn");
        }
        status = Status.SERVED;
        servedTurn = turn;
    }

    //Record payment and the customer's satisfaction at that moment
    @Override
    public void markPaid(int turn, int satisfaction) {
        if (status != Status.SERVED) {
            throw new IllegalStateException("Order " + id + " cannot be paid in state " + status);
        }
        status = Status.PAID;
        paidTurn = turn;
        satisfactionAtPayment = satisfaction;
    }

    //Cancel an unfinished order when its customer leaves
    @Override
    public void cancel() {
        if (status == Status.SERVED || status == Status.PAID || status == Status.CANCELLED) {
            throw new IllegalStateException("Order " + id + " cannot be cancelled in state " + status);
        }
        status = Status.CANCELLED;
    }

    //-------------------------------Getters-----------------------------------
    @Override public int getId() { return id; }
    @Override public int getCustomerId() { return customerId; }
    @Override public MenuItem getItem() { return item; }
    @Override public int getBillCents() { return billCents; }
    @Override public int getPreparedUnits() { return preparedUnits; }
    @Override public Status getStatus() { return status; }
    @Override public Integer getReadyTurn() { return readyTurn; }
    @Override public Integer getServedTurn() { return servedTurn; }
    @Override public Integer getPaidTurn() { return paidTurn; }
    @Override public Integer getSatisfactionAtPayment() { return satisfactionAtPayment; }
}
