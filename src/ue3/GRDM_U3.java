package ue3;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 Opens an image window and adds a panel below the image
 */
public class GRDM_U3 implements PlugIn {

    ImagePlus imp; // ImagePlus object
    private int[] origPixels;
    private int width;
    private int height;

    String[] items = {"Original", "Rot-Kanal", "Graustufen", "Binär", "Binär mit Fehlerdiffusion", "Sepia"};


    public static void main(String args[]) {
        IJ.open("resources/Bear.jpg");

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
        }

        int[] processOriginal (int[] pixels) {
            for (int y=0; y<height; y++) {
                for (int x=0; x<width; x++) {
                    int pos = y*width + x;

                    pixels[pos] = origPixels[pos];
                }
            }
            return pixels;
        }

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

        int[] processGrayscale (int[] pixels) {
            return pixels;
        }

        int[] processBinary (int[] pixels) {
            return pixels;
        }

        int[] processBinaryErrorDiffusion (int[] pixels) {
            return pixels;
        }

        int[] processSepia (int[] pixels) {
            return pixels;
        }
    }
}
