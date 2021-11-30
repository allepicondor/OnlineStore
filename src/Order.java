import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

public class Order {
    private LocalDate orderDate;
    private Customer customer;
    private Item[] items;
    private Carrier carrier;
    private int invoice;
    private double total, tax, shippingCost;
    public static final String ORIGIN = "14350+Farm+to+Market+Rd+1488+Magnolia+TX+77354";

    public Box[] getBoxes() {
        Box[] boxes = new Box[items.length];
        for (int i =0; i < items.length; i++) {
            boxes[i] = FindAppropriateBoxSize((float) (items[i].getLength()*items[i].getWidth()*items[i].getHeight()));
        }
        return boxes;
    }
    private Box FindAppropriateBoxSize(float volume){
        if (volume <= Box.SMALL.getVolume()){
            return Box.SMALL;
        }else if (volume <= Box.MEDIUM.getVolume()){
            return Box.MEDIUM;
        }else{
            return Box.LARGE;
        }
    }

    public Carrier getCarrier() {
        return carrier;
    }

    public enum Carrier{
        USPS (2,0.025),UPS(6,0.05),FEDEX(6.25,0.08);
;
        private final double flatRate;
        private final double perKm;
        Carrier(double flatRate, double perKm){
            this.flatRate = flatRate;
            this.perKm = perKm;
        }

        public double calculateShipping(double distance){
            return flatRate+distance*perKm;
        }
    }
    public enum Box{
        SMALL (10),
        MEDIUM (20),
        LARGE (30),
        PERISHABLE (20);

        private final double volume;

        Box(double volume){
            this.volume =volume;
        }
        public double getVolume(){return volume;}
    }

    public Order(Customer customer, Item[] items, Carrier carrier){

        this.customer = customer;
        this.items = items;
        this.carrier = carrier;
        for(Item i: items){
            total+=i.getPrice();
        }
        tax = total*0.0825;
        shippingCost = carrier.calculateShipping(customer.getDistance());
        orderDate = LocalDate.now();

    }

    public double getTotal() {
        return total;
    }

    public double getTax() {
        return tax;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public Item[] getItems() {
        return items;
    }

    public void setItems(Item[] items) {
        this.items = items;
    }
    public void generateInvoice(int invNum)throws Exception{
        float x=0,y=0;
        PDDocument doc = PDDocument.load(new File("itemplate.pdf"));
        doc.save("Invoice_"+invNum+".pdf");
        doc.close();

        doc = PDDocument.load(new File("Invoice_"+invNum+".pdf"));
        PDPage page = doc.getPage(0);
        PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND,false,true);
        cs.beginText();
        cs.newLineAtOffset(180, 703);
        x+= 180;
        y+= 703;
        cs.setFont(PDType1Font.TIMES_ROMAN, 12);
        cs.showText(""+invNum);
        cs.newLineAtOffset(-60, -80);
        x+=-60; y += -80;
        ArrayList<Integer> itList = new ArrayList<>();
        for(int i = 0; i<items.length;i++) {
            if(!itList.contains(items[i].getSku())) {
                int qty = 0;
                for(Item it: items) {
                    if(items[i].getSku() == it.getSku()) qty++;
                }

                cs.showText(""+items[i].getSku());
                cs.newLineAtOffset(70, 0);
                cs.showText(""+qty);
                cs.newLineAtOffset(70, 0);
                cs.showText(items[i].getName());
                cs.newLineAtOffset(250, 0);
                cs.showText(items[i].getPrice()+"");
                cs.newLineAtOffset(-390, -24);
                y += -24;
                itList.add(items[i].getSku());
            }
        }

        cs.newLineAtOffset(-x,-y);
        cs.newLineAtOffset(465,178);
        cs.showText("$ "+total);
        cs.newLineAtOffset(0,-24);
        cs.showText("$ "+tax);
        cs.newLineAtOffset(0,-24);
        cs.showText("$ "+shippingCost);
        cs.newLineAtOffset(0,-24);
        cs.showText("$ "+(total+shippingCost+tax));

        cs.endText();
        cs.close();

        doc.save("Invoice_"+invNum+".pdf");
        doc.close();
    }
    public void generateShippingLabel(int invNum) throws Exception{
        PDDocument doc = PDDocument.load(new File("stemplate.pdf"));
        doc.save("Shipping_"+invNum+".pdf");
        doc.close();

        doc = PDDocument.load(new File("Shipping_"+invNum+".pdf"));
        PDPage page = doc.getPage(0);
        PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND,false,true);
        cs.setFont(PDType1Font.TIMES_ROMAN, 12);
        cs.beginText();
        cs.newLineAtOffset(90,655);
        cs.showText(customer.getUsername());
        cs.newLineAtOffset(0,-12);
        cs.showText(customer.getAddress());
        cs.newLineAtOffset(35,-165);
        cs.showText(carrier.name());
        cs.endText();
        cs.close();
        doc.save("Shipping_"+invNum+".pdf");
        doc.close();

    }


    public Customer getCustomer(){
        return customer;
    }

}
