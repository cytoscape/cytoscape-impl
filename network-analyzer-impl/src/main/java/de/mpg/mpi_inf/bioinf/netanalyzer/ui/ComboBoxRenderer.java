package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * A class that extends the ListCellRenderer class to visualize a separator
 * line at a certain position between the JComboBoxItems.
 */
class ComboBoxRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 1977983924324671280L;
	
	/**
	 * Default constructor for creation of a ComboBoxRenderer.
	 */
	public ComboBoxRenderer() {
		setOpaque(true);
		setBorder(new EmptyBorder(1, 1, 1, 1));
		separator = new JSeparator(SwingConstants.HORIZONTAL);
		fixedHeight = new JLabel(
				"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ(){}[]'\",;/?!@#$%^&*()_+")
				.getPreferredSize().height * 12 / 10;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {
		String str = (value == null) ? null : value.toString();
		if (Utils.SEPARATOR.equals(str)) {
			return separator;
		}
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setFont(list.getFont());
		setText(str);
		updateHeight();
		return this;
	}

	private void updateHeight() {
		setPreferredSize(null);
		final Dimension size = getPreferredSize();
		size.height = fixedHeight;
		setPreferredSize(size);
	}

	/**
	 * A separator line in a JComboBox. 
	 */
	private JSeparator separator;

	/**
	 * A fixed height of the separator line.
	 */
	private int fixedHeight;		
}