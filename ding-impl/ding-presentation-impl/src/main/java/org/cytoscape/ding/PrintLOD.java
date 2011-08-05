
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

package org.cytoscape.ding;

import org.cytoscape.graph.render.stateful.GraphLOD;


/**
 *
 */
public class PrintLOD extends GraphLOD {
	/**
	 *  DOCUMENT ME!
	 *
	 * @param visibleNodeCount DOCUMENT ME!
	 * @param totalNodeCount DOCUMENT ME!
	 * @param totalEdgeCount DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public byte renderEdges(int visibleNodeCount, int totalNodeCount, int totalEdgeCount) {
		return 0;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param renderNodeCount DOCUMENT ME!
	 * @param renderEdgeCount DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean detail(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param renderNodeCount DOCUMENT ME!
	 * @param renderEdgeCount DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean nodeBorders(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param renderNodeCount DOCUMENT ME!
	 * @param renderEdgeCount DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean nodeLabels(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param renderNodeCount DOCUMENT ME!
	 * @param renderEdgeCount DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean customGraphics(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param renderNodeCount DOCUMENT ME!
	 * @param renderEdgeCount DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean edgeArrows(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param renderNodeCount DOCUMENT ME!
	 * @param renderEdgeCount DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean dashedEdges(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param renderNodeCount DOCUMENT ME!
	 * @param renderEdgeCount DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean edgeAnchors(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param renderNodeCount DOCUMENT ME!
	 * @param renderEdgeCount DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean edgeLabels(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param renderNodeCount DOCUMENT ME!
	 * @param renderEdgeCount DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean textAsShape(int renderNodeCount, int renderEdgeCount) {
		return exportTextAsShape;
	}

	private boolean exportTextAsShape = true;

	/**
	 *  DOCUMENT ME!
	 *
	 * @param pExportTextAsShape DOCUMENT ME!
	 */
	public void setPrintingTextAsShape(boolean pExportTextAsShape) {
		exportTextAsShape = pExportTextAsShape;
	}
}
