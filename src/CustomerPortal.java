import javax.print.DocFlavor;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

public class CustomerPortal {
    private static Statement stmt;
    private static Scanner  sc;
    private static Customer customer;
    private static boolean isLoggedIn = false;

    public static void main(String[] args) {
        stmt = new DBInterface("database.db").getStatement();
        sc = new Scanner(System.in);



        System.out.println("#### Welcome to Conner's Store #####\n");
        ManageLogin();
        CustomerManagementMenu();

    }
    public static void CustomerManagementMenu(){
        int n = -1;
        while(n!=3 && isLoggedIn) {
            PrintCustomerPortalMenu();
            n = sc.nextInt();
            if (n == 0){
                shop();
            }else if (n == 1){
                modifyCustomer();
            }else if(n == 2){
                pastOrders();
            }

        }
    }

    private static void pastOrders() {
        try {
            ResultSet set = stmt.executeQuery("SELECT * FROM Orders WHERE Customer LIKE \"%" + customer.getUsername() + "%\"");
            if (set.next()){
                System.out.println("PAST ORDERS");
                do{
                    System.out.println(set.getString("Order_Date")+" | total: "+set.getFloat("Total"));
                }while(set.next());
            }else{
                System.out.println("No previous orders");
            }
        }catch (SQLException e){
            System.out.println(e);

        }
    }

    private static Item ItemFromSku(int i) {
        try {
            ResultSet set = stmt.executeQuery("SELECT * FROM Inventory WHERE SKU LIKE \"%" + i + "%\"");
            if (set.next()) {
                Item it = new Item(set.getInt("SKU"), set.getInt("QTY"), set.getString("Name"), set.getDouble("Height"), set.getDouble("Length"), set.getDouble("Width"), set.getDouble("Price"));
                return it;
            }
            System.out.println("Cannot find item");
        }catch (SQLException e){
            System.out.println(e);
        }
        return null;

    }

    private static void modifyCustomer() {
        int ans = 0;

        while (ans != 2) {
            System.out.println("Welcome to Account Settings");
            System.out.println("0: Change Password");
            System.out.println("1: Change Address");
            System.out.println("2: exit");
            System.out.print("choice:");
            ans = sc.nextInt();
            System.out.println();
            if (ans == 0){
                String pass1="";
                String pass2=" ";
                while(!pass1.equals(pass2)){

                    System.out.print("Enter new password: ");
                    pass1 = sc.next();
                    System.out.print("Reenter password: ");
                    pass2 = sc.next();
                    if(!pass1.equals(pass2)){
                        System.out.println("Passwords do not match!");
                    }
                }
                customer.setPassword(pass1);
                try{
                    stmt.execute("UPDATE Customers SET Password = \""+pass1+"\" WHERE Username = \""+customer.getUsername()+"\"");

                }catch (SQLException e){
                    System.out.println(e);
                }

            }else if(ans == 1) {

                System.out.print("Enter new address:");
                sc.nextLine();
                String address = sc.nextLine();
                customer.setAddress(address);
                System.out.println();
                try{
                    stmt.execute("UPDATE Customers SET Address = \""+customer.getAddress()+"\" WHERE Username = \""+customer.getUsername()+"\"");

                }catch (SQLException e){
                    System.out.println(e);
                }

            }

        }

    }

    private static void shop(){
        ArrayList<Item> cart = new ArrayList<>();
        String search = "";
        System.out.println("Welcome to the shop search for a product.");
        System.out.println("!exit: to exit this menu");
        System.out.println("!purchase: proceed to checkout");
        System.out.println("!cart: to view your current cart");
        while (true) {
            System.out.print("search:");
            search = sc.next();
            if (search.equals("!exit")) {
                return;
            }
            if (search.equals("!purchase")) {
                if (!cart.isEmpty()) {
                    break;
                }else{
                    System.out.println("Cart is empty");
                }
                continue;
            }
            if (search.equals("!cart")) {
                if (!cart.isEmpty()) {

                    for (Item item :
                            cart) {
                        System.out.println(item);
                    }
                }else{
                    System.out.println("Cart is empty");
                }
                continue;
            }

            try {
                ResultSet set = stmt.executeQuery("SELECT * FROM Inventory WHERE Name LIKE \"%"+search+"%\"");

                if (!set.next()) {
                    System.out.println("No products found");
                }else {
                    int i = 0;
                    ArrayList<Integer> quantitys = new ArrayList<>();
                    do {
                        int qty = set.getInt("QTY");
                        for (Item item :
                                cart) {
                            if (item.getName().equals( set.getString("Name"))){
                                qty -= 1;

                            }
                        }
                        System.out.println(i+" | Name: " + set.getString("Name") + " | QTY: " + qty + " | Price:" + set.getDouble("Price"));
                        quantitys.add(qty);
                        i++;
                    } while (set.next());
                    System.out.print("select item to buy(to cancel type -1): ");
                    int itemNum = sc.nextInt();
                    if(itemNum == -1){
                        continue;
                    }
                    set = stmt.executeQuery("SELECT * FROM Inventory WHERE Name LIKE \"%"+search+"%\"");
                    for (; i < itemNum; ){
                        set.next();
                    }
                    Item it = new Item(set.getInt("SKU"),set.getInt("QTY"),set.getString("Name"), set.getDouble("Height"), set.getDouble("Length"), set.getDouble("Width"),set.getDouble("Price"));
                    if (quantitys.get(itemNum)-1 < 0){
                        System.out.println("Out of stock");
                        continue;
                    }else{
                        cart.add(it);
                    }
                }
            } catch (SQLException e) {
                System.out.println(e);
            }

        }
        double total_price = 0.0;
        for (Item item :
                cart) {
            System.out.println(item);
            total_price+=item.getPrice();
        }
        System.out.println("Total: "+ total_price+"$");
        System.out.println("Confirm purchase of items by typing total:");
        double confirmCheckout = sc.nextDouble();
        if (confirmCheckout != total_price){
            return;
        }
        if (!placeOrder(cart)){
            System.out.println("Error placing order");
            return;
        }
        Hashtable<Integer, Integer> sku_to_qty = new Hashtable<>();

        for (Item item :
                cart) {


            int qty = item.getQty();
            if(sku_to_qty.get(item.getSku()) != null){
                qty = sku_to_qty.get(item.getSku());
            }
            sku_to_qty.put(item.getSku(),qty-1);

        }
        for (Enumeration<Integer> e = sku_to_qty.keys(); e.hasMoreElements();){
            int key = e.nextElement();
            try {
                stmt.execute("UPDATE Inventory SET QTY=" + sku_to_qty.get(key) + " WHERE SKU=" + key);
            } catch (SQLException ex) {
                System.out.println(ex);
            }

        }
        System.out.println("You successfully purchased the items");



    }
    private static boolean placeOrder(ArrayList<Item> items) {
        int invoiceID = 0;
        try {
            ResultSet set = stmt.executeQuery("SELECT * FROM Orders ORDER BY Invoice DESC LIMIT 1");
            if (set.next()){
                invoiceID = set.getInt(1)+1;
            }

        } catch (SQLException ex) {

            System.out.println(ex);
            return false;
        }


        Order new_order = new Order(customer,items.toArray(new Item[0]), Order.Carrier.USPS);
        StringBuilder sql = new StringBuilder("INSERT INTO Orders VALUES ("+invoiceID+",\""+new_order.getOrderDate()+"\",\""+new_order.getCustomer().getUsername()+"\",\"");
        Item[] it = new_order.getItems();
        for(Item i:it){
            sql.append(i.getSku());
            sql.append(",");
        }
        if(it.length > 0)
            sql.delete(sql.length()-1,sql.length());
        sql.append("\",");
        sql.append(new_order.getTotal()+",");
        sql.append(new_order.getTax()+",");
        sql.append(new_order.getShippingCost()+",");
        sql.append("\"");
        Order.Box[]  boxes = new_order.getBoxes();
        for(Order.Box b:boxes){
            sql.append(b.name());
            sql.append(",");
        }
        if(boxes.length > 0)
            sql.delete(sql.length()-1,sql.length());
        sql.append("\",");
        sql.append("\""+new_order.getCarrier().name()+"\")");
        System.out.println(sql);
        try{

            stmt.execute(sql.toString());
            return true;
        }catch(SQLException e){
            System.out.println(e);
            return false;
        }

    }

    public static void PrintCustomerPortalMenu(){
        System.out.println("0| place order");
        System.out.println("1| modify customer");
        System.out.println("2| view past orders");
        System.out.println("3| Logout");
    }
    public static void ManageLogin(){
        int n = -1;
        while(n!=2 && !isLoggedIn){
            PrintLoginMenu();
            try {
                n = sc.nextInt();
            }catch(Exception e){continue;}
            if (n == 0){
                System.out.print("username:");
                String username = sc.next();
                System.out.print("password:");
                String password = sc.next();
                if (SignIn(username,password)){
                    System.out.println("Successfully logged in");
                }else{
                    System.out.println("Account not found");
                }
            }else if (n == 1){
                System.out.print("username:");
                String username = sc.next();
                while (true) {
                    try {
                        if (!isUsernameFree(username)) {
                            System.out.println("Username is taken");
                            System.out.print("new username:");
                            username = sc.next();
                        }else{
                            break;
                        }
                    } catch (SQLException e) {
                    }
                }
                System.out.print("password:");
                String password = sc.next();
                System.out.print("address:");
                String address = sc.next();
                if (SignUp(username,password,address)){
                    System.out.println("Successfully signed up. Welcome to the club!");
                    SignIn(username,password);

                }else{
                    System.out.println("Error Signing up");
                }
            }

        }
    }
    public static void PrintLoginMenu(){
        System.out.println("0. Sign in");
        System.out.println("1. Sign up");
        System.out.println("2. exit");
        System.out.print("Which option do you want? ");
    }
    public static boolean SignIn(String username,String password) {
        try {
            ResultSet set = stmt.executeQuery("SELECT Password FROM Customers WHERE Username=\"" + username + "\"");
            if (set.next()){
                String correctPass = set.getString("Password");
                if (correctPass.equals(password)){
                    Customer newCustomer = buildCustomer(username);
                    if (newCustomer == null){
                        System.out.println("Error pulling customer data");
                        return false;
                    }else{
                        customer = newCustomer;
                        isLoggedIn = true;
                        return true;
                    }

                }else{
                    System.out.println("wrong password");
                    return false;
                }
            }else{
                System.out.println("account not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }
    public static Customer buildCustomer(String username){
        Customer newCustomer = null;
        try {
            ResultSet set = stmt.executeQuery("SELECT * FROM Customers WHERE Username=\"" + username + "\"");
            if (set.next()){
                //Array arr = set.getArray("Past_Orders");//TODO turn this into a ArrayList<Integer> for Customer
                newCustomer = new Customer(username,set.getString("Password"),set.getString("Address"));
            }else{
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newCustomer;

    }
    public static boolean SignUp(String username,String password,String address){
        try{

            String query = "INSERT INTO Customers (Username,Password,Address) VALUES (\""+username+"\",\""+password+"\",\""+address+"\")";
            //System.out.println(query);
            stmt.execute(query);
        }catch (SQLException e){
            System.out.println(e);
            return false;
        }
        return true;
    }

    private static boolean isUsernameFree(String username) throws SQLException {
        ResultSet set = stmt.executeQuery("SELECT * FROM Customers WHERE Username=\"" + username + "\"");
        if (set.next()) {
            return false;
        }
        return true;
    }


}
