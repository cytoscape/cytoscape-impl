package org.cytoscape.cpath2.internal.view.model;

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