package edu.sdsu.its.video_inv;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Make Dymo Labels
 *
 * @author Tom Paulus
 *         Created on 2/26/16.
 */
@Path("/")
public class Label {
    public static final Logger LOGGER = Logger.getLogger(Label.class);

    private static String readFile(final String path) {
        InputStream inputStream = Label.class.getClassLoader().getResourceAsStream(path);
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    /**
     * Generate Label XML for an item sticker for the JS Framework
     *
     * @param name {@link String} Short Name (If null, prints 'ITS Video')
     * @param id   {@link int} Barcode ID (No Checksum)
     * @return {@link String} Label XML
     */
    static String generateItemLabel(final String name, final int id) {
        LOGGER.info("Generating Template for ID: " + id);

        try {
            File barcode = File.createTempFile(Integer.toString(id), ".png");
            Barcode.generateBarcode("0" + Integer.toString(id), barcode);

            byte[] encodedBarcode = Base64.encodeBase64(FileUtils.readFileToByteArray(barcode));
            String encodedBarcodeString = new String(encodedBarcode, "UTF8");

            String template = readFile("ITS Asset Tags.label");


            return template.replace("{{short_name}}", (name != null && name.length() != 0) ? name : "ITS Video").replace("{{barcode}}", encodedBarcodeString);
        } catch (IOException e) {
            LOGGER.error("Problem Generating Label File", e);
            return "";
        }
    }


    /**
     * Generate Label XML for a User Sticker for the JS Framework
     *
     * @param id {@link int} User ID (No Checksum)
     * @return {@link String} Label XML
     */
    static String generateUserLabel(final int id) {
        LOGGER.info("Generating User Template for ID: " + id);

        try {
            File barcode = File.createTempFile(Integer.toString(id), ".png");
            Barcode.generateBarcode("0" + Integer.toString(id), barcode);

            byte[] encodedBarcode = Base64.encodeBase64(FileUtils.readFileToByteArray(barcode));
            String encodedBarcodeString = new String(encodedBarcode, "UTF8");

            String template = readFile("User Tags.label");

            return template.replace("{{barcode}}", encodedBarcodeString);
        } catch (IOException e) {
            LOGGER.error("Problem Generating Label File", e);
            return "";
        }
    }

    /**
     * Generate Label XML for an Item Macro for the JS Framework
     *
     * @param id {@link int} Macro ID (No Checksum)
     * @return {@link String} Label XML
     */
    static String generateMacroLabel(final int id) {
        return generateItemLabel("Macro", id);
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

        String xml = Label.generateItemLabel(DB.getItem("i.pub_id = " + itemID)[0].shortName, itemID);

        return Response.status(Response.Status.OK).entity(xml).build();
    }
}
