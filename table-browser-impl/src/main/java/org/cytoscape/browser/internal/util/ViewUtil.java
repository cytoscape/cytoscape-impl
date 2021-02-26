package org.cytoscape.browser.internal.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.slf4j.Logger;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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

public class ViewUtil {

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
	
	public static void styleToolBarButton(AbstractButton btn) {
		styleToolBarButton(btn, null, true);
	}
	
	public static void styleToolBarButton(AbstractButton btn, boolean addPadding) {
		styleToolBarButton(btn, null, addPadding);
	}
	
	public static void styleToolBarButton(AbstractButton btn, Font font) {
		styleToolBarButton(btn, font, true);
	}
	
	public static void styleToolBarButton(AbstractButton btn, Font font, boolean addPadding) {
		int hPad = addPadding ? 5 : 0;
		int vPad = addPadding ? 4 : 0;
		styleToolBarButton(btn, font, hPad, vPad);
	}
	
	public static void styleToolBarButton(AbstractButton btn, Font font, int hPad, int vPad) {
		if (font != null)
			btn.setFont(font);
		
		// Decrease the padding, because it will have a border
//		if (btn instanceof JToggleButton) {
//			hPad = Math.max(0, hPad - 4);
//			vPad = Math.max(0, vPad - 4);
//		}
		
		btn.setFocusPainted(false);
		btn.setFocusable(false);
		btn.setBorder(BorderFactory.createEmptyBorder());
		btn.setContentAreaFilled(false);
		btn.setOpaque(true);
		btn.setHorizontalTextPosition(SwingConstants.CENTER);
		btn.setVerticalTextPosition(SwingConstants.TOP);
		
		if (hPad > 0 || vPad > 0) {
			var d = btn.getPreferredSize();
			d = new Dimension(d.width + 2 * hPad, d.height + 2 * vPad);
			btn.setPreferredSize(d);
			btn.setMinimumSize(d);
			btn.setMaximumSize(d);
			btn.setSize(d);
		}
		
		if (btn instanceof JToggleButton) {
			btn.addItemListener(evt -> updateToolBarStyle((JToggleButton) btn));
			updateToolBarStyle((JToggleButton) btn);
		}
	}
	
	public static void updateToolBarStyle(JToggleButton btn) {
		updateToolBarStyle(btn, true);
	}
	
	public static void updateToolBarStyle(JToggleButton btn, boolean showSelectionBorder) {
		var defBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		
		if (btn.isEnabled()) {
			Border selBorder = showSelectionBorder ?
					BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(1, 1, 1, 1, UIManager.getColor("CyToggleButton[Selected].borderColor")),
							BorderFactory.createEmptyBorder(1, 1, 1, 1))
					: defBorder;
			
			btn.setBorder(btn.isSelected() ? selBorder : defBorder);
			btn.setBackground(
					btn.isSelected() ?
					UIManager.getColor("CyToggleButton[Selected].background") :
					UIManager.getColor("CyToggleButton.background"));
			btn.setForeground(
					btn.isSelected() ?
					UIManager.getColor("CyToggleButton[Selected].foreground") :
					UIManager.getColor("CyToggleButton.foreground"));
		} else {
			btn.setBorder(defBorder);
			btn.setForeground(UIManager.getColor("ToggleButton.disabledForeground"));
			btn.setBackground(UIManager.getColor("CyToggleButton.unselectedBackground"));
		}
	}
	
	public static JSeparator createToolBarSeparator() {
		var sep = new ToolBarSeparator(JSeparator.VERTICAL);
		sep.setForeground(UIManager.getColor("Separator.foreground"));
		
		return sep;
	}
	
	private ViewUtil() {
	}
	
	@SuppressWarnings("serial")
	private static class ToolBarSeparator extends JSeparator {

		ToolBarSeparator(int orientation) {
			super(orientation);
		}
		
		@Override
		public void paint(Graphics g) {
			var s = getSize();

			if (getOrientation() == JSeparator.VERTICAL) {
				g.setColor(getForeground());
				g.drawLine(0, 0, 0, s.height);
			} else {
				g.setColor(getForeground());
				g.drawLine(0, 0, s.width, 0);
			}
		}
		
		@Override
		public Dimension getPreferredSize() {
			if (getOrientation() == JSeparator.VERTICAL)
				return new Dimension(1, 0);
			else
				return new Dimension(0, 1);
		}
	}
}
