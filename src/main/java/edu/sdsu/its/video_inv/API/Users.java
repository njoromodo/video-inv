package edu.sdsu.its.video_inv.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Label;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Random;

/**
 * User Endpoints (List, Create, Update, and Label)
 * Session Tokens are needed for all endpoints, and all endpoints that make modifications to the User need to be made
 * by a Supervisor.
 *
 * @author Tom Paulus
 *         Created on 7/29/16.
 */
@Path("user")
public class Users {
    private static final Logger LOGGER = Logger.getLogger(Users.class);
    private final Gson mGson;

    public Users() {
        final GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        this.mGson = builder.create();
    }

    /**
     * Get all users, or a specific user based on their public or their internal identifier.
     *
     * @param sessionToken {@link String} User Session Token
     * @param publicID     {@link int} User's Public (Barcode) Identifier
     * @param dbID         {@link int} User's Internal Identifier
     * @return {@link Response} JSON Array of Users
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@HeaderParam("session") final String sessionToken,
                            @QueryParam("id") final int publicID,
                            @QueryParam("db-id") final int dbID) {
        User user = Session.validate(sessionToken);
        if (user == null || user.pubID != 0) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(mGson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info(String.format("Recieved Request for User in DB Where PublicID=%d OR dbID=%d", publicID, dbID));

        User[] users;

        if (dbID != 0) {
            final String restriction = "id = " + dbID;

            LOGGER.debug(String.format("Retrieving Users with Internal ID Restriction, \"%s\"", restriction));
            users = DB.getUser(restriction);
        } else if (publicID != 0) {
            int id = formatID(publicID);
            if (intLength(id) != 6) {
                return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("Error", "Invalid ID Length"))).build();
            }
            final String restriction = "pub_id = " + id;

            LOGGER.debug(String.format("Retrieving Users with Public ID Restriction, \"%s\"", restriction));
            users = DB.getUser(restriction);
        } else {
            LOGGER.debug("Retrieving all users in DB");
            users = DB.getUser(null);
        }

        if (users.length > 0) {
            return Response.status(Response.Status.OK).entity(mGson.toJson(user)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(mGson.toJson(new SimpleMessage("Error", "No user exists with the specified ID"))).build();
        }
    }

    /**
     * Create a new User
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} User JSON {@see Models.User}
     * @return {@link Response} Status Message
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@HeaderParam("session") final String sessionToken,
                               final String payload) {
        User user = Session.validate(sessionToken);
        if (user == null || user.pubID != 0) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(mGson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(mGson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }

        LOGGER.info("Recieved Request to create new Item");
        LOGGER.debug("POST Payload: " + payload);

        User createUser = mGson.fromJson(payload, User.class);
        int id;
        do {
            Random rnd = new Random();
            id = 100000 + rnd.nextInt(900000);
        }
        while (DB.getUser("pub_id = " + id).length > 0); // Generate 6 Digit ID, and check that it doesn't already exist
        user.pubID = id;

        DB.createUser(createUser);

        return Response.status(Response.Status.CREATED).entity(mGson.toJson(new SimpleMessage("User Created Successfully"))).build();
    }

    /**
     * Update a User. The Internal Identifier (DB ID) cannot be changed since it is used to identify the user to update.
     * Either the user's current Public ID, or their internal identifier needs to be supplied to update the user.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} User JSON {@see Models.User}
     * @return {@link Response} Status Message
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@HeaderParam("session") final String sessionToken,
                               final String payload) {
        User user = Session.validate(sessionToken);
        if (user == null || user.pubID != 0) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(mGson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }

        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(mGson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }

        User updateUser = mGson.fromJson(payload, User.class);
        if (updateUser.dbID == 0 && updateUser.pubID == 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("Error", "No Identifier supplied"))).build();
        if (DB.getUser("pub_id = " + user.pubID + " OR id = " + user.dbID).length == 0)
            return Response.status(Response.Status.NOT_FOUND).entity(mGson.toJson(new SimpleMessage("Error", "User does not exist"))).build();
        if (updateUser.dbID == 0) {
            int id = formatID(updateUser.pubID);
            if (intLength(id) != 6)
                return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("Error", "Invalid ID Length"))).build();

            updateUser.dbID = DB.getUser("pub_id = " + id)[0].dbID;
        }

        DB.updateUser(updateUser);

        return Response.status(Response.Status.OK).entity(mGson.toJson(new SimpleMessage("User Updated"))).build();
    }


    /**
     * Get the DYMO Label XML for a User Label
     *
     * @param sessionToken {@link String} User Session Token
     * @param userID       {@link int} User's Public Identifier
     * @return {@link Response} Label XML
     */
    @Path("label")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_XML)
    public Response getUserLabel(@HeaderParam("session") final String sessionToken,
                                 @QueryParam("id") int userID) {
        User user = Session.validate(sessionToken);
        if (user == null || user.pubID != 0) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(mGson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }

        userID = formatID(userID);
        if (intLength(userID) != 6) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("Error", "Invalid ID Length"))).build();
        }

        return Response.status(Response.Status.OK).entity(Label.generateUserLabel(userID)).build();
    }

    /**
     * All Core IDs are 6 digits, but barcodes have an 8 digit ID, which is scanned by the barcode reader.
     * For BarcodeIDs, the first digit is always 0 and the last digit is the checksum. This last digit is the
     * one that needs to be discarded.
     *
     * @param rawID {@link int} Original
     * @return {@link int} 6-digit ID
     */
    private int formatID(int rawID) {
        if (rawID / (int) Math.pow(10, 7) > 0)
            return rawID / 10;
        return 0;
    }

    private int intLength(int i) {
        if (i == 0) return 0;
        if (i < 0) i = i * -1;

        return (int) (Math.log10(i) + 1);
    }
}
