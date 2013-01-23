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

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 *
 */
public class PreferenceValueDialog extends JDialog implements ActionListener {
	private final static long serialVersionUID = 1202339873382923L;
	String preferenceName = null;
	String preferenceValue = null;
	//String title = null;
	JLabel preferenceNameL = null;
	JTextField value = null;
	JButton okButton = null;
	JButton cancelButton = null;
	PreferenceTableModel tableModel = null;
	boolean itemChanged = false;

	/**
	 * Creates a new PreferenceValueDialog object.
	 *
	 * @param owner  DOCUMENT ME!
	 * @param name  DOCUMENT ME!
	 * @param value  DOCUMENT ME!
	 * @param caller  DOCUMENT ME!
	 * @param tm  DOCUMENT ME!
	 * @param title  DOCUMENT ME!
	 */
	public PreferenceValueDialog(JDialog owner, String name, String value,
			PreferenceTableModel tm, String title) {
		super(owner, true);
		//callerRef = caller;
		tableModel = tm;
		
		preferenceName = new String(name);
		preferenceValue = new String(value);

		initDialog(owner);

		this.okButton.addActionListener(this);
		this.cancelButton.addActionListener(this);
		
		this.setTitle(title);
		// popup relative to owner/parent
		this.setLocationRelativeTo(owner);
		this.setVisible(true);
	}

	protected void initDialog(Dialog owner) {
		preferenceNameL = new JLabel(preferenceName);
		value = new JTextField(preferenceValue, 32);
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");

		JPanel outerPanel = new JPanel(new BorderLayout());
		JPanel valuePanel = new JPanel(new FlowLayout());
		JPanel buttonPanel = new JPanel(new FlowLayout());
		valuePanel.add(preferenceNameL);
		valuePanel.add(value);

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		outerPanel.add(valuePanel, BorderLayout.NORTH);
		outerPanel.add(buttonPanel, BorderLayout.SOUTH);

		this.getContentPane().add(outerPanel, BorderLayout.CENTER);
		pack();
	}


	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
		if (src instanceof JButton){

			JButton btn = (JButton) src;
			if (btn == this.okButton){
				this.tableModel.setProperty(preferenceName, value.getText());
				this.itemChanged = true;
				this.dispose();	
			}
			else if (btn == this.cancelButton){
				this.dispose();	
			}
		}
	}
}
