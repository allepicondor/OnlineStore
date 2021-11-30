import java.time.LocalDate;

public class PDFTester {
    public static void main(String[] args) throws Exception {
        Customer c = new Customer("Jim","pass","6331 Hwy Blvd, Katy, TX 77494");
        Item[] items = new Item[3];
        items[0] = new Item(1,2,"Golf ball",2,3,4,2);
        items[1] = new Item(2,2,"Usb Hub",2,3,4,30);
        items[2] = new Item(3,2,"Tennis Racket",6,3,4,50);

        Order o = new Order(c,items, Order.Carrier.USPS);
        o.generateInvoice(123);
        o.generateShippingLabel(123);
    }
}