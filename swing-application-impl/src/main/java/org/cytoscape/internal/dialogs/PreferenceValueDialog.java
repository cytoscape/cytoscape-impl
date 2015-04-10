package org.cytoscape.internal.dialogs;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.util.swing.LookAndFeelUtil;


/**
 *
 */
public class PreferenceValueDialog extends JDialog {
	
	private final static long serialVersionUID = 1202339873382923L;
	
	private String preferenceName;
	private String preferenceValue;
	private JLabel preferenceNameLbl;
	private JTextField valueTxt;
	private JButton okBtn;
	private JButton cancelBtn;
	private PreferenceTableModel tableModel;
	boolean itemChanged;

	/**
	 * Creates a new PreferenceValueDialog object.
	 */
	public PreferenceValueDialog(
			final JDialog owner,
			final String name,
			final String value,
			final PreferenceTableModel tm,
			final String title
	) {
		super(owner, title, ModalityType.APPLICATION_MODAL);
		
		tableModel = tm;
		preferenceName = name;
		preferenceValue = value;

		init();

		setResizable(false);
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	@SuppressWarnings("serial")
	protected void init() {
		preferenceNameLbl = new JLabel(preferenceName + ":");
		valueTxt = new JTextField(preferenceValue, 32);
		
		okBtn = new JButton(new AbstractAction("OK") {
			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.setProperty(preferenceName, valueTxt.getText());
				itemChanged = true;
				dispose();	
			}
		});
		cancelBtn = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		final JPanel buttonPnl = LookAndFeelUtil.createOkCancelPanel(okBtn, cancelBtn);
		
		final JPanel contentPane = new JPanel();
		final GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(preferenceNameLbl)
						.addComponent(valueTxt)
				)
				.addComponent(buttonPnl)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(preferenceNameLbl)
						.addComponent(valueTxt)
				)
				.addComponent(buttonPnl)
		);

		setContentPane(contentPane);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), okBtn.getAction(), cancelBtn.getAction());
		getRootPane().setDefaultButton(okBtn);
		
		pack();
	}
}
