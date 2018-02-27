package org.cytoscape.internal.prefs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.cytoscape.internal.prefs.lib.AttrVal;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.internal.prefs.lib.NamespaceKeyValueTable;
import org.cytoscape.internal.prefs.lib.VBox;

public class PrefsLinks extends AbstractPrefsPanel {

	  private NamespaceKeyValueTable table;

	  protected PrefsLinks(Cy3PreferencesPanel dlog) {
		super(dlog, "linkout");
		// TODO Auto-generated constructor stub
	}
    @Override public void initUI()
    {
        super.initUI();
        VBox page = new VBox();
	    JLabel line0 = new JLabel("[Links:  This needs a Tree View of links by source.]");
	    line0.setFont(ital11);
	    JLabel line1 = new JLabel("[It might require a change in the format of the properties file.]");
	    line1.setFont(ital11);
	    page.add(new HBox(line0));
	    page.add(new HBox(line1));
	    page.add(makeTable());
		add(page);   
		TableColumn col = table.getColumnModel().getColumn(2);
		col.setPreferredWidth(260);
	}
 	
    private Component makeTable() {
		table = new NamespaceKeyValueTable();
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JPanel contentPane = new JPanel( new BorderLayout() );
		JScrollPane scroller = new JScrollPane( table  );		
		contentPane.add(scroller, BorderLayout.CENTER );
		scroller.setViewportView(table);
		scroller.setPreferredSize(AbstractPrefsPanel.getPanelSize());
		return contentPane;
	}
   
    @Override public void install(Properties properties)
    {
    		super.install(properties);
    			table.install(properties);
    }
	protected String getPropFileName()	{ return "linkout";	}
boolean verboseLinks = false;

    @Override public void extract(Properties properties)
    {
		// super.extract(cyPropMap);
		List<AttrVal> attrs = table.extract(properties);
		if (verboseLinks)			System.out.println("--  " + getName());
		if (verboseLinks)			System.out.println(attrs.size() + " linkouts");
		for (AttrVal attr : attrs) {
			properties.put(attr.getAttribute(), attr.getValue());
			if (verboseLinks)		System.out.println(attr.toString());
		}
    }

//    List<String> filterStringBy(Map<String, String> inMap, String prefix)
//    {
//    	List<String> data = new ArrayList<String>();
//    	for (String s : inMap.keySet())
//    		if (s.startsWith(prefix))
//    			data.add(s.substring(prefix.length()) + "=" + inMap.get(s));
//    	return data;
//    }
 
}
