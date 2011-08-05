/* -*-Java-*-
********************************************************************************
*
* File:         DragSourceContextCurorSetter.java
* RCS:          $Header: $
* Description:
* Author:       Michael L. Creech
* Created:      Sat Dec 16 14:54:45 2006
* Modified:     Sun Dec 17 05:41:06 2006 (Michael L. Creech) creech@w235krbza760
* Language:     Java
* Package:
/*
 
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
********************************************************************************
*/
package org.cytoscape.editor.internal.gui;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.dnd.DragSourceDragEvent;



/**
 * Interface used to allow a general computation to determine
 * the cursor to show during a drag operation originating
 * from a BasicCytoShapeEntity (palette entry) that is dragged over a
 * CyNetworkView of interest.
 * @author Michael L. Creech
 */
public interface DragSourceContextCursorSetter {
	/**
	 * Used to determine the cursor to show during a drag operation originating
	 * from a BasicCytoShapeEntity (palette entry) that is dragged over a
	 * CyNetworkView of interest. For example, we might want to show that
	 * it is illegal to drop on certain objects, such as Edges.
	 * @param netView the CyNetworkView that is being dragged over.
	 * @param netViewLoc the location within netView of the cursor, in netView coordinates.
	 * @param dsde the DragSourceDragEvent that describes this particular
	 *        drag operation.
	 * @return the Cursor to display during this drag (e.g, DragSource.DefaultCopyNoDrop).
	 */
	public Cursor computeCursor(Point netViewLoc, DragSourceDragEvent dsde);
}
