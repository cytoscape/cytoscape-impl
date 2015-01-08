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

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

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

	@Override
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

	@Override
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
		btnOK = Utils.createButton(Messages.DI_OK, null, this);
		btnOK.setEnabled(false);
		btnCancel = Utils.createButton(Messages.DI_CANCEL, null, this);
		Utils.equalizeSize(btnOK, btnCancel);
		
		final JPanel panBottom = LookAndFeelUtil.createOkCancelPanel(btnOK, btnCancel);
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
