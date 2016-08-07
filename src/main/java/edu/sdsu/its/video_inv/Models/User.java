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

    @Override
    public String toString() {
        return "User{" +
                "dbID=" + dbID +
                ", pubID=" + pubID +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", supervisor=" + supervisor +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (dbID != user.dbID && pubID != user.pubID) return false;
        if (!firstName.equals(user.firstName)) return false;
        if (!lastName.equals(user.lastName)) return false;
        return supervisor != null ? supervisor.equals(user.supervisor) : user.supervisor == null;
    }

    @Override
    public int hashCode() {
        return dbID;
    }
}
