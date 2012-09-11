package org.cytoscape.io.internal.read;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public abstract class AbstractNetworkReader extends AbstractTask implements CyNetworkReader {

	protected CyNetwork[] cyNetworks;

	protected VisualStyle[] visualstyles;
	protected InputStream inputStream;

	protected final CyNetworkViewFactory cyNetworkViewFactory;
	protected final CyNetworkFactory cyNetworkFactory;

	protected final CyNetworkManager cyNetworkManager;;
	protected final CyRootNetworkManager cyRootNetworkManager;

	@Tunable(description = "Please choose a network collection:")
	public ListSingleSelection<Object> networkCollection;

	protected HashMap<String, CyRootNetwork> name2RootMap;

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

		this.name2RootMap = ReadUtils.getRootNetworkMap(this.cyNetworkManager, this.cyRootNetworkManager);
		
		Iterator<String> it = name2RootMap.keySet().iterator();
		
		final List<Object> networkList = new ArrayList<Object>();
		networkList.add("new Collection");
		
		while (it.hasNext()){
			networkList.add(it.next());			
		}
		networkCollection = new ListSingleSelection<Object>(networkList);
	}

	@Override
	public CyNetwork[] getNetworks() {
		return cyNetworks;
	}
}
