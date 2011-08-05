
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.view.vizmap.gui.internal.cellrenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


public class FontCellRenderer extends JLabel implements ListCellRenderer {
	
	private final static long serialVersionUID = 120233986931967L;
	
	private static final Dimension SIZE = new Dimension(310, 40);
	private static final int DISPLAY_FONT_SIZE = 18;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		this.setPreferredSize(SIZE);
		this.setMinimumSize(SIZE);
		
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(Color.DARK_GRAY);
		} else {
			setBackground(list.getBackground());
			setForeground(Color.DARK_GRAY);
		}

		if ((value != null) && value instanceof Font) {
			final Font font = (Font) value;
			final Font modFont = new Font(font.getFontName(), font.getStyle(), DISPLAY_FONT_SIZE);
			this.setFont(modFont);
			this.setText(modFont.getName());
		} else
			this.setText("? (Unknown data type)");

		return this;
	}
}
