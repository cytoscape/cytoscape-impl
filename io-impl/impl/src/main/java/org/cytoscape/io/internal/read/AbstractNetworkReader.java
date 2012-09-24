package org.cytoscape.io.internal.read;

//import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
//import org.cytoscape.model.subnetwork.CySubNetwork;


public abstract class AbstractNetworkReader extends AbstractTask implements CyNetworkReader {
	
	/**
	 * If this option is selected, reader should create new CyRootNetwork.
	 */
	public static final String CRERATE_NEW_COLLECTION_STRING ="Create new network collection";

	protected CyNetwork[] cyNetworks;

	protected VisualStyle[] visualstyles;
	protected InputStream inputStream;

	protected final CyNetworkViewFactory cyNetworkViewFactory;
	protected final CyNetworkFactory cyNetworkFactory;

	protected final CyNetworkManager cyNetworkManager;;
	protected final CyRootNetworkManager cyRootNetworkManager;

	//******** tunables ********************

	public ListSingleSelection<String> rootNetworkList;
	@Tunable(description = "Network Collection List" ,groups=" ")
	public ListSingleSelection<String> getRootNetworkList(){
		return rootNetworkList;
	}
	public void setRootNetworkList (ListSingleSelection<String> roots){
		if (rootNetworkList.getSelectedValue().equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			return;
		}
		targetColumnList = getTargetColumns(name2RootMap.get(rootNetworkList.getSelectedValue()));
	}

	public ListSingleSelection<String> sourceColumnList;
	@Tunable(description = "Mapping Column for New Network:", groups=" ")
	public ListSingleSelection<String> getSourceColumnList(){
		return sourceColumnList;
	}
	public void setSourceColumnList(ListSingleSelection<String> colList){
		this.sourceColumnList = colList;
	}
	
	public ListSingleSelection<String> targetColumnList;
	@Tunable(description = "Mapping Column for Existing Network:",groups=" ", listenForChange={"RootNetworkList"})
	public ListSingleSelection<String> getTargetColumnList(){
		return targetColumnList;
	}
	public void setTargetColumnList(ListSingleSelection<String> colList){
		this.targetColumnList = colList;
	}
	
	
	@ProvidesTitle
	public String getTitle() {
		return "Import Network ";
	}

	
	public ListSingleSelection<String> getTargetColumns (CyNetwork network) {
		CyTable selectedTable = network.getTable(CyNode.class, CyRootNetwork.SHARED_ATTRS);
		
		List<String> colNames = new ArrayList<String>();
		for(CyColumn col: selectedTable.getColumns()) {
			// Exclude SUID from the mapping key list
			if (col.getName().equalsIgnoreCase("SUID")){
				continue;
			}
			colNames.add(col.getName());
		}
		
		ListSingleSelection<String> columns = new ListSingleSelection<String>(colNames);
		return columns;
	}

	
	protected HashMap<String, CyRootNetwork> name2RootMap;
	protected Map<Object, CyNode> nMap = new HashMap<Object, CyNode>(10000);

	public AbstractNetworkReader(InputStream inputStream, final CyNetworkViewFactory cyNetworkViewFactory,
			final CyNetworkFactory cyNetworkFactory, final CyNetworkManager cyNetworkManager, final CyRootNetworkManager cyRootNetworkManager) {
		if (inputStream == null)
			throw new NullPointerException("Input stream is null");
		if (cyNetworkViewFactory == null)
			throw new NullPointerException("CyNetworkViewFactory is null");
		if (cyNetworkFactory == null)
			throw new NullPointerException("CyNetworkFactory is null");

		this.inputStream = inputStream;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;

		// initialize the network Collection
		this.name2RootMap = ReadUtils.getRootNetworkMap(this.cyNetworkManager, this.cyRootNetworkManager);
		
		List<String> rootNames = new ArrayList<String>();
		rootNames.add(CRERATE_NEW_COLLECTION_STRING);
		rootNames.addAll(name2RootMap.keySet());
		rootNetworkList = new ListSingleSelection<String>(rootNames);
		rootNetworkList.setSelectedValue(rootNames.get(0));
		

		// initialize target attribute list
		List<String> colNames_target = new ArrayList<String>();
		colNames_target.add("shared name");
		this.targetColumnList = new ListSingleSelection<String>(colNames_target);
	
		// initialize source attribute list
		List<String> colNames_source = new ArrayList<String>();
		colNames_source.add("shared name");
		this.sourceColumnList = new ListSingleSelection<String>(colNames_source);
	
	}

	@Override
	public CyNetwork[] getNetworks() {
		return cyNetworks;
	}
	
	// Build the key-node map for the entire root network
	protected void initNodeMap(CyRootNetwork rootNetwork, String keyColName){	
		// Note: The keyColName should start with "shared"
		
		if (rootNetwork == null){
			return;
		}
		
		Iterator<CyNode> it = rootNetwork.getNodeList().iterator();
		
		while (it.hasNext()){
			CyNode node = it.next();
			Object keyValue =  rootNetwork.getRow(node).getRaw(keyColName);
			this.nMap.put(keyValue, node);				
		}
	}
}
