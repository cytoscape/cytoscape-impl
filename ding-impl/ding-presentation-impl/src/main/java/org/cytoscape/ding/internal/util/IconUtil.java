package org.cytoscape.ding.internal.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

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

public final class IconUtil {

	public static final String CY_FONT_NAME = "cytoscape-3";
	
	public static final String ICON_ANNOTATION_1 = "X";
	public static final String ICON_ANNOTATION_2 = "Y";
	public static final String ICON_ANNOTATION_ARROW = "Z";
	public static final String ICON_ANNOTATION_BOUNDED_TEXT_1 = "0";
	public static final String ICON_ANNOTATION_BOUNDED_TEXT_2 = "1";
	public static final String ICON_ANNOTATION_IMAGE_1 = "2";
	public static final String ICON_ANNOTATION_IMAGE_2 = "3";
	public static final String ICON_ANNOTATION_SHAPE_1 = "4";
	public static final String ICON_ANNOTATION_SHAPE_2 = "5";
	public static final String ICON_ANNOTATION_TEXT = "6";
	public static final String ICON_PIN = "'";
	
	public static ImageIcon resizeIcon(final ImageIcon icon, int width, int height) {
		final Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
		final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		final Graphics2D g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(img, 0, 0, width, height, null);
		g.dispose();
		
		return new ImageIcon(bi);
	}
	
	public static ImageIcon emptyIcon(final int width, final int height) {
		final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		return new ImageIcon(bi);
	}
	
	private IconUtil() {
		// restrict instantiation
	}
}
