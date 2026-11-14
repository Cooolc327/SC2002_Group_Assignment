import cli.RestaurantCLI;
import control.GameController;
import domain.Restaurant;
import entities.CustomerFactory;
import entities.RegularCustomer;
import java.util.Scanner;
import orders.Order;
import staff.StaffTeam;

// Stage 3 is the default; explicit modes preserve the earlier CLI regression inputs.
public class Main {
    public static void main(String[] args) {
        if (args.length > 1 || (args.length == 1 && !args[0].equals("--regular")
                && !args[0].equals("--stage2") && !args[0].equals("--stage3"))) {
            System.err.println("Usage: Main [--stage3 | --stage2 | --regular]");
            return;
        }
        boolean regularOnly = args.length == 1 && args[0].equals("--regular");
        boolean stage3 = args.length == 0 || args[0].equals("--stage3");
        CustomerFactory customers = new CustomerFactory();
        Restaurant restaurant = new Restaurant(Order::new, stage3);
        GameController controller = new GameController(restaurant, new StaffTeam(),
                regularOnly ? RegularCustomer::new : customers::createCustomer);
        String setup = regularOnly ? "Regular-only regression mode"
                : "Customer arrival seed: " + customers.getSeed();
        new RestaurantCLI(controller, new Scanner(System.in), setup).run();
    }
}
