package org.cytoscape.view.table.internal.equation;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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
public abstract class ItemPanel extends JPanel {

	private final String title;

	private JPanel topPanel;
	private JScrollPane scrollPane;
	
	public abstract JComponent getContent();
	
	
	public ItemPanel(String title) {
		this.title = title;
		init();
	}
	
	private void init() {
		setOpaque(!isAquaLAF());
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getTopPanel())
				.addComponent(getScrollPane()));
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(getTopPanel())
				.addComponent(getScrollPane()));
	}
	
	private JPanel getTopPanel() {
		if(topPanel == null) {
			JLabel label = new JLabel(title);
			LookAndFeelUtil.makeSmall(label);
			topPanel = new JPanel();
			topPanel.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
			topPanel.add(label);
			topPanel.add(Box.createHorizontalGlue());
		}
		return topPanel;
	}
	
	private JScrollPane getScrollPane() {
		if(scrollPane == null) {
			scrollPane = new JScrollPane(getContent());
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return scrollPane;
	}
	
}
