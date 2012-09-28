package org.cytoscape.psi_mi.internal.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class PsiMiTabReader extends AbstractTask implements CyNetworkReader {
	
//	@Tunable(description="Import all columns in the data file")
//	public Boolean importFull;
	
	private InputStream inputStream;

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyLayoutAlgorithmManager layouts;

	private final PsiMiTabParser parser;
	private CyNetwork network;

	private TaskMonitor parentTaskMonitor;
	private final CyProperty<Properties> prop;

	private final CyNetworkFactory cyNetworkFactory;
	private final CyRootNetworkManager cyRootNetworkManager;
	
	/**
	 * If this option is selected, reader should create new CyRootNetwork.
	 */
	public static final String CRERATE_NEW_COLLECTION_STRING ="Create new network collection";

	//******** tunables ********************

	public ListSingleSelection<String> rootNetworkList;
	@Tunable(description = "Network Collection" ,groups=" ")
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
		
		//columns.setSelectedValue("shared name"); this does not work, why
		return columns;
	}

	
	protected HashMap<String, CyRootNetwork> name2RootMap;
	protected Map<Object, CyNode> nMap = new HashMap<Object, CyNode>(10000);

	
	// Return the rootNetwork based on user selection, if not existed yet, create a new one
	private CyRootNetwork getRootNetwork(){
		String networkCollectionName = this.rootNetworkList.getSelectedValue().toString();
		CyRootNetwork rootNetwork = this.name2RootMap.get(networkCollectionName);

		if (networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			CyNetwork newNet = this.cyNetworkFactory.createNetwork();
			return this.cyRootNetworkManager.getRootNetwork(newNet);
		}

		return rootNetwork;
	}
	
	// Build the key-node map for the entire root network
	// Note: The keyColName should start with "shared"
	private void initNodeMap(){	
		
		String networkCollectionName = this.rootNetworkList.getSelectedValue().toString();
		CyRootNetwork rootNetwork = this.name2RootMap.get(networkCollectionName);
		
		if (networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			return;
		}

		String targetKeyColName = this.targetColumnList.getSelectedValue();
		
		if (rootNetwork == null){
			return;
		}
		
		Iterator<CyNode> it = rootNetwork.getNodeList().iterator();
		
		while (it.hasNext()){
			CyNode node = it.next();
			Object keyValue =  rootNetwork.getRow(node).getRaw(targetKeyColName);
			this.nMap.put(keyValue, node);				
		}
	}

	
	private static HashMap<String, CyRootNetwork> getRootNetworkMap(CyNetworkManager cyNetworkManager, CyRootNetworkManager cyRootNetworkManager) {

		HashMap<String, CyRootNetwork> name2RootMap = new HashMap<String, CyRootNetwork>();

		for (CyNetwork net : cyNetworkManager.getNetworkSet()){
			final CyRootNetwork rootNet = cyRootNetworkManager.getRootNetwork(net);
			if (!name2RootMap.containsValue(rootNet ) )
				name2RootMap.put(rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class), rootNet);
		}

		return name2RootMap;
	}

	
	
	public PsiMiTabReader(InputStream is, CyNetworkViewFactory cyNetworkViewFactory, CyNetworkFactory cyNetworkFactory,
			final CyLayoutAlgorithmManager layouts, final CyProperty<Properties> prop, CyNetworkManager cyNetworkManager, CyRootNetworkManager cyRootNetworkManager) {
		if (is == null)
			throw new NullPointerException("Input stream is null");
		this.inputStream = is;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.layouts = layouts;
		this.prop = prop;

		this.cyNetworkFactory = cyNetworkFactory;
		this.cyRootNetworkManager = cyRootNetworkManager;
		
		// initialize the network Collection
		this.name2RootMap = getRootNetworkMap(cyNetworkManager, cyRootNetworkManager);
		
		List<String> rootNames = new ArrayList<String>();
		rootNames.add(CRERATE_NEW_COLLECTION_STRING);
		rootNames.addAll(name2RootMap.keySet());
		rootNetworkList = new ListSingleSelection<String>(rootNames);
		rootNetworkList.setSelectedValue(rootNames.get(0));
		
		// initialize target attribute list
		List<String> colNames_target = new ArrayList<String>();
		colNames_target.add("shared name");
		this.targetColumnList = new ListSingleSelection<String>(colNames_target);
		targetColumnList.setSelectedValue("shared name");
	
		// initialize source attribute list
		List<String> colNames_source = new ArrayList<String>();
		colNames_source.add("shared name");
		this.sourceColumnList = new ListSingleSelection<String>(colNames_source);
		
		parser = new PsiMiTabParser(is, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);

	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		this.parentTaskMonitor = taskMonitor;
		
		// support to add network into existing collection
		this.initNodeMap();
		CyRootNetwork rootNetwork = getRootNetwork();
		
		parser.setRootNetwork(rootNetwork);
		parser.setNodeMap(this.nMap);
		
		try {
			createNetwork(taskMonitor);
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
	}

	private void createNetwork(TaskMonitor taskMonitor) throws IOException {
		taskMonitor.setTitle("Loading PSIMI-TAB File");
		taskMonitor.setStatusMessage("Loading PSI-MI-TAB25 file.");
		taskMonitor.setProgress(0.01d);

		network = parser.parse(taskMonitor);

		taskMonitor.setProgress(1.0d);
	}

	@Override
	public CyNetwork[] getNetworks() {
		return new CyNetwork[] { network };
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {

		final CyNetworkView view = cyNetworkViewFactory.createNetworkView(network);

		String pref = CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME;
		if (prop != null)
			pref = prop.getProperties().getProperty("preferredLayoutAlgorithm", pref);

		final CyLayoutAlgorithm layout = layouts.getLayout(pref);
		// Force to run this task here to avoid concurrency problem.
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(),
				CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
		Task nextTask = itr.next();
		try {
			nextTask.run(parentTaskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		return view;
	}

	@Override
	public void cancel() {
		parser.cancel();
	}
}
