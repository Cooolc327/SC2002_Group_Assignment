import cli.RestaurantCLI;
import control.GameController;
import domain.Restaurant;
import entities.CustomerFactory;
import entities.RegularCustomer;
import java.util.Scanner;
import orders.Order;
import staff.StaffTeam;

// Entry point: Stage 2 uses mixed customers; --regular reproduces the Regular-only baseline.
public class Main {
    public static void main(String[] args) {
        if (args.length > 1 || (args.length == 1 && !args[0].equals("--regular"))) {
            System.err.println("Usage: Main [--regular]");
            return;
        }
        boolean regularOnly = args.length == 1;
        CustomerFactory customers = new CustomerFactory();
        Restaurant restaurant = new Restaurant(Order::new);
        GameController controller = new GameController(restaurant, new StaffTeam(),
                regularOnly ? RegularCustomer::new : customers::createCustomer);
        String setup = regularOnly ? "Regular-only regression mode"
                : "Customer arrival seed: " + customers.getSeed();
        new RestaurantCLI(controller, new Scanner(System.in), setup).run();
    }
}
