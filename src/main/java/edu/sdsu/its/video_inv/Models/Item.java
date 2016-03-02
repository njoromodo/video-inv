package edu.sdsu.its.video_inv.Models;

/**
 * TODO JavaDoc
 *
 * @author Tom Paulus
 *         Created on 2/23/16.
 */
public class Item {
    public int dbID;
    public int pubID;

    public String name;
    public String comments;

    public Item(int dbID, int pubID, String name, String comments) {
        this.dbID = dbID;
        this.pubID = pubID;
        this.name = name;
        this.comments = comments;
    }

    public Item(int pubID, String name) {
        this.pubID = pubID;
        this.name = name;
    }
}
