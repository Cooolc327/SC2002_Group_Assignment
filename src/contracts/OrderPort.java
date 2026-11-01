package contracts;

import entities.Customer;
import fooditems.MenuItem;

/**
 * Integration boundary for member C's Order implementation.
 * Implement directly or provide an adapter; no production Order is supplied here.
 * Legal mutations must succeed atomically. Rejected/throwing mutations must not
 * change the order. Unset turn numbers and payment satisfaction are null.
 */
public interface OrderPort {
    enum Status { PLACED, PREPARING, READY, SERVED, PAID, CANCELLED }

    int getId();
    int getCustomerId();
    MenuItem getItem();
    int getBillCents();
    int getPreparedUnits();
    Status getStatus();
    Integer getReadyTurn();
    Integer getServedTurn();
    Integer getPaidTurn();
    Integer getSatisfactionAtPayment();

    void advanceOneUnit(int turn);
    void markServed(int turn);
    void markPaid(int turn, int satisfaction);
    void cancel();

    @FunctionalInterface
    interface Factory {
        /** Return a fresh PLACED order without mutating the customer or tables. */
        OrderPort create(int orderId, Customer customer, MenuItem item, int billCents);
    }
}
