package entities;

public class RegularCustomer extends Customer {

    public static final int SATISFACTION_LOSS_PER_TURN = 8; //Constant for satisfaction loss per turn for regular customers

    public RegularCustomer(int id) {
        super(id);
    }

    @Override
    protected int getSatistfactionLossPerTurn() {
        return SATISFACTION_LOSS_PER_TURN; //Regular customers lose 8 satisfaction per turn while waiting
    }

    @Override
    public String getTypeName() {
        return "Regular"; //Customer Type
    }
    
}
