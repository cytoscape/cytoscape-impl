/* File: MatchNodeTable.java

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

package org.cytoscape.network.merge.internal.ui;

import java.util.ArrayList;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.network.merge.internal.model.MatchingAttribute;

/**
 * Table for selecting which attribute to use for matching nodes 
 * 
 */
class MatchNodeTable extends JTable{ 
    private MatchingAttribute matchingAttribute;
    private MatchNodeTableModel model;

    public MatchNodeTable(MatchingAttribute matchingAttribute) {
        super();
        this.matchingAttribute = matchingAttribute;
        model = new MatchNodeTableModel();
        setModel(model);
        setRowHeight(20);
    }
    
    protected void setColumnEditorAndCellRenderer() {
        int n = getColumnCount();
        for (int i=0; i<n; i++) {
            TableColumn column = getColumnModel().getColumn(i);
            
            CyNetwork net = model.getNetork(i);
            CyTable table = net.getDefaultNodeTable();
            
            Vector<String> colNames = new Vector<String>();
            for (CyColumn cyCol : table.getColumns()) {
                String colName = cyCol.getName();
                if (!colName.equals("SUID")) {
                    colNames.add(colName);
                }
            }
            
            CyColumn cyCol = matchingAttribute.getAttributeForMatching(net);
            
            JComboBox comboBox = new JComboBox(colNames);
            ComboBoxTableCellRenderer comboRenderer = new ComboBoxTableCellRenderer(colNames);
            if (cyCol!=null) {
                String colName = cyCol.getName();
                comboBox.setSelectedItem(colName);
                comboRenderer.setSelectedItem(colName);
            }
            column.setCellEditor(new DefaultCellEditor(comboBox));
            column.setCellRenderer(comboRenderer);
        }
    }

    public void fireTableStructureChanged() {
        model.fireTableStructureChanged();
        setColumnEditorAndCellRenderer();
    }

    private class MatchNodeTableModel extends AbstractTableModel {
        ArrayList<CyNetwork> networks;

        public MatchNodeTableModel() {
            resetNetworks();
        }

        @Override
        public int getColumnCount() {
            return matchingAttribute.getSizeNetwork();
        }

        @Override
        public int getRowCount() {
            int n = matchingAttribute.getSizeNetwork();
            return n==0?0:1;
        }

        @Override
        public String getColumnName(int col) {
            return networks.get(col).toString(); //TODO: get network title
        }

        @Override
        public Object getValueAt(int row, int col) {
            return matchingAttribute.getAttributeForMatching(networks.get(col)).getName();
        }

        @Override
        public Class getColumnClass(int c) {
            //return JComboBox.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (value!=null) {
                CyNetwork net = networks.get(col);
                CyTable table = net.getDefaultNodeTable();
                CyColumn cyCol = table.getColumn((String)value);
                if (cyCol!=null) {
                    matchingAttribute.putAttributeForMatching(net, cyCol);
                    fireTableDataChanged();
                }
            }
        }

        @Override
        public void fireTableStructureChanged() {
            resetNetworks();
            super.fireTableStructureChanged();
        }
            
        private void resetNetworks() {
            networks = new ArrayList<CyNetwork>(matchingAttribute.getNetworkSet());
            //TODO: sort networks maybe alphabetically
        }
        
        public CyNetwork getNetork(int col) {
            return networks.get(col);
        }

    }

}


