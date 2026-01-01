package org.cytoscape.view.table.internal.equation;

import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2026 The Cytoscape Consortium
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
public class ItemListPanel<T> extends ItemPanel {

	private JList<T> list;
	
	public ItemListPanel(String title) {
		super(title);
	}
	
	public void addElements(Collection<T> items) {
		((DefaultListModel<T>)getList().getModel()).addAll(items);
	}
	
	public void addElement(T element) {
		((DefaultListModel<T>)getList().getModel()).addElement(element);
	}
	
	public void clearSelection() {
		getList().clearSelection();
	}
	
	public T getSelectedValue() {
		return getList().getSelectedValue();
	}

	public JList<T> getList() {
		if(list == null) {
			list = new JList<>(new DefaultListModel<>());
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return list;
	}
	
	@Override
	public JList<T> getContent() {
		return getList();
	}

}
