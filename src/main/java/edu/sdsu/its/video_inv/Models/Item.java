package edu.sdsu.its.video_inv.Models;

import edu.sdsu.its.video_inv.DB;

/**
 * Models an Item in the DB
 *
 * @author Tom Paulus
 *         Created on 2/23/16.
 */
public class Item {
    public int id;
    public int pubID;

    public String name;
    public String shortName;
    public String comments;

    public Item(int id, int pubID, String name, String shortName, String comments) {
        this.id = id;
        this.pubID = pubID;
        this.name = name;
        this.shortName = shortName;
        this.comments = comments;
    }

    public Item(int pubID, String name, String shortName) {
        this.pubID = pubID;
        this.name = name;
        this.shortName = shortName;
    }

    public void completeItem() {
        Item item = null;
        if (this.id == 0 && this.pubID != 0) {
            item = DB.getItem(pubID);
            this.id = item.id;
        } else if (this.id != 0) {
            item = DB.getItemByDB(this.id);
            this.pubID = item.pubID;
        }
        if (this.name == null) {
            if (item == null) {
                DB.getItemByDB(this.id);
            }
            if (item != null) {
                this.name = item.name;
            }
        }
    }

    @Override
    public String toString() {
        // Used by DB.addTransaction()
        if (this.id == 0) this.id = DB.getItem(this.pubID).id;

        return String.format("{\n" +
                "  \"id\": %d,\n" +
                "  \"comments\": \"%s\"\n" +
                "}", this.id, this.comments);
    }
}
