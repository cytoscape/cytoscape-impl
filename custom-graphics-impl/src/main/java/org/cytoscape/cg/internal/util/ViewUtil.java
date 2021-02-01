package org.cytoscape.cg.internal.util;

import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;

public class ViewUtil {

	public static String getShortName(String pathName) {
		var p = new File(pathName);
		
		return p.getName();
	}
	
	public static ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
		var img = icon.getImage().getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
		var bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		var g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(img, 0, 0, width, height, null);
		g.dispose();
		
		return new ImageIcon(bi);
	}
}
