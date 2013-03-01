package org.cytoscape.internal.view;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import org.cytoscape.application.swing.CytoPanelName;

import javax.swing.*;
import java.awt.*;



/**
 * Contains methods to assist with various
 * tasks performed by the CytoPanel API.
 *
 * @author Ethan Cerami, Ben Gross
 */
public class CytoPanelUtil {
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
	public static Point getLocationOfExternalWindow(Dimension screenDimension,
	                                               Rectangle containerBounds,
	                                               Dimension frameDimension, final CytoPanelName compassDirection,
	                                               boolean outputDiagnostics)
	{
		if (outputDiagnostics)
			outputDiagnostics(screenDimension, containerBounds, frameDimension, compassDirection);

		//  Get Location and Dimension of Container
		Point containerLocation = containerBounds.getLocation();
		int containerWidth = (int) containerBounds.getWidth();
		int containerHeight = (int) containerBounds.getHeight();

		//  Get Screen Dimensions
		int screenWidth = (int) screenDimension.getWidth();
		int screenHeight = (int) screenDimension.getHeight();

		//  Initialize Point
		Point p = new Point(containerLocation.x, containerLocation.y);

		//  Set Point Based on Compass Direction
		if (compassDirection == CytoPanelName.WEST) {
			p.x = containerLocation.x - INSET - (int) frameDimension.getWidth();
		} else if (compassDirection == CytoPanelName.EAST) {
			p.x = containerLocation.x + INSET + (int) containerWidth;
		} else if (compassDirection == CytoPanelName.SOUTH) {
			p.y = containerLocation.y + INSET + (int) containerHeight;
		}

		//  Remove any negative coordinates
		p.x = Math.max(0, p.x);
		p.y = Math.max(0, p.y);

		if ((p.x + frameDimension.getWidth()) > screenWidth) {
			//  Adjust for right most case
			p.x = screenWidth - (int) frameDimension.getWidth();
		}

		if ((p.y + frameDimension.getHeight()) > screenHeight) {
			//  Adjust for bottom-most case
			p.y = screenHeight - (int) frameDimension.getHeight();
		}

		return p;
	}

	/**
	 * Determines if we are running on Windows platform.
	 */
	public boolean isWindows() {
		String os = System.getProperty("os.name");

		return os.regionMatches(true, 0, WINDOWS, 0, WINDOWS.length());
	}

	/**
	 * Outputs Diagnostics Related to Screen/Window Dimensions.
	 */
	private static void outputDiagnostics(Dimension screenDimension, Rectangle containerBounds,
	                                      Dimension preferredSizeOfPanel, final CytoPanelName compassDirection)
	{
		System.err.println("Compass Direction:  " + compassDirection);
		System.err.println("Screen Dimension:  " + screenDimension);
		System.err.println("Container Bounds:  " + containerBounds.toString());
		System.err.println("Preferred Size of Panel:  " + preferredSizeOfPanel.toString());
	}
}
