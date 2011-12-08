package org.cytoscape.task.internal.quickstart;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

///////
public class GetNetworkPreviewDataTask extends AbstractTask {
	private final ImportTaskUtil util;
	private String[][]previewData;
	
	public GetNetworkPreviewDataTask(ImportTaskUtil util, String[][] previewData){
		this.util = util;
		this.previewData = previewData;
	}
	
	@Override
	public void run(TaskMonitor monitor) throws Exception {
		monitor.setProgress(0.0);
		//
		CyNetwork net = this.util.getAppManager().getCurrentNetwork();
		if (net == null || net.getNodeCount() == 0){
			this.previewData = null;
		}
		
		// Get first 20 node names from a network
		int nodeCount = 20;
		if (net.getNodeCount() < 20){
			nodeCount = net.getNodeCount();
		}
		monitor.setProgress(0.3);
		List<CyNode> nodeList = net.getNodeList();
		
		for (int i=0; i<nodeCount; i++ ){
			CyNode node = nodeList.get(i);
			String nodeName = net.getCyRow(node).get("name", String.class);
			previewData[i][0]= nodeName; 
		}
		monitor.setProgress(1.0);
	}
}
