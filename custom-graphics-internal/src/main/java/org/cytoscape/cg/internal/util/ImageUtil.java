package org.cytoscape.cg.internal.util;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.cg.model.AbstractURLImageCustomGraphics;
import org.cytoscape.cg.model.BitmapCustomGraphics;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.cg.model.SVGCustomGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImageUtil {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
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
	
	@SuppressWarnings("rawtypes")
	public static List<AbstractURLImageCustomGraphics> loadImageCustomGraphics(File[] files, CustomGraphicsManager manager) {
		var images = new ArrayList<AbstractURLImageCustomGraphics>();
		
		for (var file : files) {
			BufferedImage img = null;
			String svg = null;
			
			if (file.isFile()) {
				try {
					if (file.getName().toLowerCase().endsWith(".svg"))
						svg = Files.readString(file.toPath());
					else
						img = ImageIO.read(file);
				} catch (Exception e) {
					logger.error("Could not read file: " + file.toString(), e);
					continue;
				}
			}

			try {
				var url = file.toURI().toURL();
				var name = ViewUtil.getShortName(file.toString());
				AbstractURLImageCustomGraphics<?> cg = null;
				
				if (svg != null)
					cg = new SVGCustomGraphics(manager.getNextAvailableID(), name, url, svg);
				else if (img != null)
					cg = new BitmapCustomGraphics(manager.getNextAvailableID(), name, url, img);

				if (cg != null)
					images.add(cg);
			} catch (Exception e) {
				logger.error("Could not create custom graphics: " + file, e);
				continue;
			}
		}
		
		return images;
	}
	
	private ImageUtil() {
		// Restrict instantiation
	}
}
