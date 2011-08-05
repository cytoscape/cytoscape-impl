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

import org.cytoscape.network.merge.internal.model.MatchingAttribute;

import cytoscape.Cytoscape;

import java.util.Vector;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Arrays;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;

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
        TreeSet<String> attrset = new TreeSet<String>();
        //TODO remove in Cytoscape3
        attrset.add("ID");
        //TODO: modify if local attribute implemented
        attrset.addAll(Arrays.asList(Cytoscape.getNodeAttributes().getAttributeNames()));

        String[] attrs = attrset.toArray(new String[0]);

        int n = getColumnCount();
        for (int i=0; i<n; i++) {
            TableColumn column = getColumnModel().getColumn(i);
            
            JComboBox comboBox = new JComboBox(attrs);
            column.setCellEditor(new DefaultCellEditor(comboBox));

            ComboBoxTableCellRenderer comboRenderer = new ComboBoxTableCellRenderer(attrs);
            column.setCellRenderer(comboRenderer);
        }
    }

    public void fireTableStructureChanged() {
        model.fireTableStructureChanged();
        setColumnEditorAndCellRenderer();
        //setCellRenderer();
    }

    private class MatchNodeTableModel extends AbstractTableModel {
        Vector<String> netNames;
        Vector<String> netIDs;

        public MatchNodeTableModel() {
            resetNetworks();
        }

        //@Override
        public int getColumnCount() {
            return matchingAttribute.getSizeNetwork();
        }

        //@Override
        public int getRowCount() {
            int n = matchingAttribute.getSizeNetwork();
            return n==0?0:1;
        }

        @Override
        public String getColumnName(int col) {
            return netNames.get(col);
        }

        //@Override
        public Object getValueAt(int row, int col) {
            return matchingAttribute.getAttributeForMatching(netIDs.get(col));
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
            if (value!=null);
            matchingAttribute.putAttributeForMatching(netIDs.get(col), (String)value);
            fireTableDataChanged();
        }

        @Override
        public void fireTableStructureChanged() {
            resetNetworks();
            super.fireTableStructureChanged();
        }
            
        private void resetNetworks() {
            netNames = new Vector<String>();
            netIDs = new Vector<String>();
            int size=0;
            Iterator<String> it = matchingAttribute.getNetworkSet().iterator();
            while (it.hasNext()) {
                String netID = it.next();
                String netName = Cytoscape.getNetwork(netID).getTitle();
                int index = 0;
                while (index<size && netNames.get(index).compareToIgnoreCase(netName)<0) index++;
                
                netIDs.add(index,netID);
                netNames.add(index,netName);
                size++;
            }
       }

    }

}


