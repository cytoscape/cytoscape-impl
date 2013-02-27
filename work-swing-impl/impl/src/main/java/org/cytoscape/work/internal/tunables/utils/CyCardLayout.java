package org.cytoscape.work.internal.tunables.utils;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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


import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Container;



@SuppressWarnings("serial")
public class CyCardLayout extends CardLayout {

    @Override
    public Dimension preferredLayoutSize(Container parent) {

        Component current = findCurrentComponent(parent);
        if (current != null) {
            Insets insets = parent.getInsets();
            Dimension pref = current.getPreferredSize();
            pref.width += insets.left + insets.right;
            pref.height += insets.top + insets.bottom;
            return pref;
        }
        return super.preferredLayoutSize(parent);
    }

    public Component findCurrentComponent(Container parent) {
        for (Component comp : parent.getComponents()) {
            if (comp.isVisible()) {
                return comp;
            }
        }
        return null;
    }

}
