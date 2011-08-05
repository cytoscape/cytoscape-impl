package csplugins.layout;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractBasicLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.VertexView;
import org.jgraph.plugins.layouts.JGraphLayoutAlgorithm;
import org.jgraph.plugins.layouts.JGraphLayoutSettings;

public class JGraphLayoutWrapperTask extends AbstractBasicLayoutTask{
	
	private JGraphLayoutAlgorithm layout = null;
	private JGraphLayoutSettings layoutSettings = null;
	private boolean canceled = false;

	private CyNetworkView networkView;
	private TaskMonitor taskMonitor;
	private CyNetwork network;
	
	/**
	 * Creates a new GridNodeLayout object.
	 */
	public JGraphLayoutWrapperTask(final CyNetworkView networkView, final String name,
				  final boolean selectedOnly, final Set<View<CyNode>> staticNodes,
				  JGraphLayoutAlgorithm layout, JGraphLayoutSettings layoutSettings)
	{
		super(networkView, name, selectedOnly, staticNodes);

		this.layoutSettings = layoutSettings;
		this.layout = layout;
		this.networkView = networkView;
	}

	/**
	 *  Perform actual layout task.
	 */
	@Override
	final protected void doLayout(final TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
		this.network = networkView.getModel();
		construct();
	}
	
	
	/**
	 * Get the settings panel for this layout
	 */
	public JPanel getSettingsPanel() {
		return (JPanel) layoutSettings;
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void updateSettings() {
		if (layoutSettings != null)
			layoutSettings.apply();
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void revertSettings() {
		if (layoutSettings != null)
			layoutSettings.revert();
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void halt() {
		canceled = true;

	}

	/**
	 *  DOCUMENT ME!
	 */
	public void construct() {
		canceled = false;
		//initialize();

		double currentProgress = 0;
		double percentProgressPerIter = 0;
		CyNetwork network = networkView.getModel();
		Map j_giny_node_map = new HashMap(); //PrimeFinder.nextPrime(network.getNodeCount()));
		Map giny_j_node_map = new HashMap(); //PrimeFinder.nextPrime(network.getNodeCount()));
		Map j_giny_edge_map = new HashMap(); //PrimeFinder.nextPrime(network.getEdgeCount()));

		taskMonitor.setStatusMessage("Executing Layout");
		taskMonitor.setProgress(currentProgress/100.0);

		// Construct Model and Graph
		//
		GraphModel model = new DefaultGraphModel();
		JGraph graph = new JGraph(model);

		// Create Nested Map (from Cells to Attributes)
		//
		Map attributes = new Hashtable();

		Set cells = new HashSet();

		// update progress bar
		currentProgress = 20;
		taskMonitor.setProgress(currentProgress/100.0);
		percentProgressPerIter = 20 / (double) (networkView.getNodeViews().size());

		// create Vertices
		for (CyNode n: network.getNodeList()){
		    if (canceled) return;
			View<CyNode> node_view = networkView.getNodeView(n);

			DefaultGraphCell jcell = new DefaultGraphCell(n.getIndex());

			// Set bounds
			Rectangle2D bounds = new Rectangle2D.Double(
                                       node_view.getVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION),
				       node_view.getVisualProperty(MinimalVisualLexicon.NODE_Y_LOCATION),
				       node_view.getVisualProperty(MinimalVisualLexicon.NODE_WIDTH),
				       node_view.getVisualProperty(MinimalVisualLexicon.NODE_HEIGHT)
								    );

			GraphConstants.setBounds(jcell.getAttributes(), bounds);

			j_giny_node_map.put(jcell, n);
			giny_j_node_map.put(n, jcell);

			cells.add(jcell);

			// update progress bar
			currentProgress += percentProgressPerIter;
			taskMonitor.setProgress(currentProgress/100.0);
		}

		// update progress bar
		percentProgressPerIter = 20 / (double) (networkView.getEdgeViews().size());

		for (CyEdge edge: network.getEdgeList()){
			if (canceled) return;
			DefaultGraphCell j_source = (DefaultGraphCell) giny_j_node_map.get(edge.getSource());
			DefaultGraphCell j_target = (DefaultGraphCell) giny_j_node_map.get(edge.getTarget());

			DefaultPort source_port = new DefaultPort();
			DefaultPort target_port = new DefaultPort();

			j_source.add(source_port);
			j_target.add(target_port);

			source_port.setParent(j_source);
			target_port.setParent(j_target);

			// create the edge
			DefaultEdge jedge = new DefaultEdge();
			j_giny_edge_map.put(jedge, edge);

			// Connect Edge
			//
			ConnectionSet cs = new ConnectionSet(jedge, source_port, target_port);
			Object[] ecells = new Object[] { jedge, j_source, j_target };

			// Insert into Model
			//
			model.insert(ecells, attributes, cs, null, null);

			cells.add(jedge);

			// update progress bar
			currentProgress += percentProgressPerIter;
			taskMonitor.setProgress(currentProgress/100.0);
		}

		layout.run(graph, cells.toArray());

		GraphLayoutCache cache = graph.getGraphLayoutCache();

		CellView[] cellViews = graph.getGraphLayoutCache()
		                            .getAllDescendants(graph.getGraphLayoutCache().getRoots());

		currentProgress = 80;
		taskMonitor.setProgress(currentProgress/100.0);
		percentProgressPerIter = 20 / (double) (cellViews.length);

		if (canceled)
			return;

		for (int i = 0; i < cellViews.length; i++) {
			CellView cell_view = cellViews[i];

			if (cell_view instanceof VertexView) {
				// ok, we found a node
				Rectangle2D rect = graph.getCellBounds(cell_view.getCell());
				CyNode giny = (CyNode) j_giny_node_map.get(cell_view.getCell());
				View<CyNode> node_view = networkView.getNodeView(giny);
				node_view.setVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION, rect.getX());
				node_view.setVisualProperty(MinimalVisualLexicon.NODE_Y_LOCATION, rect.getY());

				// update progress bar
				currentProgress += percentProgressPerIter;
				taskMonitor.setProgress(currentProgress/100.0);
			}
		}

		// I don't think that any of the current layouts have edge components, 
		// so I won't bother for now.
		model = null;
		graph = null;
		attributes = null;
		cells = null;
		System.gc();
	}

}
