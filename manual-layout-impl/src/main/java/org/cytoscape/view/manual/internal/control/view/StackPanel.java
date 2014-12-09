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
import java.awt.GridLayout;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.manual.internal.control.actions.stack.HStackBottom;
import org.cytoscape.view.manual.internal.control.actions.stack.HStackCenter;
import org.cytoscape.view.manual.internal.control.actions.stack.HStackTop;
import org.cytoscape.view.manual.internal.control.actions.stack.VStackCenter;
import org.cytoscape.view.manual.internal.control.actions.stack.VStackLeft;
import org.cytoscape.view.manual.internal.control.actions.stack.VStackRight;

/**
 *
 */
@SuppressWarnings("serial")
public class StackPanel extends JPanel {
	
	/**
	 * Creates a new StackPanel object.
	 */
	public StackPanel(CyApplicationManager app) {
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);

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
		add(createJButton(val, "Vertical Stack Left"));
		add(createJButton(vac, "Vertical Stack Center"));
		add(createJButton(var, "Vertical Stack Right"));
		add(createJButton(hat, "Horizontal Stack Top"));
		add(createJButton(hac, "Horizontal Stack Center"));
		add(createJButton(hab, "Horizontal Stack Bottom"));

		setBorder(LookAndFeelUtil.createTitledBorder("Stack"));
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
