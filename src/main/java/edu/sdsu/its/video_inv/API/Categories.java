package edu.sdsu.its.video_inv.API;

import com.google.gson.Gson;
import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.Category;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Category Endpoints (List, Create, Update, and Delete)
 * All endpoints that modify content (All excluding GET) require supervisor privileges.
 *
 * @author Tom Paulus
 *         Created on 7/29/16.
 */
@Path("category")
public class Categories {
    private static final Logger LOGGER = Logger.getLogger(Categories.class);

    /**
     * List all, or a specific category.
     * If no category ID is supplied, all categories will be returned.
     *
     * @param sessionToken {@link String} User Session Token
     * @param catID        {@link Integer} Category ID (Optional)
     * @return {@link Response} JSON Array of Categories {@see Models.Category}
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategories(@HeaderParam("session") final String sessionToken,
                                  @QueryParam("id") final Integer catID) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null || user.pubID != 0) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info(String.format("Recieved Request for Macros in DB Where ID=%d", catID));

        Category[] categories = DB.getCategory(catID);
        if (categories.length == 0)
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(new SimpleMessage("Error", "No Category with that ID was found."))).build();

        return Response.status(Response.Status.OK).entity(gson.toJson(categories)).build();
    }

    /**
     * Create a new Category. The Category's ID will be determined by the DB and returned to the user.
     * The category must have a name defined.
     * No checks for duplicates are preformed, and are allowed.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} JSON Category Object {@see Models.Category}
     * @return {@link Response} Created JSON Category Object {@see Models.Category}
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCategory(@HeaderParam("session") final String sessionToken,
                                   final String payload) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null || user.pubID != 0) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();
        LOGGER.info("Recieved Request to Create Category");
        LOGGER.debug("Category Payload: " + payload);

        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(gson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }

        Category createCategory = gson.fromJson(payload, Category.class);
        if (createCategory.name == null || createCategory.name.length() == 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "No Category Name was Supplied"))).build();
        createCategory.id = DB.createCategory(createCategory).id;

        return Response.status(Response.Status.CREATED).entity(gson.toJson(createCategory)).build();
    }

    /**
     * Update a Category. The Category's ID must exist.
     * The category must have a name defined.
     * No checks for duplicates are preformed, and are allowed.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} JSON Category Object {@see Models.Category}
     * @return {@link Response} Status Message
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCategory(@HeaderParam("session") final String sessionToken,
                                   final String payload) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null || user.pubID != 0) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();
        LOGGER.info("Recieved Request to Update Category");
        LOGGER.debug("Category Payload: " + payload);

        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(gson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }

        Category updateCategory = gson.fromJson(payload, Category.class);
        if (updateCategory.name == null || updateCategory.name.length() == 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "No Category Name was Supplied"))).build();
        if (DB.getCategory(updateCategory.id).length == 0)
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(new SimpleMessage("Error", "Category Does not Exist"))).build();
        DB.updateCategory(updateCategory);

        return Response.status(Response.Status.OK).entity(gson.toJson(new SimpleMessage("Category Updated"))).build();
    }

    /**
     * Delete a Category. The Category ID must exist.
     * The Category's name does not need to be defined in the request payload.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} JSON Category Object {@see Models.Category}
     * @return {@link Response} Status Message
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCategory(@HeaderParam("session") final String sessionToken,
                                   final String payload) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null || user.pubID != 0) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();
        LOGGER.info("Recieved Request to Delete Category");
        LOGGER.debug("Category Payload: " + payload);

        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(gson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }

        Category deleteCategory = gson.fromJson(payload, Category.class);

        if (DB.getItem("i.category = " + deleteCategory.id).length > 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "Category has items. Cannot Delete."))).build();
        if (DB.getCategory(deleteCategory.id).length == 0)
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(new SimpleMessage("Error", "Category Does not Exist"))).build();
        DB.deleteCategory(deleteCategory);

        return Response.status(Response.Status.OK).entity(gson.toJson(new SimpleMessage("Category Deleted"))).build();
    }
}
