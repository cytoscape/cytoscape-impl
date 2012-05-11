package org.cytoscape.model.internal;

import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;

/**
 * NetworkNameSetListener implements NetworkAddedListener and RowsSetListener. 
 * The tables are renamed based on the network name. When a network is created
 * for the first time first root network is constructed which creates the base network 
 * immediately. Since when networks are created the rowssetevent is not fired, the 
 * networkaddedlistener is implemented which sets the name of the table at first. 
 * If the networkaddedevent is for the base network, it sets the root network name 
 * as well as the base network's table titles. By setting the name of the root network, 
 * a rowssetevent gets fired which will update the title of the shared and default 
 * tables for the root network.
 * Each time that a network name is updated, the rowsset event is fired which 
 * updates the title of the default tables for that network.
 * @author rozagh
 *
 */
public class NetworkNameSetListener implements RowsSetListener, NetworkAddedListener{

	 final CyRootNetwork rootNetwork;
	
	public NetworkNameSetListener(CyRootNetwork rootNetwork){
		this.rootNetwork = rootNetwork;
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		CyTable sourceTable = e.getSource();
		
		if (sourceTable.equals(rootNetwork.getDefaultNetworkTable())){	
			updateRootNetworkTableNames(e.getPayloadCollection());
		}
		else{
			updateSubNetworkTableNames(e.getColumnRecords(CyNetwork.NAME), sourceTable);
		}
		
	
	}
	
	private void updateSubNetworkTableNames( Collection<RowSetRecord> payloadCollection, CyTable sourceTable) {
		
		for (CyNetwork net: rootNetwork.getSubNetworkList()){
			if (sourceTable.equals(net.getDefaultNetworkTable())){
				for ( RowSetRecord record :payloadCollection) {
					// assume payload collection is for same column
					final Object name = record.getValue();
					setTablesName(name.toString() + " default ", net.getDefaultEdgeTable(), net.getDefaultNodeTable(), net.getDefaultNetworkTable());
					
					return; //assuming that this even is fired only for a single row
				}		
			}
		}
		
	}

	private void updateRootNetworkTableNames(
			Collection<RowSetRecord> payloadCollection) {

		for ( RowSetRecord record : payloadCollection ) {
			// assume payload collection is for same column
			final Object name = record.getValue();
			setTablesName(name.toString() + " root shared ", rootNetwork.getSharedEdgeTable(), rootNetwork.getSharedNodeTable(), rootNetwork.getSharedNetworkTable());
			setTablesName(name.toString() + " root default ", rootNetwork.getDefaultEdgeTable(), rootNetwork.getDefaultNodeTable(), rootNetwork.getDefaultNetworkTable());
			
			return;
		}
		
	}

	private void setTablesName (String name, CyTable edgeTable, CyTable nodeTable, CyTable networkTable){
		edgeTable.setTitle(name + " edge");
		networkTable.setTitle(name + " network");
		nodeTable.setTitle(name + " node");
		
	}

	//=========================NetworkAddedListener Implementation========================
	@Override
	public void handleEvent(NetworkAddedEvent e) {

		CyNetwork sourceNetwork = e.getNetwork();
		String name = sourceNetwork.getRow(sourceNetwork).get(CyNetwork.NAME, String.class);

		if (sourceNetwork.equals( rootNetwork.getBaseNetwork())){
			setRootNetworkName(name);
		}
		
		if(rootNetwork.containsNetwork(sourceNetwork))
			updateSubNetworkTableNames(sourceNetwork, name);
	}
	
	private void setRootNetworkName(String name) {
		rootNetwork.getRow(rootNetwork).set(CyRootNetwork.NAME, name);
	}
	
	private void updateSubNetworkTableNames(CyNetwork net, String name){
		
		net.getDefaultEdgeTable().setTitle(name + " default edge");
		net.getDefaultNetworkTable().setTitle(name + " default network");
		net.getDefaultNodeTable().setTitle(name + " default node");
	}
}
