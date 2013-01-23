package org.cytoscape.filter.internal.filters.util;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2009 - 2013 The Cytoscape Consortium
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

import java.awt.Dimension;
import java.awt.FontMetrics;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * @author Noel Ruddock
 *
 * Listener that can be attached to a JComboBox.
 * When the JComboBox to which it is attached uses a ComboBoxModel that
 * implements WidestStringProvider, the popup list will be resized so that the
 * longest String will be completely visible.
 */
public class WidestStringComboBoxPopupMenuListener implements PopupMenuListener {
    /**
     * Resize the popup list based on the longest display string for objects in
     * the model of the JComboBox being listened to.
     * The model must implement WidestStringProvider for the popup to be sized.
     *
     * @param e
     */
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        ComboBoxModel cbm;
        WidestStringProvider wsp;
        int w, h;
        Dimension d;

        JComboBox box = (JComboBox)e.getSource();
        cbm = box.getModel();

        if (!(cbm instanceof WidestStringProvider)) {
            // Silently ignore if not listening to a JComboBox with a suitable model object
            return;
        }
        wsp = (WidestStringProvider)cbm;

        Object comp = box.getUI().getAccessibleChild(box, 0);

        if (!(comp instanceof JPopupMenu)) {
            return;
        }

        Object scrollObject = ((JComponent)comp).getComponent(0);

        if (!(scrollObject instanceof JScrollPane)) {
            return;
        }

        JScrollPane scrollPane = (JScrollPane)scrollObject;

        FontMetrics fm = box.getFontMetrics(scrollPane.getFont());
        w = (int)fm.stringWidth(wsp.getWidest());
        h = (int)scrollPane.getMinimumSize().getHeight();
        d = new Dimension(Math.max((int)((double)w + scrollPane.getVerticalScrollBar().getMinimumSize().getWidth()), box.getWidth()), h);
        scrollPane.setPreferredSize(d);
        scrollPane.setMaximumSize(d);
    }

    /**
     * Not interested in this event.
     *
     * @param e
     */
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        return;
    }

    /**
     * Not interested in this event.
     *
     * @param e
     */
    public void popupMenuCanceled(PopupMenuEvent e) {
        return;
    }
}