package org.cytoscape.ding.impl.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.UIManager;

import org.cytoscape.ding.icon.VisualPropertyIconFactory;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.jdesktop.swingx.JXList;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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
public class DiscreteValueList<T> extends JXList {
	
	private int iconWidth = -1; // not initialized!
	private int iconHeight = -1; // not initialized!
	
	private final Class<T> type;
	private final VisualProperty<T> vp;
	private final Set<T> values;
	private final Map<T, Icon> iconMap;
	private final DefaultListModel<T> model;
	
	private final DefaultViewPanel defViewPanel;

	public DiscreteValueList(Class<T> type, DefaultViewPanel defViewPanel) {
		this(type, null, defViewPanel);
	}
	
	public DiscreteValueList(Class<T> type, int iconWidth, int iconHeight, DefaultViewPanel defViewPanel) {
		this(type, null, defViewPanel);
		
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
	}
	
	public DiscreteValueList(Class<T> type, VisualProperty<T> vp, DefaultViewPanel defViewPanel) {
		this.type = type;
		this.vp = vp;
		this.defViewPanel = defViewPanel;
		this.values = Collections.synchronizedSet(new LinkedHashSet<>());
		iconMap = new HashMap<>();
		
		setModel(model = new DefaultListModel<>());
		setCellRenderer(new IconCellRenderer());
		
		setAutoCreateRowSorter(true);
		setSortOrder(SortOrder.ASCENDING);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		var collator = Collator.getInstance(Locale.getDefault());
		
		setComparator(new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				if (o1 instanceof VisualPropertyValue)
					return collator.compare(((VisualPropertyValue)o1).getDisplayName(),
							((VisualPropertyValue)o2).getDisplayName());
				if (o1 instanceof Font)
					return collator.compare(((Font)o1).getFontName(), ((Font)o2).getFontName());
				return collator.compare(o1.toString(), o2.toString());
			}
		});
	}
	
	/**
	 * Use current renderer to create icons.
	 */
	private void renderIcons(Set<T> values) {
		if (type == Font.class)
			return;
		
		iconMap.clear();
		
		var engine = defViewPanel != null ? defViewPanel.getRenderingEngine() : null;
		
		// Current engine is not ready yet.
		if (engine != null) {
			synchronized (values) {
				for (T val : values) {
					Icon icon = null;
					
					if (val instanceof CyCustomGraphics)
						icon = VisualPropertyIconFactory.createIcon(val, vp, getIconWidth(), getIconHeight());
					else if (vp != null)
						icon = engine.createIcon(vp, val, getIconWidth(), getIconHeight());
					
					if (icon != null)
						iconMap.put(val, icon);
				}
			}
		}
	}
	
	public void setListItems(Collection<T> newValues, T selectedValue) {
		synchronized (values) {
			values.clear();
			
			if (newValues != null)
				values.addAll(newValues);
		}
		
		renderIcons(values);
		model.removeAllElements();
		
		synchronized (values) {
			for (T key : values)
				model.addElement(key);
		}

		if (selectedValue != null)
			setSelectedValue(selectedValue, true);
		
		repaint();
	}
	
	private int getIconWidth() {
		if (iconWidth == -1) {
			if (type == LineType.class || type == ArrowShape.class)
				iconWidth = 64;
			else
				iconWidth = 32;
		}
		
		return iconWidth;
	}
	
	private int getIconHeight() {
		if (iconHeight == -1) {
			iconHeight = 32;
		}
		
		return iconHeight;
	}
	
	private final class IconCellRenderer extends JPanel implements ListCellRenderer<T> {
		
		private final Color BG_COLOR = UIManager.getColor("Table.background");
		private final Color FG_COLOR = UIManager.getColor("Table.foreground");
		private final Color SELECTED_BG_COLOR = UIManager.getColor("Table.selectionBackground");
		private final Color SELECTED_FG_COLOR = UIManager.getColor("Table.selectionForeground");

		public IconCellRenderer() {
			setOpaque(true);
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Component getListCellRendererComponent(
				JList list,
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus
		) {
			removeAll();
			
			setBackground(isSelected ? SELECTED_BG_COLOR : BG_COLOR);
			setForeground(isSelected ? SELECTED_FG_COLOR : FG_COLOR);
			
			var border = BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground"));
			var paddingBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
			setBorder(BorderFactory.createCompoundBorder(border, paddingBorder));
			
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			var icon = iconMap.get(value);
			
			if (icon != null) {
				var iconLbl = new JLabel(iconMap.get(value));
				iconLbl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
				add(iconLbl);
				add(Box.createHorizontalStrut(20));
			}
			
			var textLbl = new JLabel(getLabel((T)value));
			
			if (value instanceof Font)
				textLbl.setFont(((Font) value).deriveFont(14.0f));

			add(textLbl);
			add(Box.createHorizontalGlue());
			
			return this;
		}
	}
	
	private String getLabel(T value) {
		String text = null;
		
		// Use reflection to check existence of "getDisplayName" method
		var valueClass = value.getClass();
		
		if (value instanceof Font) {
			text = ((Font) value).getFontName();
		} else {
			try {
				var displayMethod = valueClass.getMethod("getDisplayName", (Class<?>) null);
				var returnVal = displayMethod.invoke(value, (Class<?>)null);
				
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
