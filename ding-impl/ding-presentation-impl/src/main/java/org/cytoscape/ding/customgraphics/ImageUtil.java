package org.cytoscape.ding.customgraphics;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public class ImageUtil {
	

	public static BufferedImage toBufferedImage(final Image image) throws InterruptedException {
		if (image instanceof BufferedImage)
			return (BufferedImage) image;

		MediaTracker tracker = new MediaTracker(new Component() {
		});
		tracker.addImage(image, 0);
		tracker.waitForAll();

		PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, -1, -1, false);
		pixelGrabber.grabPixels();
		ColorModel cm = pixelGrabber.getColorModel();

		final int w = pixelGrabber.getWidth();
		final int h = pixelGrabber.getHeight();
		WritableRaster raster = cm.createCompatibleWritableRaster(w, h);
		final BufferedImage renderedImage = new BufferedImage(cm, raster, cm
				.isAlphaPremultiplied(), new Hashtable());
		renderedImage.getRaster().setDataElements(0, 0, w, h,
				pixelGrabber.getPixels());
		
		return renderedImage;

	}
}
