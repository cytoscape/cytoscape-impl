package org.cytoscape.view.manual.internal.control.view;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.manual.internal.control.actions.stack.HStackBottom;
import org.cytoscape.view.manual.internal.control.actions.stack.HStackCenter;
import org.cytoscape.view.manual.internal.control.actions.stack.HStackTop;
import org.cytoscape.view.manual.internal.control.actions.stack.VStackCenter;
import org.cytoscape.view.manual.internal.control.actions.stack.VStackLeft;
import org.cytoscape.view.manual.internal.control.actions.stack.VStackRight;
import org.cytoscape.view.manual.internal.util.Util;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
	
	private JLabel label;
	private JButton valButton;
	private JButton vacButton;
	private JButton varButton;
	private JButton hatButton;
	private JButton hacButton;
	private JButton habButton;
	
	public StackPanel(CyApplicationManager app, UndoSupport undoSupport) {
		ImageIcon vali = new ImageIcon(getClass().getResource("/images/V_STACK_LEFT.gif"));
		ImageIcon vaci = new ImageIcon(getClass().getResource("/images/V_STACK_CENTER.gif"));
		ImageIcon vari = new ImageIcon(getClass().getResource("/images/V_STACK_RIGHT.gif"));
		ImageIcon hati = new ImageIcon(getClass().getResource("/images/H_STACK_TOP.gif"));
		ImageIcon haci = new ImageIcon(getClass().getResource("/images/H_STACK_CENTER.gif"));
		ImageIcon habi = new ImageIcon(getClass().getResource("/images/H_STACK_BOTTOM.gif"));

		VStackLeft val = new VStackLeft(vali,app,undoSupport);
		VStackCenter vac = new VStackCenter(vaci,app,undoSupport);
		VStackRight var = new VStackRight(vari,app,undoSupport);

		HStackTop hat = new HStackTop(hati,app,undoSupport);
		HStackCenter hac = new HStackCenter(haci,app,undoSupport);
		HStackBottom hab = new HStackBottom(habi,app,undoSupport);

		label = new JLabel("Stack:");
		makeSmall(label);
		
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    	add(label);
    	add(Box.createHorizontalGlue());
		add(valButton = Util.createButton(val, "Vertical Stack Left"));
		add(vacButton = Util.createButton(vac, "Vertical Stack Center"));
		add(varButton = Util.createButton(var, "Vertical Stack Right"));
		add(hatButton = Util.createButton(hat, "Horizontal Stack Top"));
		add(hacButton = Util.createButton(hac, "Horizontal Stack Center"));
		add(habButton = Util.createButton(hab, "Horizontal Stack Bottom"));
		
		if (isAquaLAF())
			setOpaque(false);
	}
	
	@Override
	public void setEnabled(final boolean enabled) {
		label.setEnabled(enabled);
		valButton.setEnabled(enabled);
		vacButton.setEnabled(enabled);
		varButton.setEnabled(enabled);
		hatButton.setEnabled(enabled);
		hacButton.setEnabled(enabled);
		habButton.setEnabled(enabled);
		
		super.setEnabled(enabled);
	}
}
