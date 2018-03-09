package org.cytoscape.internal.prefs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.cytoscape.internal.prefs.lib.AttrVal;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.internal.prefs.lib.NamespaceKeyValueTable;
import org.cytoscape.internal.prefs.lib.VBox;

public class PrefsAdvanced extends AbstractPrefsPanel {

	/** This is the tabular view of all preferences.
	 *  It is turned on with the tab button in the top right
	 */
	private static final long serialVersionUID = 1L;

	protected PrefsAdvanced(Cy3PreferencesPanel dlog) {
		super(dlog, "Advanced", "", "", "", 10000000);
//		setBorder(BorderFactory.createLineBorder(Color.cyan));
	}
	
    private NamespaceKeyValueTable table;
	
	private void addNewRow() {
		System.out.println("addNewRow");				
	}
	@Override public void initUI()
    {
        super.initUI();
        VBox page = new VBox(true, true);
 	    JLabel line0 = new JLabel("This view allows textual editing of all of the propeties.");
	    JLabel label = new JLabel("(No type checking is implemented here.)");
	    line0.setFont(ital11);
	    label.setFont(ital11);
	    JButton adder = new JButton("+");
	    adder.addActionListener( e ->{	addNewRow();		});  adder.setEnabled(false);// TODO
	   page.add(new HBox(line0));
	   page.add(new HBox(true, true, label, Box.createHorizontalGlue(), adder));
	   page.add(makeTableInScroller());
		add(page);   
	}
    
  private JPanel makeTableInScroller() {
		JPanel contentPane = new JPanel( new BorderLayout() );
		table = new NamespaceKeyValueTable();
		Dimension tableSize = AbstractPrefsPanel.getPanelSize();
		tableSize.height -= 50;
		setSizes(table, tableSize);
		TableColumn col = table.getColumnModel().getColumn(table.getNColumns()-1);
		col.setPreferredWidth(260);
		col = table.getColumnModel().getColumn(0);
		col.setPreferredWidth(90);
		table.setPreferredScrollableViewportSize(tableSize);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane scroller = new JScrollPane(table);
		scroller.setPreferredSize(AbstractPrefsPanel.getPanelSize());
		contentPane.add(scroller, BorderLayout.CENTER );
//		scroller.setViewportView(table);
//		scroller.setPreferredSize(dims);
		return contentPane;
	}
//	table.setBorder(BorderFactory.createLineBorder(Color.blue));
//	scroller.setBorder(BorderFactory.createLineBorder(Color.red));

  //-------------------------------------------------------------------------
   public void install(Map<String, Properties> cyPropMap)
   {
	    	for (String namespace : cyPropMap.keySet())			
	    	{
	    		if (namespace.startsWith("layout.")) continue;
	    		Properties properties =  cyPropMap.get(namespace);
	    		if (properties != null)
			{
				for (Object key : properties.keySet()) {
					if (key instanceof String) {
						String str = key.toString();
						Object val = properties.get(key);
						String valStr = val == null ? "" : val.toString();
						table.addRow(namespace, str, valStr);
					}
				}
			}
		}
	}

   public void extract(Map<String, Properties> cyPropMap)
    {
		List<AttrVal> tableRows = table.getAllAttributes();
		for (AttrVal row : tableRows)
		{
			String namespace = row.getNamespace();
			Properties properties = cyPropMap.get(namespace);
			if (properties == null)
				continue;
			properties.put(row.getAttribute(), row.getValue());
		}
    	}

}
