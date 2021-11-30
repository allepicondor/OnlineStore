import java.sql.*;

public class DBInterface {


    private Connection con;
    private Statement stmt;

    public DBInterface(String file) {
        try {
            con = DriverManager.getConnection("jdbc:sqlite:database.db");
            stmt = con.createStatement();
        } catch (SQLException e){
            System.err.println(e);
            System.err.println("Problem connecting to database");
        }
        createTables();
    }


    public void createTables() {
        try {
            stmt.execute("CREATE TABLE IF NOT EXISTS Customers (Username TEXT, Password TEXT, Name TEXT, Address TEXT," +
                    " DOB TEXT, Past_Orders TEXT, Distance INT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Inventory  (SKU INT,QTY INT, Name TXT, Height REAL, Length REAL," +
                    " Width REAL, Price REAL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Orders (Invoice INTEGER PRIMARY KEY AUTOINCREMENT, Order_Date TEXT," +
                    "Customer TEXT,Items TEXT, Total REAL, Tax REAL, Shipping REAL, Boxes TEXT, Carrier TEXT)  ");
        } catch (SQLException e) {
            System.err.println("Error creating tables");
            System.out.println(e);
        }

    }

    public Statement  getStatement() {
        return stmt;
    }

    public void close() {
        try {
            con.close();
        }catch(SQLException e){}
    }


}






