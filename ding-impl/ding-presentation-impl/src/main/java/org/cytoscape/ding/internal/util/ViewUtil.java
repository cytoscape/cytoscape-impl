package org.cytoscape.ding.internal.util;

import java.awt.Cursor;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.FocusManager;
import javax.swing.SwingUtilities;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.property.values.Position;
import org.slf4j.Logger;

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

public final class ViewUtil {

	
	public static String toString(Rectangle2D r) {
		return String.format("(x:%.3f, y:%.3f, w:%.3f, h:%.3f)", r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}
	
	/**
	 * Utility method that invokes the code in Runnable.run on the AWT Event Dispatch Thread.
	 * @param runnable
	 */
	public static void invokeOnEDT(final Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}
	
	public static void invokeOnEDTAndWait(final Runnable runnable) {
		invokeOnEDTAndWait(runnable, null);
	}
	
	public static void invokeOnEDTAndWait(final Runnable runnable, final Logger logger) {
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
	
	public static boolean isLeftMouse(MouseEvent e) {
		boolean b = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
		if(LookAndFeelUtil.isMac()) {
			return !e.isControlDown() && b;
		}
		return b;
	}

	public static boolean isLeftClick(MouseEvent e) {
		boolean b = e.getButton() == MouseEvent.BUTTON1;
		if(LookAndFeelUtil.isMac()) {
			return !e.isControlDown() && b;
		}
		return b;
	}

	public static boolean isRightClick(MouseEvent e) {
		boolean b = e.getButton() == MouseEvent.BUTTON3; 
		if(!b && LookAndFeelUtil.isMac()) {
			// control - right click
			return e.isControlDown() && !e.isMetaDown() && (e.getButton() == MouseEvent.BUTTON1);
		}
		return b;
	}

	public static boolean isMiddleClick(MouseEvent e) {
		return e.getButton() == MouseEvent.BUTTON2; 
	}
	
	public static boolean isSingleClick(MouseEvent e) {
		return e.getClickCount() == 1;
	}
	
	public static boolean isDoubleClick(MouseEvent e) {
		return e.getClickCount() == 2;
	}
	
	public static boolean isSingleLeftClick(MouseEvent e) {
		return isLeftClick(e) && isSingleClick(e);
	}
	
	public static boolean isSingleRightClick(MouseEvent e) {
		return isRightClick(e) && isSingleClick(e);
	}
	
	public static boolean isDoubleLeftClick(MouseEvent e) {
		return isLeftClick(e) && isDoubleClick(e);
	}
	
	public static boolean isDragSelectionKeyDown(InputEvent e) {
		return e.isShiftDown() || isControlOrMetaDown(e);
	}
	
	public static boolean isAdditiveSelect(InputEvent e) {
		return e.isShiftDown() || isControlOrMetaDown(e);
	}
	
	public static boolean isControlOrMetaDown(final InputEvent e) {
		final boolean isMac = LookAndFeelUtil.isMac();
		return (isMac && e.isMetaDown()) || (!isMac && e.isControlDown());
	}
	
	public static Window getActiveWindow(DRenderingEngine re) {
		Window window = SwingUtilities.getWindowAncestor(re.getComponent());
		if (window == null)
			window = FocusManager.getCurrentManager().getActiveWindow();
		return window;
	}
	
	public static Cursor getResizeCursor(Position anchor) {
		switch(anchor) {
			case NORTH_EAST: return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
			case NORTH:      return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
			case NORTH_WEST: return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
			case WEST:       return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
			case SOUTH_WEST: return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
			case SOUTH:      return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
			case SOUTH_EAST: return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
			case EAST:       return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
			default:         return null;
		}
	}
	
	private ViewUtil() {
		// restrict instantiation
	}
}
