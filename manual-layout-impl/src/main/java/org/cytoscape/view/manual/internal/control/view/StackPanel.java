
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.view.manual.internal.control.view;

import org.cytoscape.view.manual.internal.control.actions.stack.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import org.cytoscape.application.CyApplicationManager;

/**
 *
 */
public class StackPanel extends JPanel {
	/**
	 * Creates a new StackPanel object.
	 */
	public StackPanel(CyApplicationManager app) {

		ImageIcon vali = new ImageIcon(getClass().getResource("/images/V_STACK_LEFT.gif"));
		ImageIcon vaci = new ImageIcon(getClass().getResource("/images/V_STACK_CENTER.gif"));
		ImageIcon vari = new ImageIcon(getClass().getResource("/images/V_STACK_RIGHT.gif"));
		ImageIcon hati = new ImageIcon(getClass().getResource("/images/H_STACK_TOP.gif"));
		ImageIcon haci = new ImageIcon(getClass().getResource("/images/H_STACK_CENTER.gif"));
		ImageIcon habi = new ImageIcon(getClass().getResource("/images/H_STACK_BOTTOM.gif"));

		VStackLeft val = new VStackLeft(vali,app);
		VStackCenter vac = new VStackCenter(vaci,app);
		VStackRight var = new VStackRight(vari,app);

		HStackTop hat = new HStackTop(hati,app);
		HStackCenter hac = new HStackCenter(haci,app);
		HStackBottom hab = new HStackBottom(habi,app);

		setLayout(new GridLayout(1,6,0,0));
		add(createJButton(val, "Vertical Left"));
		add(createJButton(vac, "Vertical Center"));
		add(createJButton(var, "Vertical Right"));
		add(createJButton(hat, "Horizontal Top"));
		add(createJButton(hac, "Horizontal Center"));
		add(createJButton(hab, "Horizontal Bottom"));

		setBorder(new TitledBorder("Stack"));
	}

	protected JButton createJButton(Action a, String tt) {
		JButton b = new JButton(a);
		b.setToolTipText(tt);
		b.setPreferredSize(new Dimension(27, 18));
		b.setMaximumSize(new Dimension(27, 18));
		b.setMinimumSize(new Dimension(27, 18));
		b.setBorder(BorderFactory.createEmptyBorder());
		b.setBorderPainted(false);
		b.setOpaque(false);
		b.setContentAreaFilled(false);

		return b;
	}
}
