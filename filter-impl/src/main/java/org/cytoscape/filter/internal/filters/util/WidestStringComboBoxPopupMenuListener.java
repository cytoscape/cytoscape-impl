/*
 Copyright (c) 2009, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.filter.internal.filters.util;

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