package entities;

public class VipCustomer extends Customer {
    
    private static final int LOSS_PER_TURN=5; //VIP customers lose 5 satisfaction points per turn while waiting for food
    private static final int BILL_DISCOUNT_PERCENT=20; //VIP customers receive a 20% discount on their bill

    public VipCustomer(int id) {
        super(id);
    }

    @Override
    protected int getSatistfactionLossPerTurn() {
        return LOSS_PER_TURN;
    }

    @Override
    public String getTypeName() {
        return "VIP";
    }

    @Override
    public int getBillDiscountPercent() {
        return BILL_DISCOUNT_PERCENT;
    }
}
