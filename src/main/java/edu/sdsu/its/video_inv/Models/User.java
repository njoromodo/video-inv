package edu.sdsu.its.video_inv.Models;

/**
 * Models a User of the Inventory System
 *
 * @author Tom Paulus
 *         Created on 2/23/16.
 */
public class User {
    public int dbID;
    public int pubID;

    public String firstName;
    public String lastName;

    public boolean supervisor;

    public User(int dbID, int pubID, String firstName, String lastName, boolean supervisor) {
        this.dbID = dbID;
        this.pubID = pubID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.supervisor = supervisor;
    }
}
