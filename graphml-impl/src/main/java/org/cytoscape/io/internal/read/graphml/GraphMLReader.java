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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/*
 * @author kozo.nishida
 */
public class GraphMLReader extends AbstractCyNetworkReader {

	private InputStream inputStream;
	private final CyLayoutAlgorithmManager layouts;
	private final CyRootNetworkManager cyRootNetworkManager;

	private GraphMLParser parser;
	private TaskMonitor taskMonitor;

	public GraphMLReader(final InputStream inputStream,
						 final CyLayoutAlgorithmManager layouts,
						 final CyApplicationManager cyApplicationManager,
						 final CyNetworkFactory cyNetworkFactory,
						 final CyNetworkManager cyNetworkManager,
						 final CyRootNetworkManager cyRootNetworkManager) {
		super(inputStream, cyApplicationManager, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		if (inputStream == null)
			throw new NullPointerException("Input stream is null");

		this.inputStream = inputStream;
		this.layouts = layouts;
		this.cyRootNetworkManager = cyRootNetworkManager;
	}

	@Override
	public CyNetwork[] getNetworks() {
		if(parser == null)
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
			CySubNetwork newNetwork = null;
			if(root== null) {
				newNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
				root = cyRootNetworkManager.getRootNetwork(newNetwork);
			}
			
			parser = new GraphMLParser(taskMonitor, cyNetworkFactory, cyRootNetworkManager, root, newNetwork);
			xmlReader.setContentHandler(parser);
			final InputSource inputSource = new InputSource(inputStream);
			inputSource.setEncoding("UTF-8");
			xmlReader.parse(inputSource);
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);

		final CyLayoutAlgorithm layout = layouts.getDefaultLayout();
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(),CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
		Task nextTask = itr.next();
		try {
			nextTask.run(taskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		return view;
	}
}
