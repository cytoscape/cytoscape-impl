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
import org.cytoscape.view.manual.internal.control.actions.align.HAlignCenter;
import org.cytoscape.view.manual.internal.control.actions.align.HAlignLeft;
import org.cytoscape.view.manual.internal.control.actions.align.HAlignRight;
import org.cytoscape.view.manual.internal.control.actions.align.VAlignBottom;
import org.cytoscape.view.manual.internal.control.actions.align.VAlignCenter;
import org.cytoscape.view.manual.internal.control.actions.align.VAlignTop;
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
public class AlignPanel extends JPanel {
	
	private JLabel label;
	private JButton halButton;
	private JButton hacButton;
	private JButton harButton;
	private JButton vatButton;
	private JButton vacButton;
	private JButton vabButton;
	
	public AlignPanel(CyApplicationManager app, UndoSupport undoSupport) {
		ImageIcon hari = new ImageIcon(getClass().getResource("/images/H_ALIGN_RIGHT.gif"));
		ImageIcon haci = new ImageIcon(getClass().getResource("/images/H_ALIGN_CENTER.gif"));
		ImageIcon hali = new ImageIcon(getClass().getResource("/images/H_ALIGN_LEFT.gif"));
		ImageIcon vati = new ImageIcon(getClass().getResource("/images/V_ALIGN_TOP.gif"));
		ImageIcon vaci = new ImageIcon(getClass().getResource("/images/V_ALIGN_CENTER.gif"));
		ImageIcon vabi = new ImageIcon(getClass().getResource("/images/V_ALIGN_BOTTOM.gif"));

		HAlignRight har = new HAlignRight(hari,app,undoSupport);
		HAlignCenter hac = new HAlignCenter(haci,app,undoSupport);
		HAlignLeft hal = new HAlignLeft(hali,app,undoSupport);

		VAlignTop vat = new VAlignTop(vati,app,undoSupport);
		VAlignCenter vac = new VAlignCenter(vaci,app,undoSupport);
		VAlignBottom vab = new VAlignBottom(vabi,app,undoSupport);

		label = new JLabel("Align:");
		makeSmall(label);
		
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
	    add(label);
	    add(Box.createHorizontalGlue());
	    add(halButton = Util.createButton(hal, "Horizontal Align Left"));
		add(hacButton = Util.createButton(hac, "Horizontal Align Center"));
		add(harButton = Util.createButton(har, "Horizontal Align Right"));
		add(vatButton = Util.createButton(vat, "Vertical Align Top"));
		add(vacButton = Util.createButton(vac, "Vertical Align Center"));
		add(vabButton = Util.createButton(vab, "Vertical Align Bottom"));
		
		if (isAquaLAF())
			setOpaque(false);
	}
	
	@Override
	public void setEnabled(final boolean enabled) {
		label.setEnabled(enabled);
		halButton.setEnabled(enabled);
		hacButton.setEnabled(enabled);
		harButton.setEnabled(enabled);
		vatButton.setEnabled(enabled);
		vacButton.setEnabled(enabled);
		vabButton.setEnabled(enabled);
		
		super.setEnabled(enabled);
	}
}
