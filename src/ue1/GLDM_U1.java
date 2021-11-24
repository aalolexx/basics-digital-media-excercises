package ue1;

import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.List;

//erste Uebung (elementare Bilderzeugung)

public class GLDM_U1 implements PlugIn {
	
	final static String[] choices = {
		"Schwarzes Bild",
		"gelben Bildes",
		"belgischen Fahne",
		"Fahne der USA (ohne Sterne)",
		"horizontaler Schwarz/Weiß Verlaufs",
		"horizontalen Schwarz/Rot Verlaufs bei gleichzeitigem vertikalen Schwarz/Blau Verlauf",
		"tschechische Fahne"
	};
	
	private String choice;
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		GLDM_U1 imageGeneration = new GLDM_U1();
		imageGeneration.run("");
	}
	
	public void run(String arg) {
		
		int width  = 566;  // Breite
		int height = 400;  // Hoehe
		
		// RGB-Bild erzeugen
		ImagePlus imagePlus = NewImage.createRGBImage("GLDM_U1", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ip = imagePlus.getProcessor();
		
		// Arrays fuer den Zugriff auf die Pixelwerte
		int[] pixels = (int[])ip.getPixels();
		
		dialog();
		
		////////////////////////////////////////////////////////////////
		// Hier bitte Ihre Aenderungen / Erweiterungen
		
		if ( choice.equals("Schwarzes Bild") ) {
			generateBlackImage(width, height, pixels);
		} else if ( choice.equals("gelben Bildes") ) {
			generateYellowImage(width, height, pixels);
		} else if ( choice.equals("belgischen Fahne") ) {
			generateBelgiumImage(width, height, pixels);
		} else if ( choice.equals("Fahne der USA (ohne Sterne)") ) {
			generateUSAImage(width, height, pixels);
		} else if ( choice.equals("horizontaler Schwarz/Weiß Verlaufs") ) {
			generateBlackWhiteGradient(width, height, pixels);
		} else if ( choice.equals("horizontalen Schwarz/Rot Verlaufs bei gleichzeitigem vertikalen Schwarz/Blau Verlauf") ) {
			generateXYGradient(width, height, pixels);
		} else if ( choice.equals("tschechische Fahne") ) {

		}
		
		////////////////////////////////////////////////////////////////////
		
		// neues Bild anzeigen
		imagePlus.show();
		imagePlus.updateAndDraw();
	}

	private void generateBlackImage(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r = 0;
				int g = 0;
				int b = 0;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateYellowImage(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				// Jeder Pixel wird immer auf gelb gesetzt
				int r = 255;
				int g = 255;
				int b = 0;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateBelgiumImage(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r, g, b;

				// Abfragen in welchem x Drittel man sich befindet und die jeweilige Farbe setzten.
				if (x < width/3) {
					r = g = b = 0;
				} else if (x < (2 * width/3)) {
					r = 255;
					g = 255;
					b = 0;
				} else {
					r = 255;
					g = 0;
					b = 0;
				}

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateUSAImage(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x; // Arrayposition bestimmen

				int r, g, b;

				// Stripes
				if (y / (height/13) % 2 == 0) {
					r = 255;
					g = 0;
					b = 0;
				} else {
					r = 255;
					g = 255;
					b = 255;
				}

				// Blaues Quadrat
				if (x < width/3 && y < (7 / (13/ (double) height))) {
					r = 0;
					g = 0;
					b = 255;
				}

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateBlackWhiteGradient(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		int colorProgress = 0;
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x;

				// Den r, g und b Wert auf den jeweiligen x-ten 255xtel setzen.
				colorProgress = x * 255/width;

				int r = colorProgress;
				int g = colorProgress;
				int b = colorProgress;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}

	private void generateXYGradient(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		int colorProgressX = 0;
		int colorProgressY = 0;
		for (int y=0; y<height; y++) {
			// Schleife ueber die x-Werte
			for (int x=0; x<width; x++) {
				int pos = y*width + x;

				// Den r, g und b Wert auf den jeweiligen x-ten bzw y-ten 255xtel setzen.
				colorProgressX = x * 255/width;
				colorProgressY = y * 255/height;

				int r = colorProgressX;
				int g = 0;
				int b = colorProgressY;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
			}
		}
	}
	
	
	private void dialog() {
		// Dialog fuer Auswahl der Bilderzeugung
		GenericDialog gd = new GenericDialog("Bildart");
		
		gd.addChoice("Bildtyp", choices, choices[0]);
		
		
		gd.showDialog();	// generiere Eingabefenster
		
		choice = gd.getNextChoice(); // Auswahl uebernehmen
		
		if (gd.wasCanceled())
			System.exit(0);
	}
}

