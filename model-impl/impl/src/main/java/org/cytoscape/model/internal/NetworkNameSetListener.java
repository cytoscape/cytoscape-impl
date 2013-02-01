package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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

import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
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
			setTablesName(name + " root shared ", rootNetwork.getSharedEdgeTable(), rootNetwork.getSharedNodeTable(), rootNetwork.getSharedNetworkTable());
			setTablesName(name + " root default ", rootNetwork.getDefaultEdgeTable(), rootNetwork.getDefaultNodeTable(), rootNetwork.getDefaultNetworkTable());
			
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

		if (sourceNetwork.equals( rootNetwork.getBaseNetwork()))
			setRootNetworkName(name);
		
		if (rootNetwork.containsNetwork(sourceNetwork))
			updateSubNetworkTableNames(sourceNetwork, name);
	}
	
	private void setRootNetworkName(String name) {
		CyRow row = rootNetwork.getRow(rootNetwork);
		
		if (!row.isSet(CyNetwork.NAME) || row.get(CyNetwork.NAME, String.class).isEmpty())
			rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, name);
	}
	
	private void updateSubNetworkTableNames(CyNetwork net, String name){
		net.getDefaultEdgeTable().setTitle(name + " default edge");
		net.getDefaultNetworkTable().setTitle(name + " default network");
		net.getDefaultNodeTable().setTitle(name + " default node");
	}
}
