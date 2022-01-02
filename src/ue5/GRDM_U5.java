package ue5;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

/**
     Opens an image window and adds a panel below the image
 */
public class GRDM_U5 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	double[][] matrix1;
	double[][] matrix2;
	double[][] matrix3;

	String[] items = {"Original", "Weichzeichnung", "Hochpass", "Geschärft"};


	public static void main(String args[]) {

		IJ.open("../resources/sail.jpg");

		GRDM_U5 pw = new GRDM_U5();
		pw.imp = IJ.getImage();
		pw.run("");
	}


	public void run(String arg) {

		initializeMatrices();

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

	} // CustomCanvas inner class


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

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						
						pixels[pos] = origPixels[pos];
					}
				}
			}
			
			if (method.equals("Weichzeichnung")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						/*int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						int rn = r/2;
						int gn = g/2;
						int bn = b/2;*/

						//pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
						pixels = applyMatrix(origPixels, pixels, x, y, matrix1);
					}
				}
			}
		}
	} // CustomWindow inner class

	public void initializeMatrices () {
		matrix1 = new double[][] {
				{1.0/9, 1.0/9, 1.0/9},
				{1.0/9, 1.0/9, 1.0/9},
				{1.0/9, 1.0/9, 1.0/9}
		};
	}


	public int[] applyMatrix (int[] orgPixels, int[] pixels, int x, int y, double[][] operationMatrix) {
		int pos = y*width + x;
		int argb = origPixels[pos];

		int[][] pixelMatrix;
		if (x < width-1 && x > 1 && y < height-1 && y > 1) {
			pixelMatrix = new int[][] {
					{orgPixels[pos - width - 1], orgPixels[pos - width], orgPixels[pos - width + 1]},
					{orgPixels[pos - 1], orgPixels[pos], orgPixels[pos + 1]},
					{orgPixels[pos - width + 1], orgPixels[pos + width], orgPixels[pos + width + 1]}
			};
		} else {
			pixelMatrix = new int[][] {
					{0, 0, 0},
					{0, 0, 0},
					{0, 0, 0},
			};
		}

		int rn = 0;
		int gn = 0;
		int bn = 0;

		for (int mx = 0; mx < 3; mx++) {
			for (int my = 0; my < 3; my++) {
				int r = (pixelMatrix[mx][my] >> 16) & 0xff;
				int g = (pixelMatrix[mx][my] >>  8) & 0xff;
				int b =  pixelMatrix[mx][my]        & 0xff;
				rn += r * operationMatrix[mx][my];
				gn += g * operationMatrix[mx][my];
				bn += b * operationMatrix[mx][my];
			}
		}

		pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
		return pixels;
	}
} 
