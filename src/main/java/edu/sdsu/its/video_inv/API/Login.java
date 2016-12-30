package edu.sdsu.its.video_inv.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Create initial sessions for users and verify pins from supervisors.
 *
 * @author Tom Paulus
 *         Created on 8/4/16.
 */
@Path("/")
public class Login {
    private static final Logger LOGGER = Logger.getLogger(Login.class);
    private final Gson mGson;

    public Login() {
        final GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        this.mGson = builder.create();
    }

    /**
     * Login a User via their public identifier
     *
     * @param payload {@link String} Login JSON Object - A User object with the Username(username) and Password {@see Models.User}
     * @return {@link Response} User JSON Object {@see Models.User} and Session Token (Header)
     */
    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(final String payload) {
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(mGson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();
        User user = mGson.fromJson(payload, User.class);
        if (user.username == null)
            return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("Error", "No valid identifier supplied"))).build();

        User loginUser = user.login();
        if ( loginUser == null) return Response.status(Response.Status.NOT_FOUND).entity(mGson.toJson(new SimpleMessage("Error", "That user does not exist or the password is incorrect."))).build();

        Session session = new Session(loginUser);
        return Response.status(Response.Status.OK).entity(mGson.toJson(loginUser)).header("session", session.getToken()).build();

    }

//    /**
//     * Verify a supervisor's PIN to verify their identity and authorize a transaction, or other secure action.
//     * The Supervisor's Public ID and their PIN must be included in the Payload
//     *
//     * @param sessionToken {@link String} User Session Token
//     * @param payload      {@link String} Supervisor User JSON Object (With ID and PIN) {@see Models.Users}
//     * @return {@link Response} Supervisor User JSON Object if Valid
//     */
//    @Path("verifyPin")
//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response verifyPin(@HeaderParam("session") final String sessionToken,
//                              final String payload) {
//        User user = Session.validate(sessionToken);
//        if (user == null || user.username != 0) {
//            return Response.status(Response.Status.UNAUTHORIZED).entity(mGson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
//        }
//        LOGGER.info("Recieved request to Verify Pin");
//        LOGGER.debug("Verify Payload: " + payload);
//
//        User supervisor = mGson.fromJson(payload, User.class);
//        if (supervisor.username == 0)
//            return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("Error", "No valid identifier supplied"))).build();
//        User[] verifyUser = DB.getUser("id = " + user.username);
//        if (verifyUser.length == 0)
//            return Response.status(Response.Status.NOT_FOUND).entity(mGson.toJson(new SimpleMessage("Error", "That user does not exist"))).build();
//
//        if (DB.checkPin(verifyUser[0], supervisor.getPin())) {
//            LOGGER.info(String.format("Pin for Supervisor %s %s is Valid", verifyUser[0].firstName, verifyUser[0].lastName));
//            return Response.status(Response.Status.OK).entity(mGson.toJson(verifyUser)).build();
//        } else {
//            LOGGER.warn(String.format("Pin for Supervisor %s %s is NOT Valid", verifyUser[0].firstName, verifyUser[0].lastName));
//            return Response.status(Response.Status.UNAUTHORIZED).entity(mGson.toJson(new SimpleMessage("Error", "PIN invalid"))).build();
//        }
//
//    }

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

