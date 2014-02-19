package org.cytoscape.ding.impl.cyannotator.dialogs;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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


import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class SelectColor extends JDialog {
	private ActionListener okListener;
	
	public SelectColor() {
		initComponents();
	}
    
	public SelectColor(Paint newColor){
		initComponents();
		jColorChooser1.setColor((Color)newColor);
	}    

	private void initComponents() {
		jColorChooser1 = new javax.swing.JColorChooser();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Select Color");
		setModalityType(DEFAULT_MODALITY_TYPE);
		setResizable(false);
		getContentPane().setLayout(null);
		getContentPane().add(jColorChooser1);
		jColorChooser1.setBounds(0, 0, 429, 340);

		okButton.setText("OK");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});
		getContentPane().add(okButton);
		okButton.setBounds(280, 351, 47, 23);

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});
		getContentPane().add(cancelButton);
		cancelButton.setBounds(345, 351, 75, 23);

		pack();
		setModalityType(DEFAULT_MODALITY_TYPE);
	}

	public void setOKListener(ActionListener l) {
		okListener = l;
	}

	public Color getColor() { return jColorChooser1.getColor(); }

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (okListener != null)
			okListener.actionPerformed(evt);
		dispose();
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	private javax.swing.JButton cancelButton;
	private javax.swing.JColorChooser jColorChooser1;
	private javax.swing.JButton okButton;

}

