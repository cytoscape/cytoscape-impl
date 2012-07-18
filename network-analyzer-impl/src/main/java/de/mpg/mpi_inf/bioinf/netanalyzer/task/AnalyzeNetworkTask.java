package de.mpg.mpi_inf.bioinf.netanalyzer.task;

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
