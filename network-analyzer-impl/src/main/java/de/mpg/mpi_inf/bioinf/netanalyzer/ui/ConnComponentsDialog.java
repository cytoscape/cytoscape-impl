package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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


import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

import de.mpg.mpi_inf.bioinf.netanalyzer.ConnComponentAnalyzer;
import de.mpg.mpi_inf.bioinf.netanalyzer.CyNetworkUtils;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.CCInfo;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;

/**
 * Dialog listing the connected components of a network. This dialog also allows for a connected
 * component to be saved as a new network.
 * 
 * @author Yassen Assenov
 */
public class ConnComponentsDialog extends JDialog implements DocumentListener, ListSelectionListener {

	private final NewNetworkSelectedNodesAndEdgesTaskFactory tf;
	private final TaskManager<?, ?> tm;
	
	/**
	 * Initializes a new instance of <code>ConnComponentsDialog</code>.
	 * 
	 * @param aOwner The <code>Frame</code> from which this dialog is displayed.
	 * @param aNetwork Target network, to which the connected components belong.
	 * @param aComponents Array of all the connected components of <code>aNetwork</code>.
	 * @throws HeadlessException if <code>GraphicsEnvironment.isHeadless()</code> returns
	 *         <code>true</code>.
	 */
	public ConnComponentsDialog(Frame aOwner, CyNetwork aNetwork, CCInfo[] aComponents,
			final NewNetworkSelectedNodesAndEdgesTaskFactory tf, final TaskManager<?, ?> tm)
		throws HeadlessException {
		super(aOwner, Messages.DT_CONNCOMP, true);
		network = aNetwork;
		components = aComponents;
		this.tf = tf;
		this.tm = tm;
		
		initControls();
		pack();
		setResizable(false);
		setLocationRelativeTo(aOwner);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int i = listComp.getSelectedIndex() + 1;
		boolean enabled = (i != 0);
		btnExtract.getAction().setEnabled(enabled);
		
		if (enabled) {
			String title = network.getRow(network).get("name", String.class);
			if (title == null) {
				title = "_" + i;
			} else if (title.endsWith(".gml") || title.endsWith(".sif")) {
				final int cut = title.length() - 4;
				title = title.substring(0, cut) + "_" + i + title.substring(cut);
			} else {
				title = title + "_" + i;
			}
			//fieldNetName.setText(title);
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateBtnExtract();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateBtnExtract();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// Event is not processed
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 6103679911736096166L;

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	@SuppressWarnings("serial")
	private void initControls() {
		// Title
		final String tt = "<html>" + Messages.DI_CCOF + "<b>" + network.getRow(network).get("name", String.class) + "</b>:";
		final JLabel title = new JLabel(tt);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);

		// List of Components
		final int compCount = components.length;
		final String[] ccItems = new String[compCount];
		
		for (int i = 0; i < compCount; ++i) {
			ccItems[i] = Messages.DI_COMP + " " + (i + 1) + " (" + components[i].getSize() + ")";
		}
		
		listComp = new JList(ccItems);
		listComp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listComp.addListSelectionListener(this);
		
		if (compCount < 8) {
			listComp.setVisibleRowCount(compCount);
		}
		
		final JScrollPane scrollList = new JScrollPane(listComp);
		scrollList.setAlignmentX(Component.LEFT_ALIGNMENT);

		btnExtract = Utils.createButton(new AbstractAction(Messages.DI_EXTR) {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Reset
				for(CyNode node: network.getNodeList())
					network.getRow(node).set(CyNetwork.SELECTED, false);
				for(CyEdge edge: network.getEdgeList())
					network.getRow(edge).set(CyNetwork.SELECTED, false);
				
				CCInfo comp = components[listComp.getSelectedIndex()];
				final List<CyNode> nodes = new ArrayList<CyNode>(ConnComponentAnalyzer.getNodesOf(network, comp));
				final Set<CyEdge> edges = CyNetworkUtils.getAllConnectingEdges(network,nodes);
				
				for(CyNode node: nodes)
					network.getRow(node).set(CyNetwork.SELECTED, true);
				for(CyEdge edge: edges)
					network.getRow(edge).set(CyNetwork.SELECTED, true);
				
				final TaskIterator itr = tf.createTaskIterator(network);
				tm.execute(itr);
				//fieldNetName.getText();
			}
		}, null);
		btnCancel = Utils.createButton(new AbstractAction(Messages.DI_CANCEL) {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		}, null);
		
		Utils.equalizeSize(btnExtract, btnCancel);
		btnExtract.getAction().setEnabled(false);
		
		// Buttons Panel
		final JPanel panButtons = LookAndFeelUtil.createOkCancelPanel(btnExtract, btnCancel);
		panButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		// Layout
		final GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(title, Alignment.CENTER, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(scrollList, Alignment.CENTER, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(panButtons, Alignment.CENTER, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(title)
				.addComponent(scrollList, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(panButtons)
		);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), btnExtract.getAction(), btnCancel.getAction());
	}

	/**
	 * Updates the &quot;enabled&quot; status of the Extract button.
	 */
	private void updateBtnExtract() {
		if (listComp.getSelectedIndex() == -1) {
			btnExtract.getAction().setEnabled(false);
		} else {
			btnExtract.getAction().setEnabled(true);
		}
	}

	/**
	 * Network that contains the connected components listed in this dialog.
	 */
	private CyNetwork network;

	/**
	 * List of all connected component contained in {@link #network}.
	 */
	private CCInfo[] components;

	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnCancel;

	/**
	 * &quot;Extract&quot; button.
	 */
	private JButton btnExtract;

	/**
	 * List control that contains the connected components.
	 */
	private JList listComp;

//	/**
//	 * Text field for entering the name of a new network.
//	 */
//	private JTextField fieldNetName;
}
