package org.cytoscape.welcome.internal.task;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.welcome.internal.style.IntActXGMMLVisualStyleBuilder;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Created by David Welker on 8/14/14
 * Copyright © 2014. All rights reserved.
 */
public class ShowBuiltNetworkTask extends AbstractTask
{
	private final CyNetworkReader reader;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewFactory networkViewFactory;
	private final CyLayoutAlgorithmManager layoutAlgorithmManager;
	private final VisualMappingManager visualMappingManager;
	private final CyNetworkViewManager networkViewManager;
	private final IntActXGMMLVisualStyleBuilder intActVSBuilder;

	public ShowBuiltNetworkTask(CyNetworkReader reader, CyNetworkManager networkManager, CyNetworkViewFactory networkViewFactory, CyLayoutAlgorithmManager layoutAlgorithmManager, VisualMappingManager visualMappingManager, CyNetworkViewManager networkViewManager, IntActXGMMLVisualStyleBuilder intActVSBuilder)
	{
		this.reader = reader;
		this.networkManager = networkManager;
		this.networkViewFactory = networkViewFactory;
		this.layoutAlgorithmManager = layoutAlgorithmManager;
		this.visualMappingManager = visualMappingManager;
		this.networkViewManager = networkViewManager;
		this.intActVSBuilder = intActVSBuilder;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception
	{
		CyNetwork[] networks = reader.getNetworks();
		for( int i = 0; i < networks.length; i++ )
		{
			CyNetwork network = networks[i];
			renameColumns(network);

			networkManager.addNetwork(network);
			CyNetworkView view = networkViewFactory.createNetworkView(network);
			CyLayoutAlgorithm layoutAlgorithm = layoutAlgorithmManager.getLayout("force-directed");

			insertTasksAfterCurrentTask(layoutAlgorithm.createTaskIterator(view, layoutAlgorithm.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, ""));

			// Check Visual Style exists or not
			VisualStyle psiStyle = null;
			for (VisualStyle style : visualMappingManager.getAllVisualStyles()) {
				if (style.getTitle().equals(IntActXGMMLVisualStyleBuilder.DEF_VS_NAME)) {
					psiStyle = style;
					break;
				}
			}
			if (psiStyle == null) {
				psiStyle = intActVSBuilder.getVisualStyle();
				visualMappingManager.addVisualStyle(psiStyle);
			}
			visualMappingManager.setVisualStyle(psiStyle, view);

			psiStyle.apply(view);
			view.updateView();
			networkViewManager.addNetworkView(view);
		}


	}

	private void renameColumns(CyNetwork network)
	{
		CyTable nodeTable = network.getDefaultNodeTable();

		nodeTable.getColumn("gene name").setName("Human Readable Label");
		nodeTable.getColumn("type.psi-mi").setName("Interactor Type");
		nodeTable.getColumn("taxid_identifier").setName("Taxonomy ID");

		CyTable edgeTable = network.getDefaultEdgeTable();
		edgeTable.getColumn("detmethod.psi-mi").setName("Detection Method");
		edgeTable.getColumn("type.psi-mi").setName("Primary Interaction Type");


	}
}
