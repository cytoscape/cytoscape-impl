package org.cytoscape.browser.internal.view;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import org.cytoscape.util.swing.OpenBrowser;

public class OpenURLMenu extends JMenu {

	private static final long serialVersionUID = 2344898200050965794L;

	private final String value;
	private final Map<String, Map<String, String>> structure;
	private final OpenBrowser openBrowser;

	public OpenURLMenu(String value, final Map<String, Map<String, String>> menuStructure,
			final OpenBrowser openBrowser) {
		this.value = value;
		this.structure = menuStructure;
		this.openBrowser = openBrowser;

		setBackground(UIManager.getColor("Table.background"));

		final String dispStr;
		if (value.length() > 30)
			dispStr = value.substring(0, 29) + " ... ";
		else
			dispStr = value;

		setText("<html>Search <strong text=\"#DC143C\">" + dispStr + "</strong> on the web</html>");
		buildLinks();
	}

	private void buildLinks() {
		if (structure == null)
			return;

		for (final String category : structure.keySet()) {
			JMenu cat = new JMenu(category);
			Map<String, String> children = structure.get(category);

			for (final String name : children.keySet()) {
				JMenuItem dbLink = new JMenuItem(name);
				dbLink.addActionListener(e -> {
                    final String url = structure.get(category).get(name).replace("%ID%", value);
                    openBrowser.openURL(url);
                });
				cat.add(dbLink);
			}

			this.add(cat);
		}
	}
}
