package org.cytoscape.tableimport.internal.task;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;



public class ImportNoGuiNetworkReaderFactory extends AbstractTaskFactory {

	private final boolean fromURL;
	private final CyServiceRegistrar serviceRegistrar;
	public static final String JSON_EXAMPLE = "{ \"networks\":[101,102,103],\"views\":[200,201,204] }";


	public ImportNoGuiNetworkReaderFactory(final boolean fromURL, final CyServiceRegistrar serviceRegistrar) {
		this.fromURL = fromURL;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		final LoadNetworkReaderTask readerTask = new LoadNetworkReaderTask(serviceRegistrar, true);
		final NetworkCollectionHelper networkCollectionHelperTask = new NetworkCollectionHelper(readerTask, serviceRegistrar);
		final GenerateNetworkViewsTask generateViewTask = new GenerateNetworkViewsTask(readerTask);

		if (fromURL) {
			return new TaskIterator(new SelectURLTableTask(readerTask, serviceRegistrar), networkCollectionHelperTask,
					readerTask, generateViewTask);
		} else {
			return new TaskIterator(new SelectFileTableTask(readerTask, serviceRegistrar),
					networkCollectionHelperTask, readerTask, generateViewTask);
		}
	}

	class GenerateNetworkViewsTask extends AbstractTask implements ObservableTask {

		private String name;
		private final CyNetworkReader netReader;
		private int viewThreshold;
		private	List<CyNetworkView> results;
		private List<CyNetwork> largeNetworks;

		private final String VIEW_THRESHOLD = "viewThreshold";
		private static final int DEF_VIEW_THRESHOLD = 3000;

		public GenerateNetworkViewsTask(final CyNetworkReader netReader) {
			name = null;
			this.netReader = netReader;
		}

		@Override
		public void run(final TaskMonitor taskMonitor) throws Exception {
			taskMonitor.setProgress(0.0);

			final VisualMappingManager vmManager = serviceRegistrar.getService(VisualMappingManager.class);
			final CyNetworkNaming netNaming = serviceRegistrar.getService(CyNetworkNaming.class);
			final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
			final CyNetworkViewManager netViewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
			final CyNetworkViewFactory nullNetViewFactory =
					serviceRegistrar.getService(CyNetworkViewFactory.class, "(id=NullCyNetworkViewFactory)");

			final VisualStyle style = vmManager.getCurrentVisualStyle(); // get the current style before registering the views!
			final CyNetwork[] networks = netReader.getNetworks();
			double numNets = (double)(networks.length);
			int i = 0;

			if (netReader instanceof LoadNetworkReaderTask)
				name = ((LoadNetworkReaderTask)netReader).getName();

			viewThreshold = getViewThreshold();
			results = new ArrayList<CyNetworkView>();

			for (CyNetwork network : networks) {
				// Use original name if exists
				String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);

				if (networkName == null || networkName.trim().length() == 0) {
					networkName = name;

					if (networkName == null)
						networkName = "? (Name is missing)";

					network.getRow(network).set(CyNetwork.NAME, netNaming.getSuggestedNetworkTitle(networkName));
				}

				netManager.addNetwork(network, false);

				final int numGraphObjects = network.getNodeCount() + network.getEdgeCount();

				if (numGraphObjects < viewThreshold) {
					final CyNetworkView view = netReader.buildCyNetworkView(network);
					netViewManager.addNetworkView(view, false);
					vmManager.setVisualStyle(style, view);
					style.apply(view);

					if (!view.isSet(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)
							&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)
							&& !view.isSet(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION))
						view.fitContent();
					results.add(view);
				} else {
					results.add(nullNetViewFactory.createNetworkView(network));
					largeNetworks.add(network);
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
						rootNet.getRow(rootNet).set(
								CyNetwork.NAME, networks[0].getRow(networks[0]).get(CyNetwork.NAME, String.class));
					}
				}
			}


			// Make sure rootNetwork has a name
			for (CyNetwork network : networks) {
				if (network instanceof CySubNetwork){
					CySubNetwork subNet = (CySubNetwork) network;
					CyRootNetwork rootNet = subNet.getRootNetwork();
					String networkName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);

					if (networkName == null || networkName.trim().length() == 0) {
						networkName = name;

						if (networkName == null)
							networkName = "? (Name is missing)";

						rootNet.getRow(rootNet).set(CyNetwork.NAME, netNaming.getSuggestedNetworkTitle(networkName));
					}
				}
			}

			final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);

			if (!results.isEmpty()) {
				applicationManager.setCurrentNetworkView(results.iterator().next());
			} else {
				for (CyNetwork net : networks) {
					if (net instanceof CySubNetwork) {
						applicationManager.setCurrentNetwork(net);
						break;
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		private int getViewThreshold() {
			final CyProperty<Properties> cyProperties =
					serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
			final String vts = cyProperties.getProperties().getProperty(VIEW_THRESHOLD);
			int threshold;

			try {
				threshold = Integer.parseInt(vts);
			} catch (Exception e) {
				threshold = DEF_VIEW_THRESHOLD;
			}

			return threshold;
		}

		private Object getStringResults() {
			String strRes = "";

			for (CyNetworkView view: results)
				strRes += (view.toString() + "\n");

			return strRes.isEmpty() ? null : strRes.substring(0, strRes.length()-1);
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Object getResults(Class expectedType) {
			if (expectedType.equals(String.class))
				return getStringResults();
			else if (expectedType.equals(JSONResult.class)) {
				JSONResult res = () -> {if (results == null && largeNetworks.isEmpty()) 
					return "{}";
				else {
					CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
					List<CyNetwork> networks = new ArrayList<>();
					for (CyNetworkView view: results)
						networks.add(view.getModel());
					for (CyNetwork net: largeNetworks) {
						if (!networks.contains(net))
							networks.add(net);
					}
					String jsonRes = "{ \"networks\":";
					jsonRes += cyJSONUtil.cyIdentifiablesToJson(networks);
					jsonRes += ", \"views\":";
					jsonRes += cyJSONUtil.cyIdentifiablesToJson(results);
					return jsonRes;
				}};
				return res;
			}
			return results;
		}
		
		@Override
		public List<Class<?>> getResultClasses() {
			return Arrays.asList(List.class, String.class, JSONResult.class);
		}
	}
}
