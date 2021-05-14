package org.cytoscape.ding.internal.util;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Hashtable;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
