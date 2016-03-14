package edu.sdsu.its.video_inv;

import com.google.gson.Gson;
import edu.sdsu.its.video_inv.Models.Transaction;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Manage Checkout EndPoints
 *
 * @author Tom Paulus
 *         Created on 3/10/16.
 */
@Path("/")
public class CheckOut {
    public static final Logger LOGGER = Logger.getLogger(CheckOut.class);
    private final Gson GSON = new Gson();

    /**
     * Add a new Checkout Transaction to the Server
     *
     * @param payload {@link String} JSON Post Payload
     * @return {@link Response} Completed Transaction Record
     */
    @Path("checkout")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTransaction(final String payload) {
        LOGGER.debug("CHECKOUT [POST] Recieved: " + payload);
        if (payload == null || payload.length() == 0) {
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("{\n" +
                    "  \"message\": \"empty payload\",\n" +
                    "}").build();
        }

        Transaction transaction = GSON.fromJson(payload, Transaction.class);

        final User ownerUser = DB.getUser(transaction.ownerID);
        if (transaction.ownerID == 0 || ownerUser == null) {
            LOGGER.warn("Invalid Owner ID");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("{\n" +
                    "  \"message\": \"invalid ownerID\",\n" +
                    "}").build();
        }
        final User supervisorUser = DB.getUser(transaction.supervisorID);
        if (transaction.supervisorID == 0 || supervisorUser == null || !supervisorUser.supervisor) {
            LOGGER.warn("Invalid Supervisor ID");
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("{\n" +
                    "  \"message\": \"invalid supervisorID\",\n" +
                    "}").build();
        }

        transaction.direction = 0;
        DB.addTransaction(transaction);
        transaction.items.forEach(DB::updateComments);

        return Response.status(Response.Status.ACCEPTED).build();
    }
}
