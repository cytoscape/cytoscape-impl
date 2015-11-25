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

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.manual.internal.control.actions.dist.HDistCenter;
import org.cytoscape.view.manual.internal.control.actions.dist.HDistLeft;
import org.cytoscape.view.manual.internal.control.actions.dist.HDistRight;
import org.cytoscape.view.manual.internal.control.actions.dist.VDistBottom;
import org.cytoscape.view.manual.internal.control.actions.dist.VDistCenter;
import org.cytoscape.view.manual.internal.control.actions.dist.VDistTop;


/**
 *
 */
@SuppressWarnings("serial")
public class DistPanel extends JPanel {
	
	/**
	 * Creates a new DistPanel object.
	 */
	public DistPanel(CyApplicationManager app) {
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
		ImageIcon hali = new ImageIcon(getClass().getResource("/images/H_DIST_LEFT.gif"));
		ImageIcon haci = new ImageIcon(getClass().getResource("/images/H_DIST_CENTER.gif"));
		ImageIcon hari = new ImageIcon(getClass().getResource("/images/H_DIST_RIGHT.gif"));
		ImageIcon vati = new ImageIcon(getClass().getResource("/images/V_DIST_TOP.gif"));
		ImageIcon vaci = new ImageIcon(getClass().getResource("/images/V_DIST_CENTER.gif"));
		ImageIcon vabi = new ImageIcon(getClass().getResource("/images/V_DIST_BOTTOM.gif"));

		HDistLeft hal = new HDistLeft(hali,app);
		HDistCenter hac = new HDistCenter(haci,app);
		HDistRight har = new HDistRight(hari,app);

		VDistTop vat = new VDistTop(vati,app);
		VDistCenter vac = new VDistCenter(vaci,app);
		VDistBottom vab = new VDistBottom(vabi,app);

		setLayout(new java.awt.GridLayout(1,6));

		add(createJButton(hal, "Horizontal Distribute Left"));
		add(createJButton(hac, "Horizontal Distribute Center"));
		add(createJButton(har, "Horizontal Distribute Right"));
		add(createJButton(vat, "Vertical Distribute Top"));
		add(createJButton(vac, "Vertical Distribute Center"));
		add(createJButton(vab, "Vertical Distribute Bottom"));

		setBorder(LookAndFeelUtil.createTitledBorder("Distribute"));
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
