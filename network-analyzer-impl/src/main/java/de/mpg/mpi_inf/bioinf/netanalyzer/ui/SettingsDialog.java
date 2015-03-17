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

import java.awt.Component;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.cytoscape.util.swing.LookAndFeelUtil;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;

/**
 * Dialog used to view and edit the visual settings assigned to a specific complex parameter.
 * 
 * @author Yassen Assenov
 */
public class SettingsDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -5462354078680551493L;
	
	/** Constant identifying the user has pressed the &quot;Cancel&quot; button. */
	public static final int STATUS_CANCEL = 0;

	/** Constant identifying the user has pressed the &quot;Save as Default&quot; button. */
	public static final int STATUS_DEFAULT = 1;

	/** Constant identifying the user has pressed the &quot;OK&quot; button. */
	public static final int STATUS_OK = 2;

	/**
	 * Initializes a new instance of <code>SettingsDialog</code>.
	 * 
	 * @param aOwner The <code>Dialog</code> from which this dialog is displayed.
	 * @param aTitle Title of the dialog.
	 * @throws HeadlessException If GraphicsEnvironment.isHeadless()
	 */
	public SettingsDialog(Window aOwner, String aTitle) throws HeadlessException {
		super(aOwner, aTitle);

		status = STATUS_CANCEL;
		initControls();
		setResizable(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Object source = e.getSource();
		
		if (source == btnSaveDefault)
			status = STATUS_DEFAULT;
		
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
		final Stack<Container> containers = new Stack<Container>();
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
	 * Creates and lays out the controls inside this dialog.
	 * <p> This method is called upon initialization only.</p>
	 */
	@SuppressWarnings("serial")
	private void initControls() {
		settingsPane = new JTabbedPane();

		// Add Save as Default, OK and Cancel buttons
		btnSaveDefault = Utils.createButton(Messages.DI_SAVEDEFAULT, null, this);
		btnOK = Utils.createButton(new AbstractAction(Messages.DI_OK) {
			@Override
			public void actionPerformed(ActionEvent e) {
				status = STATUS_OK;
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
		final JPanel buttonsPanel = LookAndFeelUtil.createOkCancelPanel(btnOK, btnCancel, btnSaveDefault);
		
		final JPanel contentPane = new JPanel();
		final GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(settingsPane)
				.addComponent(buttonsPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(settingsPane)
				.addComponent(buttonsPanel)
		);
		
		setContentPane(contentPane);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), btnOK.getAction(), btnCancel.getAction());
		getRootPane().setDefaultButton(btnOK);
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
