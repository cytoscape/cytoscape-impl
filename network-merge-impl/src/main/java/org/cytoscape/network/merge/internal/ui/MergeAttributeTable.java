/* File: MergeAttributeTable.java

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

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.network.merge.internal.model.AttributeMapping;
import org.cytoscape.network.merge.internal.model.MatchingAttribute;
import org.cytoscape.network.merge.internal.util.ColumnType;

import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;

import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.Component;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JOptionPane;
import javax.swing.table.JTableHeader;
import javax.swing.JComboBox;
import javax.swing.table.TableColumn;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Table for customizing attribute mapping from original netowrks
 * to resulting network
 * 
 * 
 */
class MergeAttributeTable extends JTable{
    private final String nullAttr = "[DELETE THIS]";
    private MatchingAttribute matchingAttribute;
    private AttributeMapping attributeMapping; //attribute mapping
    private String mergedNetworkName;
    private MergeAttributeTableModel model;
    private boolean isNode;
    private int indexMatchingAttr; // the index of matching attribute in the attribute mapping
                                   // only used when isNode==true
    
    public MergeAttributeTable(final AttributeMapping attributeMapping, final MatchingAttribute matchingAttribute) {
        super();
        isNode = true;
        indexMatchingAttr = -1;
        this.mergedNetworkName = "Merged.Network";
        this.attributeMapping = attributeMapping;
        this.matchingAttribute = matchingAttribute;
        model = new MergeAttributeTableModel();
        setModel(model);
        setRowHeight(20);
    }
    
    public MergeAttributeTable(final AttributeMapping attributeMapping) {
        super();        
        this.mergedNetworkName = "Merged.Network";
        this.attributeMapping = attributeMapping;
        model = new MergeAttributeTableModel();
        isNode = false;
        setModel(model);
        setRowHeight(20);
    }

    public String getMergedNetworkName() {
        return mergedNetworkName;
    }
    
    private Vector<String> getComboboxOption(int col) {
        CyNetwork net = model.getNetork(col);
        CyTable table = attributeMapping.getCyTable(net);
        Vector<String> colNames = new Vector<String>();
            for (CyColumn cyCol : table.getColumns()) {
                String colName = cyCol.getName();
                if (!colName.equals("SUID") && !colName.equals("selected")) {
                    colNames.add(colName);
                }
            }
        colNames.add(nullAttr);
        return colNames;
    }

    protected void setColumnEditorAndRenderer() {
//        final TreeSet<String> attrset = new TreeSet<String>();
//        attrset.addAll(Arrays.asList(attributeMapping.getCyAttributes().getAttributeNames()));

//        final Vector<String> attrs = new Vector<String>(attrset);
//        attrs.add(nullAttr);

        //final int nnet = attributeMapping.getSizeNetwork();

        final int n = this.getColumnCount();//attributeMapping.getSizeNetwork();
        for (int i=0; i<n; i++) { // for each network
            final TableColumn column = getColumnModel().getColumn(i);

            if (this.isColumnOriginalNetwork(i)) {
                final Vector<String> attrs = getComboboxOption(i);
                final JComboBox comboBox = new JComboBox(attrs);
                column.setCellEditor(new DefaultCellEditor(comboBox));
                column.setCellRenderer(new TableCellRenderer() {
                    private DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
                    private ComboBoxTableCellRenderer comboBoxRenderer = new ComboBoxTableCellRenderer(attrs);

                    //@Override
                    public Component getTableCellRendererComponent(
                                    JTable table, Object value,
                                    boolean isSelected, boolean hasFocus,
                                    int row, int column) {

                                if (row<(isNode?1:0)) {//TODO Cytoscape3
                                        JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                        label.setBackground(Color.LIGHT_GRAY);
                                        label.setToolTipText("Change this in the matching node table above");
                                        return label;
                                } else {
                                        Component renderer = comboBoxRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                        if (isSelected) {
                                            renderer.setForeground(table.getSelectionForeground());
                                            renderer.setBackground(table.getSelectionBackground());
                                        } else {
                                            renderer.setForeground(table.getForeground());
                                            renderer.setBackground(table.getBackground());
                                        }
                                        return renderer;
                                }
                          }
                });

            } else if (this.isColumnMergedNetwork(i)) {
                column.setCellRenderer(new TableCellRenderer() {
                    private DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
                    
                    //@Override
                    public Component getTableCellRendererComponent(
                                    JTable table, Object value,
                                    boolean isSelected, boolean hasFocus,
                                    int row, int column) {

                                if (row>=table.getRowCount()-1) {
                                        JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                        label.setBackground(Color.LIGHT_GRAY);
                                        if (isSelected) {
                                                label.setForeground(table.getSelectionForeground());
                                        } else {
                                                label.setForeground(table.getForeground());
                                        }
                                        return label;
                                } else {
                                        if (isNode && row==0) {
                                                JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                                label.setForeground(Color.RED);
                                                if (isSelected) {
                                                    label.setBackground(table.getSelectionBackground());
                                                } else {
                                                    label.setBackground(table.getBackground());
                                                }
                                                label.setToolTipText("CHANGE ME!");
                                                return label;
                                        } else {
                                                JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                                label.setToolTipText("Double click to change");
                                                if (isSelected) {
                                                    label.setForeground(table.getSelectionForeground());
                                                    label.setBackground(table.getSelectionBackground());
                                                } else {
                                                    label.setForeground(table.getForeground());
                                                    label.setBackground(table.getBackground());
                                                }
                                                return label;
                                        }
                                }
                    }
                });
            } else if (this.isColumnMergedType(i)) {
                //set editor
                RowTableCellEditor rowEditor = new RowTableCellEditor(this);
                int nr = this.getRowCount();
                //List<JComboBox> combos = new Vector<JComboBox>(nr); // save for render
                final Vector<ColumnType>[] cbvalues = new Vector[nr];
                for (int ir=0; ir<nr-1; ir++) {
                        int iAttr = ir;

                        Map<CyNetwork, String> mapNetAttr = attributeMapping.getOriginalAttributeMap(iAttr);
                        Set<ColumnType> types = EnumSet.noneOf(ColumnType.class);
                        for (Map.Entry<CyNetwork,String> entry : mapNetAttr.entrySet()) {
                            CyTable cyTable = attributeMapping.getCyTable(entry.getKey());
                            types.add(ColumnType.getType(cyTable.getColumn(entry.getValue())));
                        }
                        ColumnType reasonalbeType = ColumnType.getResonableCompatibleConvertionType(types);
                        Vector<ColumnType> convertiableTypes = new Vector<ColumnType>(ColumnType.getConvertibleTypes(reasonalbeType));
                        
                        cbvalues[ir] = convertiableTypes;

                        JComboBox cb = new JComboBox(convertiableTypes);
                        cb.setSelectedItem(attributeMapping.getMergedAttributeType(iAttr));

                        rowEditor.setEditorAt(ir, new DefaultCellEditor(cb));
                        //combos.add(cb);
                }
                column.setCellEditor(rowEditor);

                // set renderer
                column.setCellRenderer(new TableCellRenderer() {
                    private DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
                    //private ComboBoxTableCellRenderer comboBoxRenderer = new ComboBoxTableCellRenderer(attrs);

                    //@Override
                    public Component getTableCellRendererComponent(
                                    JTable table, Object value,
                                    boolean isSelected, boolean hasFocus,
                                    int row, int column) {

                                if (row>=table.getRowCount()-1) {
                                        JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                        label.setBackground(Color.LIGHT_GRAY);
                                        if (isSelected) {
                                                label.setForeground(table.getSelectionForeground());
                                        } else {
                                                label.setForeground(table.getForeground());
                                        }
                                        return label;
                                } else {
                                        if (!table.isCellEditable(row, column)) {
                                                JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                                label.setBackground(Color.LIGHT_GRAY);
                                                label.setToolTipText("Only types of new attribute are changeable");
                                                if (isSelected) {
                                                        label.setForeground(table.getSelectionForeground());
                                                } else {
                                                        label.setForeground(table.getForeground());
                                                }
                                                return label;
                                        } else {
                                                ComboBoxTableCellRenderer comboBoxRenderer = new ComboBoxTableCellRenderer(cbvalues[row]);
                                                Component renderer = comboBoxRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                                if (isSelected) {
                                                    renderer.setForeground(table.getSelectionForeground());
                                                    renderer.setBackground(table.getSelectionBackground());
                                                } else {
                                                    renderer.setForeground(table.getForeground());
                                                    renderer.setBackground(table.getBackground());
                                                }
                                                return renderer;
                                        }
                                }
                    }

                });

            }
        }
        
    }
    
    protected void setMergedNetworkNameTableHeaderListener() {
        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTableHeader header = (JTableHeader) e.getSource();
                JTable table = header.getTable();
                int columnIndex = header.columnAtPoint(e.getPoint());
                if (columnIndex==attributeMapping.getSizeNetwork()) { // the merged network
                    String title = JOptionPane.showInputDialog(table.getParent(), "Input the title for the merged network");
                    if (title!=null && title.length()!=0) {
                        mergedNetworkName = title;
                        fireTableHeaderChanged();           
                    } else {
                        fireTableHeaderChanged(); //TODO: this is just for remove the duplicate click event
                                                  // there should be better ways                        
                    }
                }
            }            
        });
    }
    
    public void fireTableStructureChanged() {
        //pack();
        model.fireTableStructureChanged();
        setColumnEditorAndRenderer();
        setMergedNetworkNameTableHeaderListener();
    }
    
    public void updateMatchingAttribute() { 
        if (!isNode) {
            throw new java.lang.UnsupportedOperationException("updateMatchingAttribute is only supported for node table");
        }
        
        boolean update = false;
        if (indexMatchingAttr==-1) {
            indexMatchingAttr = 0;
            String attr_merged = "Matching.Attribute";
            
            Map<CyNetwork, String> netAttrMap = new HashMap<CyNetwork, String>();
            Set<ColumnType> types = EnumSet.noneOf(ColumnType.class);
            for (Map.Entry<CyNetwork, CyColumn> entry : matchingAttribute.getNetAttrMap().entrySet()) {
                netAttrMap.put(entry.getKey(), entry.getValue().getName());
                types.add(ColumnType.getType(entry.getValue()).toPlain());
            }
            
            attributeMapping.addAttributes(netAttrMap, attr_merged, indexMatchingAttr);
            attributeMapping.setMergedAttributeType(indexMatchingAttr, ColumnType.getResonableCompatibleConvertionType(types).toList());
            update = true;
        } else {
            Set<CyNetwork> networks = matchingAttribute.getNetworkSet();
            Iterator<CyNetwork> it = networks.iterator();
            if (!it.hasNext()) { // empty
                indexMatchingAttr = -1;
            }

            while (it.hasNext()) {
                CyNetwork network = it.next();
                String attr = matchingAttribute.getAttributeForMatching(network).getName();
                String old = attributeMapping.setOriginalAttribute(network, attr, indexMatchingAttr);
                if (attr.compareTo(old)!=0) {
                    update=true;
                }
            }
        }
        
        if (update) {
            fireTableStructureChanged();
        }
    }
    
    protected void fireTableHeaderChanged() {
        model.fireTableStructureChanged();
        //setCellRender();
        setColumnEditorAndRenderer();
    }

    protected boolean isColumnOriginalNetwork(final int col) {
            return col>=0 && col<attributeMapping.getSizeNetwork();
    }

    protected boolean isColumnMergedNetwork(final int col) {
            return col==attributeMapping.getSizeNetwork();
    }

    protected boolean isColumnMergedType(final int col) {
            return col==attributeMapping.getSizeNetwork()+1;
    }

 
    // table model
    protected class MergeAttributeTableModel extends AbstractTableModel {
        ArrayList<CyNetwork> networks;

        public MergeAttributeTableModel() {
            resetNetworks();
        }

        //@Override
        public int getColumnCount() {
            final int n = attributeMapping.getSizeNetwork();
            return n==0?0:n+2;
        }

        //@Override
        public int getRowCount() {
            int n = attributeMapping.getSizeMergedAttributes()+1; // +1: add an empty row in the end
            return attributeMapping.getSizeNetwork()==0?0:n; 
        }

        @Override
        public String getColumnName(final int col) {
            if (isColumnMergedType(col)) {
                return "Attribute type";
            }

            if (isColumnMergedNetwork(col)) {
                return mergedNetworkName;
            }

            if (isColumnOriginalNetwork(col)){
                return networks.get(col).toString();
            }

            return null;
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            final int iAttr = row; //TODO used in Cytoscape3

            if (row==getRowCount()-1) {
                return null;
            }

            if (isColumnOriginalNetwork(col)) {
                return attributeMapping.getOriginalAttribute(networks.get(col), iAttr);
            }

            if (isColumnMergedNetwork(col)) {
                return attributeMapping.getMergedAttribute(iAttr);
            }

            if (isColumnMergedType(col)) {
                return attributeMapping.getMergedAttributeType(iAttr);
            }

            return null;
        }

        @Override
        public Class getColumnClass(final int c) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(final int row, final int col) {
            if (isNode && row==0) // make the matching attribute ineditable
                return !isColumnOriginalNetwork(col);

            if (isColumnOriginalNetwork(col))
                    return true;

            if (isColumnMergedNetwork(col)) 
                    return row!=getRowCount()-1;
            

            if (isColumnMergedType(col)) 
                return true;
//                    String mergedAttribute = getValueAt(row,col-1);
//                    CyAttributes attrs = attributeMapping.getCyAttributes();
//                    return !Arrays.asList(attrs.getAttributeNames()).contains(mergedAttribute);// non-editable for existing attribute
            

            return false;
        }


        @Override
        public void setValueAt(final Object value, final int row, final int col) {
            if (value==null) return;
            
            final int iAttr = row; //TODO use in Cytoscape3.0
            
            final int n = attributeMapping.getSizeMergedAttributes();
            if (iAttr>n) return; // should not happen

            if (isColumnMergedType(col)) {
                if (iAttr==n) return;
                
                ColumnType type = (ColumnType)value;
                ColumnType type_curr = attributeMapping.getMergedAttributeType(iAttr);
                if (type==type_curr) return;

                attributeMapping.setMergedAttributeType(iAttr, type);

            } else if (isColumnMergedNetwork(col)) { //column of merged network
                if (iAttr==n) return;
                
                final String v = (String) value;
            
                String attr_curr = attributeMapping.getMergedAttribute(iAttr);
                if (attr_curr.compareTo(v)==0) { //if the same
                    return;
                }
                
                if (v.length()==0) {
                    JOptionPane.showMessageDialog(getParent(),"Please use a non-empty name for the attribute!", "Error: empty attribute Name", JOptionPane.ERROR_MESSAGE );
                    return;
                } 
                
                if (attributeMapping.containsMergedAttribute(v)) {
                    JOptionPane.showMessageDialog(getParent(),"Atribute "+v+" is already exist! Please use another name for this attribute!", "Error: duplicated attribute Name", JOptionPane.ERROR_MESSAGE );
                    return;
                }
                                
                attributeMapping.setMergedAttribute(iAttr, v);
                fireTableDataChanged();
                return;
            } else { //column of original network
                CyNetwork net = networks.get(col);
                final String v = (String) value;
                if (iAttr==n) { // the last row
                    if (v.compareTo(nullAttr)==0) return;
                    
                    Map<CyNetwork,String> map = new HashMap<CyNetwork,String>();
                    map.put(net, v);

                    attributeMapping.addAttributes(map, v);
                    fireTableDataChanged();
                    return;

                } else {
                    String curr_attr = attributeMapping.getOriginalAttribute(net, iAttr);                    
                    if (curr_attr!=null && curr_attr.compareTo(v)==0) {
                        return;
                    }
                    
                    if (v.compareTo(nullAttr)==0) {
                        if (curr_attr==null) return;
                        //if (attributeMapping.getOriginalAttribute(netID, iAttr)==null) return;
                        attributeMapping.removeOriginalAttribute(net, iAttr);
                    } else {
                        String mergedAttr = attributeMapping.getMergedAttribute(iAttr);
                                                
                        attributeMapping.setOriginalAttribute(net, v, iAttr);// set the v
                    }
                    fireTableDataChanged();
                    return;
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


