package edu.sdsu.its.video_inv;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

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
public class Label {
    public static final Logger LOGGER = Logger.getLogger(Label.class);

    private static String readFile(final String path) {
        InputStream inputStream = Label.class.getClassLoader().getResourceAsStream(path);
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    /**
     * Generate Label XML File for JS Framework
     *
     * @param id {@link int} Barcode ID (No Checksum)
     * @return {@link String} Label XML
     */
    public static String generateLabel(final int id) {
        LOGGER.info("Generating Template for ID: " + id);

        try {
            File barcode = File.createTempFile(Integer.toString(id), ".png");
            Barcode.generateBarcode("0" + Integer.toString(id), barcode);

            byte[] encodedBarcode = Base64.encodeBase64(FileUtils.readFileToByteArray(barcode));
            String encodedBarcodeString = new String(encodedBarcode, "UTF8");

            String template = readFile("ITS Asset Tags.label");

            return template.replace("{{barcode}}", encodedBarcodeString);
        } catch (IOException e) {
            LOGGER.error("Problem Generating Label File", e);
            return "";
        }
    }

    public static String generateUserLabel(final int id) {
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
}
