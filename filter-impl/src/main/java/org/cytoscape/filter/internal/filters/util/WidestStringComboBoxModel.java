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

import java.util.Vector;
import javax.swing.DefaultComboBoxModel;

/**
 * @author Noel Ruddock
 * This ComboBoxModel tracks the widest string
 * getLabel(Object anObject) should be overridden to derive the String from
 * an Object in the model when getString() doesn't provide what will be
 * displayed.
 */
public class WidestStringComboBoxModel extends DefaultComboBoxModel implements WidestStringProvider {
    private String widest = null;
    private boolean widestValid = false;

    /**
     * Class Constructor.
     */
    public WidestStringComboBoxModel() {
        super();
    }

    /**
     * Class Constructor specifying an array of Objects to form the model.
     *
     * @param items
     */
    public WidestStringComboBoxModel(Object[] items) {
        super(items);
    }

    /**
     * Class Constructor specifying a vector of Objects to form the model.
     *
     * @param v
     */
    public WidestStringComboBoxModel(Vector<?> v) {
        super(v);
    }

    /**
     * Adds the given object to the model.
     * Additionally updates cached longest String.
     *
     * @param anObject the object to be added to the model
     */
    @Override
    public void addElement(Object anObject) {
        updateWidest(anObject, true);
        super.addElement(anObject);
    }

    /**
     * Inserts an object into the model at the given index.
     * Additionally updates cached longest String.
     *
     * @param anObject the object to be added to the model
     * @param index the index at which the given object is to be added
     */
    @Override
    public void insertElementAt(Object anObject, int index) {
        updateWidest(anObject, true);
        super.insertElementAt(anObject, index);
    }

    /**
     * Removes all objects from the model.
     * Additionally updates cached longest String.
     */
    @Override
    public void removeAllElements() {
        resetWidest();
        super.removeAllElements();
    }

    /**
     * Removes the given object from the model.
     * Additionally updates cached longest String.
     *
     * @param anObject the object to be removed from the model
     */
    @Override
    public void removeElement(Object anObject) {
        updateWidest(anObject, false);
        super.removeElement(anObject);
    }

    /**
     * Removes the object at the specified index from the model.
     * Additionally updates cached longest String.
     */
    @Override
    public void removeElementAt(int index) {
        updateWidest(getElementAt(index), false);
        super.removeElementAt(index);
    }

    /**
     * Invalidates the cached longest String.
     */
    public void resetWidest() {
        widest = null;
        widestValid = false;
    }

    /**
     * Returns the longest display String for the objects in this model.
     * If the cached longest String is not valid, the longest String is
     * determined and cached.
     * @return the longest display String for the objects in this model
     */
    public String getWidest() {
        if (!widestValid) {
            findWidest();
        }

        return widest;
    }

    /**
     * Update the cached longest String if required when a new object is added
     * to the model, or invalidate it when removing.
     *
     * @param anObject the Object being added or removed
     * @param adding true if the anObject is being added to the model, false if
     * it is being removed
     */
    private void updateWidest(Object anObject, boolean adding) {
        String label;

        label = getLabel(anObject);
        if (widestValid) {
            if (adding) {
                if (label.length() > widest.length()) {
                    widest = label;
                }
            }
            else {
                if (widest.equals(label)) {
                    resetWidest();
                }
            }
        }
        else {
            if (adding) {
                widest = label;
                widestValid = true;
            }
        }
    }

    /**
     * Find which display string for the objects in the model is the longest.
     */
    private void findWidest() {
        int size;

        size = getSize();
        widest = "";
        for (int i = 0; i < size; i++) {
            String label;

            label = getLabel(getElementAt(i));
            if (label.length() > widest.length()) {
                widest = label;
            }
        }
        widestValid = true;
    }

    /**
     * Returns the String corresponding to the parameter object for use in
     * calculating the width of popup required.
     * This method should be overridden when the toString method of whatever
     * objects are stored in the model, doesn't return a suitable String.
     * Should return an empty string when anObject == null.
     *
     * @param anObject
     * @return the string that will be displayed for anObject. "" is returned
     * when the passed object is null
     */
    protected String getLabel(Object anObject) {
        return (anObject != null) ? anObject.toString() : "";
    }
}