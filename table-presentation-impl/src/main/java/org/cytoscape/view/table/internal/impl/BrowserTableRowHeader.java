package org.cytoscape.view.table.internal.impl;

import javax.swing.DefaultListModel;
import javax.swing.JList;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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
public class BrowserTableRowHeader extends JList<Integer> {

	private final BrowserTable table;

	public BrowserTableRowHeader(BrowserTable table) {
		super(new DefaultListModel<Integer>());
		
		this.table = table;
		updateModel();
	}

	public void updateModel() {
		var model = (DefaultListModel<Integer>) getModel();
		int rowCount = table.getModel().getRowCount();
		
		if (rowCount != model.getSize()) {
			model.removeAllElements();
			
			for (int i = 0; i < rowCount; i++)
				model.addElement(i + 1);
		}
	}

	public void update() {
		// Force repaint on JList (set updateLayoutStateNeeded = true) on BasicListUI
		firePropertyChange("cellRenderer", 0, 1);
		repaint();
	}
}
