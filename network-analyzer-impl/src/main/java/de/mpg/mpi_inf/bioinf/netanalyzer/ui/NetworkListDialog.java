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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;

/**
 * Base class for dialogs which display a list of all loaded networks in Cytoscape.
 * <p>
 * This type of dialog is always created modal in order to prevent network modification (e.g.
 * deletion of network from Cytoscape) while the dialog is visible.
 * </p>
 * 
 * @author Yassen Assenov
 */
abstract class NetworkListDialog extends JDialog implements ListSelectionListener {

	private final CyNetworkManager netMgr;
	protected final Window aOwner;
	
	/**
	 * Initializes the fields of <code>NetworkListDialog</code>.
	 * 
	 * @param aOwner The <code>Frame</code> from which this dialog is displayed.
	 * @param aTitle Title of the dialog.
	 */
	protected NetworkListDialog(Frame aOwner, String aTitle, final CyNetworkManager netMgr) {
		super(aOwner, aTitle, true);
		this.aOwner = aOwner;
		this.netMgr = netMgr;
		initNetworkList();
	}

	/**
	 * Initializes the fields of <code>NetworkListDialog</code>.
	 * 
	 * @param aOwner The <code>Dialog</code> from which this dialog is displayed.
	 * @param aTitle Title of the dialog.
	 */
	protected NetworkListDialog(Dialog aOwner, String aTitle, final CyNetworkManager netMgr) {
		super(aOwner, aTitle, true);
		this.netMgr = netMgr;
		this.aOwner = aOwner;
		initNetworkList();
	}

	/**
	 * Checks if a network name in the list of networks is selected.
	 * 
	 * @return <code>true</code> if at least one of the listed network names is selected; <code>false</code> otherwise.
	 */
	protected boolean isNetNameSelected() {
		int[] indices = listNetNames.getSelectedIndices();
		return indices.length > 0;
	}

	/**
	 * List of all available networks.
	 */
	protected List<CyNetwork> networks;

	/**
	 * List control that contains the names of the available networks.
	 * 
	 * @see #networks
	 */
	protected JList listNetNames;

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 5706001778102104118L;

	/**
	 * Initializes the network list and list control containing network names.
	 */
	private void initNetworkList() {
		final Set<CyNetwork> networkSet = netMgr.getNetworkSet();
		final int netCount = networkSet.size();
		networks = new ArrayList<CyNetwork>(netCount);
		String[] netTitles = new String[netCount];
		int i = 0;
		for (final CyNetwork network : networkSet) {
			networks.add(network);
			netTitles[i++] = network.getCyRow(network).get("name", String.class);
		}
		listNetNames = new JList(netTitles);
		listNetNames.addListSelectionListener(this);
		if (netCount < 8) {
			listNetNames.setVisibleRowCount(netCount);
		}
	}
}
