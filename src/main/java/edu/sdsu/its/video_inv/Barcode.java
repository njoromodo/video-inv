package edu.sdsu.its.video_inv;

import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

import javax.ws.rs.Path;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Generate Barcode for Inventory Tag
 * Uses Barcode4J (barcode4j.sourceforge.net)
 *
 * @author Tom Paulus
 *         Created on 2/25/16.
 */
@Path("barcode")
public class Barcode {
    /**
     * Generate a Barcode with the Provided base Code
     *
     * @param code       {@link Integer} Base Code (7 Digits)
     * @param outputFile {@link File} Output File for the Barcode, Type PNG
     * @return {@link Integer} Full Barcode Value, including Checksum
     * @throws IOException Thrown if there are problems writing to the file
     */
    public static void generateBarcode(final int code, final File outputFile) throws IOException {
        // Create the barcode bean
        UPCEBean bean = new UPCEBean();
        final int dpi = 600;
        bean.doQuietZone(true);

        // Open output file
        OutputStream out = new FileOutputStream(outputFile);
        // Set up the canvas provider for monochrome PNG output
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                out, "image/png", dpi, BufferedImage.TYPE_BYTE_BINARY, true, 0);

        // Generate the barcode
        bean.generateBarcode(canvas, "0" + Integer.toString(code));


        // Signal end of generation
        canvas.finish();
    }

    public static void main(String[] args) {
        Integer code = 123456;
        try {
            File outFile = File.createTempFile("out", ".png");

            generateBarcode(code, outFile);
            System.out.println("Path: " + outFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Problem Generating Code - " + e.getMessage());
        }

    }
}
