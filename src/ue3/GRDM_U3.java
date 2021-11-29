package ue3;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

/**
 Opens an image window and adds a panel below the image
 */
public class GRDM_U3 implements PlugIn {

    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;

    String[] items = {"Original", "Rot-Kanal", "Graustufen", "Binär", "Binär mit Fehlerdiffusion", "Sepia", "8 Farben Bild"};


    public static void main(String args[]) {
        IJ.open("../resources/Bear.jpg");

        GRDM_U3 pw = new GRDM_U3();
        pw.imp = IJ.getImage();
        pw.run("");
    }

    public void run(String arg) {
        if (imp==null)
            imp = WindowManager.getCurrentImage();
        if (imp==null) {
            return;
        }
        CustomCanvas cc = new CustomCanvas(imp);

        storePixelValues(imp.getProcessor());

        new CustomWindow(imp, cc);
    }


    private void storePixelValues(ImageProcessor ip) {
        width = ip.getWidth();
        height = ip.getHeight();
        origPixels = ((int []) ip.getPixels()).clone();
    }


    class CustomCanvas extends ImageCanvas {
        CustomCanvas(ImagePlus imp) {
            super(imp);
        }
    }


    class CustomWindow extends ImageWindow implements ItemListener {

        private String method;

        CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }

        void addPanel() {
            //JPanel panel = new JPanel();
            Panel panel = new Panel();

            JComboBox cb = new JComboBox(items);
            panel.add(cb);
            cb.addItemListener(this);

            add(panel);
            pack();
        }

        public void itemStateChanged(ItemEvent evt) {
            // Get the affected item
            Object item = evt.getItem();

            if (evt.getStateChange() == ItemEvent.SELECTED) {
                System.out.println("Selected: " + item.toString());
                method = item.toString();
                changePixelValues(imp.getProcessor());
                imp.updateAndDraw();
            }

        }


        private void changePixelValues(ImageProcessor ip) {
            // Array zum Zurückschreiben der Pixelwerte
            int[] pixels = (int[])ip.getPixels();

            if (method.equals("Original")) {
                pixels = processOriginal(pixels);
            }

            if (method.equals("Rot-Kanal")) {
                pixels = processRedChannel(pixels);
            }

            if (method.equals("Graustufen")) {
                pixels = processGrayscale(pixels);
            }

            if (method.equals("Binär")) {
                pixels = processBinary(pixels);
            }

            if (method.equals("Binär mit Fehlerdiffusion")) {
                pixels = processBinaryErrorDiffusion(pixels);
            }

            if (method.equals("Sepia")) {
                pixels = processSepia(pixels);
            }

            if (method.equals("8 Farben Bild")) {
                pixels = process8Colors(pixels);
            }
        }

        /**
         * Returns Image as original
         * @param pixels
         * @return pixel Array
         */
        int[] processOriginal (int[] pixels) {
            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x++) {
                    int pos = y*width + x;

                    pixels[pos] = origPixels[pos];
                }
            }
            return pixels;
        }

        /**
         * Returns Image as a red image
         * @param pixels
         * @return pixel Array
         */
        int[] processRedChannel (int[] pixels) {
            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x++) {
                    int pos = y*width + x;
                    int argb = origPixels[pos];

                    int r = (argb >> 16) & 0xff;

                    int rn = r;
                    int gn = 0;
                    int bn = 0;

                    pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                }
            }
            return pixels;
        }

        /**
         * Returns Image as grayscale Image
         * @param pixels
         * @return pixel Array
         */
        int[] processGrayscale (int[] pixels) {
            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x++) {
                    int pos = y*width + x;
                    int argb = origPixels[pos];

                    int r = (argb >> 16) & 0xff;
                    int g = (argb >>  8) & 0xff;
                    int b =  argb        & 0xff;

                    // calculate the average of the pixel's 3 channels to receive the grayscale
                    int average = (r + g + b) / 3;

                    int rn = average;
                    int gn = average;
                    int bn = average;

                    pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                }
            }
            return pixels;
        }

        /**
         * Returns Image as a binary image (black and white)
         * @param pixels
         * @return pixel Array
         */
        int[] processBinary (int[] pixels) {
            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x++) {
                    int pos = y*width + x;
                    int argb = origPixels[pos];

                    int r = (argb >> 16) & 0xff;
                    int g = (argb >>  8) & 0xff;
                    int b =  argb        & 0xff;

                    // calculate the average of the pixel's 3 channels to receive the grayscale
                    int average = Math.round((r + g + b) / 3);
                    // Decide wether the pixel is black or white
                    boolean isBlack = average < 100;

                    int rn = isBlack ? 0 : 255;
                    int gn = isBlack ? 0 : 255;
                    int bn = isBlack ? 0 : 255;

                    pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                }
            }
            return pixels;
        }

        /**
         * Returns Image as a binary Image (black and white) with error diffusion implemented
         * @param pixels
         * @return pixel Array
         */
        int[] processBinaryErrorDiffusion (int[] pixels) {
            // Store the current error height
            int[] error = new int[width];
            // Define the border for black and white
            int borderLine = 100;

            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x++) {
                    int pos = y*width + x;
                    int argb = origPixels[pos];

                    int r = (argb >> 16) & 0xff;
                    int g = (argb >>  8) & 0xff;
                    int b =  argb        & 0xff;

                    // calculate the average of the pixel's 3 channels to receive the grayscale
                    int average = Math.round((r + g + b) / 3);
                    boolean isBlack;

                    // First row -> no error diffusion
                    if (y == 0) {
                        isBlack = average < borderLine;
                        error[x] = borderLine - average;
                    } else {
                        // Not first row anymore so check error
                        int newAverage = average + error[x];
                        isBlack = newAverage < borderLine;
                        error[x] = error[x] + (borderLine - newAverage);
                    }

                    int rn = isBlack ? 0 : 255;
                    int gn = isBlack ? 0 : 255;
                    int bn = isBlack ? 0 : 255;

                    pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
                }
            }
            return pixels;
        }

        /**
         * Returns Image as a sepia image
         * @param pixels
         * @return pixel Array
         */
        int[] processSepia (int[] pixels) {
            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x++) {
                    int pos = y*width + x;
                    int argb = origPixels[pos];

                    int r = (argb >> 16) & 0xff;
                    int g = (argb >>  8) & 0xff;
                    int b =  argb        & 0xff;

                    // RGB to YUV
                    double yColor = 0.299 * r + 0.587 * g + 0.114 * b;
                    double uColor = (b - yColor) * 0.493;
                    double vColor = (r - yColor) * 0.877;

                    // Remove Saturation
                    uColor = uColor * 0;
                    vColor = vColor * 0;

                    // YUV zu RGB
                    r = (int) (yColor + vColor/0.877);
                    b = (int) (yColor + uColor/0.493);
                    g = (int) (1/0.587 * yColor - 0.299/0.587*r - 0.114/0.587 * b);

                    // Sepia adjustments:
                    r = (int) Math.round(r * 1.1);
                    g = (int) Math.round(g * 0.75);
                    b = (int) Math.round(b * 0.35);
                    if (r > 255) r = 255;

                    pixels[pos] = (0xFF<<24) | (r<<16) | (g<<8) | b;
                }
            }
            return pixels;
        }

        /**
         * Returns Image as a 8 color image
         * @param pixels
         * @return pixel Array
         */
        int[] process8Colors (int[] pixels) {
            // 8 best colors detected by fiji
            int[] c1 = new int[]{46, 46, 45};
            int[] c2 = new int[]{157, 147, 142};
            int[] c3 = new int[]{210, 208, 208};
            int[] c4 = new int[]{53, 104, 140};
            int[] c5 = new int[]{82, 87, 88};
            int[] c6 = new int[]{11, 16, 16};
            int[] c7 = new int[]{25, 32, 33};
            int[] c8 = new int[]{78, 67, 58};

            ArrayList<int[]> colors = new ArrayList<>();
            colors.add(c1);
            colors.add(c2);
            colors.add(c3);
            colors.add(c4);
            colors.add(c5);
            colors.add(c6);
            colors.add(c7);
            colors.add(c8);

            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x++) {
                    int pos = y * width + x;
                    int argb = origPixels[pos];

                    int r = (argb >> 16) & 0xff;
                    int g = (argb >> 8) & 0xff;
                    int b = argb & 0xff;

                    // Find fitting color for current pixel
                    int[] color = getNearestColor(new int[]{r, g, b}, colors);

                    pixels[pos] = (0xFF << 24) | (color[0] << 16) | (color[1] << 8) | color[2];
                }
            }
            return pixels;
        }

        /**
         * Function that returns the nearest color from array
         * @param color
         * @param colorList
         * @return color
         */
        public int[] getNearestColor (int[] color, ArrayList colorList) {

            HashMap<Float, int[]> distanceMap = new HashMap<>();
            for (int i = 0; i < colorList.size(); i++) {
                distanceMap.put(getDistanceFromColors(color, (int[]) colorList.get(i)), (int[]) colorList.get(i));
            }

            // get smallest entry
            Map.Entry<Float, int[]> min = null;
            for (Map.Entry<Float, int[]> entry : distanceMap.entrySet()) {
                if (min == null || min.getKey() > entry.getKey()) {
                    min = entry;
                }
            }

            return min.getValue();
        }

        /**
         * Gets the distance between two colors
         * @param c1
         * @param c2
         * @return distanc
         */
        public Float getDistanceFromColors (int[] c1, int[] c2) {
            Float deltaR = (float) (c1[0] - c2[0]);
            Float deltaG = (float) (c1[1] - c2[1]);
            Float deltaB = (float) (c1[2] - c2[2]);
            return (float) Math.round(Math.sqrt((deltaR*deltaR) + (deltaG*deltaG) + (deltaB*deltaB)));
        }
    }
}
