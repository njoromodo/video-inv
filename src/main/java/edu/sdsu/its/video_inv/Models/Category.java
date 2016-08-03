package edu.sdsu.its.video_inv.Models;

/**
 * Models an Item Category.
 * Items can have at most one category.
 *
 * @author Tom Paulus
 *         Created on 7/27/16.
 */
public class Category {
    public int id;
    public String name;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
