
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

package org.cytoscape.ding;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.cytoscape.ding.icon.VisualPropertyIconFactory;
import org.cytoscape.ding.impl.DVisualLexicon;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;


/**
 *
 */
public class ObjectPositionCellRenderer extends DefaultCellRenderer {
	
	private static final long serialVersionUID = -7898871787941450155L;
	
	private static final int ICON_WIDTH = 32;
	private static final int ICON_HEIGHT = 32;

	/**
	 *  DOCUMENT ME!
	 *
	 * @param table DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 * @param isSelected DOCUMENT ME!
	 * @param hasFocus DOCUMENT ME!
	 * @param row DOCUMENT ME!
	 * @param column DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
	                                               boolean hasFocus, int row, int column) {
		
		final JLabel label = new JLabel();
		
		if (isSelected) {
			label.setBackground(table.getSelectionBackground());
			label.setForeground(table.getSelectionForeground());
		} else {
			label.setBackground(table.getBackground());
			label.setForeground(table.getForeground());
		}

		if ((value != null) && value instanceof ObjectPosition) {
			
			
			final ObjectPosition lp = (ObjectPosition) value;
			
			label.setIcon(VisualPropertyIconFactory.createIcon(DVisualLexicon.NODE_LABEL_POSITION, lp, ICON_WIDTH, ICON_HEIGHT));
			label.setVerticalAlignment(SwingConstants.CENTER);
			label.setHorizontalAlignment(SwingConstants.CENTER);
		} 

		return label;
	}
}
