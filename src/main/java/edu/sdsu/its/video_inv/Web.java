package edu.sdsu.its.video_inv;

import com.google.gson.Gson;
import edu.sdsu.its.video_inv.Models.Item;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Primary Web Interface
 *
 * @author Tom Paulus
 *         Created on 2/22/16.
 */
@Path("/")
public class Web {
    private static final Logger LOGGER = Logger.getLogger(Web.class);
    private final Gson GSON = new Gson();
    private final String HASH_SALT = Param.getParam("video_inv", "hash_salt");

    /**
     * Get a User's Information based on their Public ID.
     *
     * @param pubID {@link Integer} User's Public Identifier
     * @return {@link Response} User Information as JSON
     */
    @Path("user")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@QueryParam("id") final Integer pubID) {
        LOGGER.info(String.format("Recieved GET Request to USER - id = %d", pubID));
        User user = DB.getUser(pubID);
        if (user != null) {
            return Response.status(Response.Status.OK).entity(GSON.toJson(user)).build();
        } else {
            // If user is null, then the user was not found in the DB
            return Response.status(Response.Status.BAD_REQUEST).entity("{\n" +
                    "  \"message\": \"user does not exist\",\n" +
                    "}").build();
        }
    }

    /**
     * Get an Inventory Item's Information based on its Public ID.
     *
     * @param pubID {@link Integer} Item's Public Identifier
     * @return {@link Response} Item's Information as JSON
     */
    @Path("item")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItem(@QueryParam("id") final Integer pubID) {
        Item item = DB.getItem(pubID);
        if (item != null) {
            return Response.status(Response.Status.OK).entity(GSON.toJson(item)).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\n" +
                    "  \"message\": \"item does not exist\",\n" +
                    "}").build();
        }
    }

    /**
     * Verify the Supervisor's Pin
     *
     * @param pin {@link String} Supervisor's Pin
     * @return {@link Response} User as JSON
     */
    @Path("verifyPin")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyPin(@QueryParam("pin") final String pin) {
        User user = DB.getUser(hash(pin));

        if (user != null) {
            return Response.status(Response.Status.OK).entity(GSON.toJson(user)).build();
        } else {
            // If user is null, then the user was not found in the DB
            return Response.status(Response.Status.BAD_REQUEST).entity("{\n" +
                    "  \"message\": \"pin does not exist\",\n" +
                    "}").build();
        }
    }

    private String hash(final String string) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] thedigest = md.digest(String.format(HASH_SALT, string).getBytes());
            final StringBuilder sb = new StringBuilder();
            for (byte aThedigest : thedigest) {
                sb.append(Integer.toHexString((aThedigest & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
