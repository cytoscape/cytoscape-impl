package org.cytoscape.browser.internal.view;

import java.awt.Component;
import java.util.Collections;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

@SuppressWarnings("serial")
public class TableChooserCellRenderer extends DefaultListCellRenderer {

	private final Map<CyTable, String> tableToStringMap;
	private final CyNetworkTableManager netTableManager;

	TableChooserCellRenderer(CyServiceRegistrar seviceRegistrar) {
		this(Collections.emptyMap(), seviceRegistrar);
	}
	
	TableChooserCellRenderer(Map<CyTable, String> tableToStringMap, CyServiceRegistrar seviceRegistrar) {
		this.tableToStringMap = tableToStringMap;
		this.netTableManager = seviceRegistrar.getService(CyNetworkTableManager.class);
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		
		if (value instanceof CyTable == false) {
			setText("-- No Table --");
			return this;
		}
		
		var table = (CyTable) value;
		var text = tableToStringMap.get(table);
		
		if (text == null) {
			var namespace = netTableManager.getTableNamespace(table);
			var type = netTableManager.getTableType(table);
			
			if (type != null && CyNetwork.DEFAULT_ATTRS.equals(namespace)) {
				text = "Default " + type.getSimpleName().replace("Cy", "") + " Table";
			} else {
				text = table.getTitle();
				
				if (!text.toLowerCase().contains("table"))
					text += " Table";
				
				if (table != null && !table.isPublic())
					text += " [ PRIVATE ]";
			}
		}

		setText(text);
		
		if (!isSelected)
			setForeground(table.isPublic() ? list.getForeground() : LookAndFeelUtil.getErrorColor());

		return this;
	}
}
