package org.cytoscape.task.internal.utils;

public final class CoreImplDocumentationConstants {
	
	/**
	 * See org.cytoscape.task.internal.table.AbstractTableDataTask.getNamespace(String)
	 */
	public static final String COLUMN_NAMESPACE_LONG_DESCRIPTION = "Node, Edge, and Network objects support the '''default''', '''local''', and '''hidden''' namespaces. Root networks also support the '''shared''' namespace. Custom namespaces may be specified by Apps.";
	
	public static final String COLUMN_NAMESPACE_EXAMPLE_STRING = "default";
	
	/**
	 * See 
	 * 
	 * org.cytoscape.task.internal.utils.ColumnListTunable.getColumnList(CyTable)
	 * org.cytoscape.task.internal.utils.ColumnValueTunable.getColumnList(CyTable)
	 * 
	 */
	public static final String COLUMN_LIST_LONG_DESCRIPTION = "A list of column names, separated by commas.";
	
	public static final String COLUMN_LIST_EXAMPLE_STRING ="name,EdgeBetweenness";
	
	/**
	 * See org.cytoscape.task.internal.networkobjects.AbstractGetTask#getNode(CyNetwork, String)
	 */
	public static final String NODE_LONG_DESCRIPTION = "Selects a node by name, or, if the parameter has the prefix ```suid:```, selects a node by SUID.";
	
	/**
	 * See org.cytoscape.task.internal.networkobjects.AbstractGetTask#getEdge(CyNetwork, String)
	 */
	public static final String EDGE_LONG_DESCRIPTION = "Selects an edge by name, or, if the parameter has the prefix ```suid:```, selects an edge by SUID.";

	/**
	 * See org.cytoscape.task.internal.utils.DataUtils.getCSV(String)
	 */
	public static final String VALUE_LIST_LONG_DESCRIPTION = "A list of values separated by commas.";
	
	/**
	 * See 
	 * 
	 * org.cytoscape.task.internal.networkobjects.GetEdgePropertiesTask.run(TaskMonitor)
	 * org.cytoscape.task.internal.networkobjects.GetNetworkPropertiesTask.run(TaskMonitor)
	 * org.cytoscape.task.internal.networkobjects.GetNodePropertiesTask.run(TaskMonitor)
	 * org.cytoscape.task.internal.networkobjects.SetEdgePropertiesTask.run(TaskMonitor)
	 * org.cytoscape.task.internal.networkobjects.SetNetworkPropertiesTask.run(TaskMonitor)
	 * org.cytoscape.task.internal.networkobjects.SetNodePropertiesTask.run(TaskMonitor)
	 */
	public static final String PROPERTY_LIST_LONG_DESCRIPTION = "A list of property names separated by commas.";

	/**
	 * Renaming is the equivalent of a set/PUT, and doesn't need to return anything.
	 */
	public static final String RENAME_EXAMPLE_JSON = "{}";
	

}
