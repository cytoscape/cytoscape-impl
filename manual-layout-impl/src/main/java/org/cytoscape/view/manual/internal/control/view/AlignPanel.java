package org.cytoscape.view.manual.internal.control.view;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
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
