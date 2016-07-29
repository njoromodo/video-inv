package edu.sdsu.its.video_inv;

import com.google.gson.Gson;
import edu.sdsu.its.video_inv.Models.Macro;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Random;

/**
 * Macro Endpoints
 * Macros allow multiple items to be represented by a single id/barcode.
 *
 * @author Tom Paulus
 *         Created on 5/11/16.
 */
@Path("macros")
public class Macros {
    private static final Logger LOGGER = Logger.getLogger(Macros.class);

    /**
     * Generate and Return Macro Label XML for JS FrontEnd
     *
     * @param macroID {@link int} Macro ID
     * @return {@link Response} Label XML
     */
    @Path("getLabel")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_XML)
    public Response getLabel(@QueryParam("id") final int macroID) {
        return Response.status(Response.Status.OK).entity(Label.generateMacroLabel(macroID)).build();
    }

    /**
     * Get the list of all Macros.
     *
     * @return {@link Response} List of Macros {@see Models.Macro}
     */
    @Path("list")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listMacros() {
        Macro[] list = DB.getMacros();
        LOGGER.info(String.format("Request for Macros List returned %d items", list.length));

        Gson gson = new Gson();
        return Response.status(Response.Status.OK).entity(gson.toJson(list)).build();
    }

    /**
     * Create or Update a Macro
     * If the Macro ID is supplied in the Payload, the Macro is updated.
     * Else, a new Macro is created
     *
     * @param payload {@link String} POST Payload {@see Models.Macro}
     * @return {@link Response} Updated/Created Macro JSON
     */
    @Path("create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMacro(final String payload) {
        LOGGER.debug(String.format("Create Payload - \"%s\"", payload));

        Response.ResponseBuilder status;

        Gson gson = new Gson();
        Macro macro = gson.fromJson(payload, Macro.class);
        if (macro.id != 0) {
            // Update Macro
            DB.updateMacro(macro);
            LOGGER.info("Updated Macro with ID: " + macro.id);
            status = Response.status(Response.Status.ACCEPTED);
        } else {
            // Create Macro
            Macro[] macros = DB.getMacros();
            int id = 0;
            boolean exists = false;

            while (id == 0 || exists) {
                // Generate 6 Digit ID, and check that it doesn't already exist
                Random rnd = new Random();
                id = 100000 + rnd.nextInt(900000);

                for (Macro m : macros) if (m.id == id) exists = true;
                if (DB.getItem("i.pub_id = " + id)[0] != null) exists = true;
            }
            LOGGER.debug("Creating Macro with ID: " + id);
            macro.id = id;
            DB.createMacro(macro);
            status = Response.status(Response.Status.CREATED);
        }

        return status.entity(gson.toJson(macro)).build();
    }
}
