package org.cytoscape.internal.prefs.lib;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.cytoscape.property.CyProperty;

public class NamespaceKeyValueTable extends JTable {
	private static final long serialVersionUID = 1L;
//https://docs.oracle.com/javase/tutorial/uiswing/components/table.html#sorting
	public NamespaceKeyValueTable()
	{
		super();
		NamespaceKeyValueTableModel model = new NamespaceKeyValueTableModel();
		setModel(model);
		sorter = new TableRowSorter<NamespaceKeyValueTableModel>(model);
		setRowSorter(sorter);	
	    TableCellRenderer renderer = new EvenOddRenderer();
	    setDefaultRenderer(Object.class, renderer);
	    cellEditorField = new JTextField();
	    cellEditorField.setFont(SMALL_FONT);
	    TableCellEditor cellEditor = new PrefCellEditor(cellEditorField, this);
        setDefaultEditor(Object.class, cellEditor);
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
//        getSelectionModel().addListSelectionListener(
//                new ListSelectionListener() {
//                    public void valueChanged(ListSelectionEvent event) {
//                        int viewRow = getSelectedRow();
//                        if (viewRow < 0) {
//                            //Selection got filtered away.
////                            statusText.setText("");
//                        } else {
//                            int modelRow =  convertRowIndexToModel(viewRow);
////                            statusText.setText(
////                                String.format("Selected Row in view: %d. " +
////                                    "Selected Row in model: %d.", 
////                                    viewRow, modelRow));
//                        }
//                    }
//                }
//        );

	}
	static Font SMALL_FONT = new Font("Serif", Font.PLAIN, 12);
	
	String[] columnNames = {"Namespace", "Attribute", "Value"};
	TableRowSorter<NamespaceKeyValueTableModel> sorter;
	JTextField cellEditorField;
	private List<AttrVal> attrValList = new ArrayList<AttrVal>();  // the model
	public List<AttrVal> getAllAttributes() { return attrValList;}
	private Map<String, AttrVal> dictionary = new HashMap<String, AttrVal>();
	
	public void install(Properties props)
	{
		String namespace="";
		TableModel model =  getModel();
		NamespaceKeyValueTableModel m ;
		
		if (model instanceof NamespaceKeyValueTableModel)
			 m = (NamespaceKeyValueTableModel) getModel();
		else return;
			for (Object key : props.keySet())
			{
				Object value = props.get(key);
				if (value instanceof String)
				{
					int idx = ((String) key).indexOf('.');
					if (idx > 0)
					{
						namespace = ((String) key).substring(0, idx);
						if ("layout".equals(namespace))
						{
							idx = ((String) key).indexOf('.', idx+1);
							namespace = ((String) key).substring(0, idx);
						}	
						key = ((String) key).substring(idx+1);
					}
					AttrVal row = new AttrVal(namespace, ((String) key), (String)value );
					m.addRow(row);
				}
			}
	}
	
	public void addRow(String namespace, String str, String val) {
		AttrVal row = new AttrVal(namespace, str, val );
		NamespaceKeyValueTableModel m = (NamespaceKeyValueTableModel) getModel();
		m.addRow(row);
		
	}   
	
	public List<AttrVal> extract(Properties data)
	{
		TableModel model =  getModel();
		NamespaceKeyValueTableModel m ;
		
		if (model instanceof NamespaceKeyValueTableModel)
			 m = (NamespaceKeyValueTableModel) getModel();
		else return null;		
		return m.getAttrValList();		
	}

	public int getNColumns() {		return 3;	}

	//=========================
	private AttrVal currentAttribute = null;

	class NamespaceKeyValueTableModel extends AbstractTableModel {
	
		private static final long serialVersionUID = 1L;
		public List<AttrVal> getAttrValList() 	{ 	return attrValList;	}
	    public int getColumnCount() 				{   return columnNames.length;    }		//3
	    public int getRowCount() 				{   return attrValList == null ? 0 : attrValList.size();    }
	    public String getColumnName(int col) 		{   return columnNames[col];    }
	    public void addRow(AttrVal newRow)		{ 	attrValList.add(newRow);  dictionary.put(newRow.getAttribute(), newRow);  }
	    public boolean isCellEditable(int row, int col) { 	return col > 1;        }

	    public Object getValueAt(int row, int col) 
	    {      
//			   System.out.print("getting: " + col + ", " + row + " to " );
//    		int idx = convertColumnIndexToView(col);
	    	if (getRowCount() == 0) return "";
		    	AttrVal av =  attrValList.get(row); 
		    String val = (col == 0) ? av.getNamespace() : ((col == 1) ? av.getAttribute() : av.getValue());
//		    	System.out.println(val);
			return val;
		 }
	
	 public void setValueFor(String attr, String val) {
		 AttrVal row =  dictionary.get(attr);  
		 row.setValue(val);
//			    fireTableCellUpdated(row, col);
	}
	
	   @Override  public void setValueAt(Object value, int row, int col) {
		   System.out.println("setting: " + col + ", " + row + " to " + value);
	    	AttrVal av =  attrValList.get(row);  
	    	if (col == 1) 	av.setAttribute(value.toString());
	    	else 			av.setValue(value.toString());
//		    fireTableCellUpdated(row, col);
}
	}


	  public static final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
	    public final static Color offwhite     = new Color(250, 250, 250);
	  //---------------------------------------------------------------
class EvenOddRenderer implements TableCellRenderer {


  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    
    ((JLabel) renderer).setOpaque(true);
    ((JLabel) renderer).setFont(SMALL_FONT);
     Color background;			//foreground, 
	 if (isSelected) 			background = offwhite.darker();
     else if (row % 2 == 0)    	background = Color.white;
     else        			 	background = offwhite;
    renderer.setBackground(background);

    return renderer;
  }
}
boolean isAttributeColumn(TableColumn column)
{
	return "Attribute".equals(column.getHeaderValue());
}
boolean isValueColumn(TableColumn column)
{
	return "Value".equals(column.getHeaderValue());
}
//---------------------------------------------------------------
		class PrefCellEditor extends DefaultCellEditor {
			private static final long serialVersionUID = 1L;
			JTable table;
			public PrefCellEditor(JTextField textField, JTable tab) {
				super(textField);
				table = tab;
				
				
				delegate = new EditorDelegate()
				{
					private static final long serialVersionUID = 1L;
					public void setValue(Object param) {	    	cellEditorField.setText("" + param);   }
				    public Object getCellEditorValue() {		return 	cellEditorField.getText();    }
				};
			
			}

			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
					int columnIdx) {
				// empty the cell on edit start
				delegate.setValue((editorComponent instanceof JTextField) ? "" + value : value);
				AttrVal av = attrValList.get(row);
				System.out.println("startEditing: " + av);
				TableColumn column = table.getColumnModel().getColumn(columnIdx);
				if (isAttributeColumn(column))
					cellEditorField.setText(av.getAttribute());
				if (isValueColumn(column))
					cellEditorField.setText(av.getValue());
				cellEditorField.setVisible(true);
				cellEditorField.selectAll();
				currentAttribute = av;
				return cellEditorField;
			}

			@Override
			public boolean stopCellEditing() {
				super.stopCellEditing();
				if (!cellEditorField.isVisible()) 		return true;
				if (getCellEditorValue() == null) 		return true;
				String val = getCellEditorValue().toString();
				// cellEditorField.setText("");
				int row = getSelectedRow();
				int col = getSelectedColumn();
				if (row < 0 || col < 0)					return false;
				System.out.println("stopEditing: " + val + " (" + row + ", " + col + ")");
				System.out.println("currentAttribute: " + currentAttribute.getAttribute() + " -> " + currentAttribute.getValue());
				
				currentAttribute.setValue(val);
				cellEditorField.setVisible(false);
				cellEditorField.setText("" + val);
				currentAttribute = null;
				return true;
		}
	}
}

