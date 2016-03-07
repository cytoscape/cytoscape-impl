package org.cytoscape.internal.view;

import java.awt.Point;
import java.awt.Rectangle;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
 * Interface for Container of CytoPanel Objects.
 *
 * @author Ethan Cerami
 */
public interface CytoPanelContainer {
	/**
	 * Inserts CytoPanel at Specified Compass Direction.
	 *
	 * @param cytoPanel        CytoPanel Object.
	 * @param compassDirection CytoPanelName enum value.
	 */
	void insertCytoPanel(CytoPanel cytoPanel, final CytoPanelName compassDirection);

	/**
	 * Gets Location of Container, in screen coordinates.
	 *
	 * @return Point Object.
	 */
	Point getLocationOnScreen();

	/**
	 * Gets Bounds of Container, relative to parent component.
	 *
	 * @return Rectangle Object.
	 */
	Rectangle getBounds();
}
