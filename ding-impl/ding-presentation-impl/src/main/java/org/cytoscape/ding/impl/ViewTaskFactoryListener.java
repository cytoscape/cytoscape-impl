package org.cytoscape.ding.impl;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;

public class ViewTaskFactoryListener {

	final Map<CyNetworkView, DGraphView> viewMap;
	final Map<NodeViewTaskFactory, Map> nodeViewTFs;
	final Map<EdgeViewTaskFactory, Map> edgeViewTFs;
	final Map<NetworkViewTaskFactory, Map> emptySpaceTFs;
	final Map<NetworkViewLocationTaskFactory, Map> networkViewLocationTFs;


	public ViewTaskFactoryListener(){
		viewMap = new HashMap<CyNetworkView, DGraphView>();
		nodeViewTFs = new HashMap<NodeViewTaskFactory, Map>();
		edgeViewTFs = new HashMap<EdgeViewTaskFactory, Map>();
		emptySpaceTFs = new HashMap<NetworkViewTaskFactory, Map>();
		networkViewLocationTFs = new HashMap<NetworkViewLocationTaskFactory, Map>();

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
	}
	
	public void removeNetworkViewLocationTaskFactory(NetworkViewLocationTaskFactory nvltf, Map props){
		if(nvltf == null)
			return;
		networkViewLocationTFs.remove(nvltf);
	}
}
