package org.cytoscape.cg.util;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public final class CustomGraphicsUtil {

	public static boolean isSVG(URL url) throws IOException {
		var conn = url.openConnection();
		var type = conn.getHeaderField("Content-Type");
		
		if ("image/svg+xml".equalsIgnoreCase(type))
			return true;
		
		// If the Content-Type is null, check whether this is a local file
		// (it may happen that the OS does not return the file type, in which case we can still
		// try to check the file extension)
		if (type == null) {
			var protocol = url.getProtocol();
			
			if (protocol.equalsIgnoreCase("file"))
				return url.getFile().toLowerCase().endsWith(".svg");
		}
		
		return false;
	}
	
	public static String getShortName(URL url) {
		if (url == null)
			return null;
		
		if (url.getProtocol().equals("http") || url.getProtocol().equals("https"))
			return url.getAuthority() + url.getFile();
		else if (url.getProtocol().equals("file"))
			return getShortName(url.getPath());
		
		return url.toString();
	}
	
	public static String getShortName(String pathName) {
		if (pathName == null)
			return null;
		
		return new File(pathName).getName();
	}
	
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
	
	private CustomGraphicsUtil() {
		// Restrict instantiation
	}
}
