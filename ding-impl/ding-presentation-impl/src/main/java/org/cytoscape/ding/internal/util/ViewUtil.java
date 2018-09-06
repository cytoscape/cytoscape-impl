package org.cytoscape.ding.internal.util;

import java.awt.Window;
import java.awt.event.InputEvent;

import javax.swing.FocusManager;
import javax.swing.SwingUtilities;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.slf4j.Logger;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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
	
	public static boolean isDragSelectionKeyDown(final InputEvent e) {
		return e.isShiftDown() || isControlOrMetaDown(e);
	}
	
	public static boolean isControlOrMetaDown(final InputEvent e) {
		final boolean isMac = LookAndFeelUtil.isMac();
		
		return (isMac && e.isMetaDown()) || (!isMac && e.isControlDown());
	}
	
	public static Window getActiveWindow(CyNetworkView view) {
		Window window = null;
		
		if (view instanceof DGraphView)
			window = SwingUtilities.getWindowAncestor(((DGraphView) view).getComponent());
		
		if (window == null)
			window = FocusManager.getCurrentManager().getActiveWindow();
		
		return window;
	}
	
	private ViewUtil() {
	}
}
