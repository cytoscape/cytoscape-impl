/*
  File: CytoPanelContainer.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.internal.view;


import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;

import java.awt.*;



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
