package ue2;

import ij.IJ;
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

import javax.swing.BorderFactory;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
*/
public class GRDM_U2 implements PlugIn {

    ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;
	
    public static void main(String args[]) {
		//new ImageJ();
    	//IJ.open("/users/barthel/applications/ImageJ/_images/orchid.jpg");
    	IJ.open("resources/orchid.jpg");
		
		GRDM_U2 pw = new GRDM_U2();
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
    
    } // CustomCanvas inner class
    
    
    class CustomWindow extends ImageWindow implements ChangeListener {
         
        private JSlider jSliderBrightness;
		private JSlider jSliderContrast;
		private JSlider jSliderSaturation;
		private JSlider jSliderHue;
		private double brightness = 0;
		private double contrast = 1;
		private double saturation = 1;
		private double hueRotationDeg = 0;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }
    
        void addPanel() {
        	//JPanel panel = new JPanel();
        	Panel panel = new Panel();

            panel.setLayout(new GridLayout(4, 1));
            jSliderBrightness = makeTitledSilder("Helligkeit", -128, 128, (int) brightness);
            jSliderContrast = makeTitledSilder("Kontrast", 0, 10, (int) contrast);
			jSliderSaturation = makeTitledSilder("Sättigung", 0, 5, (int) saturation);
			jSliderHue = makeTitledSilder("HUE Rotation", 0, 360, (int) hueRotationDeg);
            panel.add(jSliderBrightness);
            panel.add(jSliderContrast);
            panel.add(jSliderSaturation);
			panel.add(jSliderHue);

            add(panel);
            
            pack();
         }
      
        private JSlider makeTitledSilder(String string, int minVal, int maxVal, int val) {
		
        	JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
        	Dimension preferredSize = new Dimension(width, 50);
        	slider.setPreferredSize(preferredSize);
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(), 
					string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
			slider.setMajorTickSpacing((maxVal - minVal)/10 );
			slider.setPaintTicks(true);
			slider.addChangeListener(this);
			
			return slider;
		}
        
        private void setSliderTitle(JSlider slider, String str) {
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
				str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
		}

		public void stateChanged( ChangeEvent e ){
			JSlider slider = (JSlider)e.getSource();

			if (slider == jSliderBrightness) {
				brightness = (double) slider.getValue();
				String str = "Helligkeit " + brightness;
				setSliderTitle(jSliderBrightness, str); 
			}
			
			if (slider == jSliderContrast) {
				contrast = (double) slider.getValue();
				String str = "Kontrast " + contrast;
				setSliderTitle(jSliderContrast, str);
			}

			if (slider == jSliderSaturation) {
				saturation = (double) slider.getValue();
				String str = "Sättigung " + saturation;
				setSliderTitle(jSliderSaturation, str);
			}

			if (slider == jSliderHue) {
				hueRotationDeg = (double) slider.getValue();
				String str = "HUE Rotation " + hueRotationDeg;
				setSliderTitle(jSliderHue, str);
			}
			
			changePixelValues(imp.getProcessor());
			
			imp.updateAndDraw();
		}

		
		private void changePixelValues(ImageProcessor ip) {
			
			// Array fuer den Zugriff auf die Pixelwerte
			int[] pixels = (int[])ip.getPixels();
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 
					
					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					
					
					// Brightness Anpassungen
					/*int rn = (int) (r + brightness);
					int gn = (int) (g + brightness);
					int bn = (int) (b + brightness);
					if (rn > 255) rn = 255;
					if (gn > 255) gn = 255;
					if (bn > 255) bn = 255;
					if (rn < 0) rn = 0;
					if (gn < 0) gn = 0;
					if (bn < 0) bn = 0;*/


					// RGB zu YUV konvertieren
					double yColor = 0.299 * r + 0.587 * g + 0.114 * b;
					double uColor = (b - yColor) * 0.493;
					double vColor = (r - yColor) * 0.877;


					// Helligkeit
					yColor = yColor + brightness;


					// Kontrast
					yColor = yColor * contrast;


					// Farbsättigung
					uColor = uColor * saturation;
					vColor = vColor * saturation;

					// HUE
					double hueRotationRad = Math.toRadians(hueRotationDeg);
					uColor = uColor * Math.cos(hueRotationRad) - vColor * Math.sin(hueRotationRad);
					vColor = uColor * Math.sin(hueRotationRad) + vColor * Math.cos(hueRotationRad);

					// YUV zu RGB
					int rn = (int) (yColor + vColor/0.877);
					int bn = (int) (yColor + uColor/0.493);
					int gn = (int) (1/0.587 * yColor - 0.299/0.587*rn - 0.114/0.587 * bn);

					rn = truncate(rn);
					bn = truncate(bn);
					gn = truncate(gn);

					
					// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
		}

		int truncate (int x) {
			if (x < 0) x = 0;
			if (x > 255) x = 255;
			return x;
		}
		
    } // CustomWindow inner class
} 
