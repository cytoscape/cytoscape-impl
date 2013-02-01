package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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


import java.lang.ref.Reference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyNetworkViewContextMenuFactory;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;

public class ViewTaskFactoryListener {

	final Map<CyNetworkView, Reference<DGraphView>> viewMap;
	final Map<NodeViewTaskFactory, Map> nodeViewTFs;
	final Map<EdgeViewTaskFactory, Map> edgeViewTFs;
	final Map<NetworkViewTaskFactory, Map> emptySpaceTFs;
	final Map<NetworkViewLocationTaskFactory, Map> networkViewLocationTFs;
	final Map<CyNodeViewContextMenuFactory, Map> cyNodeViewContexMenuFactory;
	final Map<CyEdgeViewContextMenuFactory, Map> cyEdgeViewContextMenuFactory;
	final Map<CyNetworkViewContextMenuFactory, Map> cyNetworkViewContextMenuFactory;
	
	private final NVLTFActionSupport nvltfActionSupport;

	public ViewTaskFactoryListener(NVLTFActionSupport nvltfActionSupport){
		this.nvltfActionSupport = nvltfActionSupport;
		viewMap = new WeakHashMap<CyNetworkView, Reference<DGraphView>>();
		nodeViewTFs = new HashMap<NodeViewTaskFactory, Map>();
		edgeViewTFs = new HashMap<EdgeViewTaskFactory, Map>();
		emptySpaceTFs = new HashMap<NetworkViewTaskFactory, Map>();
		networkViewLocationTFs = new HashMap<NetworkViewLocationTaskFactory, Map>();
		cyNodeViewContexMenuFactory = new HashMap<CyNodeViewContextMenuFactory, Map>();
		cyEdgeViewContextMenuFactory = new HashMap<CyEdgeViewContextMenuFactory, Map>();
		cyNetworkViewContextMenuFactory = new HashMap<CyNetworkViewContextMenuFactory, Map>();
	}

	
	public void addNodeViewTaskFactory(NodeViewTaskFactory nvtf, Map props) {
		if (nvtf == null)
			return;

		nodeViewTFs.put(nvtf, props);
	}

	public void removeNodeViewTaskFactory(NodeViewTaskFactory nvtf, Map props) {
		if (nvtf == null)
			return;

		nodeViewTFs.remove(nvtf);
	}

	public void addEdgeViewTaskFactory(EdgeViewTaskFactory evtf, Map props) {
		if (evtf == null)
			return;

		edgeViewTFs.put(evtf, props);
	}

	public void removeEdgeViewTaskFactory(EdgeViewTaskFactory evtf, Map props) {
		if (evtf == null)
			return;

		edgeViewTFs.remove(evtf);
	}

	public void addNetworkViewTaskFactory(NetworkViewTaskFactory evtf, Map props) {
		if (evtf == null)
			return;

		emptySpaceTFs.put(evtf, props);
	}

	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory evtf,
			Map props) {
		if (evtf == null)
			return;

		emptySpaceTFs.remove(evtf);
	}

	public void addNetworkViewLocationTaskFactory(NetworkViewLocationTaskFactory nvltf, Map props){
		if(nvltf == null)
			return;
		networkViewLocationTFs.put(nvltf, props);
		nvltfActionSupport.registerAction(nvltf,props);
	}
	
	public void removeNetworkViewLocationTaskFactory(NetworkViewLocationTaskFactory nvltf, Map props){
		if(nvltf == null)
			return;
		networkViewLocationTFs.remove(nvltf);
	}
	
	public void addCyNodeViewContextMenuFactory(CyNodeViewContextMenuFactory cnvcmf, Map props){
		if(cnvcmf == null)
			return;
		cyNodeViewContexMenuFactory.put(cnvcmf, props);
	}
	
	public void removeCyNodeViewContextMenuFactory(CyNodeViewContextMenuFactory cnvcmf, Map props){
		if(cnvcmf == null)
			return;
		cyNodeViewContexMenuFactory.remove(cnvcmf);
	}
	
	public void addCyEdgeViewContextMenuFactory(CyEdgeViewContextMenuFactory cevcmf, Map props){
		if(cevcmf == null)
			return;
		cyEdgeViewContextMenuFactory.put(cevcmf, props);
	}
	
	public void removeCyEdgeViewContextMenuFactory(CyEdgeViewContextMenuFactory cevcmf, Map props){
		if(cevcmf == null)
			return;
		cyEdgeViewContextMenuFactory.remove(cevcmf);
	}
	
	public void addCyNetworkViewContextMenuFactory(CyNetworkViewContextMenuFactory cnvcmf, Map props){
		if(cnvcmf == null)
			return;
		cyNetworkViewContextMenuFactory.put(cnvcmf, props);
	}
	
	public void removeCyNetworkViewContextMenuFactory(CyNetworkViewContextMenuFactory cnvcmf, Map props){
		if(cnvcmf == null)
			return;
		cyNetworkViewContextMenuFactory.remove(cnvcmf);
	}
}
