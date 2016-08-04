package edu.sdsu.its.video_inv.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Macros represent a group of multiple Items and are returned as a JSON Array of itemIDs.
 *
 * @author Tom Paulus
 *         Created on 5/11/16.
 */
public class Macro {
    public int id;
    public String name;
    public Integer[] items;

    public Macro(int id, String name, String items) {
        this.id = id;
        this.name = name;
        List<Integer> list = new ArrayList<>();
        for (String i : items
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "")
                .split(",")) {
            list.add(Integer.parseInt(i));
        }
        this.items = list.toArray(new Integer[]{});
    }
}
