package org.cytoscape.work.internal.tunables;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.cytoscape.util.swing.LookAndFeelUtil;

public final class TunableDialog extends JDialog {

	private static final long serialVersionUID = 7438623438647443009L;

	protected final Component optionPanel;
	private JButton btnCancel;
	private JButton btnOK;
	private JScrollPane jScrollPane1;
	private JPanel pnlButtons;
	
	private String userInput = "";

	/**
	 * Construct this TunableDialog.
	 * @param parent The parent Window of this TunableDialog.
	 */
	public TunableDialog(final Window parent, final Component optionPanel) {
		super(parent);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.optionPanel = optionPanel;
		initComponents();
	}

	/** Set the text to replace the "OK" string on OK button. 
	 * @param okText the text to replace "OK" on the OK button.
	 * */
	public void setOKtext(String okText) {
		this.btnOK.setText(okText);
	}

	private void btnOKActionPerformed(ActionEvent evt) {
		this.userInput = "OK";
		this.jScrollPane1.removeAll();
		this.dispose();
	}

	private void btnCancelActionPerformed(ActionEvent evt) {
		this.userInput = "CANCEL";
		this.jScrollPane1.removeAll();
		this.dispose();
	}

	public String getUserInput() {
		return userInput;
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new GridBagLayout());
		
		GridBagConstraints gridBagConstraints;
		
		jScrollPane1 = new JScrollPane();
		jScrollPane1.setViewportView(optionPanel);
		jScrollPane1.setBorder(null);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(2, 2, 2, 2);
		getContentPane().add(jScrollPane1, gridBagConstraints);

		btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				btnOKActionPerformed(evt);
			}
		});

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				btnCancelActionPerformed(evt);
			}
		});
		
		// TODO create utility method that creates a proper OK+Cancel+etc panel
		pnlButtons = LookAndFeelUtil.createOkCancelPanel(btnOK, btnCancel);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(2, 0, 2, 0);
		getContentPane().add(pnlButtons, gridBagConstraints);
		
		pack();
		// Shouldn't call setSize after we pack.  Leads to really ugly dialogs if we only have a single tunable
		// setSize(this.getSize().width + 30, this.getSize().height + 30);
	}
}
