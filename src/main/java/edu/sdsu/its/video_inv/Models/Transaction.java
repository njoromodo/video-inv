package edu.sdsu.its.video_inv.Models;

import java.util.List;

/**
 * Models a Checkout Transaction
 *
 * @author Tom Paulus
 *         Created on 2/23/16.
 */
public class Transaction {
    public int ownerID;
    public int supervisorID;
    public List<Item> items;
}
