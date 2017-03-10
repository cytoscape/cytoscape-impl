package csapps.layout.algorithms.cose;

import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

/**
 * The CoSE (Compound Spring Embedder) layout uses a physics simulation to lay out graphs.
 * It works well with noncompound graphs but also supports compound graphs.
 * The Cytoscape implementation uses the CoSE algorithm from the Bilkent University, which is provided
 * by the ChiLay project.
 * @see <a href="http://www.cs.bilkent.edu.tr/~ivis/chilay.html">Chisio Layout</a>
 */
public class CoSELayoutAlgorithm extends AbstractLayoutAlgorithm {

	private final CyServiceRegistrar serviceRegistrar;

	public CoSELayoutAlgorithm(final UndoSupport undoSupport, final CyServiceRegistrar serviceRegistrar) {
		super("cose", "Compound Spring Embedder (CoSE)", undoSupport);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(final CyNetworkView networkView, final Object context,
			final Set<View<CyNode>> nodesToLayOut, final String attrName) {
		return new TaskIterator(new CoSELayoutAlgorithmTask(toString(), networkView, nodesToLayOut,
				(CoSELayoutContext) context, undoSupport, serviceRegistrar));
	}
	
	@Override
	public Object createLayoutContext() {
		return new CoSELayoutContext();
	}
	
	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
