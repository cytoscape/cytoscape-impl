/*
  File: FontRenderer.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

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

//--------------------------------------------------------------------------
// $Revision: 12968 $
// $Date: 2008-02-06 15:34:25 -0800 (Wed, 06 Feb 2008) $
// $Author: mes $
//--------------------------------------------------------------------------
package org.cytoscape.view.vizmap.gui.internal.cellrenderer;


//--------------------------------------------------------------------------

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;

import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.FontChooser;


//--------------------------------------------------------------------------
/**
 * FontRenderer describes a class that renders each font name in a
 * {@link FontChooser}
 * JList or JComboBox in the face specified.
 */
class FontListCellRenderer extends DefaultListCellRenderer {
	private final static long serialVersionUID = 1202339876741828L;
    /**
     *  DOCUMENT ME!
     *
     * @param list DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param index DOCUMENT ME!
     * @param isSelected DOCUMENT ME!
     * @param cellHasFocus DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        setComponentOrientation(list.getComponentOrientation());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setEnabled(list.isEnabled());

        // just allow a ClassCastException to be thrown if the renderer is not
        // called correctly. Always display in 12 pt.
        if (value instanceof Font) {
            //Font fontValue = ((Font) value).deriveFont(12F);
            setFont((Font) value);
            setText(((Font) value).getFontName());
        } else {
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
        }

        setBorder((cellHasFocus)
            ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

        return this;
    }
}
