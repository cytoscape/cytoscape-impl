package org.cytoscape.cpath2.internal.view.model;

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

import javax.swing.table.DefaultTableModel;
import java.util.Vector;
import java.util.ArrayList;

/**
 * Pathway Table Model.
 *
 * @author Ethan Cerami
 */
public class PathwayTableModel extends DefaultTableModel {
    ArrayList internalIdList = new ArrayList();

    /**
     * Constructor.
     */
    public PathwayTableModel() {
        super();
        Vector columnNames = new Vector();
        columnNames.add("Pathway");
        columnNames.add("Data Source");
        //columnNames.add("Select");
        this.setColumnIdentifiers(columnNames);
    }

    /**
     * Is the specified cell editable?
     *
     * @param row row index.
     * @param col col index.
     * @return true or false.
     */
    public boolean isCellEditable(int row, int col) {
        return false;
//        if (col == 2) {
//            return true;
//        } else {
//            return false;
//        }
    }

    /**
     * Gets the column class.
     *
     * @param columnIndex column index.
     * @return Class.
     */
    public Class getColumnClass(int columnIndex) {
        return String.class;
//        if (columnIndex == 2) {
//            return Boolean.class;
//        } else {
//            return String.class;
//        }
    }

    public void resetInternalIds (int size) {
        internalIdList = new ArrayList(size);
    }

    public void setInternalId (int index, long internalId) {
        internalIdList.add(index, internalId);
    }

    public long getInternalId (int index) {
        return (Long) internalIdList.get(index);
    }
}