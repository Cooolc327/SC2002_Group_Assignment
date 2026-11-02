import cli.RestaurantCLI;
import control.GameController;
import domain.Restaurant;
import java.util.Scanner;
import orders.Order;
import staff.StaffTeam;

// Entry point: wires the base-setting game together and starts the command-line interface
public class Main {
    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant(Order::new);
        GameController controller = new GameController(restaurant, new StaffTeam());
        new RestaurantCLI(controller, new Scanner(System.in)).run();
    }
}
