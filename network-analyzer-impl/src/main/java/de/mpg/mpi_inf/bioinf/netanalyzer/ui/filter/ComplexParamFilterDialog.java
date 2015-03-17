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

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.cytoscape.util.swing.LookAndFeelUtil;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.ComplexParamFilter;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

/**
 * Base class for all dialogs for creating filters by the user.
 * 
 * @author Yassen Assenov
 */
public abstract class ComplexParamFilterDialog extends JDialog {

	/**
	 * Displays the dialog and initializes a filter based on user's input.
	 * 
	 * @return Instance of a class extending <code>ComplexParamFilter</code> reflecting the user's
	 *         filtering criteria; <code>null</code> if the user has pressed the
	 *         &quot;Cancel&quot; button.
	 */
	public ComplexParamFilter showDialog() {
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
	@SuppressWarnings("serial")
	private void initControls() {
		centralPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

		// Add OK and Cancel Buttons
		btnOK = Utils.createButton(new AbstractAction(Messages.DI_OK) {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnOK = null;
				setVisible(false);
			}
		}, null);
		btnCancel = Utils.createButton(new AbstractAction(Messages.DI_CANCEL) {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		}, null);
		Utils.equalizeSize(btnOK, btnCancel);
		
		final JPanel buttonsPanel = LookAndFeelUtil.createOkCancelPanel(btnOK, btnCancel);
		
		final JPanel contentPane = new JPanel();
		final GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(centralPane)
				.addComponent(buttonsPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(centralPane)
				.addComponent(buttonsPanel)
		);
		
		setContentPane(contentPane);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), btnOK.getAction(), btnCancel.getAction());
		getRootPane().setDefaultButton(btnOK);
	}
}
