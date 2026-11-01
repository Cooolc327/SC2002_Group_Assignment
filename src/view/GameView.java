package view;

import contracts.ChefAction;
import contracts.OrderPort;
import contracts.WaiterAction;
import control.GameResult;
import control.TurnPhase;
import entities.CustomerState;
import java.util.List;

/** A detached, immutable snapshot. The CLI never receives mutable entities. */
public record GameView(int turn, TurnPhase phase, List<CustomerView> customers,
                       List<Integer> waitingCustomerIds, List<TableView> tables,
                       List<OrderView> orders, List<MenuItemView> menu,
                       StaffView staff, GameSummary summary, GameResult result) {
    public GameView {
        customers = List.copyOf(customers);
        waitingCustomerIds = List.copyOf(waitingCustomerIds);
        tables = List.copyOf(tables);
        orders = List.copyOf(orders);
        menu = List.copyOf(menu);
    }

    public record CustomerView(int id, String type, int satisfaction,
                               CustomerState state, Integer readyToPayTurn) {}

    public record TableView(int id, Integer customerId) {}

    public record MenuItemView(String name, int priceCents, int preparationUnits) {}

    public record OrderView(int id, int customerId, String itemName, int billCents,
                            int preparedUnits, int preparationUnits,
                            OrderPort.Status status, Integer readyTurn,
                            Integer servedTurn, Integer paidTurn,
                            Integer satisfactionAtPayment) {}

    public record StaffView(boolean waiterAvailable, boolean chefAvailable,
                            boolean cashierAvailable, List<WaiterAction> waiterOptions,
                            List<ChefAction> chefOptions) {
        public StaffView {
            waiterOptions = List.copyOf(waiterOptions);
            chefOptions = List.copyOf(chefOptions);
        }
    }
}
