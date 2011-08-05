/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

import java.awt.Frame;

import javax.swing.JDialog;

import org.cytoscape.model.CyNetwork;

/**
 * Basic class for a visualization dialog.
 * 
 * @author Nadezhda Doncheva
 */
public abstract class VisualizeParameterDialog extends JDialog {

	/**
	 * Initializes a new instance of <code>MapParameterDialog</code>.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aName
	 *            Name of the dialog.
	 * @param modal
	 *            Flag indicating if the dialog should be modal or not.
	 * @param aNetwork
	 *            Target network for visualization of network parameters.
	 * @param aNodeAttr
	 *            Two-dimensional array with computed parameters stored as node attributes. The
	 *            first column contains all NetworkAnalyzer attributes, and the second one other
	 *            attributes.
	 * @param aEdgeAttr
	 *            Two-dimensional array with computed parameters stored as edge attributes. The
	 *            first column contains all NetworkAnalyzer attributes, and the second one other
	 *            attributes.
	 */
	protected VisualizeParameterDialog(Frame aOwner, String aName, boolean modal,
			CyNetwork aNetwork, String[][] aNodeAttr, String[][] aEdgeAttr) {
		super(aOwner, aName, modal);
		network = aNetwork;
		nodeAttr = aNodeAttr;
		edgeAttr = aEdgeAttr;
	}

	/**
	 * Combines two string arrays in one and adds the empty string at the beginning if
	 * <code>addEmpty</code> is <code>true</code>, and the string "SEPARATOR" between the two
	 * arrays.
	 * 
	 * @param attr
	 *            A 2d-String array with attributes. <code>attr[0]</code> are attributes computed
	 *            by NetworkAnalyzer and <code>attr[1]</code> other attributes.
	 * @param addEmpty
	 *            The method adds an empty string at the beginning of the new array, if this flag is
	 *            <code>true</code>.
	 * @return One string array that combines both the arrays from attr and adds the string
	 *         "SEPARATOR" between them. Depending on the flag <code>addEmpty</code> the first
	 *         position of the array could be an empty string.
	 */
	protected String[] combineAttrArray(String[][] attr, boolean addEmpty) {
		final int length0 = attr[0].length;
		final int length1 = attr[1].length;
		int combinedLength = length0 + length1;
		if (length0 > 0 && length1 != 0) {
			combinedLength++;
		}
		final int emptyCount = addEmpty ? 1 : 0;
		combinedLength = combinedLength + emptyCount;
		final String[] combined = new String[combinedLength];
		if (addEmpty) {
			combined[0] = "";
		}
		System.arraycopy(attr[0], 0, combined, emptyCount, length0);
		if (length1 > 0) {
			if (length0 > 0) {
				combined[length0 + emptyCount] = Utils.SEPARATOR;
			}
			System.arraycopy(attr[1], 0, combined, combinedLength - length1, length1);
		}
		return combined;
	}

	/**
	 * Two-dimensional array with computed parameters stored as node attributes. The first column
	 * contains all networkAnalyzer attributes, and the second one other attributes.
	 */
	protected String[][] nodeAttr;

	/**
	 * Two-dimensional array with computed parameters stored as edge attributes. The first column
	 * contains all networkAnalyzer attributes, and the second one other attributes.
	 */
	protected String[][] edgeAttr;

	/**
	 * Target network for visualization of network parameters.
	 */
	protected CyNetwork network;

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 2349065850841032444L;
}