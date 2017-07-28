package org.cytoscape.internal.view;


import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;

import org.cytoscape.application.swing.CytoPanelName;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

/**
 * Contains methods to assist with various tasks performed by the CytoPanel API.
 */
public final class CytoPanelUtil {
	
	public static final int WEST_MIN_WIDTH = 100;
	public static final int WEST_MAX_WIDTH = 400;
	public static final int WEST_MIN_HEIGHT = 250;
	
	public static final int SOUTH_MIN_WIDTH = 500;
	public static final int SOUTH_MIN_HEIGHT = 50;
	
	public static final int EAST_MIN_WIDTH = 100;
	public static final int EAST_MAX_WIDTH = 1500;
	public static final int EAST_MIN_HEIGHT = 100;
	
	static final int BUTTON_SIZE = 16;
	
	/**
	 * String used to compare against os.name System property -
	 * to determine if we are running on Windows platform.
	 */
	static final String WINDOWS = "windows";

	/**
	 * Inset between owner frame and floating window.
	 */
	private static final int INSET = 5;

	/**
	 * Gets Location of External Window, based on current UI Dimensions.
	 *
	 * @param screenDimension  Current Screen Dimensions.
	 * @param containerBounds  Container Bounds Rectangle.
	 * @param frameDimension   Current Frame Dimensions.
	 * @param compassDirection Compass Direction, SwingConstants.
	 * @return Point Object.
	 */
	public static Point getLocationOfExternalWindow(
			Dimension screenDimension,
			Rectangle containerBounds,
			Dimension frameDimension,
			CytoPanelName compassDirection,
			boolean outputDiagnostics) {
		if (outputDiagnostics)
			outputDiagnostics(screenDimension, containerBounds, frameDimension, compassDirection);

		// Get Location and Dimension of Container
		Point containerLocation = containerBounds.getLocation();
		int containerWidth = (int) containerBounds.getWidth();
		int containerHeight = (int) containerBounds.getHeight();

		// Get Screen Dimensions
		int screenWidth = (int) screenDimension.getWidth();
		int screenHeight = (int) screenDimension.getHeight();

		// Initialize Point
		Point p = new Point(containerLocation.x, containerLocation.y);

		// Set Point Based on Compass Direction
		if (compassDirection == CytoPanelName.WEST) {
			p.x = containerLocation.x - INSET - (int) frameDimension.getWidth();
		} else if (compassDirection == CytoPanelName.EAST) {
			p.x = containerLocation.x + INSET + (int) containerWidth;
		} else if (compassDirection == CytoPanelName.SOUTH) {
			p.y = containerLocation.y + INSET + (int) containerHeight;
		}

		// Remove any negative coordinates
		p.x = Math.max(0, p.x);
		p.y = Math.max(0, p.y);

		// Adjust for right most case
		if ((p.x + frameDimension.getWidth()) > screenWidth)
			p.x = screenWidth - (int) frameDimension.getWidth();

		// Adjust for bottom-most case
		if ((p.y + frameDimension.getHeight()) > screenHeight)
			p.y = screenHeight - (int) frameDimension.getHeight();

		return p;
	}

	public static void styleButton(final AbstractButton btn) {
		btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		btn.setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
		btn.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
		btn.setSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
		btn.setRolloverEnabled(false);
		btn.setFocusPainted(false);
		btn.setFocusable(false);
		btn.setContentAreaFilled(false);
	}
	
	/**
	 * Outputs Diagnostics Related to Screen/Window Dimensions.
	 */
	private static void outputDiagnostics(Dimension screenDimension, Rectangle containerBounds,
			Dimension preferredSizeOfPanel, final CytoPanelName compassDirection) {
		System.err.println("Compass Direction:  " + compassDirection);
		System.err.println("Screen Dimension:  " + screenDimension);
		System.err.println("Container Bounds:  " + containerBounds.toString());
		System.err.println("Preferred Size of Panel:  " + preferredSizeOfPanel.toString());
	}
}
