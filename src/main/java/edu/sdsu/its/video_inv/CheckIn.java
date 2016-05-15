package edu.sdsu.its.video_inv;

import com.google.gson.Gson;
import edu.sdsu.its.video_inv.Models.Item;
import edu.sdsu.its.video_inv.Models.Transaction;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Manage CheckIn Endpoints
 *
 * @author Tom Paulus
 *         Created on 3/10/16.
 */
@Path("/")
public class CheckIn {
    public static final Logger LOGGER = Logger.getLogger(CheckOut.class);
    private final Gson GSON = new Gson();

    /**
     * Retrieve the most recent outward transaction for an Item by its public ID
     *
     * @param pubID {@link String} Item's Public ID
     * @return {@link Response} Transaction JSON if found
     */
    @Path("transaction")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTransaction(@QueryParam("id") final String pubID) {
        LOGGER.info(String.format("Recieved GET Request to TRANSACTION - id = %s", pubID));

        int itemID;
        if (pubID.length() > 6) {
            // Supplied ID includes the checksum, we don't care about the checksum
            itemID = Integer.parseInt(pubID) / 10;
        } else if (pubID.length() == 6) {
            itemID = Integer.parseInt(pubID);
        } else {
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("{\n" +
                    "  \"message\": \"invalid ID Length\",\n" +
                    "}").build();
        }
        final Item item = DB.getItem(itemID)[0];
        if (!item.checked_out) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\n" +
                    "  \"message\": \"Designated item is not currently checked out\",\n" +
                    "}").build();
        }

        final Transaction transaction = DB.getTransactionByItem(item);
        if (transaction == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\n" +
                    "  \"message\": \"No matching transaction was found\",\n" +
                    "}").build();
        }

        return Response.status(Response.Status.OK).entity(GSON.toJson(transaction)).build();
    }

    /**
     * Add a new Check In Transaction to the Server
     *
     * @param payload {@link String} JSON Post Payload
     * @return {@link Response} Completed Transaction Record
     */
    @Path("checkin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTransaction(final String payload, @QueryParam("id") final int id) {
        LOGGER.debug("CHECKIN [POST] Recieved: " + payload);
        if (payload == null || payload.length() == 0) {
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("{\n" +
                    "  \"message\": \"empty payload\",\n" +
                    "}").build();
        }

        Transaction transaction = GSON.fromJson(payload, Transaction.class);
        transaction.id = id;

        final User ownerUser = DB.getUser(transaction.ownerID);
        if (transaction.ownerID == 0 || ownerUser == null) {
            LOGGER.warn("Invalid Owner ID");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("{\n" +
                    "  \"message\": \"invalid ownerID\",\n" +
                    "}").build();
        }
        final User supervisorUser = DB.getUser(transaction.in_components.supervisorID);
        if (transaction.in_components.supervisorID == 0 || supervisorUser == null || !supervisorUser.supervisor) {
            LOGGER.warn("Invalid Supervisor ID");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("{\n" +
                    "  \"message\": \"invalid supervisorID\",\n" +
                    "}").build();
        }

        DB.updateTransaction(transaction);
        transaction.in_components.items.forEach(DB::updateComments);

        for (Item i : transaction.in_components.items) i.checked_out = false;
        transaction.in_components.items.forEach(DB::updateItemStatus);

        return Response.status(Response.Status.ACCEPTED).build();
    }

}
