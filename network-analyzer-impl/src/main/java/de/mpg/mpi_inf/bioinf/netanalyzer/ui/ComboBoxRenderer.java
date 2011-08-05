/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.ListCellRenderer#getListCellRenderer
	 */
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

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 1977983924324671280L;

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