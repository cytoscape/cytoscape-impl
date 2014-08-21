package org.cytoscape.welcome.internal.task;

import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.welcome.internal.style.IntActXGMMLVisualStyleBuilder;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import java.util.List;

/**
 * Created by David Welker on 8/12/14
 * Copyright © 2014. All rights reserved.
 */
public class BuildNetworkBasedOnGenesTaskFactory extends AbstractTaskFactory
{
	private final CyNetworkReaderManager networkReaderManager;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewFactory networkViewFactory;
	private final CyLayoutAlgorithmManager layoutAlgorithmManager;
	private final VisualMappingManager visualMappingManager;
	private final CyNetworkViewManager networkViewManager;
	private final IntActXGMMLVisualStyleBuilder intActVSBuilder;
	private final String species;
	private final List<String> geneNames;

	public BuildNetworkBasedOnGenesTaskFactory(CyNetworkReaderManager networkReaderManager, CyNetworkManager networkManager, CyNetworkViewFactory networkViewFactory, CyLayoutAlgorithmManager layoutAlgorithmManager, VisualMappingManager visualMappingManager, CyNetworkViewManager networkViewManager, IntActXGMMLVisualStyleBuilder intActVSBuilder, String species, List<String> geneNames)
	{
		this.networkReaderManager = networkReaderManager;
		this.networkManager = networkManager;
		this.networkViewFactory = networkViewFactory;
		this.layoutAlgorithmManager = layoutAlgorithmManager;
		this.visualMappingManager = visualMappingManager;
		this.networkViewManager = networkViewManager;
		this.intActVSBuilder = intActVSBuilder;

		this.species = species;
		this.geneNames = geneNames;
	}

	@Override
	public TaskIterator createTaskIterator()
	{
		AbstractTask task = new BuildNetworkBasedOnGenesTask(networkReaderManager, networkManager, networkViewFactory, layoutAlgorithmManager, visualMappingManager, networkViewManager, intActVSBuilder, species, geneNames);
		return new TaskIterator(task);
	}
}
