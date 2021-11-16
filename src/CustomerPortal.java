import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class CustomerPortal {
    private static Statement stmt;
    private static Scanner  sc;
    private static Customer customer;

    public static void main(String[] args) {
        stmt = new DBInterface("database.db").getStatement();
        sc = new Scanner(System.in);

        int n = -1;

        System.out.println("#### Welcome to Awemazon #####\n");
        while (n != 3) {
            printMenu();
            n = sc.nextInt();
            sc.nextLine();
            if (n == 1) {
                createUser();
            } else if (n == 2) {
                signIn();
                if(customer != null){
                    portal();
                }
            } else {
                System.out.println("Thank you for shopping with us");
            }
        }
    }

    private static void portal() {
        int n = -1;
        while(n !=4){
            System.out.println("1. Modify Profile");
            System.out.println("2. Place order");
            System.out.println("3. View past orders");
            System.out.println("4. Log Off");

            n = sc.nextInt(); sc.nextLine();
            switch(n){
                case 1:
                    modifyProfile();
                    break;
                case 2:
                    placeOrder();
                    break;
                case 3:
                    ArrayList<Integer> orders  = customer.getPastOrders();
                    StringBuilder sql =new StringBuilder("SELECT * FROM Orders WHERE Invoice IN (");
                    for(Integer o: orders){
                        sql.append(o);
                        sql.append(",");
                    }
                    sql.append(")");
                    String out="";
                    try {
                        ResultSet set = stmt.executeQuery(sql.toString());
                        while(set.next()){
                            out += "Invoice: ";
                            out += set.getInt(1)+"\n";
                            out += "Date: ";
                            out += set.getString(2);
                            String items = set.getString(4);
                            out += getItems(items);
                            out +="Total: ";
                            out += set.getDouble(5);
                            out +="Tax: ";
                            out += set.getDouble(6);
                            out +="Shipping: ";
                            out += set.getDouble(7);
                        }
                    }catch(SQLException e){}
                    System.out.println(out);
                    break;
                case 4:
                    System.out.println("Logged out.");
                    customer = null;
                    break;
            }

        }

    }

    private static void placeOrder() {
        String ans = "";
        ArrayList<Item> items = new ArrayList<>();
        while(!ans.equalsIgnoreCase("exit")){
            System.out.println("Enter search(type exit to exit):");
            ans = sc.nextLine();

            if(!ans.equalsIgnoreCase("exit")) {
                try {
                    ResultSet set = stmt.executeQuery("");
                    if (!set.next()) {
                        System.out.println("No results");
                    }
                    int i = 1;
                    do {
                        System.out.println(i + " QTY: " + set.getInt(2) + " Name: " + set.getString(3) + " Price:"+set.getDouble(7));
                        i++;
                    } while (set.next());
                    System.out.println("Select item to purchase");
                    int n = sc.nextInt();
                    stmt.execute("UPDATE QTY");
                    set.beforeFirst();
                    for(int t= 0;t< n;t++) set.next();
                    Item it = new Item(set.getString(3),set.getDouble(4),set.getDouble(5),set.getDouble(6),set.getDouble(7));
                    items.add(it);

                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
        }
        Order  o = new Order(customer,(Item[])items.toArray(), Order.Carrier.USPS);
        addOrderToDB(o);

    }

    private static void addOrderToDB(Order o) {

    }

    private static String getItems(String items) {
        StringBuilder ans = new StringBuilder();
        try {
               ResultSet  set =  stmt.executeQuery("SELECT * FROM Inventory WHERE SKU IN ("+items+")");
               while(set.next()){
                   ans.append("Name: ");
                   ans.append(set.getString(3));
                   ans.append(" Price: ");
                   ans.append(set.getDouble(7));
                   ans.append("\n");
               }
        }catch(SQLException e){}
        return ans.toString();
    }

    private static void modifyProfile() {
        int n = -1;
        while(n !=4){
            System.out.println("1. Change name");
            System.out.println("2. Change password");
            System.out.println("3. Change address");
            System.out.println("4. Exit");

            switch(n){
                case 1:
                    System.out.print("Enter name: ");
                    String name = sc.nextLine();
                    break;
                case 2:
                    String pass1="";
                    String pass2=" ";
                    while(!pass1.equals(pass2)){
                        System.out.print ("Enter new password: ");
                        pass1 = sc.nextLine();
                        System.out.print("Reenter password: ");
                        pass2 = sc.nextLine();
                        if(!pass1.equals(pass2)){
                            System.out.println("Passwords do not match!");
                        }
                    }
                    break;
                case 3:

                    break;
            }


        }
    }

    private static void signIn() {
        String user="";
        boolean loggedIn = false;
        while(true) {
            System.out.print("Enter username: ");
            user =  sc.nextLine();
            try {
                ResultSet set = stmt.executeQuery("SELECT Password FROM Customers WHERE Username=\"" + user + "\"");
                if(set.next()){
                    System.out.print("Enter password:");
                    String pass = sc.nextLine();
                    if(pass.equals(set.getString(1))){
                        loggedIn =true;
                        break;
                    }
                    else{
                        System.out.println("Invalid password");
                    }
                }else{
                    System.out.println("User not found");
                }
            }catch(SQLException e){
                System.out.println(e);
            }
        }
        if(loggedIn){
            setCustomer(user);
            System.out.println("\n "+user+" successfully logged in:");
        }else{
            System.out.print("Login Failed");
        }
    }


    private static void createUser() {
        String usr ="";
        while(true) {
            System.out.print("Enter a username:");
            usr = sc.nextLine();
            try {
                ResultSet set = stmt.executeQuery("SELECT * FROM Customers WHERE Username=\"" + usr + "\"");

                if (set.next())
                    System.out.println("Username already taken! please choose another");
                else
                    break;
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
        System.out.println("HERE");
        System.out.print("Enter password: ");
        String pass  = sc.nextLine();
        System.out.print("Enter name: ");
        String name = sc.nextLine();
        System.out.print("Enter address: ");
        String addr = sc.nextLine();
        String dob ="";
        while(true) {
            System.out.print("Enter date of birth (yyyy-mm-dd): ");
            dob = sc.nextLine();
            if(true)
                break;
        }
        LocalDate DOB = LocalDate.parse(dob);
        Customer c = new Customer(usr, pass,name, addr,DOB);
        int dist = c.getDistance();
        try{
            stmt.execute("INSERT INTO Customers VALUES (\""+usr+"\",+\""+pass+"\",\""+name+"\",\""+addr+"\",\""+dob+"\",\"\","+dist+")");
        }catch(SQLException e){
            System.out.println(e);
        }
    }

    private static void printMenu() {
        System.out.println("1. Sign up");
        System.out.println("2. Sign in");
        System.out.println("3. Exit");
    }

    public static void setCustomer(String username){
        Customer c;
        try{
            ResultSet set = stmt.executeQuery("SELECT * FROM Customers WHERE Username=\""+username+"\"");
            String pass = set.getString(2);
            String name = set.getString(3);
            String address = set.getString(4);
            LocalDate dob = LocalDate.parse(set.getString(5));
            ArrayList<Integer> past = new ArrayList<>();
            if(set.getString(6).length() > 0) {
                String[] pastOrdersString = set.getString(6).split(",");
                for (String s : pastOrdersString) {
                    past.add(Integer.valueOf(s));
                }
            }
            int dist = set.getInt(7);
            customer = new Customer(username,pass,name,address,dob,past,dist);
        }catch(SQLException e){
            System.out.println(e);
        }

    }

    public Order getOrder(){
        return null;
    }


}
