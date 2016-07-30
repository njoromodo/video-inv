package edu.sdsu.its.video_inv.API;

/**
 * Simple JSON Object that can be used to send a message when a JSON object is expected by the Client.
 *
 * @author Tom Paulus
 *         Created on 7/29/16.
 */
@SuppressWarnings("WeakerAccess")
public class SimpleMessage {
    private String status = null;
    private String message;

    public SimpleMessage(String message) {
        this.message = message;
    }

    public SimpleMessage(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
