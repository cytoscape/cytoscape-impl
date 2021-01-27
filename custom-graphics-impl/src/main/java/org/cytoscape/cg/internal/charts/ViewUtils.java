package org.cytoscape.cg.internal.charts;

import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class ViewUtils {

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
