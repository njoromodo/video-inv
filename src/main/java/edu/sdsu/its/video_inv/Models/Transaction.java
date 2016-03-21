package edu.sdsu.its.video_inv.Models;

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
    public Component out_components;
    public Component in_components;

    public Transaction(int id, int ownerID, Component out_components, Component in_components) {
        this.id = id;
        this.ownerID = ownerID;
        this.out_components = out_components;
        this.in_components = in_components;
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
