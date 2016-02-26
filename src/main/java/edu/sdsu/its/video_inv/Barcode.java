package edu.sdsu.its.video_inv;

import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate Barcode for Inventory Tag
 * Uses Barcode4J (barcode4j.sourceforge.net)
 *
 * @author Tom Paulus
 *         Created on 2/25/16.
 */
public class Barcode {
    /**
     * Generate a Barcode with the Provided base Code
     *
     * @param code {@link Integer} Base Code (7 Digits)
     * @param outputFile {@link File} Output File for the Barcode, Type PNG
     * @return {@link Integer} Full Barcode Value, including Checksum
     * @throws IOException Thrown if there are problems writing to the file
     */
    public static Integer generateBarcode(final int code, final File outputFile) throws IOException {
        // ==== Calculate Value ====
        Integer barcodeValue = code * 10 + calcChecksum(code);


        // ==== Make Barcode Image ====
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
        bean.generateBarcode(canvas, "0" + barcodeValue.toString());


        // Signal end of generation
        canvas.finish();

        return barcodeValue;
    }

    public static void main(String[] args) {
        Integer code = 123456;
        try {
            File outFile = File.createTempFile("out", ".png");

            Integer generatedCode = generateBarcode(code, outFile);
            System.out.println("Generated Code: " + generatedCode);
            System.out.println("Path: " + outFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Problem Generating Code - " + e.getMessage());
        }

    }

    private static Integer[] getDigits(String number) {
        List<Integer> digits = new ArrayList<Integer>();
        for (int i = 0; i < number.length(); i++) {
            int j = Character.digit(number.charAt(i), 10);
            digits.add(j);
        }
        return digits.toArray(new Integer[digits.size()]);
    }

    private static int calcChecksum(final int code) {
        Integer[] codeDigits = getDigits(Integer.toString(code));

        int a = codeDigits[1] + codeDigits[3] + codeDigits[5];
        int b = codeDigits[0] + codeDigits[2] + codeDigits[4];

        return 10 - ((3 * a + b) % 10);
    }
}
