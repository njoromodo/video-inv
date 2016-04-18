package edu.sdsu.its.video_inv;

import com.google.gson.Gson;
import edu.sdsu.its.video_inv.Models.Item;
import edu.sdsu.its.video_inv.Models.Transaction;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Reporting API Endpoints
 *
 * @author Tom Paulus
 *         Created on 3/16/16.
 */
@Path("reports")
public class Reports {
    private static final Logger LOGGER = Logger.getLogger(Reports.class);
    private static final Gson GSON = new Gson();

    /**
     * List all Items in the DB as a JSON Array
     *
     * @return {@link Response} All Items JSON
     */
    @GET
    @Path("inventory")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInventory() {
        Item[] inventory = DB.getInventory();
        LOGGER.debug(String.format("Inventory Request returned %d items", inventory.length));
        return Response.status(Response.Status.OK).entity(GSON.toJson(inventory)).build();
    }

    /**
     * Access an Items Transaction History and return a Transaction Array as JSON
     *
     * @param itemID {@link int} Item's Public ID
     * @return {@link Response} Transaction History for an Item JSON
     */
    @GET
    @Path("history")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItemHistory(@QueryParam("id") final int itemID) {
        Item item = DB.getItem(itemID);
        Transaction[] history = DB.getHistory(item);
        LOGGER.debug(String.format("Item History for %s(%d) returned %d transactions", item.name, item.id, history.length));
        return Response.status(Response.Status.OK).entity(GSON.toJson(history)).build();
    }

    @GET
    @Path("transaction")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTransaction(@QueryParam("id") final int transactionID) {
        Transaction transaction = DB.getTransactionByID(transactionID);
        return Response.status(Response.Status.OK).entity(GSON.toJson(transaction)).build();
    }
}
