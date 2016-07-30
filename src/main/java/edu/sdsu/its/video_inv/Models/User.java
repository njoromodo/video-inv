package edu.sdsu.its.video_inv.Models;

import com.google.gson.annotations.Expose;

/**
 * Models a User of the Inventory System
 *
 * @author Tom Paulus
 *         Created on 2/23/16.
 */
public class User {
    @Expose
    public int dbID;

    @Expose
    public int pubID;

    @Expose
    public String firstName;
    @Expose
    public String lastName;

    @Expose
    public Boolean supervisor;

    @Expose(serialize = false)
    private String pin;

    public User(int dbID, int pubID, String firstName, String lastName, boolean supervisor) {
        this.dbID = dbID;
        this.pubID = pubID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.supervisor = supervisor;
    }

    public User(int pubID, String firstName, String lastName, boolean supervisor) {
        this.pubID = pubID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.supervisor = supervisor;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
