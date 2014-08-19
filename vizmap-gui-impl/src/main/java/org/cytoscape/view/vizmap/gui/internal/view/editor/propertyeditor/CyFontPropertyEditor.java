package org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor;

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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


public class CyFontPropertyEditor extends CyComboBoxPropertyEditor {

	protected static final float FONT_SIZE = 14.0f;
	
	private final List<Font> fonts;
	
	public CyFontPropertyEditor() {
		fonts = scaleFonts(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
		
		final JComboBox comboBox = (JComboBox) editor;
		comboBox.setRenderer(new FontCellRenderer());
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateComboBox();
			}
		});
	}
	
	private void updateComboBox() {
		final JComboBox box = (JComboBox) editor;
		final Object selected = box.getSelectedItem();
		box.removeAllItems();
		
		for (final Font f : fonts)
			box.addItem(f);

		box.setSelectedItem(selected);
	}
	
	private List<Font> scaleFonts(final Font[] inFonts) {
		final List<Font> outFonts = new ArrayList<Font>(inFonts.length);
		
		for (final Font f : inFonts)
			outFonts.add(f.deriveFont(FONT_SIZE));

		return outFonts;
	}
	
	private static class FontCellRenderer extends JLabel implements ListCellRenderer {
		
		private final static long serialVersionUID = 120233986931967L;
		
		private static final Dimension SIZE = new Dimension(280, 32);

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			this.setPreferredSize(SIZE);
			this.setMinimumSize(SIZE);
			this.setOpaque(true);
			this.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			this.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

			if (value != null && value instanceof Font) {
				final Font font = (Font) value;
				this.setFont(font);
				this.setText(font.getName());
			} else {
				this.setText("");
			}

			return this;
		}
	}
}
