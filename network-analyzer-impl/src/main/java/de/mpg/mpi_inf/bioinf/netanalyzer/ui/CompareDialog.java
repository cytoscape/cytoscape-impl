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
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetwork;

import de.mpg.mpi_inf.bioinf.netanalyzer.GOPTAlgorithm;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;

/**
 * Dialog for choosing networks to compare and operations to perform on the networks.
 * 
 * @author Yassen Assenov
 * @author Caroline Becker
 */
public class CompareDialog extends NetworkListDialog implements ActionListener {

	/**
	 * Initializes a new instance of <code>CompareDialog</code>.
	 * 
	 * @param aOwner The <code>Frame</code> from which this dialog is displayed.
	 * 
	 * @throws HeadlessException If <code>GraphicsEnvironment.isHeadless()</code> returns
	 *         <code>true</code>.
	 */
	public CompareDialog(Frame aOwner, CyNetworkManager netMgr) throws HeadlessException {
		super(aOwner, Messages.DT_COMPNETWORKS, netMgr);

		initControls();
		pack();
		setLocationRelativeTo(aOwner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (union == src || intersect == src || diff == src) {
			updateOKButton();
		} else if (btnCancel == src) {
			setVisible(false);
			dispose();
		} else if (btnOK == src) {
			final int[] indices = listNetNames.getSelectedIndices();
			final CyNetwork network1 = networks.get(indices[0]);
			final CyNetwork network2 = networks.get(indices[1]);
			// TODO use Advanced Network Merge once that becomes available.
			//GOPTAlgorithm algorithm = new GOPTAlgorithm(network1, network2);
			//algorithm.computeNetworks(intersect.isSelected(), union.isSelected(), diff.isSelected());
			JOptionPane.showMessageDialog(aOwner, "This functionality is currently disabled");
			setVisible(false);
			dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		updateOKButton();
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 5093100205881455450L;

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	private void initControls() {
		Box contentPane = Box.createVerticalBox();
		Utils.setStandardBorder(contentPane);

		JLabel title = new JLabel(Messages.DI_CNETWORKS);
		JPanel panTitle = new JPanel();
		panTitle.add(title);
		contentPane.add(panTitle);
		contentPane.add(Box.createVerticalStrut(Utils.BORDER_SIZE / 2));

		final JScrollPane scroller = new JScrollPane(listNetNames);
		final JPanel panNetList = new JPanel();
		panNetList.add(scroller);
		contentPane.add(panNetList);

		// Add checkboxes for the functions
		JPanel panCheckBoxes = new JPanel(new GridLayout(0, 1));
		union = Utils.createCheckBox(Messages.DI_CUNION, null, this);
		panCheckBoxes.add(union);
		intersect = Utils.createCheckBox(Messages.DI_CINTERSECTION, null, this);
		panCheckBoxes.add(intersect);
		diff = Utils.createCheckBox(Messages.DI_CDIFF, null, this);
		panCheckBoxes.add(diff);
		JPanel panChoices = new JPanel();
		panChoices.add(panCheckBoxes);
		contentPane.add(panChoices);

		// Add OK and Cancel buttons
		JPanel panBottom = new JPanel();
		JPanel panButtons = new JPanel(new GridLayout(1, 2, Utils.BORDER_SIZE, 0));
		btnOK = Utils.createButton(Messages.DI_OK, null, this);
		btnOK.setEnabled(false);
		panButtons.add(btnOK);
		btnCancel = Utils.createButton(Messages.DI_CANCEL, null, this);
		panButtons.add(btnCancel);
		panBottom.add(panButtons);
		contentPane.add(panBottom);
		setContentPane(contentPane);
	}

	/**
	 * Updates the &quot;enabled&quot; status of the &quot;OK&quot; button.
	 */
	private void updateOKButton() {
		boolean enable = false;
		if (union.isEnabled() || intersect.isEnabled() || diff.isEnabled()) {
			int[] indices = listNetNames.getSelectedIndices();
			enable = (indices.length == 2);
		}
		btnOK.setEnabled(enable);
	}

	/**
	 * &quot;OK&quot; button.
	 */
	private JButton btnOK;

	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnCancel;

	/**
	 * &quot;Compute Intersection&quot; check box.
	 */
	private JCheckBox intersect;

	/**
	 * &quot;Compute Union&quot; check box.
	 */
	private JCheckBox union;

	/**
	 * &quot;Compute Difference&quot; check box.
	 */
	private JCheckBox diff;
}
