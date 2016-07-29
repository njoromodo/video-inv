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
    private static final String HASH_SALT = Param.getParam("hash_salt");
    private final Gson GSON = new Gson();

    /**
     * Get a User's Information based on their Public ID.
     *
     * @param pubID {@link String} User's Public Identifier
     * @return {@link Response} User Information as JSON
     */
    @Path("user")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@QueryParam("id") final String pubID) {
        LOGGER.info(String.format("Recieved GET Request to USER - id = %s", pubID));
        int userID;
        if (pubID.length() > 6) {
            // Supplied Checksum includes the checksum, we don't care about the checksum
            userID = Integer.parseInt(pubID) / 10;
        } else if (pubID.length() == 6) {
            userID = Integer.parseInt(pubID);
        } else {
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("{\n" +
                    "  \"message\": \"invalid ID Length\",\n" +
                    "}").build();
        }

        User user = DB.getUser(userID);
        if (user != null) {
            return Response.status(Response.Status.OK).entity(GSON.toJson(user)).build();
        } else {
            // If user is null, then the user was not found in the DB
            return Response.status(Response.Status.BAD_REQUEST).entity("{\n" +
                    "  \"message\": \"user does not exist\",\n" +
                    "}").build();
        }
    }

    @Path("allUsers")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        LOGGER.info("Recieved GET Request to ALLUSERS");

        User[] users = DB.getAllUsers("");
        return Response.status(Response.Status.OK).entity(GSON.toJson(users)).build();

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
    public Response getItem(@QueryParam("id") final String pubID) {
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
        Item[] items = DB.getItem("i.pub_id = " + itemID + " OR m.id = " + itemID);
        if (items.length > 0) {
            return Response.status(Response.Status.OK).entity(GSON.toJson(items)).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\n" +
                    "  \"message\": \"item does not exist\",\n" +
                    "}").build();
        }
    }

    /**
     * Verify the Supervisor's Pin
     *
     * @param payload {@link String} JSON Payload with Pin
     * @return {@link Response} User as JSON
     */
    @Path("verifyPin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyPin(final String payload) {
        User user = GSON.fromJson(payload, User.class);
        user = DB.getUser(hash(user.pin));

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
     * @param payload {@link String} {@see Models.Item} Item JSON
     */
    @Path("addItem")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addItem(final String payload) {
        Item item = GSON.fromJson(payload, Item.class);
        int id;
        do {
            Random rnd = new Random();
            id = 100000 + rnd.nextInt(900000);
        } while (DB.getItem("i.pub_id = " + id + " OR m.id = " + id)[0] != null); // Generate 6 Digit ID, and check that it doesn't already exist

        item.pubID = id;

        DB.addItem(item);

        return Response.status(Response.Status.CREATED).entity(GSON.toJson(item)).build();
    }

    /**
     * Add a new User to the System
     *
     * @param payload {@link String} {@see Models.User} User JSON
     * @return {@link Response} User JSON
     */
    @Path("addUser")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(final String payload) {
        User user = GSON.fromJson(payload, User.class);

        int id;
        do {
            Random rnd = new Random();
            id = 100000 + rnd.nextInt(900000);
        } while (DB.getUser(id) != null); // Generate 6 Digit ID, and check that it doesn't already exist
        user.pubID = id;

        DB.addUser(user, hash(user.pin));

        return Response.status(Response.Status.CREATED).entity(GSON.toJson(user)).build();
    }

    /**
     * Get a User's Label
     *
     * @param pubID {@link String} User's Public ID
     * @return {@link Response} Label XML
     */
    @Path("userLabel")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_XML)
    public Response getUserLabel(@QueryParam("id") final String pubID) {
        int userID;
        if (pubID.length() > 6) {
            // Supplied Checksum includes the checksum, we don't care about the checksum
            userID = Integer.parseInt(pubID) / 10;
        } else if (pubID.length() == 6) {
            userID = Integer.parseInt(pubID);
        } else {
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("{\n" +
                    "  \"message\": \"invalid ID Length\",\n" +
                    "}").build();
        }

        String xml = Label.generateUserLabel(userID);

        return Response.status(Response.Status.OK).entity(xml).build();
    }

    /**
     * Generate MD5 Hash with Salt.
     * <p>
     * Object toString will be used to create the hash
     *
     * @param obj {@link Object} Object to Hash
     * @return {@link String} Hashed String
     */
    private String hash(final Object obj) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] thedigest = md.digest(String.format(HASH_SALT, obj.toString()).getBytes());
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
