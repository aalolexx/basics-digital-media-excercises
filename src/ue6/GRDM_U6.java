package ue6;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ue5.GRDM_U5;


public class GRDM_U6 implements PlugInFilter {

    ImagePlus imp; // ImagePlus object

    public static void main(String args[]) {
        IJ.open("../resources/component.jpg");
        GRDM_U6 pw = new GRDM_U6();
        pw.imp = IJ.getImage();
        pw.run(pw.imp.getProcessor());
    }

    public int setup(String arg, ImagePlus imp) {
        if (arg.equals("about"))
        {showAbout(); return DONE;}
        return DOES_RGB+NO_CHANGES;
    }

    public void run(ImageProcessor ip) {

        String[] dropdownmenue = {"Kopie", "Pixelwiederholung", "Bilinear"};

        GenericDialog gd = new GenericDialog("scale");
        gd.addChoice("Methode",dropdownmenue,dropdownmenue[0]);
        gd.addNumericField("Hoehe:",920,0);
        gd.addNumericField("Breite:",1000,0);

        gd.showDialog();

        String interpolationChoice = gd.getNextChoice();
        int height_n = (int) gd.getNextNumber(); // _n fuer das neue skalierte Bild
        int width_n =  (int) gd.getNextNumber();

        int width  = ip.getWidth();  // Breite bestimmen
        int height = ip.getHeight(); // Hoehe bestimmen

        //height_n = height;
        //width_n  = width;

        ImagePlus newImage = NewImage.createRGBImage("Skaliertes Bild",
                width_n, height_n, 1, NewImage.FILL_BLACK);
        ImageProcessor ip_n = newImage.getProcessor();

        int[] pix = (int[]) ip.getPixels();
        int[] pix_n = (int[]) ip_n.getPixels();

        float scaleXFactor = (float) width_n / (float) width;
        float scaleYFactor = (float) height_n / (float) height;

        if (interpolationChoice.equals("Kopie")) {

            // Schleife ueber das neue Bild
            for (int y_n = 0; y_n < height_n; y_n++) {
                for (int x_n = 0; x_n < width_n; x_n++) {
                    int y = y_n;
                    int x = x_n;

                    if (y < height && x < width) {
                        int pos_n = y_n * width_n + x_n;
                        int pos = y * width + x;

                        pix_n[pos_n] = pix[pos];
                    }
                }
            }


        } else if (interpolationChoice.equals("Pixelwiederholung")) {

            for (int y_n = 0; y_n < height_n; y_n++) {
                for (int x_n = 0; x_n < width_n; x_n++) {
                    int pos_n = y_n * width_n + x_n;
                    // get position of where to scan (abtasten) in the old image for the new one
                    int oldPixelXPosition = Math.round(x_n / scaleXFactor);
                    int oldPixelYPosition = Math.round(y_n / scaleYFactor);
                    int oldPixelPosition = oldPixelYPosition * width + oldPixelXPosition;
                    if (oldPixelPosition >= pix.length) oldPixelPosition = pix.length-1;
                    pix_n[pos_n] = pix[oldPixelPosition];
                }
            }


        } else if (interpolationChoice.equals("Bilinear")) {

            int counterSinceSameOldPixel = 0;
            int lastOldPixelPos = 0;

            for (int y_n = 0; y_n < height_n; y_n++) {
                for (int x_n = 0; x_n < width_n; x_n++) {
                    int pos_n = y_n * width_n + x_n;
                    // get position of where to scan (abtasten) in the old image for the new one
                    int oldPixelXPosition = Math.round(x_n / scaleXFactor);
                    int oldPixelYPosition = Math.round(y_n / scaleYFactor);
                    int oldPixelPosition = oldPixelYPosition * width + oldPixelXPosition;
                    if (oldPixelPosition >= pix.length-1) oldPixelPosition = pix.length-2;

                    // Now bilinearly interpolate the pixel with the next (old) pixel
                    int oldColor1 = pix[oldPixelPosition];
                    int oldColor2 = pix[oldPixelPosition + 1];

                    // Find out (theoretical) range between the two new pixels compared to the old one
                    if (lastOldPixelPos != Math.round(x_n / scaleXFactor)) {
                        lastOldPixelPos = oldPixelXPosition;
                        counterSinceSameOldPixel = 0;
                    } else {
                        counterSinceSameOldPixel++;
                    }

                    pix_n[pos_n] = bilinearInterpolate(oldColor1, oldColor2, scaleXFactor, counterSinceSameOldPixel);
                }
            }

        }

        // neues Bild anzeigen
        newImage.show();
        newImage.updateAndDraw();
    }

    private int mixColors(int color1, int color2, float alpha) {
        int r1 = (color1 >> 16) & 0xff;
        int g1 = (color1 >>  8) & 0xff;
        int b1 =  color1        & 0xff;

        int r2 = (color2 >> 16) & 0xff;
        int g2 = (color2 >>  8) & 0xff;
        int b2 =  color2        & 0xff;

        int rn = Math.round(((float) r2 * alpha) + (float) r1 * (1 - alpha));
        int gn = Math.round(((float) g2 * alpha) + (float) g1 * (1 - alpha));
        int bn = Math.round(((float) b2 * alpha) + (float) b1 * (1 - alpha));

        return (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
    }

    private int bilinearInterpolate (int color1, int color2, float range, int current) {
        return mixColors(color1, color2, ((float)current / range));
    }

    void showAbout() {
        IJ.showMessage("");
    }
}

