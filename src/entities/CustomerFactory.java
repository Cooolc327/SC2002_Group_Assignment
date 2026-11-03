package entities;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public final class CustomerFactory {

    public static final long DEFAULT_SEED=24369; //Default seed for random number generation, ensuring consistent customer generation across runs

    private enum Kind{Regular, VIP, Critic} //Types of Customers available in the game

    private final long seed;
    private final Kind[] schedule;
    
    
    //Default constructor that initializes the schedule of customers arrival turn based on a default seed for randomization
    public CustomerFactory() {this(DEFAULT_SEED);} 


    //Constructor that initializes the schedule of customers arrival turn based on a given seed for randomization
    public CustomerFactory(long seed) {
        this.seed=seed;
        List<Kind> kinds = new ArrayList<>(List.of(
            Kind.VIP, Kind.Critic, Kind.Regular, Kind.Regular, Kind.Regular, Kind.Regular));//Game specifies that there must be 6 customers, with 1 VIP and 1 Critic
        Collections.shuffle(kinds, new Random(seed));
        schedule=kinds.toArray(new Kind[0]);
    }

    //Creates a customer based on the turn number, returning the appropriate subclass instance (Regular, VIP, or Critic)
    public Customer createCustomer(int turn) {
        switch (schedule[turn]){
            case VIP: return new VipCustomer(turn);
            case Critic: return new CriticCustomer(turn);
            default: return new RegularCustomer(turn);
        }
    }

    //Used to reproduce the run for TA use
    public long getSeed() {
        return seed;
    }
}
