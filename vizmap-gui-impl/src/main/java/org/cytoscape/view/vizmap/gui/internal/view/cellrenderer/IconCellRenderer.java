package org.cytoscape.view.vizmap.gui.internal.view.cellrenderer;

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

import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Method;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer for cells with icon.
 * 
 * Icon size is fixed, so caller of this class is responsible for passing proper
 * icons.
 */
public class IconCellRenderer<T> extends JPanel implements TableCellRenderer, ListCellRenderer {
	
	private static final long serialVersionUID = 8942821990143018260L;
	
	private static final float FONT_SIZE = 14.0f;
	
	

	final JLabel iconLbl;
	final JLabel textLbl;
	
	private Map<? extends T, Icon> icons;
	
	public IconCellRenderer(final Map<? extends T, Icon> icons) {
		this.icons = icons;
		iconLbl = new JLabel();
		iconLbl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		textLbl = new JLabel();
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(iconLbl);
		add(Box.createHorizontalStrut(20));
		add(textLbl);
		add(Box.createHorizontalGlue());
	}

	@Override
	public Component getListCellRendererComponent(final JList list,
												  final Object value,
												  final int index,
												  final boolean isSelected,
												  final boolean cellHasFocus) {
		update(value, isSelected, cellHasFocus);
		setBackground(isSelected ?
				UIManager.getColor("Table.selectionBackground") : UIManager.getColor("Table.background"));
		textLbl.setFont(UIManager.getFont("TextField.font").deriveFont(FONT_SIZE));
		
		final Color BORDER_COLOR = UIManager.getColor("Separator.foreground");
		final Border BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
				BorderFactory.createEmptyBorder(4, 4, 4, 4)
		);
		setBorder(BORDER);
		
		return this;
	}
	
	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus,
			final int row, final int column) {
		update(value, isSelected, hasFocus);
		setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
		iconLbl.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
		textLbl.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());

		return this;
	}
	
	@SuppressWarnings("unchecked")
	private void update(final Object value, final boolean isSelected, final boolean hasFocus) {
		setBackground(isSelected ?
				UIManager.getColor("Table.selectionBackground") : UIManager.getColor("Table.background"));
		final String label = getLabel((T)value);
		final Icon icon = icons.get(value);
		
		iconLbl.setIcon(icon);
		textLbl.setText(label);
		textLbl.setToolTipText(label);
	}
	
	private String getLabel(final T value) {
		String text = null;
		
		if (value != null) {
			// Use reflection to check existence of "getDisplayName" method
			final Class<? extends Object> valueClass = value.getClass();
			
			try {
				final Method displayMethod = valueClass.getMethod("getDisplayName", (Class<?>)null);
				final Object returnVal = displayMethod.invoke(value, (Class<?>)null);
				
				if (returnVal != null)
					text = returnVal.toString();
			} catch (Exception e) {
				// Use toString is failed.
				text = value.toString();
			}
		}
		
		return text;
	}
}
