package org.cytoscape.internal.prefs;

import java.awt.Component;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;

import org.cytoscape.internal.prefs.lib.NamespaceKeyValueTable;
import org.cytoscape.internal.prefs.lib.VBox;

public class PrefsAdvanced extends AbstractPrefsPanel {

	protected PrefsAdvanced(Cy3PreferencesRoot dlog) {
		super(dlog, "Advanced");
	}
	
    @Override public void initUI()
    {
        super.initUI();
        VBox page = new VBox(true, true);
		add(page);   
 	    page.add(new JLabel("Namespace Attribute Value View"));
	    page.add(new JLabel(""));
	    page.add(makeTable());
		add(page);   
	}
    
    private NamespaceKeyValueTable table;
	
    private Component makeTable() {
        Prefs data = root.getPrefs();
//		TableModel model = makePrefsTableModel(data);
		table = new NamespaceKeyValueTable();
		TableColumn col = table.getColumnModel().getColumn(table.getNColumns()-1);
		col.setPreferredWidth(260);
		table.setPreferredScrollableViewportSize(dims);
		
		table.setFillsViewportHeight(true);
		JScrollPane scroller = new JScrollPane(table);
		return scroller;
	}
    

	@Override public void install(Map<String, String> settings)
	{
		table.install(settings);
	}

}
