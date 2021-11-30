
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;


public class Customer {
    private String username, password, address, sku;
    private ArrayList<Integer> pastOrders;
    private int distance;

    public Customer(String username, String password,String address,ArrayList<Integer> pastOrders) {
        this.username = username;
        this.password = password;
        this.sku = sku;
        this.address = address;
        this.pastOrders = pastOrders;
    }
    public Customer(String username, String password,String address) {
        this.username = username;
        this.password = password;
        this.sku = sku;
        this.address = address;
        this.pastOrders = new ArrayList<Integer>();
    }
    public double getDistance(){
        String apiKey = "";
        int distance = 0;
        try {
            Scanner sc = new Scanner(new File("apikey"));
            apiKey = sc.nextLine();
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        String destination = "42202+FM+1774+Magnolia+TX+77354";
        try {
            URL mapsAPI = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=" + Order.ORIGIN + "&destination=" + destination + "&units=metric&key=" + apiKey);
            URLConnection con = mapsAPI.openConnection();
            Scanner sc = new Scanner(con.getInputStream());

            StringBuilder bob = new StringBuilder();
            while (sc.hasNext()) {
                bob.append(sc.next());
            }

            JSONObject obj = new JSONObject(bob.toString());
            distance = ((JSONObject) obj.query("/routes/0/legs/0/distance")).getInt("value");
        } catch(Exception e){
            e.printStackTrace();}
        return distance;
    }
    public void addOrder(Integer o){
        pastOrders.add(o);
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<Integer> getPastOrders() {
        return pastOrders;
    }




}
