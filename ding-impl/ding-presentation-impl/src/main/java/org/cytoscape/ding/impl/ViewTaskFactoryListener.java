package org.cytoscape.ding.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyNetworkViewContextMenuFactory;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class ViewTaskFactoryListener {

	private final Map<NodeViewTaskFactory, Map<String,String>> nodeViewTFs;
	private final Map<EdgeViewTaskFactory, Map<String,String>> edgeViewTFs;
	private final Map<NetworkViewTaskFactory, Map<String,String>> emptySpaceTFs;
	private final Map<NetworkViewLocationTaskFactory, Map<String,String>> networkViewLocationTFs;
	private final Map<CyNodeViewContextMenuFactory, Map<String,String>> cyNodeViewContexMenuFactory;
	private final Map<CyEdgeViewContextMenuFactory, Map<String,String>> cyEdgeViewContextMenuFactory;
	private final Map<CyNetworkViewContextMenuFactory, Map<String,String>> cyNetworkViewContextMenuFactory;
	
	private final NVLTFActionSupport nvltfActionSupport;

	public ViewTaskFactoryListener(NVLTFActionSupport nvltfActionSupport) {
		this.nvltfActionSupport = nvltfActionSupport;
		nodeViewTFs = new ConcurrentHashMap<>(16, 0.75f, 2);
		edgeViewTFs = new ConcurrentHashMap<>(16, 0.75f, 2);
		emptySpaceTFs = new ConcurrentHashMap<>(16, 0.75f, 2);
		networkViewLocationTFs = new ConcurrentHashMap<>(16, 0.75f, 2);
		cyNodeViewContexMenuFactory = new ConcurrentHashMap<>(16, 0.75f, 2);
		cyEdgeViewContextMenuFactory = new ConcurrentHashMap<>(16, 0.75f, 2);
		cyNetworkViewContextMenuFactory = new ConcurrentHashMap<>(16, 0.75f, 2);
	}

	
	
	public Map<NodeViewTaskFactory, Map<String,String>> getNodeViewTaskFactoryMap() {
		return nodeViewTFs;
	}
	
	public Map<EdgeViewTaskFactory, Map<String,String>> getEdgeViewTaskFactoryMap() {
		return edgeViewTFs;
	}
	
	public Map<NetworkViewTaskFactory, Map<String,String>> getEmptySpaceTaskFactoryMap() {
		return emptySpaceTFs;
	}
	
	public Map<NetworkViewLocationTaskFactory, Map<String,String>> getNetworkViewLocationTaskFactoryMap() {
		return networkViewLocationTFs;
	}
	
	public Map<CyNodeViewContextMenuFactory, Map<String,String>> getCyNodeViewContextMenuFactoryMap() {
		return cyNodeViewContexMenuFactory;
	}
	
	public Map<CyEdgeViewContextMenuFactory, Map<String,String>> getCyEdgeViewContextMenuFactoryMap() {
		return cyEdgeViewContextMenuFactory;
	}
	
	public Map<CyNetworkViewContextMenuFactory, Map<String,String>> getCyNetworkViewContextMenuFactoryMap() {
		return cyNetworkViewContextMenuFactory;
	}
	
	
	
	public void addNodeViewTaskFactory(NodeViewTaskFactory nvtf, Map<String,String> props) {
		if (nvtf != null)
			nodeViewTFs.put(nvtf, props);
	}

	public void removeNodeViewTaskFactory(NodeViewTaskFactory nvtf, Map<String,String> props) {
		if (nvtf != null)
			nodeViewTFs.remove(nvtf);
	}

	public void addEdgeViewTaskFactory(EdgeViewTaskFactory evtf, Map<String,String> props) {
		if (evtf != null)
			edgeViewTFs.put(evtf, props);
	}

	public void removeEdgeViewTaskFactory(EdgeViewTaskFactory evtf, Map<String,String> props) {
		if (evtf != null)
			edgeViewTFs.remove(evtf);
	}

	public void addNetworkViewTaskFactory(NetworkViewTaskFactory evtf, Map<String,String> props) {
		if (evtf != null)
			emptySpaceTFs.put(evtf, props);
	}

	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory evtf, Map<String,String> props) {
		if (evtf != null)
			emptySpaceTFs.remove(evtf);
	}

	public void addNetworkViewLocationTaskFactory(NetworkViewLocationTaskFactory nvltf, Map<String,String> props){
		if(nvltf != null) {
			networkViewLocationTFs.put(nvltf, props);
			nvltfActionSupport.registerAction(nvltf,props);
		}
	}
	
	public void removeNetworkViewLocationTaskFactory(NetworkViewLocationTaskFactory nvltf, Map<String,String> props){
		if(nvltf != null)
			networkViewLocationTFs.remove(nvltf);
	}
	
	public void addCyNodeViewContextMenuFactory(CyNodeViewContextMenuFactory cnvcmf, Map<String,String> props){
		if(cnvcmf != null)
			cyNodeViewContexMenuFactory.put(cnvcmf, props);
	}
	
	public void removeCyNodeViewContextMenuFactory(CyNodeViewContextMenuFactory cnvcmf, Map<String,String> props){
		if(cnvcmf != null)
			cyNodeViewContexMenuFactory.remove(cnvcmf);
	}
	
	public void addCyEdgeViewContextMenuFactory(CyEdgeViewContextMenuFactory cevcmf, Map<String,String> props){
		if(cevcmf != null)
			cyEdgeViewContextMenuFactory.put(cevcmf, props);
	}
	
	public void removeCyEdgeViewContextMenuFactory(CyEdgeViewContextMenuFactory cevcmf, Map<String,String> props){
		if(cevcmf != null)
			cyEdgeViewContextMenuFactory.remove(cevcmf);
	}
	
	public void addCyNetworkViewContextMenuFactory(CyNetworkViewContextMenuFactory cnvcmf, Map<String,String> props){
		if(cnvcmf != null)
			cyNetworkViewContextMenuFactory.put(cnvcmf, props);
	}
	
	public void removeCyNetworkViewContextMenuFactory(CyNetworkViewContextMenuFactory cnvcmf, Map<String,String> props){
		if(cnvcmf != null)
		cyNetworkViewContextMenuFactory.remove(cnvcmf);
	}
}
