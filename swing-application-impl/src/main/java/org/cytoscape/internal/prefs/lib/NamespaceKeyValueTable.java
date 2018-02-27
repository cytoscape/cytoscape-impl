package org.cytoscape.internal.prefs.lib;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class NamespaceKeyValueTable extends JTable {
	public NamespaceKeyValueTable()
	{
		super();
		NamespaceKeyValueTableModel model = new NamespaceKeyValueTableModel();
		setModel(model);
		sorter = new TableRowSorter<NamespaceKeyValueTableModel>(model);
		setRowSorter(sorter);	
	    TableCellRenderer renderer = new EvenOddRenderer();
	    setDefaultRenderer(Object.class, renderer);
	}
	String[] columnNames = {"Namespace", "Attribute", "Value"};
	TableRowSorter<NamespaceKeyValueTableModel> sorter;
	
	private List<AttrVal> attrValList = new ArrayList<AttrVal>();
	
	public void install(Map<String, String> settings)
	{
		String value, namespace="";
		TableModel model =  getModel();
		for (String key : settings.keySet())
		{
			value = settings.get(key);
			int idx = key.indexOf('.');
			if (idx > 0)
			{
				namespace = key.substring(0, idx);
				if ("layout".equals(namespace))
				{
					idx = key.indexOf('.', idx+1);
					namespace = key.substring(0, idx);
				}	
				key = key.substring(idx+1);
			}
			AttrVal row = new AttrVal(namespace, key, value );
			if (model instanceof NamespaceKeyValueTableModel)
			{		
				NamespaceKeyValueTableModel m = (NamespaceKeyValueTableModel) getModel();
				m.addRow(row);
			}
		}
	}
	public int getNColumns() {		return 3;	}

	//=========================
	

	class NamespaceKeyValueTableModel extends AbstractTableModel {
	
		public List<AttrVal> getAttrValList() 	{ 	return attrValList;	}
	    public int getColumnCount() 			{   return columnNames.length;    }
	    public int getRowCount() 				{   return attrValList == null ? 0 : attrValList.size();    }
	    public String getColumnName(int col) 	{   return columnNames[col];    }
	    public void addRow(AttrVal newRow)		{ 	attrValList.add(newRow); }
	    public Object getValueAt(int row, int col) 
	    {      
	    	if (getRowCount() == 0) return "";
	    	AttrVal av =  attrValList.get(row);  
	    	if (col == 0) return av.getNamespace();
	    	return (col == 1) ? av.getAttribute() : av.getValue();
	    }
	
	    /*
	     * Don't need to implement this method unless your table's editable.
	     */
	    public boolean isCellEditable(int row, int col) { 	return col > 0;        }
	 
	    /*
	     * Don't need to implement this method unless your table's data can change.
	     */
	    public void setValueAt(String value, int row, int col) {
	    	AttrVal av =  attrValList.get(row);  
	    	if (col == 0) av.setAttribute(value);
	    	else av.setValue(value);
	        fireTableCellUpdated(row, col);
	    }
	}

	  public static final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
	    public final static Color offwhite     = new Color(250, 250, 250);

class EvenOddRenderer implements TableCellRenderer {


  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    ((JLabel) renderer).setOpaque(true);
    Color foreground, background;
    if (isSelected) {
      foreground = Color.LIGHT_GRAY;
      background = Color.black;
    } else {
      if (row % 2 == 0) {
    	  background = Color.white;
      } else {
        background = offwhite;
      }
    }
    renderer.setBackground(background);
    return renderer;
  }
}

           
         
}
