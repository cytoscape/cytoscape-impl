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
package org.cytoscape.filter.internal.quickfind.util;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.cytoscape.filter.internal.widgets.autocomplete.index.GenericIndex;
import org.cytoscape.filter.internal.widgets.autocomplete.index.Hit;
import org.cytoscape.filter.internal.widgets.autocomplete.index.IndexFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.work.TaskMonitor;


/**
 * Default implementation of the QuickFind interface.  For details, see
 * {@link QuickFind}.
 *
 * @author Ethan Cerami.
 */
class QuickFindImpl implements QuickFind {
	private ArrayList listenerList = new ArrayList();
	private HashMap networkMap = new HashMap();
	private int maxProgress;
	private int currentProgress;
	private static final boolean OUTPUT_PERFORMANCE_STATS = false;

	/**
	 * Creates a new QuickFindImpl object.
	 *
	 * @param nodeAttributes  DOCUMENT ME!
	 * @param edgeAttributes  DOCUMENT ME!
	 */
	public QuickFindImpl() {
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param network DOCUMENT ME!
	 * @param taskMonitor DOCUMENT ME!
	 */
	public synchronized void addNetwork(CyNetwork network, TaskMonitor taskMonitor) {
		// check args - short circuit if necessary
		if (network.getNodeCount() == 0) {
			return;
		}

		//  Use default index specified by network, if available.
		//  Otherwise, index by UNIQUE_IDENTIFIER.
		String controllingAttribute = network.getCyRow().get(QuickFind.DEFAULT_INDEX, String.class);

		CyTable nodeTable = null;
		CyNode node = network.getNodeList().iterator().next();
		if (node != null) {
			nodeTable = node.getCyRow().getTable();
		}
		
		if (controllingAttribute == null) {
			//  Small hack to index BioPAX Networks by default with node_label.

			if (nodeTable.getColumn("bioPax.node_label") != null)
				controllingAttribute = "biopax.node_label";
			else
				controllingAttribute = QuickFind.UNIQUE_IDENTIFIER;
		}

		if (controllingAttribute.equalsIgnoreCase(QuickFind.UNIQUE_IDENTIFIER)||
		    controllingAttribute.equalsIgnoreCase(QuickFind.INDEX_ALL_ATTRIBUTES)||
		    controllingAttribute.equalsIgnoreCase("biopax.node_label")){
			// do nothing
		}
		else if (isNullAttribute(nodeTable, controllingAttribute)){
			return;
		}

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

	/**
	 *  DOCUMENT ME!
	 *
	 * @param network DOCUMENT ME!
	 */
	public synchronized void removeNetwork(CyNetwork network) {
		if (networkMap.containsKey(network)){
			networkMap.remove(networkMap);
		}

		// Notify all listeners of remove event
		for (int i = 0; i < listenerList.size(); i++) {
			QuickFindListener listener = (QuickFindListener) listenerList.get(i);
			listener.networkRemovedfromIndex(network);
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param network DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public synchronized GenericIndex getIndex(CyNetwork network) {
		if (networkMap.containsKey(network)){
			return (GenericIndex) networkMap.get(network);	
		}
		return null;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param cyNetwork DOCUMENT ME!
	 * @param indexType DOCUMENT ME!
	 * @param controllingAttribute DOCUMENT ME!
	 * @param taskMonitor DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public synchronized GenericIndex reindexNetwork(CyNetwork cyNetwork, int indexType,
	                                                String controllingAttribute,
	                                                TaskMonitor taskMonitor) {
        
		// If all the values for the controllingAttribute are NULL, return null
		CyTable table;
		if (indexType == QuickFind.INDEX_NODES) {
			CyNode node = cyNetwork.getNodeList().iterator().next();
			if (node == null) {
				return null;
			}
			table = node.getCyRow().getTable();
		} else if (indexType == QuickFind.INDEX_EDGES){
			CyEdge edge = cyNetwork.getEdgeList().iterator().next();
			if (edge == null) {
				return null;
			}
			table = edge.getCyRow().getTable();
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
		Iterator<? extends CyTableEntry> iterator;

		if (indexType == QuickFind.INDEX_NODES) {
			taskMonitor.setStatusMessage("Indexing node attributes");
			iterator = network.getNodeList().iterator();
		} else if (indexType == QuickFind.INDEX_EDGES) {
			taskMonitor.setStatusMessage("Indexing edge attributes");
			iterator = network.getEdgeList().iterator();
		} else {
			throw new IllegalArgumentException("indexType must be set to: "
			                                   + "QuickFind.INDEX_NODES or QuickFind.INDEX_EDGES");
		}

		//  Iterate through all nodes or edges
		while (iterator.hasNext()) {
			currentProgress++;

			CyTableEntry graphObject = iterator.next();
			addToIndex(attributeType, graphObject, controllingAttribute, index);

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
	private void addToIndex(Class<?> attributeType, CyTableEntry graphObject,
	                        String controllingAttribute, GenericIndex index) {
		CyRow row = graphObject.getCyRow();
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
			String[] values = CyAttributesUtil.getAttributeValues(graphObject, controllingAttribute);
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
	private void addStringsToIndex(String[] value, CyTableEntry graphObject, GenericIndex index) {
		//  Add to index
		for (int i = 0; i < value.length; i++) {
			index.addToIndex(value[i], graphObject);
		}
	}
}
