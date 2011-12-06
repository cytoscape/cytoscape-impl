
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

import org.cytoscape.view.manual.internal.control.actions.align.*;

import org.cytoscape.application.CyApplicationManager;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;


/**
 *
 */
public class AlignPanel extends JPanel {
	/**
	 * Creates a new AlignPanel object.
	 */
	public AlignPanel(CyApplicationManager app) {
		ImageIcon hari = new ImageIcon(getClass().getResource("/images/H_ALIGN_RIGHT.gif"));
		ImageIcon haci = new ImageIcon(getClass().getResource("/images/H_ALIGN_CENTER.gif"));
		ImageIcon hali = new ImageIcon(getClass().getResource("/images/H_ALIGN_LEFT.gif"));
		ImageIcon vati = new ImageIcon(getClass().getResource("/images/V_ALIGN_TOP.gif"));
		ImageIcon vaci = new ImageIcon(getClass().getResource("/images/V_ALIGN_CENTER.gif"));
		ImageIcon vabi = new ImageIcon(getClass().getResource("/images/V_ALIGN_BOTTOM.gif"));

		HAlignRight har = new HAlignRight(hari,app);
		HAlignCenter hac = new HAlignCenter(haci,app);
		HAlignLeft hal = new HAlignLeft(hali,app);

		VAlignTop vat = new VAlignTop(vati,app);
		VAlignCenter vac = new VAlignCenter(vaci,app);
		VAlignBottom vab = new VAlignBottom(vabi,app);

		setLayout(new GridLayout(1,6));

		add(createJButton(hal, "Horizontal Align Left"));
		add(createJButton(hac, "Horizontal Align Center"));
		add(createJButton(har, "Horizontal Align Right"));

		add(createJButton(vat, "Vertical Align Top"));
		add(createJButton(vac, "Vertical Align Center"));
		add(createJButton(vab, "Vertical Align Bottom"));

		setBorder(new TitledBorder("Align"));
	}

	protected JButton createJButton(Action a, String tt) {
		JButton b = new JButton(a);
		b.setToolTipText(tt);
		b.setPreferredSize(new Dimension(27, 18));
		b.setMaximumSize(new Dimension(27, 18));
		b.setBorder(BorderFactory.createEmptyBorder());
		b.setBorderPainted(false);
		b.setOpaque(false);
		b.setContentAreaFilled(false);

		return b;
	}
}
