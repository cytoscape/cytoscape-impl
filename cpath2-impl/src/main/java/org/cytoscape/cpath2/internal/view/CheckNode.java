package org.cytoscape.cpath2.internal.view;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;


/**
 * Model for selectable nodes.
 *
 * Code was originally obtained from:
 * http://www.javaresearch.org/source/javaresearch/jrlib0.6/org/jr/swing/tree/
 *
 * and, has since been modified. 
 *
 */
public class CheckNode extends DefaultMutableTreeNode {
    public final static int SINGLE_SELECTION = 0;
    public final static int DIG_IN_SELECTION = 4;
    protected int selectionMode;
    protected boolean isSelected;

    /**
     * Constructor.
     * 
     * @param userObject User Object.
     */
    public CheckNode(Object userObject) {
        this(userObject, true, false);
    }

    /**
     * Constructor.
     *
     * @param userObject        User Object.
     * @param allowsChildren    Node allows children.
     * @param isSelected        Node is currently selected.
     */
    public CheckNode(Object userObject, boolean allowsChildren, boolean isSelected) {
        super(userObject, allowsChildren);
        this.isSelected = isSelected;
        setSelectionMode(DIG_IN_SELECTION);
    }

    /**
     * Sets the selection mode.
     *
     * @param mode Selection mode.
     */
    public void setSelectionMode(int mode) {
        selectionMode = mode;
    }

    /**
     * Gets the selection mode.
     * @return selection mode.
     */
    public int getSelectionMode() {
        return selectionMode;
    }

    /**
     * Sets selected / unselected.
     *
     * @param isSelected selected/uncselected.
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;

        //  DIG_IN Option
        if ((selectionMode == DIG_IN_SELECTION)
                && (children != null)) {
            Enumeration enum1 = children.elements();
            while (enum1.hasMoreElements()) {
                CheckNode node = (CheckNode) enum1.nextElement();
                node.setSelected(isSelected);
            }
        }
    }

    /**
     * Node is selected.
     * @return true or false.
     */
    public boolean isSelected() {
        return isSelected;
    }
}