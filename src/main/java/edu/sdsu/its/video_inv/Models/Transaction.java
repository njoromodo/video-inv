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
    public String id;
    public User owner;
    public User supervisor;
    public Timestamp time;
    public boolean direction; // 0 for our; 1 for in
    public List<Component> components;

    public Transaction(String id, User owner, User supervisor, Timestamp time, boolean direction) {
        this.id = id;
        this.owner = owner;
        this.supervisor = supervisor;
        this.time = time;
        this.direction = direction;
    }


    public static class Component {
       public int id;
        public int pubID;
        public Category category;

        public String name;
        public String comments;

        public Component(int id, int pubID, Category category, String name, String comments) {
            this.id = id;
            this.pubID = pubID;
            this.category = category;
            this.name = name;
            this.comments = comments;
        }
    }
}
