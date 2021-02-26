package org.cytoscape.cg.model;

import java.awt.Image;

public class CustomGraphicsUtil {

	public static Image getResizedImage(Image original, Integer w, Integer h, boolean keepAspectRatio) {
		if (original == null)
			throw new IllegalArgumentException("Original image cannot be null.");

		if (w == null && h == null)
			return original;

		int currentW = original.getWidth(null);
		int currentH = original.getHeight(null);
		float ratio;
		int converted;

		if (keepAspectRatio == false) {
			return original.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING);
		} else if (h == null) {
			ratio = ((float) currentH) / ((float) currentW);
			converted = (int) (w * ratio);
			return original.getScaledInstance(w, converted, Image.SCALE_AREA_AVERAGING);
		} else {
			ratio = ((float) currentW) / ((float) currentH);
			converted = (int) (h * ratio);
			return original.getScaledInstance(converted, h, Image.SCALE_AREA_AVERAGING);
		}
	}
}
