package org.cytoscape.cg.internal.util;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Hashtable;

public final class ImageUtil {

	@SuppressWarnings("serial")
	public static BufferedImage toBufferedImage(Image image) throws InterruptedException {
		if (image instanceof BufferedImage)
			return (BufferedImage) image;

		var tracker = new MediaTracker(new Component() { });
		tracker.addImage(image, 0);
		tracker.waitForAll();

		var pixelGrabber = new PixelGrabber(image, 0, 0, -1, -1, false);
		pixelGrabber.grabPixels();
		var cm = pixelGrabber.getColorModel();

		int w = pixelGrabber.getWidth();
		int h = pixelGrabber.getHeight();
		var raster = cm.createCompatibleWritableRaster(w, h);
		var renderedImage = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), new Hashtable<>());
		renderedImage.getRaster().setDataElements(0, 0, w, h, pixelGrabber.getPixels());

		return renderedImage;
	}
	
	private ImageUtil() {
		// Restrict instantiation
	}
}
