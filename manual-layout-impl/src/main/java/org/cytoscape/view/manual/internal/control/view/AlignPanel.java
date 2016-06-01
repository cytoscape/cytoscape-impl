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
import org.cytoscape.view.manual.internal.control.actions.align.HAlignCenter;
import org.cytoscape.view.manual.internal.control.actions.align.HAlignLeft;
import org.cytoscape.view.manual.internal.control.actions.align.HAlignRight;
import org.cytoscape.view.manual.internal.control.actions.align.VAlignBottom;
import org.cytoscape.view.manual.internal.control.actions.align.VAlignCenter;
import org.cytoscape.view.manual.internal.control.actions.align.VAlignTop;

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
public class AlignPanel extends JPanel {
	
	private JButton halButton;
	private JButton hacButton;
	private JButton harButton;
	private JButton vatButton;
	private JButton vacButton;
	private JButton vabButton;
	
	public AlignPanel(CyApplicationManager app) {
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
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

		int HGHT = 32;
		setMinimumSize(new Dimension(120, HGHT));
		setPreferredSize(new Dimension(300, HGHT));
		setMaximumSize(new Dimension(350, HGHT));

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		JLabel algn = new JLabel("Align");
		algn.setPreferredSize(new Dimension(105, 25));
		algn.setMinimumSize(new Dimension(105, 25));
		algn.setMaximumSize(new Dimension(105, 25));
		add(Box.createRigidArea(new Dimension(25, 0)));
	    add(algn);
	    add(halButton = createJButton(hal, "Horizontal Align Left"));
		add(hacButton = createJButton(hac, "Horizontal Align Center"));
		add(harButton = createJButton(har, "Horizontal Align Right"));

		add(vatButton = createJButton(vat, "Vertical Align Top"));
		add(vacButton = createJButton(vac, "Vertical Align Center"));
		add(vabButton = createJButton(vab, "Vertical Align Bottom"));
	}
	
	@Override
	public void setEnabled(final boolean enabled) {
		halButton.setEnabled(enabled);
		hacButton.setEnabled(enabled);
		harButton.setEnabled(enabled);
		vatButton.setEnabled(enabled);
		vacButton.setEnabled(enabled);
		vabButton.setEnabled(enabled);
		
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
