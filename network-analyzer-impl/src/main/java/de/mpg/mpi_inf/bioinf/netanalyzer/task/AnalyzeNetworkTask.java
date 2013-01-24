package de.mpg.mpi_inf.bioinf.netanalyzer.task;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2013 The Cytoscape Consortium
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.task.AbstractNetworkCollectionTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import de.mpg.mpi_inf.bioinf.netanalyzer.CyNetworkUtils;
import de.mpg.mpi_inf.bioinf.netanalyzer.DirNetworkAnalyzer;
import de.mpg.mpi_inf.bioinf.netanalyzer.NetworkAnalyzer;
import de.mpg.mpi_inf.bioinf.netanalyzer.UndirNetworkAnalyzer;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInspection;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInterpretation;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkStatus;

public class AnalyzeNetworkTask extends AbstractNetworkCollectionTask {

	//@Tunable(description = "Analyze as Directed Graph?")
	public Boolean directed = false;
	
	//@Tunable(description = "Analyze only selected nodes?")
	public Boolean selectedOnly = false;
	
	
	public AnalyzeNetworkTask(final Collection<CyNetwork> networks) {
		super(networks);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		double processed = 0.0d;
		final double increment= 1.0d/networks.size();
		
		taskMonitor.setProgress(processed);
		taskMonitor.setTitle("Analyzing Networks");

		for (final CyNetwork network : networks) {
			taskMonitor.setStatusMessage("Analyzing Network: "
					+ network.getRow(network).get(CyNetwork.NAME, String.class));
			
			final Set<CyNode> selectedNodes;
			if(selectedOnly) {
				final Collection<CyRow> matched = network.getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true);
				selectedNodes = new HashSet<CyNode>();
				for(CyRow row:matched)
					selectedNodes.add(network.getNode(row.get(CyIdentifiable.SUID, Long.class)));

			} else {
				selectedNodes = null;
			}
			analyze(network, selectedNodes);
			processed = processed+increment;
			taskMonitor.setProgress(processed);
		}
	}

	private void analyze(final CyNetwork network, final Set<CyNode> nodes) {
		final NetworkInspection status = CyNetworkUtils.inspectNetwork(network);
		final NetworkInterpretation interpr = interpretNetwork(status);
		
		if(interpr == null)
			throw new NullPointerException("NetworkInterpretation is null.");
		
		final NetworkAnalyzer analyzer;
		if (directed)
			analyzer = new DirNetworkAnalyzer(network, nodes, interpr);
		else
			analyzer = new UndirNetworkAnalyzer(network, nodes, interpr);
		
		analyzer.computeAll();
	}
	
	private final NetworkInterpretation interpretNetwork(NetworkInspection aInsp) {
		final NetworkStatus status = NetworkStatus.getStatus(aInsp);
		final NetworkInterpretation[] interpretations = status.getInterpretations();
		for(NetworkInterpretation ni: interpretations) {
			if(directed == ni.isDirected())
				return ni;
		}
		return null;
	}

}
