package entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaitingQueue {
    private final List<Customer> customers = new ArrayList<>();

    //-------------------------------Queue Management-----------------------------------

    //Add a customer to the waiting queue
    public void add(Customer customer) {
        customers.add(customer);
    }

    //Remove a customer from the waiting queue
    public boolean remove(Customer customer) {
        if (!customers.contains(customer)) {
            throw new IllegalStateException("Customer " + customer.describe() + " is not in the waiting queue");
        }
        return customers.remove(customer);
    }

    //Check if queue has specific customer
    public boolean contains(Customer customer) {
        return customers.contains(customer);
    }

    //Check if queue is empty
    public boolean isEmpty() {
        return customers.isEmpty();
    }

    //Show customers in the waiting queue
    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }
}
