package de.mpg.mpi_inf.bioinf.netanalyzer.ui.filter;

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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.ComplexParamFilter;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

/**
 * Base class for all dialogs for creating filters by the user.
 * 
 * @author Yassen Assenov
 */
public abstract class ComplexParamFilterDialog extends JDialog
	implements ActionListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (btnOK == source) {
			btnOK = null;
			setVisible(false);
		} else if (btnCancel == source) {
			setVisible(false);
		}
	}

	/**
	 * Displays the dialog and initializes a filter based on user's input.
	 * 
	 * @return Instance of a class extending <code>ComplexParamFilter</code> reflecting the user's
	 *         filtering criteria; <code>null</code> if the user has pressed the
	 *         &quot;Cancel&quot; button.
	 */
	public ComplexParamFilter showDialog() {
		setModal(true); // make sure this window is modal
		setVisible(true);
		if (btnOK == null) {
			// User has pressed OK
			return createFilter();
		}
		// User has pressed Cancel
		return null;
	}

	/**
	 * Initializes the common controls of <code>ComplexParamFilterDialog</code>.
	 * 
	 * @param aOwner The <code>Dialog</code> from which this dialog is displayed.
	 * @param aTitle Dialog's title.
	 */
	protected ComplexParamFilterDialog(Dialog aOwner, String aTitle) {
		super(aOwner, aTitle, true);
		initControls();
	}

	/**
	 * Creates and initializes a filter instance based on user's input.
	 * 
	 * @return Instance of a class extending <code>ComplexParamFilter</code> reflecting the user's
	 *         filtering criteria.
	 */
	protected abstract ComplexParamFilter createFilter();

	/**
	 * &quot;OK&quot; button.
	 */
	protected JButton btnOK;

	/**
	 * &quot;Cancel&quot; button.
	 */
	protected JButton btnCancel;

	/**
	 * Panel in the dialog that contains filter-specific controls.
	 */
	protected JPanel centralPane;

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -5390738966565631892L;

	/**
	 * Creates and lays out the common controls for this dialog.
	 * <p>
	 * The common controls include a central panel for adding filter-specific controls and
	 * &quot;OK&quot; and &quot;Cancel&quot; buttons.
	 * </p>
	 */
	private void initControls() {
		final int BS = Utils.BORDER_SIZE;
		final JPanel contentPane = new JPanel(new BorderLayout(BS, BS));
		Utils.setStandardBorder(contentPane);

		centralPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		contentPane.add(centralPane, BorderLayout.CENTER);

		// Add OK and Cancel Buttons
		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, Utils.BORDER_SIZE, 0));
		btnOK = Utils.createButton(Messages.DI_OK, null, this);
		btnCancel = Utils.createButton(Messages.DI_CANCEL, null, this);
		buttonsPanel.add(btnOK);
		buttonsPanel.add(btnCancel);
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		bottomPanel.add(buttonsPanel);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
		setContentPane(contentPane);
	}
}
