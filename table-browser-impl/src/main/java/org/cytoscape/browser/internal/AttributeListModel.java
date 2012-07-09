/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.browser.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.service.util.CyServiceRegistrar;


public class AttributeListModel
	implements ListModel, ComboBoxModel, ColumnCreatedListener, ColumnDeletedListener
{
	private Vector listeners = new Vector();
	private BrowserTableModel browserTableModel;
	private List<String> attributeNames;
	private Object selection = null;
	private Set<Class<?>> validAttrTypes;

	/**
	 * Creates a new AttributeListModel object.
	 */
	public AttributeListModel(final BrowserTableModel browserTableModel,
				  final Set<Class<?>> validAttrTypes)
	{
		this.browserTableModel = browserTableModel;
		this.validAttrTypes = validAttrTypes;
		updateAttributes();
//		serviceRegistrar.registerAllServices(this, new Properties());
	}

	//@SuppressWarnings("unchecked")
	public AttributeListModel(final BrowserTableModel browserTableModel) {
		this(browserTableModel,
		     new HashSet<Class<?>>((List<Class<?>>)(Arrays.asList(new Class<?>[] {
			String.class,
			Boolean.class,
			Double.class,
			Integer.class,
			Long.class,
			List.class
		     }))));
	}

	public void setBrowserTableModel(final BrowserTableModel newBrowserTableModel) {
		browserTableModel = newBrowserTableModel;
		updateAttributes();
	}

	/**
	 *  Sets "attributeNames" to the sorted list of user-visible attribute names with supported data types.
	 */
	public void updateAttributes() {
		if (browserTableModel == null)
			return;

		attributeNames = new ArrayList<String>();
		final CyTable attributes = browserTableModel.getAttributes();
		
		
		for (final CyColumn col : attributes.getColumns()){
			attributeNames.add(col.getName());
		}
		Collections.sort(attributeNames);

		notifyListeners(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0,
		                                  attributeNames.size()));
	}

	/**
	 *  @return the i-th attribute name
	 */
	@Override
	public Object getElementAt(int i) {
		if (i > attributeNames.size())
			return null;

		return attributeNames.get(i);
	}

	/**
	 *  @return the number of attribute names
	 */
	@Override
	public int getSize() {
		return attributeNames.size();
	}

	@Override
	public void setSelectedItem(Object anItem) {
		selection = anItem;
	}

	@Override
	public Object getSelectedItem() {
		return selection;
	}

	@Override
	public void handleEvent(final ColumnCreatedEvent e) {
		if (browserTableModel == null)
			return;

		if (e.getSource() == browserTableModel.getAttributes())
			updateAttributes();
	}

	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		if (browserTableModel == null)
			return;

		if (e.getSource() == browserTableModel.getAttributes())
			updateAttributes();
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}

	private void notifyListeners(ListDataEvent e) {
		for (Iterator listenIt = listeners.iterator(); listenIt.hasNext();) {
			if (e.getType() == ListDataEvent.CONTENTS_CHANGED) {
				((ListDataListener) listenIt.next()).contentsChanged(e);
			} else if (e.getType() == ListDataEvent.INTERVAL_ADDED) {
				((ListDataListener) listenIt.next()).intervalAdded(e);
			} else if (e.getType() == ListDataEvent.INTERVAL_REMOVED) {
				((ListDataListener) listenIt.next()).intervalRemoved(e);
			}
		}
	}
}
