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

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Dialog for viewing and editing plugin's settings.
 * 
 * @author Yassen Assenov
 */
public class PluginSettingsDialog extends JDialog implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(PluginSettingsDialog.class);

	/**
	 * Initializes a new instance of <code>PluginSettingsDialog</code>.
	 * <p>
	 * The dialog created is modal and has a title &quot;NetworkAnalyzer Settings&quot;. The
	 * constructor creates and lays out all the controls of the dialog. It also positions the window
	 * according to its parent, so no subsequent calls to <code>pack()</code> or
	 * <code>setLocation(...)</code> are necessary.
	 * </p>
	 * 
	 * @param aOwner The <code>Dialog</code> from which this dialog is displayed.
	 * 
	 */
	public PluginSettingsDialog(Dialog aOwner) {
		super(aOwner, Messages.DT_SETTINGS, true);
		initControls();
		pack();
		setLocationRelativeTo(aOwner);
	}

	/**
	 * Initializes a new instance of <code>PluginSettingsDialog</code>.
	 * 
	 * @param aOwner The <code>Dialog</code> from which this dialog is displayed.
	 * 
	 */
	public PluginSettingsDialog(Frame aOwner) {
		super(aOwner, Messages.DT_SETTINGS, true);
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
		Object source = e.getSource();
		if (source == btnOK) {
			try {
				panSettings.updateData();
				SettingsSerializer.save();
			} catch (InvocationTargetException ex) {
				// NetworkAnalyzer internal error
				logger.error(Messages.SM_LOGERROR, ex);
			} catch (IOException ex) {
				Utils.showErrorBox(this,Messages.DT_IOERROR, Messages.SM_DEFFAILED);
			} finally {
				this.setVisible(false);
				this.dispose();
			}
		} else if (source == btnCancel) {
			this.setVisible(false);
			this.dispose();
		}
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 7118008694414543131L;

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	private void initControls() {
		JPanel contentPane = new JPanel(new BorderLayout(0, Utils.BORDER_SIZE));
		Utils.setStandardBorder(contentPane);

		panSettings = new SettingsPanel(SettingsSerializer.getPluginSettings());
		contentPane.add(panSettings, BorderLayout.CENTER);

		// Add OK, Cancel and Help buttons
		JPanel panButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, Utils.BORDER_SIZE, 0));
		btnOK = Utils.createButton(Messages.DI_OK, null, this);
		btnCancel = Utils.createButton(Messages.DI_CANCEL, null, this);
		Utils.equalizeSize(btnOK, btnCancel);
		panButtons.add(btnOK);
		panButtons.add(btnCancel);
		panButtons.add(Box.createHorizontalStrut(Utils.BORDER_SIZE * 2));
		contentPane.add(panButtons, BorderLayout.SOUTH);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().add(contentPane);
		getRootPane().setDefaultButton(btnOK);
		btnOK.requestFocusInWindow();
	}

	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnCancel;

	/**
	 * &quot;OK&quot; button.
	 */
	private JButton btnOK;

	/**
	 * Panel that contains the controls for adjusting the plugin's settings.
	 */
	private SettingsPanel panSettings;
}
