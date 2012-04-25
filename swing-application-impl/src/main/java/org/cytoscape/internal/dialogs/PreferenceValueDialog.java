/*
  File: PreferenceValueDialog.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.internal.dialogs;

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
