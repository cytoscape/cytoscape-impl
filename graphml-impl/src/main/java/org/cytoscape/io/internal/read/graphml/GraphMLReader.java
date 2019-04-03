package org.cytoscape.io.internal.read.graphml;

/*
 * #%L
 * Cytoscape GraphML Impl (graphml-impl)
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.group.CyGroupSettingsManager.GroupViewType;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/*
 * @author kozo.nishida
 */
public class GraphMLReader extends AbstractCyNetworkReader {

	private InputStream inputStream;
	private final CyLayoutAlgorithmManager layouts;
	private final CyRootNetworkManager cyRootNetworkManager;
	
	private final CyGroupFactory cyGroupFactory;
	private final CyGroupManager cyGroupManager;
	private final CyGroupSettingsManager cyGroupSettings;
	
	private final List<CyGroup> cyGroups;

	private GraphMLParser parser;
	private TaskMonitor taskMonitor;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public GraphMLReader(final InputStream inputStream,
						 final CyLayoutAlgorithmManager layouts,
						 final CyApplicationManager cyApplicationManager,
						 final CyNetworkFactory cyNetworkFactory,
						 final CyNetworkManager cyNetworkManager,
						 final CyRootNetworkManager cyRootNetworkManager,
						 final CyServiceRegistrar cyServiceRegistrar) {
		super(inputStream, cyApplicationManager, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		
		if (inputStream == null)
			throw new NullPointerException("Input stream is null");

		this.inputStream = inputStream;
		this.layouts = layouts;
		this.cyRootNetworkManager = cyRootNetworkManager;
		
		this.cyGroupFactory = cyServiceRegistrar.getService(CyGroupFactory.class);
		this.cyGroupManager = cyServiceRegistrar.getService(CyGroupManager.class);
		this.cyGroupSettings = cyServiceRegistrar.getService(CyGroupSettingsManager.class);
		this.cyGroups = new ArrayList<>();
	}

	@Override
	public CyNetwork[] getNetworks() {
		if (parser == null)
			throw new IllegalStateException("Parser is not initialized.");
		
		return parser.getCyNetworks();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		
		try {
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			final SAXParser sp = spf.newSAXParser();
			final XMLReader xmlReader = sp.getXMLReader();
			
			CyRootNetwork root = getRootNetwork();
			final CySubNetwork newNetwork;
			
			if (root != null) {
				newNetwork = root.addSubNetwork();
			} else {
				// Need to create new network with new root.
				newNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
				root = newNetwork.getRootNetwork();
			}
			
			parser = new GraphMLParser(taskMonitor, cyNetworkFactory, cyRootNetworkManager, root, newNetwork);
			xmlReader.setContentHandler(parser);
			final InputSource inputSource = new InputSource(inputStream);
			inputSource.setEncoding("UTF-8");
			xmlReader.parse(inputSource);
			
			createGroups(root, newNetwork);
			
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
					inputStream = null;
				} catch (Exception e) {
					logger.warn("Cannot close GraphML input stream", e);
				}
			}
		}
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);
		
		// now that a view exists, ensure that the groups are rendered as a compound node
		// because it's more intuitive than the other options
		for (CyGroup group: cyGroups) {
			// HACK: need to cause a settings change in order for the view change to stick
			cyGroupSettings.setGroupViewType(group, GroupViewType.SINGLENODE);
			cyGroupSettings.setGroupViewType(group, GroupViewType.COMPOUND);
		}
		
		final CyLayoutAlgorithm layout = layouts.getDefaultLayout();
		String attribte = layouts.getLayoutAttribute(layout, view);
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(),CyLayoutAlgorithm.ALL_NODE_VIEWS, attribte);
		Task nextTask = itr.next();
		try {
			nextTask.run(taskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		return view;
	}
	
	private void createGroups(final CyRootNetwork root, final CySubNetwork net) {
		
		for (CySubNetwork subnet : root.getSubNetworkList()) {
			if (subnet == net) {
				continue;
			}
			
			// create the group, add it to the base network
			CyGroup group = cyGroupFactory.createGroup(net, subnet.getNodeList(), null, true);
			CyNode groupNode = group.getGroupNode();
			net.addNode(groupNode);
			cyGroups.add(group);
			
			// give each group a name
			String groupName = subnet.getRow(subnet).get(CyNetwork.NAME, String.class);
			
			// set shared name and name properties
			CyRow sharedRow = root.getRow(group.getGroupNode(), CyRootNetwork.SHARED_ATTRS);
			sharedRow.set(CyRootNetwork.SHARED_NAME, groupName);
			net.getRow(groupNode).set(CyNetwork.NAME, groupName);
		}
	}
}
