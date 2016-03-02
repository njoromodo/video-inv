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
import java.util.Random;

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
        Item item = DB.getItem(pubID / 10);
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

    /**
     * Add Inventory Item
     *
     * @param name {@link String} Item Name
     * @return {@link Response} Item JSON
     */
    @Path("addItem")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addItem(@QueryParam("name") final String name) {
        int id;
        do {
            Random rnd = new Random();
            id = 100000 + rnd.nextInt(900000);
        } while (DB.getItem(id) != null); // Generate 6 Digit ID, and check that it doesn't already exist

        Item item = new Item(id, name);

        DB.addItem(item);

        return Response.status(Response.Status.CREATED).entity(GSON.toJson(item)).build();
    }

    /**
     * Get Asset Tag Label XML
     *
     * @param pubID {@link String} Public Identifier
     * @return {@link Response} Label XML
     */
    @Path("label")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_XML)
    public Response getLabel(@QueryParam("id") final String pubID) {
        int itemID;
        if (pubID.length() > 6) {
            // Supplied Checksum includes the checksum, we don't care about the checksum
            itemID = Integer.parseInt(pubID) / 10;
        } else if (pubID.length() == 6) {
            itemID = Integer.parseInt(pubID);
        } else {
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("{\n" +
                    "  \"message\": \"invalid ID Length\",\n" +
                    "}").build();
        }

        String xml = Label.generateLabel(itemID);

        return Response.status(Response.Status.OK).entity(xml).build();
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
