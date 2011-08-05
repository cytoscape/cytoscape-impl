package org.cytoscape.view.vizmap.gui.internal.theme;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class IconManager {
	
	private Map<String, Icon> iconMap;
	
	public IconManager() {
		// for setter injection
		iconMap = new HashMap<String, Icon>();
		loadIcon();
	}
	
	public Icon getIcon(String name) {
		final Icon icon = iconMap.get(name);
		
		if(icon == null) {
			// This should return default icon.
			return null;
		} else
			return icon;
	}

	private void loadIcon() {
		iconMap.put("optionIcon", new ImageIcon(getClass().getResource("/images/icons/stock_form-properties.png")));
		iconMap.put("delIcon", new ImageIcon(getClass().getResource("/images/icons/stock_delete-16.png")));
		iconMap.put("addIcon", new ImageIcon(getClass().getResource("/images/icons/stock_data-new-table-16.png")));
		iconMap.put("rndIcon", new ImageIcon(getClass().getResource("/images/icons/stock_filters-16.png")));
		iconMap.put("renameIcon", new ImageIcon(getClass().getResource("/images/icons/stock_redo-16.png")));
		iconMap.put("copyIcon", new ImageIcon(getClass().getResource("/images/icons/stock_slide-duplicate.png")));
		iconMap.put("legendIcon", new ImageIcon(getClass().getResource("/images/icons/stock_graphic-styles-16.png")));
		iconMap.put("editIcon", new ImageIcon(getClass().getResource("/images/icons/stock_edit-16.png")));
	}
}
