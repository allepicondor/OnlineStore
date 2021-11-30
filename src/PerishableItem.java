public class PerishableItem  extends Item {
    private int daysLeft;
    private boolean donatable;

    public PerishableItem(int sku,int qty, String name, double height, double length, double width,double price,int daysLeft){
        super(sku, qty,name,height,length,width, price);
        donatable = true;
        this.daysLeft = daysLeft;
    }

}
