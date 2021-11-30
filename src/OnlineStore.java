import org.sqlite.SQLiteUpdateListener;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
public class OnlineStore {
    private static Statement stmt;
    public static void main(String[] args) {

        DBInterface db = new DBInterface("database.db");
        stmt = db.getStatement();
        Scanner sc= new Scanner(System.in);
        int n = -1;
        while(n !=4){
            System.out.println("1. Add item");
            System.out.println("2. Remove item");
            System.out.println("3. Modify Quantity");
            System.out.println("4. Exit");
            n = sc.nextInt();sc.nextLine();
            if(n == 1 ){

                System.out.print("Item name(String):");
                String name = sc.nextLine();
                System.out.print("Item height(Double):");
                double height = sc.nextDouble();sc.nextLine();
                System.out.print("Item width(Double):");
                double width = sc.nextDouble();
                System.out.print("Item length(Double):");
                double length = sc.nextDouble();
                System.out.print("Item price(Double):");
                double price = sc.nextDouble();sc.nextLine();
                System.out.print("SKU(Int):");
                int sku = sc.nextInt();sc.nextLine();
                System.out.print("QTY(Int):");
                int qty = sc.nextInt();sc.nextLine();
                if (AddItem(sku,qty,name,height,length,width,price)){
                    System.out.println("Successfully added Item");
                }else{
                    System.out.println("Error adding Item");
                }


            }else if(n ==2 || n == 3){
                System.out.println("Enter SKU");
                int sku = sc.nextInt();sc.nextLine();
                if(n == 2){
                    if (RemoveItem(sku)) {
                        System.out.println("Successfully removed Item="+sku);
                    }
                }else{
                    System.out.println("Enter new QTY:");
                    int qty = sc.nextInt();
                    if (UpdateQuantity(sku, qty)) {
                        System.out.println("Quantity updated");
                    }
                }

            }

        }
    }
    static boolean UpdateQuantity(int sku, int qty){
        try {
            stmt.execute("UPDATE Inventory SET QTY=" + qty + " WHERE SKU=" + sku);
        }catch(SQLException e) {return false;}
        return true;

    }
    static boolean RemoveItem(int sku){
        try{
            stmt.execute("DELETE FROM Inventory WHERE SKU="+sku);
        }catch(SQLException e){return false;}
        return true;
    }
    static boolean AddItem(int sku,int qty,String name, double height,double length,double width,double price){
        try{
            stmt.execute("INSERT INTO Inventory VALUES ("+sku+","+qty+",\""+name+"\","+height+","+length+","
                    + width+","+price+")");
        }catch(SQLException e){return false;}
        return true;
    }
}
