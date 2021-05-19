package org.cytoscape.view.table.internal.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.cytoscape.view.table.internal.util.ViewUtil;

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
	
	private boolean isRowSelected;
	
	private final Color defBgColor;
	private final Color selBgColor;
	
	private final JTable table;

	public BrowserTableRowHeaderRenderer(JTable table) {
		this.table = table;
		
		defBgColor = ViewUtil.getDefaultTableHeaderBg();
		selBgColor = ViewUtil.getSelectedTableHeaderBg();
		
		setText("");
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
		setOpaque(true);
		setForeground(UIManager.getColor("TableHeader.foreground"));
		setBorder(UIManager.getBorder("TableRowHeader.cellBorder"));
		setFont(UIManager.getFont("TableHeader.font"));
		
		setDoubleBuffered(true);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index,
			boolean isSelected, boolean hasFocus) {
		isRowSelected = table.isRowSelected(index);
		
		setBackground(isSelected ? selBgColor : defBgColor);
		setPreferredSize(new Dimension((int) getPreferredSize().getWidth(), table.getRowHeight(index)));
		
		return this;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if (isRowSelected) {
			var g2 = (Graphics2D) g.create();
			g2.setColor(UIManager.getColor("Table.focusCellBackground"));
			
			var w = getWidth();
			var h = getHeight();
			g2.fillRect(w - SELECTION_WIDTH, 0, SELECTION_WIDTH, h);
			
			g2.dispose();
		}
	}
}