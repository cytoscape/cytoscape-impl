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
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;

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

	private static final long serialVersionUID = 5093100205881455450L;
	
	/** &quot;OK&quot; button. */
	private JButton btnOK;
	/** &quot;Cancel&quot; button. */
	private JButton btnCancel;
	/** &quot;Compute Intersection&quot; check box. */
	private JCheckBox intersect;
	/** &quot;Compute Union&quot; check box. */
	private JCheckBox union;
	/** &quot;Compute Difference&quot; check box. */
	private JCheckBox diff;
	
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
		updateOKButton();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		updateOKButton();
	}

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	@SuppressWarnings("serial")
	private void initControls() {
		final JLabel titleLbl = new JLabel(Messages.DI_CNETWORKS);
		final JScrollPane networkListScr = new JScrollPane(listNetNames);

		// Add checkboxes for the functions
		union = Utils.createCheckBox(Messages.DI_CUNION, null, this);
		intersect = Utils.createCheckBox(Messages.DI_CINTERSECTION, null, this);
		diff = Utils.createCheckBox(Messages.DI_CDIFF, null, this);
		
		// Add OK and Cancel buttons
		btnOK = Utils.createButton(new AbstractAction(Messages.DI_OK) {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO use Advanced Network Merge once that becomes available.
//				final int[] indices = listNetNames.getSelectedIndices();
//				final CyNetwork network1 = networks.get(indices[0]);
//				final CyNetwork network2 = networks.get(indices[1]);
//				GOPTAlgorithm algorithm = new GOPTAlgorithm(network1, network2);
//				algorithm.computeNetworks(intersect.isSelected(), union.isSelected(), diff.isSelected());
				JOptionPane.showMessageDialog(aOwner, "This functionality is currently disabled");
				setVisible(false);
				dispose();
			}
		}, null);
		btnOK.getAction().setEnabled(false);
		
		btnCancel = Utils.createButton(new AbstractAction(Messages.DI_CANCEL) {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		}, null);
		
		Utils.equalizeSize(btnOK, btnCancel);
		
		final JPanel buttonPnl = LookAndFeelUtil.createOkCancelPanel(btnOK, btnCancel);

		// Layout
		final JPanel contentPane = new JPanel();
		final GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(titleLbl)
				.addComponent(networkListScr, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(union)
				.addComponent(intersect)
				.addComponent(diff)
				.addComponent(buttonPnl)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(titleLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(networkListScr, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(union, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(intersect, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(diff, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(buttonPnl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
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
		
		btnOK.getAction().setEnabled(enable);
	}
}
