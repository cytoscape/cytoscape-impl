package org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.cytoscape.view.vizmap.gui.internal.view.util.ViewUtil;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

public class CyFontPropertyEditor extends CyComboBoxPropertyEditor {

	protected static final float FONT_SIZE = 14.0f;
	
	private final List<Font> fonts;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CyFontPropertyEditor() {
		fonts = scaleFonts(ViewUtil.getAvailableFonts());
		
		var comboBox = (JComboBox) editor;
		comboBox.setRenderer(new FontCellRenderer());
		comboBox.addActionListener(evt -> updateComboBox());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateComboBox() {
		var box = (JComboBox) editor;
		var selected = box.getSelectedItem();
		box.removeAllItems();
		
		for (var f : fonts)
			box.addItem(f);

		box.setSelectedItem(selected);
	}
	
	private List<Font> scaleFonts(Collection<Font> inFonts) {
		var outFonts = new ArrayList<Font>(inFonts.size());
		
		for (var f : inFonts)
			outFonts.add(f.deriveFont(FONT_SIZE));

		return outFonts;
	}
	
	@SuppressWarnings({ "rawtypes", "serial" })
	private static class FontCellRenderer extends JLabel implements ListCellRenderer {
		
		private static final Dimension SIZE = new Dimension(280, 32);

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			setPreferredSize(SIZE);
			setMinimumSize(SIZE);
			setOpaque(true);
			setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

			if (value != null && value instanceof Font) {
				var font = (Font) value;
				setFont(font);
				setText(font.getName());
			} else {
				setText("");
			}

			return this;
		}
	}
}
