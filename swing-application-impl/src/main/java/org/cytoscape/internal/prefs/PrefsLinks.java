package org.cytoscape.internal.prefs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.cytoscape.internal.prefs.lib.NamespaceKeyValueTable;

public class PrefsLinks extends AbstractPrefsPanel {

	protected PrefsLinks(Cy3PreferencesRoot dlog) {
		super(dlog, "linkout");
		// TODO Auto-generated constructor stub
	}
    @Override public void initUI()
    {
        super.initUI();
		Box page = Box.createVerticalBox();
	    page.add(new JLabel("Links"));
	    page.add(makeTable());
		add(page);   
//		setBorder(new EmptyBorder(5, 0, 0, 30));
	}
    
    @Override public void install(Map<String, String> p)
    {
    		super.install(p);
    		Map<String, String> data = getPropertyMap("linkout.props");
    		table.install(data);
		TableColumn col = table.getColumnModel().getColumn(2);
		col.setPreferredWidth(260);
    }

//    List<String> filterStringBy(Map<String, String> inMap, String prefix)
//    {
//    	List<String> data = new ArrayList<String>();
//    	for (String s : inMap.keySet())
//    		if (s.startsWith(prefix))
//    			data.add(s.substring(prefix.length()) + "=" + inMap.get(s));
//    	return data;
//    }
    private NamespaceKeyValueTable table;
	
    private Component makeTable() {
		table = new NamespaceKeyValueTable();
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//		table.setPreferredScrollableViewportSize(dims);
		JPanel contentPane = new JPanel( new BorderLayout() );
		JScrollPane scroller = new JScrollPane( table  );		
		contentPane.add(scroller, BorderLayout.CENTER );
//		scroller.setMaximumSize(dims);
		scroller.setViewportView(table);
		scroller.setPreferredSize(dims);
//		scroller.setMaximumSize(new Dimension(2000, 1500));
		return contentPane;
	}

}
