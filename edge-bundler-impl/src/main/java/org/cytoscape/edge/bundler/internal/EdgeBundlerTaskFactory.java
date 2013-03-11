package org.cytoscape.edge.bundler.internal;

/*
 * #%L
 * Cytoscape Edge Bundler Impl (edge-bundler-impl)
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


import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.application.CyApplicationManager;


public class EdgeBundlerTaskFactory extends AbstractNetworkTaskFactory {

	private HandleFactory hf;
	private BendFactory bf;
	private VisualMappingManager vmm;
	private VisualMappingFunctionFactory discreteFactory;
	private int selection;
	private CyApplicationManager cyApplicationManagerServiceRef;
	
	public EdgeBundlerTaskFactory(HandleFactory hf, BendFactory bf, VisualMappingManager vmm, VisualMappingFunctionFactory discreteFactory, int selection, CyApplicationManager cam) {
		super();
		this.hf = hf;
		this.bf = bf;
		this.vmm = vmm;
		this.discreteFactory = discreteFactory;
		this.selection = selection;
		this.cyApplicationManagerServiceRef = cam;
	}
	
	public TaskIterator createTaskIterator(CyNetwork network) {
		CyNetworkView view = cyApplicationManagerServiceRef.getCurrentNetworkView();
		return new TaskIterator(new EdgeBundlerTask(view, hf, bf, vmm, discreteFactory, selection));
	}
}
