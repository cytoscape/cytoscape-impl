/*
  File: IndeterminateProgressBar.java

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

// IndeterminateProgressBar.java
//-----------------------------------------------------------------
// $Date: 2007-03-19 17:25:45 -0700 (Mon, 19 Mar 2007) $
// $Author: iliana
//-----------------------------------------------------------------
package org.cytoscape.app.internal.util;


//-----------------------------------------------------------------
import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.*;


//-----------------------------------------------------------------
/**
 * This class creates a dialog with a JProgressBar in indeterminate state.
 * Showing a bar in indeterminate state is useful when a long task is running,
 * the task's running time can't be approximated (if it can, then it is
 * better to use csplugins.util.CytoscapeProgressMonitor), and the client
 * wants to let the user know that something is happening (instead of giving the
 * impression of a frozen program).
 */
public class IndeterminateProgressBar extends JDialog {
	JPanel mainPanel;
	JPanel labelPanel;
	JPanel barPanel;
	JLabel label;
	String labelText;
	JProgressBar pBar;

	/**
	 * Constructs an initially invisible, non-modal Dialog with no owner, the given title and label.
	 */
	public IndeterminateProgressBar(String title, String label) {
		super();
		labelText = label;
		setTitle(title);
	} //cons

	/**
	 * Constructs an initially invisible, non-modal Dialog with the specified owner dialog, title and label.
	 */
	public IndeterminateProgressBar(Dialog owner, String title, String label) {
		super(owner, title);
		labelText = label;
		create();
	} //cons

	/**
	 * Constructs an initially invisible, non-modal Dialog with the specified owner frame, title and label.
	 */
	public IndeterminateProgressBar(Frame owner, String title, String label) {
		super(owner, title);
		labelText = label;
		create();
	} //cons

	protected void create() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		labelPanel = new JPanel();
		label = new JLabel(labelText);
		labelPanel.add(label);
		mainPanel.add(labelPanel);
		barPanel = new JPanel();
		pBar = new JProgressBar(JProgressBar.HORIZONTAL);
		pBar.setIndeterminate(true);
		barPanel.add(pBar);
		mainPanel.add(barPanel);
		getContentPane().add(mainPanel);
	} //create

	/**
	 *  DOCUMENT ME!
	 *
	 * @param label_text DOCUMENT ME!
	 */
	public void setLabelText(String label_text) {
		labelText = label_text;
		label.setText(labelText);
	} //setLabelText
} //IndeterminateProgressBar
