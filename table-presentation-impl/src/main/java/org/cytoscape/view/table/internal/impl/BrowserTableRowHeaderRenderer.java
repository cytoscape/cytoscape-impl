package org.cytoscape.view.table.internal.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

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
public class BrowserTableRowHeaderRenderer extends JLabel implements ListCellRenderer<Integer> {

	private static final int SELECTION_WIDTH = 2;
	
	private boolean isTableRowSelected;
	
	private final JTable table;

	public BrowserTableRowHeaderRenderer(JTable table) {
		this.table = table;
		
		setText("");
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
		setOpaque(true);
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		setForeground(UIManager.getColor("TableHeader.foreground"));
		setFont(UIManager.getFont("TableHeader.font"));
		
		setDoubleBuffered(true);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index,
			boolean isSelected, boolean hasFocus) {
		isTableRowSelected = table.isRowSelected(index);
		
		setPreferredSize(new Dimension((int) getPreferredSize().getWidth(), table.getRowHeight(index)));
		setBackground(UIManager.getColor(isSelected ? "Table.selectionBackground" : "TableHeader.background"));
		
		return this;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (isTableRowSelected) {
			var g2 = (Graphics2D) g.create();
			g2.setColor(UIManager.getColor("Table.focusCellBackground"));
			
			var w = getWidth();
			var h = getHeight();
			g2.fillRect(w - SELECTION_WIDTH - 1/*usual "TableHeader.cellBorder" border width*/, 0, SELECTION_WIDTH, h);
			
			g2.dispose();
		}
	}
}