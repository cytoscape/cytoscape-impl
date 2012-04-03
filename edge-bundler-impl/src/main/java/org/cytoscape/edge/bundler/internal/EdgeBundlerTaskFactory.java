package org.cytoscape.edge.bundler.internal;

import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;


public class EdgeBundlerTaskFactory extends AbstractNetworkViewTaskFactory {

	private HandleFactory hf;
	private BendFactory bf;
	private VisualMappingManager vmm;
	private VisualMappingFunctionFactory discreteFactory;
	private int selection;
	
	public EdgeBundlerTaskFactory(HandleFactory hf, BendFactory bf, VisualMappingManager vmm, VisualMappingFunctionFactory discreteFactory, int selection) {
		super();
		this.hf = hf;
		this.bf = bf;
		this.vmm = vmm;
		this.discreteFactory = discreteFactory;
		this.selection = selection;
	}
	
	public TaskIterator createTaskIterator(CyNetworkView view) {
		return new TaskIterator(new EdgeBundlerTask(view, hf, bf, vmm, discreteFactory, selection));
	}
}
