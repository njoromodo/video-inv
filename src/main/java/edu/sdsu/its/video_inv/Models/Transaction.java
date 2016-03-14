package edu.sdsu.its.video_inv.Models;

import java.util.List;

/**
 * Models a Checkout Transaction
 *
 * @author Tom Paulus
 *         Created on 2/23/16.
 */
public class Transaction {
    public int direction;
    public int id;
    public int ownerID;
    public int supervisorID;
    public List<Item> items;

    public Transaction(int direction, int id, int ownerID, int supervisorID, List<Item> items) {
        this.direction = direction;
        this.id = id;
        this.ownerID = ownerID;
        this.supervisorID = supervisorID;
        this.items = items;
    }
}
