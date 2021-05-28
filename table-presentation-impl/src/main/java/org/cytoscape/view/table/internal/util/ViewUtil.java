package org.cytoscape.view.table.internal.util;

import java.awt.Color;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.slf4j.Logger;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

public final class ViewUtil {

	public static Color getDefaultTableHeaderBg() {
		return UIManager.getColor("TableHeader.background");
	}
	
	public static Color getSelectedTableHeaderBg() {
		int alpha = LookAndFeelUtil.isNimbusLAF() ? 50 : 25;
		
		return alphaBlendColors(getDefaultTableHeaderBg(), UIManager.getColor("Focus.color"), alpha);
	}
	
	public static Color alphaBlendColors(Color bottom, Color top, int topAlpha) {
		if (topAlpha == 255)
			return top;

    // If this is being called by the tests, then "Focus.color" is apparently null
		if (topAlpha == 0 || top == null)
			return bottom;

        int br = bottom.getRed();
        int bg = bottom.getGreen();
        int bb = bottom.getBlue();
        
        int tr = top.getRed();
        int tg = top.getGreen();
        int tb = top.getBlue();
        
        var r = (tr * topAlpha + br * (255 - topAlpha)) / 255;
        var g = (tg * topAlpha + bg * (255 - topAlpha)) / 255;
        var b = (tb * topAlpha + bb * (255 - topAlpha)) / 255;
        
        return new Color(r, g, b);
	}
	
	/**
	 * Utility method that invokes the code in Runnable.run on the AWT Event Dispatch Thread.
	 * @param runnable
	 */
	public static void invokeOnEDT(Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}
	
	public static void invokeOnEDTAndWait(Runnable runnable) {
		invokeOnEDTAndWait(runnable, null);
	}
	
	public static void invokeOnEDTAndWait(Runnable runnable, Logger logger) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (Exception e) {
				if (logger != null)
					logger.error("Unexpected error", e);
				else
					e.printStackTrace();
			}
		}
	}
	
	private ViewUtil() {
	}
}
