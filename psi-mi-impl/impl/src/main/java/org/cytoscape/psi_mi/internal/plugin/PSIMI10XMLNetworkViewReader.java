package org.cytoscape.psi_mi.internal.plugin;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.psi_mi.internal.cyto_mapper.MapToCytoscape;
import org.cytoscape.psi_mi.internal.data_mapper.MapPsiOneToInteractions;
import org.cytoscape.psi_mi.internal.model.Interaction;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PSIMI10XMLNetworkViewReader extends AbstractCyNetworkReader {
	
	private static final Logger logger = LoggerFactory.getLogger(PSIMI10XMLNetworkViewReader.class);
	
	private static final int BUFFER_SIZE = 16384;

	private CyLayoutAlgorithmManager layouts;
	private TaskMonitor parentTaskMonitor;
	
	public PSIMI10XMLNetworkViewReader(
			final InputStream inputStream,
			final CyApplicationManager applicationManager,
			final CyNetworkFactory networkFactory, 
			final CyNetworkViewFactory networkViewFactory,
			final CyLayoutAlgorithmManager layouts, 
			final CyNetworkManager networkManager,
			final CyRootNetworkManager rootNetworkManager
	) {
		super(inputStream, applicationManager, networkFactory, networkManager, rootNetworkManager);
		this.layouts = layouts;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		parentTaskMonitor = taskMonitor;
		final long start = System.currentTimeMillis();
		
		taskMonitor.setStatusMessage("Loading PSI-MI 1.x XML file...");
		taskMonitor.setProgress(0.05);
		final String xml = readString(inputStream);

		final List<Interaction> interactions = new ArrayList<Interaction>();

		final MapPsiOneToInteractions mapper1 = new MapPsiOneToInteractions(xml, interactions);
		mapper1.doMapping();
		
		taskMonitor.setProgress(0.4);
		
		if (cancelled) {
			inputStream.close();
			return;
		}

		CyRootNetwork root = getRootNetwork();
		final CySubNetwork newNetwork;
		
		if (root != null)
			newNetwork = root.addSubNetwork();
		else // Need to create new network with new root.
			newNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
		
		final MapToCytoscape mapper2 = new MapToCytoscape(newNetwork, interactions, MapToCytoscape.SPOKE_VIEW);
		mapper2.doMapping();

		networks = new CyNetwork[] { newNetwork };
		
		taskMonitor.setProgress(1.0d);
		logger.info("PSI-MI XML Data Import finihsed in " + (System.currentTimeMillis() - start) + " msec.");
	}

	/**
	 * Create big String object from the entire XML file
	 * TODO: is this OK for huge data files?
	 */
	private static String readString(InputStream source) throws IOException {
		final StringWriter writer = new StringWriter();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(source));
		
		try {
			char[] buffer = new char[BUFFER_SIZE];
			int charactersRead = reader.read(buffer, 0, buffer.length);
			
			while (charactersRead != -1) {
				writer.write(buffer, 0, charactersRead);
				charactersRead = reader.read(buffer, 0, buffer.length);
			}
		} finally {
			reader.close();
		}
		
		return writer.toString();
	}

	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network) {
		final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);
		final CyLayoutAlgorithm layout = layouts.getDefaultLayout();
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(),
				CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
		Task nextTask = itr.next();
		
		try {
			nextTask.run(parentTaskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		parentTaskMonitor.setProgress(1.0d);
		
		return view;		
	}
}
