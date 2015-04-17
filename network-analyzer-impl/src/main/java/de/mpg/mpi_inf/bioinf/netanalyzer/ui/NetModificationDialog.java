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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;

/**
 * Dialog for selecting networks on which to apply analysis or a basic modification.
 * 
 * @author Yassen Assenov
 */
public class NetModificationDialog extends NetworkListDialog {

	/**
	 * Initializes a new instance of <code>NetworkModificationDialog</code> with a modification warning.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aTitle
	 *            Window's title.
	 * @param aLabel
	 *            Label to be displayed on top of the network list.
	 * @param aHelpURL
	 *            URL of the page to be displayed when the user clicks on the &quot;Help&quot; button.
	 * 
	 * @throws HeadlessException
	 *             If <code>GraphicsEnvironment.isHeadless()</code> returns <code>true</code>.
	 */
	public NetModificationDialog(Frame aOwner, String aTitle, String aLabel, String aHelpURL, CyNetworkManager netMgr)
			throws HeadlessException {
		this(aOwner, aTitle, aLabel, aHelpURL, true, netMgr);
	}

	/**
	 * Initializes a new instance of <code>NetworkModificationDialog</code>.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aTitle
	 *            Window's title.
	 * @param aLabel
	 *            Label to be displayed on top of the network list.
	 * @param aHelpURL
	 *            URL of the page to be displayed when the user clicks on the &quot;Help&quot; button.
	 * @param aWarning
	 *            Flag indicating if a modification warning must be displayed. The text of the modification
	 *            warning is {@link Messages#SM_NETMODIFICATION}.
	 * 
	 * @throws HeadlessException
	 *             If <code>GraphicsEnvironment.isHeadless()</code> returns <code>true</code>.
	 */
	public NetModificationDialog(Frame aOwner, String aTitle, String aLabel, String aHelpURL, boolean aWarning, CyNetworkManager netMgr)
			throws HeadlessException {
		super(aOwner, aTitle, netMgr);

		helpURL = aHelpURL;
		selectedNetworks = null;
		initControls(aLabel, aWarning);
		pack();
		setLocationRelativeTo(aOwner);
	}

	/**
	 * Gets an array of all networks selected by the user.
	 * 
	 * @return A non-empty array of all networks selected by the user; <code>null</code> if the user has not
	 *         closed this dialog by clicking on the &quot;OK&quot; button.
	 */
	public CyNetwork[] getSelectedNetworks() {
		return selectedNetworks;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// Update the enabled status of the "OK" button.
		btnOK.getAction().setEnabled(isNetNameSelected());
	}

	/**
	 * Initializes and lays out additional controls to be added to this dialog.
	 * <p>
	 * Extender classes can override this method to create control(s).
	 * </p>
	 * 
	 * @return Newly initialized control or container with controls; <code>null</code> if no additional
	 *         controls are to be placed on the content pane of this dialog.
	 */
	protected JComponent initAdditionalControls() {
		return null;
	}

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 * 
	 * @param title
	 *            Label to be displayed on top of the network list.
	 * @param showWarning
	 *            Flag indicating if a modification warning must be displayed. The text of the modification
	 *            warning is {@link Messages#SM_NETMODIFICATION}.
	 */
	@SuppressWarnings("serial")
	protected void initControls(final String title, final boolean showWarning) {
		// Labels
		final JLabel titleLbl = new JLabel(title);
		
		final JLabel warningLbl = new JLabel(Messages.SM_NETMODIFICATION, SwingConstants.LEADING);
		warningLbl.setFont(warningLbl.getFont().deriveFont(11.0f));
		warningLbl.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		warningLbl.setVisible(showWarning);

		// List of loaded networks to select from
		final JScrollPane networkListScr = new JScrollPane(listNetNames);
		networkListScr.setMinimumSize(new Dimension(80, 120));
		
		JComponent additionalControls = initAdditionalControls();
		
		if (additionalControls == null) {
			additionalControls = new JPanel();
			additionalControls.setVisible(false);
		}
		
		// OK and Cancel buttons
		btnOK = Utils.createButton(new AbstractAction(Messages.DI_OK) {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Store the list of networks selected by the user
				final int[] indices = listNetNames.getSelectedIndices();
				final int size = indices.length;
				selectedNetworks = new CyNetwork[size];
				for (int i = 0; i < size; ++i) {
					selectedNetworks[i] = networks.get(indices[i]);
				}

				setVisible(false);
				dispose();
			}
		}, null);
		btnCancel = Utils.createButton(new AbstractAction(Messages.DI_CANCEL) {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		}, null);
		
		Utils.equalizeSize(btnOK, btnCancel);
		btnOK.getAction().setEnabled(false);
		
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
				.addComponent(additionalControls)
				.addComponent(warningLbl)
				.addComponent(buttonPnl)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(titleLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(networkListScr, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(additionalControls, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(warningLbl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(buttonPnl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		setContentPane(contentPane);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), btnOK.getAction(), btnCancel.getAction());
		getRootPane().setDefaultButton(btnOK);
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 1576652348409963833L;

	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnCancel;


	/**
	 * &quot;OK&quot; button.
	 */
	private JButton btnOK;

	/**
	 * URL of the page to be displayed when the user clicks on the &quot;Help&quot; button.
	 */
	private String helpURL;

	/**
	 * Array of networks selected by the user.
	 */
	private CyNetwork[] selectedNetworks;
}
