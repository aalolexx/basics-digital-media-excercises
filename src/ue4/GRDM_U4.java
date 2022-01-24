package ue4;

import ij.*;
import ij.io.*;
import ij.process.*;
import ij.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;


public class GRDM_U4 implements PlugInFilter {

	protected ImagePlus imp;
	final static String[] choices = {"Wischen", "Weiche Blende", "Schieben", "Chroma Key", "Extra", "Overlay"};

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB+STACK_REQUIRED;
	}
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		IJ.open("../resources/StackB.zip");
		
		GRDM_U4 sd = new GRDM_U4();
		sd.imp = IJ.getImage();
		ImageProcessor B_ip = sd.imp.getProcessor();
		sd.run(B_ip);
	}

	public void run(ImageProcessor B_ip) {
		// Film B wird uebergeben
		ImageStack stack_B = imp.getStack();
		
		int length = stack_B.getSize();
		int width  = B_ip.getWidth();
		int height = B_ip.getHeight();
		
		// ermoeglicht das Laden eines Bildes / Films
		//Opener o = new Opener();
		//OpenDialog od_A = new OpenDialog("Auswählen des 2. Filmes ...",  "");
				
		// Film A wird dazugeladen
		// String dateiA = od_A.getFileName();
		// if (dateiA == null) return; // Abbruch
		// String pfadA = od_A.getDirectory();
		// ImagePlus A = o.openImage(pfadA,dateiA);
		// if (A == null) return; // Abbruch

		IJ.open("../resources/StackA.zip");

		imp = IJ.getImage();
		ImageProcessor A_ip = imp.getProcessor();
		ImageStack stack_A  = imp.getStack();

		if (A_ip.getWidth() != width || A_ip.getHeight() != height)
		{
			IJ.showMessage("Fehler", "Bildgrößen passen nicht zusammen");
			return;
		}
		
		// Neuen Film (Stack) "Erg" mit der kleineren Laenge von beiden erzeugen
		length = Math.min(length,stack_A.getSize());

		ImagePlus Erg = NewImage.createRGBImage("Ergebnis", width, height, length, NewImage.FILL_BLACK);
		ImageStack stack_Erg  = Erg.getStack();

		// Dialog fuer Auswahl des Ueberlagerungsmodus
		GenericDialog gd = new GenericDialog("Überlagerung");
		gd.addChoice("Methode",choices,"");
		gd.showDialog();

		int methode = 0;		
		String s = gd.getNextChoice();
		if (s.equals("Wischen")) methode = 1;
		if (s.equals("Weiche Blende")) methode = 2;
		if (s.equals("Schieben")) methode = 3;
		if (s.equals("Chroma Key")) methode =4;
		if (s.equals("Extra (Quadrate)")) methode = 5;
		if (s.equals("Overlay")) methode = 6;

		// Arrays fuer die einzelnen Bilder
		int[] pixels_B;
		int[] pixels_A;
		int[] pixels_Erg;

		// Schleife ueber alle Bilder
		for (int z=1; z<=length; z++)
		{
			pixels_B   = (int[]) stack_B.getPixels(z);
			pixels_A   = (int[]) stack_A.getPixels(z);
			pixels_Erg = (int[]) stack_Erg.getPixels(z);

			int pos = 0;
			for (int y=0; y<height; y++) {
				for (int x = 0; x < width; x++, pos++) {
					int cA = pixels_A[pos];
					int rA = (cA & 0xff0000) >> 16;
					int gA = (cA & 0x00ff00) >> 8;
					int bA = (cA & 0x0000ff);

					int cB = pixels_B[pos];
					int rB = (cB & 0xff0000) >> 16;
					int gB = (cB & 0x00ff00) >> 8;
					int bB = (cB & 0x0000ff);

					int progress = length / 100 * z;

					if (methode == 1) {

						if (y + 1 > (z - 1) * (double) height / (length - 1)) {
							pixels_Erg[pos] = pixels_B[pos];
						} else {
							pixels_Erg[pos] = pixels_A[pos];
						}

					} else if (methode == 2) {

						// Weiche Blende -> Nach Fortschritt gewichteten Durchschnitt berechnen
						float pA = (float) progress / 100;
						float pB = (float) (100 - progress) / 100;

						int r = Math.round((rA * pA + rB * pB) / 2);
						int g = Math.round((gA * pA + gB * pB) / 2);
						int b = Math.round((bA * pA + bB * pB) / 2);
						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);

					} else if (methode == 3) {

						// Schiebe Blende
						int spaceToEnd = width - x;
						int moveValue = width / 100 * progress;

						if (pos+moveValue < pixels_Erg.length) pixels_Erg[pos+moveValue] = pixels_A[pos];
						if (moveValue < spaceToEnd) {
							pixels_Erg[pos+moveValue] = pixels_B[pos];
						} else {
							pixels_Erg[pos + (spaceToEnd-moveValue)] = pixels_B[pos];
						}

					} else if (methode == 4) {

						// Chroma Key -> Nur Oranginge Farben überlagen
						int r, g, b;
						if (rA - bA < 25 && rA - gA < 25) {
							r = rA;
							g = gA;
							b = bA;
						} else {
							float pA = (float) progress/100;
							float pB = (float) (100-progress)/100;

							r = Math.round((rA * pA + rB * pB) / 2);
							g = Math.round((gA * pA + gB * pB) / 2);
							b = Math.round((bA * pA + bB * pB) / 2);
						}

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);

					} else if (methode == 5) {

						float pA = (float) progress / 100;
						float pB = (float) (100 - progress) / 100;

						int r;
						int g;
						int b;

						if (x % 20 > 10 && y % 20 > 10) {
							if (progress > 50) {
								r = Math.round((rA * pA*2 + rB * pB) / 2);
								g = Math.round((gA * pA*2 + gB * pB) / 2);
								b = Math.round((bA * pA*2 + bB * pB) / 2);
							} else {
								r = rB;
								b = bB;
								g = gB;
							}
						} else {
							if (progress < 50) {
								r = Math.round((rA * pA*2 + rB * pB) / 2);
								g = Math.round((gA * pA*2 + gB * pB) / 2);
								b = Math.round((bA * pA*2 + bB * pB) / 2);
							} else {
								r = rA;
								b = bA;
								g = gA;
							}
						}

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);

					} else if (methode == 6) {

						// Overlays überlagern
						float pA = (float) progress / 100;
						float pB = (float) (100 - progress) / 100;

						int rOA = (int) Math.round((rA * 0.75 + rB * 0.25) / 2);
						int gOA = (int) Math.round((gA * 0.75 + gB * 0.25) / 2);
						int bOA = (int) Math.round((bA * 0.75 + bB * 0.25) / 2);

						int rOB = (int) Math.round((rA * 0.25 + rB * 0.75) / 2);
						int gOB = (int) Math.round((gA * 0.25 + gB * 0.75) / 2);
						int bOB = (int) Math.round((bA * 0.25 + bB * 0.75) / 2);

						int r = Math.round((rOA * pA + rOB * pB) / 2);
						int g = Math.round((gOA * pA + gOB * pB) / 2);
						int b = Math.round((bOA * pA + bOB * pB) / 2);

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);

					}
				}
			}
		}

		// neues Bild anzeigen
		Erg.show();
		Erg.updateAndDraw();
	}
}

