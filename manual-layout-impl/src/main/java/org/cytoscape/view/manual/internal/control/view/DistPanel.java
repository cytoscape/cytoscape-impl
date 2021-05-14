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
import org.cytoscape.view.manual.internal.control.actions.dist.HDistCenter;
import org.cytoscape.view.manual.internal.control.actions.dist.HDistLeft;
import org.cytoscape.view.manual.internal.control.actions.dist.HDistRight;
import org.cytoscape.view.manual.internal.control.actions.dist.VDistBottom;
import org.cytoscape.view.manual.internal.control.actions.dist.VDistCenter;
import org.cytoscape.view.manual.internal.control.actions.dist.VDistTop;
import org.cytoscape.view.manual.internal.util.Util;

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
public class DistPanel extends JPanel {
	
	private JLabel label;
	private JButton halButton;
	private JButton hacButton;
	private JButton harButton;
	private JButton vatButton;
	private JButton vacButton;
	private JButton vabButton;
	
	public DistPanel(CyApplicationManager app) {
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

		label = new JLabel("Distribute:");
		makeSmall(label);
		
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(label);
        add(Box.createHorizontalGlue());
		add(halButton = Util.createButton(hal, "Horizontal Distribute Left"));
		add(hacButton = Util.createButton(hac, "Horizontal Distribute Center"));
		add(harButton = Util.createButton(har, "Horizontal Distribute Right"));
		add(vatButton = Util.createButton(vat, "Vertical Distribute Top"));
		add(vacButton = Util.createButton(vac, "Vertical Distribute Center"));
		add(vabButton = Util.createButton(vab, "Vertical Distribute Bottom"));
		
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
