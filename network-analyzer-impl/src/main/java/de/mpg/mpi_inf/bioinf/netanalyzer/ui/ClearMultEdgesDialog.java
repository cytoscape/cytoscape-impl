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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.cytoscape.model.CyNetworkManager;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.sconnect.HelpConnector;

/**
 * Dialog for selecting networks on which multiple edges are to be cleared.
 * 
 * @author Yassen Assenov
 */
public class ClearMultEdgesDialog extends NetModificationDialog {

	/**
	 * Initializes a new instance of <code>ClearMultEdgesDialog</code>.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 */
	public ClearMultEdgesDialog(Frame aOwner, CyNetworkManager netMgr) {
		super(aOwner, Messages.DT_REMDUPEDGES, Messages.DI_REMDUPEDGES, HelpConnector.getRemDuplicatesURL(), netMgr);

		ignoreDirection = false;
		createEdgeAttr = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.ui.NetModificationDialog#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cbxIgnoreDirection) {
			ignoreDirection = cbxIgnoreDirection.isSelected();
		} else if (e.getSource() == cbxCreateEdgeAttr) {
			createEdgeAttr = cbxCreateEdgeAttr.isSelected();
		} else {
			super.actionPerformed(e);
		}
	}

	/**
	 * Gets the value of the &quot;Ignore edge direction&quot; flag.
	 * 
	 * @return <code>true</code> if the user has chosen to ignore edge direction; <code>false</code>
	 *         otherwise.
	 */
	public boolean getIgnoreDirection() {
		return ignoreDirection;
	}

	/**
	 * Gets the value of the &quot;Create edge attribute &quot; flag.
	 * 
	 * @return <code>true</code> if the user has chosen to create an edge attribute; <code>false</code>
	 *         otherwise.
	 */
	public boolean getCreateEdgeAttr() {
		return createEdgeAttr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.ui.NetModificationDialog#initAdditionalControls()
	 */
	@Override
	protected JComponent initAdditionalControls() {
		final JPanel panel = new JPanel(new GridLayout(2, 1));
		cbxIgnoreDirection = Utils.createCheckBox(Messages.DI_IGNOREEDGEDIR, Messages.TT_IGNOREEDGEDIR, this);
		panel.add(cbxIgnoreDirection);
		cbxCreateEdgeAttr = Utils.createCheckBox(Messages.DI_SAVENUMBEREDGES, Messages.TT_SAVENUMBEREDGES, this);
		panel.add(cbxCreateEdgeAttr);
		return panel;
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -2207325147812076427L;

	/**
	 * Check box control for selecting if edge direction should be ignored.
	 */
	private JCheckBox cbxIgnoreDirection;

	/**
	 * Flag indicating if edge direction should be ignored, that is, if all edges are to be treated as
	 * undirected.
	 */
	private boolean ignoreDirection;

	/**
	 * Check box control for selecting if an edge attribute representing the number of duplicated edges should
	 * be created.
	 */
	private JCheckBox cbxCreateEdgeAttr;

	/**
	 * Flag indicating if an edge attribute representing the number of duplicated edges should be created.
	 */
	private boolean createEdgeAttr;
}
