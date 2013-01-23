package org.cytoscape.view.vizmap.gui.internal.cellrenderer;

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
