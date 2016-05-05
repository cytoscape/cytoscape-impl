package org.cytoscape.filter.internal.quickfind.util;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.cytoscape.filter.internal.widgets.autocomplete.index.GenericIndex;
import org.cytoscape.filter.internal.widgets.autocomplete.index.Hit;
import org.cytoscape.filter.internal.widgets.autocomplete.index.IndexFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.TaskMonitor;


/**
 * Default implementation of the QuickFind interface.  For details, see
 * {@link QuickFind}.
 *
 * @author Ethan Cerami.
 */
public class QuickFindImpl implements QuickFind {
	
	private final List<QuickFindListener> listenerList;
	private final Map<CyNetwork, GenericIndex> networkMap;
	
	private int maxProgress;
	private int currentProgress;
	private static final boolean OUTPUT_PERFORMANCE_STATS = false;


	public QuickFindImpl() {
		this.listenerList = new ArrayList<>();
		this.networkMap = new WeakHashMap<>();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void addNetwork(CyNetwork network, TaskMonitor taskMonitor) {
		// check args - short circuit if necessary
		if (network.getNodeCount() == 0)
			return;

		//  Use default index specified by network, if available.
		//  Otherwise, index by UNIQUE_IDENTIFIER.
		String controllingAttribute = network.getRow(network).get(QuickFind.DEFAULT_INDEX, String.class);

		CyTable nodeTable = network.getDefaultNodeTable();
		
		if (controllingAttribute == null) {
			//  Small hack to index BioPAX Networks by default with node_label.

			if (nodeTable.getColumn("bioPax.node_label") != null)
				controllingAttribute = "biopax.node_label";
			else
				controllingAttribute = QuickFind.UNIQUE_IDENTIFIER;
		}

		if (controllingAttribute.equalsIgnoreCase(QuickFind.UNIQUE_IDENTIFIER)
		    || controllingAttribute.equalsIgnoreCase(QuickFind.INDEX_ALL_ATTRIBUTES)
		    || controllingAttribute.equalsIgnoreCase("biopax.node_label"))
			/* Do nothing. */;

		else if (isNullAttribute(nodeTable, controllingAttribute))
			return;

		//  Determine maxProgress
		currentProgress = 0;
		maxProgress = getGraphObjectCount(network, QuickFind.INDEX_NODES);

		// Notify all listeners of add event
		for (int i = 0; i < listenerList.size(); i++) {
			QuickFindListener listener = (QuickFindListener) listenerList.get(i);
			listener.networkAddedToIndex(network);
		}

		// Notify all listeners of index start event
		for (int i = 0; i < listenerList.size(); i++) {
			QuickFindListener listener = (QuickFindListener) listenerList.get(i);
			listener.indexingStarted(network, QuickFind.INDEX_NODES, controllingAttribute);
		}

		//  Create Appropriate Index Type, based on attribute type.
		CyColumn column = nodeTable.getColumn(controllingAttribute);
		Class<?> attributeType;
		if (column == null) {
			attributeType = String.class;
		} else {
			attributeType = column.getType();
		}
		GenericIndex index = createIndex(QuickFind.INDEX_NODES, attributeType, controllingAttribute);
		indexNetwork(network, QuickFind.INDEX_NODES, attributeType, controllingAttribute, index, taskMonitor);
		networkMap.put(network, index);

		// Notify all listeners of end index event
		for (int i = 0; i < listenerList.size(); i++) {
			QuickFindListener listener = (QuickFindListener) listenerList.get(i);
			listener.indexingEnded();
		}
	}

	private boolean isNullAttribute(final CyTable table, final String controllingAttribute) {
		final CyColumn column = table.getColumn(controllingAttribute);
		if (column == null)
			return true;

		final Class<?> type = column.getType();
		for (CyRow row : table.getAllRows()) {
			Object value = row.get(controllingAttribute, type);
			if (value != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public synchronized void removeNetwork(CyNetwork network) {
		networkMap.remove(network);

		// Notify all listeners of remove event
		for (int i = 0; i < listenerList.size(); i++) {
			QuickFindListener listener = (QuickFindListener) listenerList.get(i);
			listener.networkRemovedfromIndex(network);
		}
	}

	@Override
	public synchronized GenericIndex getIndex(final CyNetwork network) {
		return networkMap.get(network);	
	}


	@Override
	public synchronized GenericIndex reindexNetwork(CyNetwork cyNetwork, int indexType, String controllingAttribute,
			TaskMonitor taskMonitor) {
		// If all the values for the controllingAttribute are NULL, return null
		CyTable table;
		if (indexType == QuickFind.INDEX_NODES) {
			if (cyNetwork.getNodeCount() == 0) {
				return null;
			}
			table = cyNetwork.getDefaultNodeTable();
		} else if (indexType == QuickFind.INDEX_EDGES){
			if (cyNetwork.getEdgeCount() == 0) {
				return null;
			}
			table = cyNetwork.getDefaultEdgeTable(); 
		} else {
			return null;
		}

		if (controllingAttribute.equalsIgnoreCase(QuickFind.UNIQUE_IDENTIFIER)||
		    controllingAttribute.equalsIgnoreCase(QuickFind.INDEX_ALL_ATTRIBUTES)||
		    controllingAttribute.equalsIgnoreCase("biopax.node_label")){
			// do nothing
		}
		else if (isNullAttribute(table, controllingAttribute)){
			return null;
		}
		
		//
		Date start = new Date();
        if ((indexType != QuickFind.INDEX_NODES) && (indexType != QuickFind.INDEX_EDGES)) {
			throw new IllegalArgumentException("indexType must be set to: "
			                                   + "QuickFind.INDEX_NODES or QuickFind.INDEX_EDGES");
		}

		// Notify all listeners of index start event
		for (int i = 0; i < listenerList.size(); i++) {
			QuickFindListener listener = (QuickFindListener) listenerList.get(i);
			listener.indexingStarted(cyNetwork, indexType, controllingAttribute);
		}

		//  Determine maxProgress
		currentProgress = 0;
		maxProgress = 0;

		if (controllingAttribute.equals(QuickFind.INDEX_ALL_ATTRIBUTES)) {
			for (final CyColumn column : table.getColumns())
				maxProgress += getGraphObjectCount(cyNetwork, indexType);
		} else
			maxProgress = getGraphObjectCount(cyNetwork, indexType);

		GenericIndex index = null;
		if (controllingAttribute.equals(QuickFind.INDEX_ALL_ATTRIBUTES)) {
			//  Option 1:  Index all attributes
			index = createIndex(indexType, String.class, controllingAttribute);

			for (final CyColumn column : table.getColumns()) {
				final String attributeName = column.getName();
				indexNetwork(cyNetwork, indexType, String.class, attributeName, index, taskMonitor);
			}
		} else {
			//  Option 2:  Index single attribute.
			//  Create appropriate index type, based on attribute type.
			CyColumn column = table.getColumn(controllingAttribute);
			Class<?> attributeType;
			if (column == null) {
				attributeType = String.class;
			} else {
				attributeType = column.getType();
			}
			index = createIndex(indexType, attributeType, controllingAttribute);
			indexNetwork(cyNetwork, indexType, attributeType, controllingAttribute, index, taskMonitor);
		}

		networkMap.put(cyNetwork, index);

		// Notify all listeners of index end event
		for (int i = 0; i < listenerList.size(); i++) {
			QuickFindListener listener = (QuickFindListener) listenerList.get(i);
			listener.indexingEnded();
		}

        Date stop = new Date();
        long duration = stop.getTime() - start.getTime();
        // System.out.println("Time to re-index:  " + duration + " ms");
        return index;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param network DOCUMENT ME!
	 * @param hit DOCUMENT ME!
	 */
	public synchronized void selectHit(CyNetwork network, Hit hit) {
		// Notify all listeners of event
		for (int i = 0; i < listenerList.size(); i++) {
			QuickFindListener listener = (QuickFindListener) listenerList.get(i);
			listener.onUserSelection(network, hit);
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param network DOCUMENT ME!
	 * @param low DOCUMENT ME!
	 * @param high DOCUMENT ME!
	 */
	public synchronized void selectRange(CyNetwork network, Number low, Number high) {
		// Notify all listeners of event
		for (int i = 0; i < listenerList.size(); i++) {
			QuickFindListener listener = (QuickFindListener) listenerList.get(i);
			listener.onUserRangeSelection(network, low, high);
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param listener DOCUMENT ME!
	 */
	public synchronized void addQuickFindListener(QuickFindListener listener) {
		this.listenerList.add(listener);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param listener DOCUMENT ME!
	 */
	public synchronized void removeQuickFindListener(QuickFindListener listener) {
		this.listenerList.remove(listener);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public synchronized QuickFindListener[] getQuickFindListeners() {
		return (QuickFindListener[]) listenerList.toArray(new QuickFindListener[listenerList.size()]);
	}

	private synchronized int getGraphObjectCount(CyNetwork network, int indexType) {
		if (indexType == QuickFind.INDEX_NODES) {
			return network.getNodeCount();
		} else {
			return network.getEdgeCount();
		}
	}

	private void indexNetwork(CyNetwork network, int indexType, Class<?> attributeType, String controllingAttribute, GenericIndex index, TaskMonitor taskMonitor) {
		
		//
		Date start = new Date();
		Iterator<? extends CyIdentifiable> iterator;

		if (indexType == QuickFind.INDEX_NODES) {
			iterator = network.getNodeList().iterator();
		} else if (indexType == QuickFind.INDEX_EDGES) {
			iterator = network.getEdgeList().iterator();
		} else {
			throw new IllegalArgumentException("indexType must be set to: "
			                                   + "QuickFind.INDEX_NODES or QuickFind.INDEX_EDGES");
		}

		//  Iterate through all nodes or edges
		while (iterator.hasNext()) {
			currentProgress++;

			CyIdentifiable graphObject = iterator.next();
			addToIndex(network,attributeType, graphObject, controllingAttribute, index);

			//  Determine percent complete
			taskMonitor.setProgress(currentProgress / (double) maxProgress);
		}

		Date stop = new Date();
		long interval = stop.getTime() - start.getTime();

		if (OUTPUT_PERFORMANCE_STATS) {
			System.out.println("Time to index network:  " + interval + " ms");
		}

		networkMap.put(network, index);
	}

	/**
	 * Creates appropriate index, based on attribute type.
	 * @param indexType             QuickFind.INDEX_NODES or QuickFind.INDEX_EDGES
	 * @param attributeType         CyAttributes type.
	 * @param controllingAttribute  Controlling attribute.
	 * @return GenericIndex Object.
	 */
	private GenericIndex createIndex(int indexType, Class<?> attributeType, String controllingAttribute) {
		GenericIndex index;

		// If all the values for the controllingAttribute are NULL, return null
		String _type = "node";
		if (indexType == QuickFind.INDEX_EDGES){
			_type = "edge";
		}
		//
		if ((attributeType == Integer.class)
		    || (attributeType == Double.class)) {
			index = IndexFactory.createDefaultNumberIndex(indexType);
		} else {
			index = IndexFactory.createDefaultTextIndex(indexType);
		}

		index.setControllingAttribute(controllingAttribute);

		return index;
	}

	/**
	 * Adds new items to index.
	 * @param attributeType         CyAttributes type.
	 * @param graphObject           Graph Object.
	 * @param controllingAttribute  Controlling attribute.
	 * @param index                 Index to add to.
	 */
	private void addToIndex(CyNetwork network, Class<?> attributeType, CyIdentifiable graphObject,
	                        String controllingAttribute, GenericIndex index) {
        // make sure our network actually has graphObject
        if ((graphObject instanceof CyNode) && (!network.containsNode((CyNode) graphObject)))
            return;
        if ((graphObject instanceof CyEdge) && (!network.containsEdge((CyEdge) graphObject)))
            return;
		CyRow row = network.getRow(graphObject);
		//  Get attribute values, and index
		if (attributeType == Integer.class) {
			Integer value = row.get(controllingAttribute, Integer.class);
			if (value != null) {
				index.addToIndex(value, graphObject);
			}
		} else if (attributeType == Double.class) {
			Double value = row.get(controllingAttribute, Double.class); 
			if (value != null) {
				index.addToIndex(value, graphObject);
			}
		} else {
			String[] values = CyAttributesUtil.getAttributeValues(network, graphObject, controllingAttribute);
			if (values != null) {
				addStringsToIndex(values, graphObject, index);
			}
		}
	}

	/**
	 * Adds multiple strings to an index.
	 * @param value         Array of Strings.
	 * @param graphObject   Graph Object.
	 * @param index         Index to add to.
	 */
	private void addStringsToIndex(String[] value, CyIdentifiable graphObject, GenericIndex index) {
		//  Add to index
		for (int i = 0; i < value.length; i++) {
			index.addToIndex(value[i], graphObject);
		}
	}
}
