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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;

/**
 * Dialog used to view and edit the visual settings assigned to a specific complex parameter.
 * 
 * @author Yassen Assenov
 */
public class SettingsDialog extends JDialog implements ActionListener {

	/**
	 * Constant identifying the user has pressed the &quot;Cancel&quot; button.
	 */
	public static final int STATUS_CANCEL = 0;

	/**
	 * Constant identifying the user has pressed the &quot;Save as Default&quot; button.
	 */
	public static final int STATUS_DEFAULT = 1;

	/**
	 * Constant identifying the user has pressed the &quot;OK&quot; button.
	 */
	public static final int STATUS_OK = 2;

	/**
	 * Initializes a new instance of <code>SettingsDialog</code>.
	 * <p>
	 * The created instance is a modal dialog.
	 * </p>
	 * 
	 * @param aOwner The <code>Dialog</code> from which this dialog is displayed.
	 * @param aTitle Title of the dialog.
	 * @throws HeadlessException If GraphicsEnvironment.isHeadless()
	 */
	public SettingsDialog(Dialog aOwner, String aTitle) throws HeadlessException {
		this(aOwner, aTitle, true);
	}

	/**
	 * Initializes a new instance of <code>SettingsDialog</code>.
	 * 
	 * @param aOwner The <code>Dialog</code> from which this dialog is displayed.
	 * @param aTitle Title of the dialog.
	 * @param aModal Flag indicating if the dialog must be modal or not.
	 * @throws HeadlessException If GraphicsEnvironment.isHeadless()
	 */
	public SettingsDialog(Dialog aOwner, String aTitle, boolean aModal) throws HeadlessException {
		super(aOwner, aTitle, aModal);

		status = STATUS_CANCEL;
		initControls();
		setResizable(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == btnSaveDefault) {
			status = STATUS_DEFAULT;
		} else if (source == btnOK) {
			status = STATUS_OK;
		}
		setVisible(false);
	}

	/**
	 * Gets the central panel, to which tabs are added for viewing and editing settings.
	 * 
	 * @return Setting's panel container of this dialog.
	 */
	public JTabbedPane getSettingsPane() {
		return settingsPane;
	}

	/**
	 * Gets the status of this windows's closure.
	 * <p>
	 * The following constants are used to describe the way this window was closed:
	 * </p>
	 * <ul>
	 * <li>{@link #STATUS_CANCEL}</li>
	 * <li>{@link #STATUS_DEFAULT}</li>
	 * <li>{@link #STATUS_OK}</li>
	 * </ul>
	 * 
	 * @return Status of this window's closure as one of the constants described above.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Updates the underlying data of all the settings panels displayed in this dialog.
	 * 
	 * @throws InvocationTargetException If an internal error occurs.
	 */
	public void update() throws InvocationTargetException {
		Stack<Container> containers = new Stack<Container>();
		containers.push(settingsPane);
		do {
			Component[] comp = containers.pop().getComponents();
			for (int i = 0; i < comp.length; ++i) {
				if (comp[i] instanceof SettingsPanel) {
					((SettingsPanel) comp[i]).updateData();
				} else if (comp[i] instanceof Container) {
					containers.add((Container) comp[i]);
				}
			}
		} while (!containers.isEmpty());
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -5462354078680551493L;

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	private void initControls() {
		Container contentPane = getContentPane();

		settingsPane = new JTabbedPane();
		contentPane.add(settingsPane);

		// Add Save as Default, OK and Cancel buttons
		JPanel buttonsPanel = new JPanel();
		btnSaveDefault = Utils.createButton(Messages.DI_SAVEDEFAULT, null, this);
		btnOK = Utils.createButton(Messages.DI_OK, null, this);
		btnCancel = Utils.createButton(Messages.DI_CANCEL, null, this);
		buttonsPanel.add(btnSaveDefault);
		buttonsPanel.add(Box.createHorizontalStrut(10));
		buttonsPanel.add(btnOK);
		buttonsPanel.add(btnCancel);
		Utils.equalizeSize(btnSaveDefault, btnOK, btnCancel);
		contentPane.add(buttonsPanel, BorderLayout.SOUTH);
	}

	/**
	 * Status of this window's closure.
	 */
	private int status;

	/**
	 * Setting's panels container of this dialog.
	 */
	private JTabbedPane settingsPane;

	/**
	 * &quot;Save as Default&quot; button.
	 */
	private JButton btnSaveDefault;

	/**
	 * &quot;OK&quot; button.
	 */
	private JButton btnOK;

	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnCancel;
}
