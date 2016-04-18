package edu.sdsu.its.video_inv.Models;

import java.sql.Timestamp;
import java.util.List;

/**
 * Models a Checkout Transaction
 *
 * @author Tom Paulus
 *         Created on 2/23/16.
 */
public class Transaction {
    public int id;
    public int ownerID;
    public int ownerPubID;
    public Component out_components;
    public Component in_components;
    public Timestamp out_time;
    public Timestamp in_time;

    public Transaction(int id, int ownerID, int ownerPubID, Component out_components, Component in_components, Timestamp out_time, Timestamp in_time) {
        this.id = id;
        this.ownerID = ownerID;
        this.ownerPubID = ownerPubID;
        this.out_components = out_components;
        this.in_components = in_components;
        this.out_time = out_time;
        this.in_time = in_time;
    }

    public static class Component {
        public int supervisorID;
        public List<Item> items;

        @Override
        public String toString() {
            // Used by DB.addTransaction()
            String transactionJSON = "{\n" +
                    "\"supervisorID\": " + supervisorID + ",\n" +
                    "\"items\": [";

            for (int i = 0; i < items.size(); i++) {
                transactionJSON += items.get(i).toString();
                if (i != (items.size() - 1)) {  // Omit the Comma in the last item
                    transactionJSON += ",\n";
                } else {
                    transactionJSON += "\n";
                }
            }
            transactionJSON += "]}";

            return transactionJSON;
        }
    }
}
