package org.cytoscape.view.vizmap.gui.internal.legend;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;

public class LegendTable extends JPanel {

	private final CyApplicationManager appManager;
	private VisualProperty<Object> vp;
	private JTable legendTable;

	public LegendTable(final CyApplicationManager appManager, final Object[][] data, final VisualProperty<Object> vp) {
		super();
		this.appManager = appManager;
		this.vp = vp;
		
		legendTable = new JTable(data.length, 2);
		legendTable.setRowHeight(50);
		legendTable.setDefaultRenderer(Object.class, (TableCellRenderer) new LegendCellRenderer());
		this.vp = vp;

		setLayout(new BorderLayout());

		Object value = null;

		for (int i = 0; i < data.length; i++) {
			value = getIcon(data[i][0]);

			if (value != null) {
				legendTable.getModel().setValueAt(value, i, 0);
			}

			legendTable.getModel().setValueAt(data[i][1], i, 1);
		}

		add(legendTable, SwingConstants.CENTER);
	}

	private Object getIcon(final Object value) {
		if (value == null)
			return null;
		
		RenderingEngine<CyNetwork> engine = appManager.getCurrentRenderingEngine();
		Icon icon = engine.createIcon(vp, value, 32, 32);
		return icon;
	}


	public static JPanel getHeader(String attrName, VisualProperty<?> vp) {
		final JPanel titles = new JPanel();
		final JLabel[] labels = new JLabel[2];
		labels[0] = new JLabel(vp.getDisplayName());
		labels[1] = new JLabel(attrName);

		for (int i = 0; i < labels.length; i++) {
			labels[i].setVerticalAlignment(SwingConstants.CENTER);
			labels[i].setHorizontalAlignment(SwingConstants.LEADING);
			labels[i].setVerticalTextPosition(SwingConstants.CENTER);
			labels[i].setHorizontalTextPosition(SwingConstants.LEADING);
			labels[i].setForeground(Color.DARK_GRAY);
			labels[i].setBorder(new EmptyBorder(10, 0, 7, 10));
			labels[i].setFont(new Font("SansSerif", Font.BOLD, 14));
		}

		titles.setLayout(new GridLayout(1, 2));
		titles.setBackground(Color.white);

		titles.add(labels[0]);
		titles.add(labels[1]);
		titles.setBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY));

		return titles;
	}

	public class LegendCellRenderer implements TableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			final JLabel cell = new JLabel();

			if (value instanceof Icon) {
				Icon icon = (Icon) value;
				// icon.setBottomPadding(0);
				cell.setIcon(icon);

				cell.setVerticalAlignment(SwingConstants.CENTER);
				cell.setHorizontalAlignment(SwingConstants.CENTER);
			} else {
				cell.setText(value.toString());
				cell.setVerticalTextPosition(SwingConstants.CENTER);
				cell.setVerticalAlignment(SwingConstants.CENTER);
				cell.setHorizontalAlignment(SwingConstants.LEADING);
				cell.setHorizontalTextPosition(SwingConstants.LEADING);
			}

			cell.setPreferredSize(new Dimension(170, 1));

			return cell;
		}
	}
}
