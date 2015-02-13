package org.cytoscape.tableimport.internal;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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



import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.tableimport.internal.ui.theme.IconManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;



public class ImportNoGuiNetworkReaderFactory extends AbstractTaskFactory {
	
	protected final StreamUtil streamUtil;
	private boolean fromURL;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkNaming namingUtil;
	private final VisualMappingManager vmm;
	private final CyNetworkViewFactory nullNetworkViewFactory;
	private final CyNetworkReaderManager networkReaderManager;
	private final IconManager iconManager;
	private Properties props;

	/**
	 * Creates a new ImportAttributeTableReaderFactory object.
	 */
	public ImportNoGuiNetworkReaderFactory(final StreamUtil streamUtil, boolean fromURL,
			final CyNetworkManager networkManager, final CyNetworkViewManager networkViewManager, CyProperty<Properties> props,
			final CyNetworkNaming namingUtil, final VisualMappingManager vmm,final CyNetworkViewFactory nullNetworkViewFactory,
			final CyNetworkReaderManager networkReaderManager, final IconManager iconManager) {
		this.streamUtil = streamUtil;
		this.fromURL = fromURL;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.namingUtil = namingUtil;
		this.vmm = vmm;
		this.nullNetworkViewFactory = nullNetworkViewFactory;
		this.networkReaderManager = networkReaderManager;
		this.iconManager = iconManager;
		this.props = props.getProperties();
	}

	@Override
	public TaskIterator createTaskIterator() {
		LoadNetworkReaderTask readerTask = new LoadNetworkReaderTask(networkReaderManager);
		NetworkCollectionHelper networkCollectionHelperTask = new NetworkCollectionHelper(readerTask);
		GenerateNetworkViewsTask generateViewTask = new GenerateNetworkViewsTask(readerTask,networkManager,networkViewManager,props,namingUtil,vmm,nullNetworkViewFactory);
		
		if (fromURL) {
			return new TaskIterator(new SelectURLTableTask(readerTask,streamUtil,iconManager),networkCollectionHelperTask,readerTask,generateViewTask);
		} else {
			return new TaskIterator(new SelectFileTableTask(readerTask,streamUtil,iconManager),networkCollectionHelperTask,readerTask,generateViewTask);
		}
	}
	
	class GenerateNetworkViewsTask extends AbstractTask  {
		private String name;
		private final CyNetworkReader viewReader;
		private final CyNetworkManager networkManager;
		private final CyNetworkViewManager networkViewManager;
		private final CyNetworkNaming namingUtil;
		private int viewThreshold;
		private final VisualMappingManager vmm;
		private final CyNetworkViewFactory nullNetworkViewFactory;
		private	Collection<CyNetworkView> results;
		private Properties props;
		
		private final String VIEW_THRESHOLD = "viewThreshold";
		private static final int DEF_VIEW_THRESHOLD = 3000;

		public GenerateNetworkViewsTask( final CyNetworkReader viewReader,
					final CyNetworkManager networkManager, final CyNetworkViewManager networkViewManager,final Properties props,
					final CyNetworkNaming namingUtil, final VisualMappingManager vmm,
					final CyNetworkViewFactory nullNetworkViewFactory) {
			name = null;
			this.viewReader = viewReader;
			this.networkManager = networkManager;
			this.networkViewManager = networkViewManager;
			this.namingUtil = namingUtil;
			this.vmm = vmm;
			this.nullNetworkViewFactory = nullNetworkViewFactory;
			this.props = props;
		}

		public void run(final TaskMonitor taskMonitor) throws Exception {
			taskMonitor.setProgress(0.0);

			final VisualStyle style = vmm.getCurrentVisualStyle(); // get the current style before registering the views!
			final CyNetwork[] networks = viewReader.getNetworks();
			double numNets = (double)(networks.length);
			int i = 0;
			
			if(viewReader instanceof LoadNetworkReaderTask)
				name = ((LoadNetworkReaderTask)viewReader).getName();
			
			viewThreshold = getViewThreshold();

			results = new ArrayList<CyNetworkView>();
			for (CyNetwork network : networks) {
				// Use original name if exists
				String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
				if (networkName == null || networkName.trim().length() == 0) {
					networkName = name;
					if (networkName == null)
						networkName = "? (Name is missing)";
					
					network.getRow(network).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(networkName));
				}
				networkManager.addNetwork(network);

				final int numGraphObjects = network.getNodeCount() + network.getEdgeCount();
				if (numGraphObjects < viewThreshold) {
					final CyNetworkView view = viewReader.buildCyNetworkView(network);
					networkViewManager.addNetworkView(view);
					vmm.setVisualStyle(style, view);
					style.apply(view);
					
					if (!view.isSet(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)
							&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)
							&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION))
						view.fitContent();
					results.add(view);
				} else {
					results.add(nullNetworkViewFactory.createNetworkView(network));
				}
				taskMonitor.setProgress((double)(++i)/numNets);
			}

			// If this is a subnetwork, and there is only one subnetwork in the root, check the name of the root network
			// If there is no name yet for the root network, set it the same as its base subnetwork
			if (networks.length == 1){
				if (networks[0] instanceof CySubNetwork){
					CySubNetwork subnet = (CySubNetwork) networks[0];
					final CyRootNetwork rootNet = subnet.getRootNetwork();
					String rootNetName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
					if (rootNetName == null || rootNetName.trim().length() == 0){
						// The root network does not have a name yet, set it the same as the base subnetwork
						rootNet.getRow(rootNet).set(CyNetwork.NAME, networks[0].getRow(networks[0]).get(CyNetwork.NAME, String.class));
					}
				}
			}
			
			
			// Make sure rootNetwork has a name
			for (CyNetwork network : networks) {

				if (network instanceof CySubNetwork){
					CySubNetwork subNet = (CySubNetwork) network;
					CyRootNetwork rootNet = subNet.getRootNetwork();

					String networkName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
					if(networkName == null || networkName.trim().length() == 0) {
						networkName = name;
						if(networkName == null)
							networkName = "? (Name is missing)";
						
						rootNet.getRow(rootNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(networkName));
					}
				}			
			}
		}
		
		private int getViewThreshold() {
			final String vts = props.getProperty(VIEW_THRESHOLD);
			int threshold;
			try {
				threshold = Integer.parseInt(vts);
			} catch (Exception e) {
				threshold = DEF_VIEW_THRESHOLD;
			}

			return threshold;
		}

	}
	
	
}
