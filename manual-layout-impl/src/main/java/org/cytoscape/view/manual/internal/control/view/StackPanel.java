package org.cytoscape.view.manual.internal.control.view;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.manual.internal.control.actions.stack.HStackBottom;
import org.cytoscape.view.manual.internal.control.actions.stack.HStackCenter;
import org.cytoscape.view.manual.internal.control.actions.stack.HStackTop;
import org.cytoscape.view.manual.internal.control.actions.stack.VStackCenter;
import org.cytoscape.view.manual.internal.control.actions.stack.VStackLeft;
import org.cytoscape.view.manual.internal.control.actions.stack.VStackRight;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class StackPanel extends JPanel {
	
	private JButton valButton;
	private JButton vacButton;
	private JButton varButton;
	private JButton hatButton;
	private JButton hacButton;
	private JButton habButton;
	
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

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		int HGHT = 32;
		setMinimumSize(new Dimension(120, HGHT));
		setPreferredSize(new Dimension(300, HGHT));
		setMaximumSize(new Dimension(350, HGHT));
		
		JLabel stack = new JLabel("Stack");
		add(Box.createRigidArea(new Dimension(25, 0)));
    	add(stack);
		stack.setPreferredSize(new Dimension(105, 25));
		stack.setMinimumSize(new Dimension(105, 25));
		stack.setMaximumSize(new Dimension(105, 25));
		add(valButton = createJButton(val, "Vertical Stack Left"));
		add(vacButton = createJButton(vac, "Vertical Stack Center"));
		add(varButton = createJButton(var, "Vertical Stack Right"));
		add(hatButton = createJButton(hat, "Horizontal Stack Top"));
		add(hacButton = createJButton(hac, "Horizontal Stack Center"));
		add(habButton = createJButton(hab, "Horizontal Stack Bottom"));
	}
	
	@Override
	public void setEnabled(final boolean enabled) {
		valButton.setEnabled(enabled);
		vacButton.setEnabled(enabled);
		varButton.setEnabled(enabled);
		hatButton.setEnabled(enabled);
		hacButton.setEnabled(enabled);
		habButton.setEnabled(enabled);
		
		super.setEnabled(enabled);
	}

	protected JButton createJButton(Action a, String tt) {
		JButton b = new JButton(a);
		b.setToolTipText(tt);
		b.setPreferredSize(new Dimension(32, 24));
		b.setMaximumSize(new Dimension(32, 24));
		b.setMinimumSize(new Dimension(32, 24));
		b.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		b.setBorderPainted(false);
		b.setOpaque(false);
		b.setContentAreaFilled(false);

		return b;
	}
}
