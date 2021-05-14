package org.cytoscape.ding.dependency;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualPropertyDependencyFactory;

public class NodeSizeDependencyFactory implements VisualPropertyDependencyFactory<Double> {

private final VisualLexicon lexicon;
	
	public NodeSizeDependencyFactory(final VisualLexicon lexicon) {
		this.lexicon = lexicon;
	}
	
	@Override
	public VisualPropertyDependency<Double> createVisualPropertyDependency() {
		// Node Size Dependency
		final Set<VisualProperty<Double>> nodeSizeVisualProperties = new HashSet<VisualProperty<Double>>();
		nodeSizeVisualProperties.add(BasicVisualLexicon.NODE_WIDTH);
		nodeSizeVisualProperties.add(BasicVisualLexicon.NODE_HEIGHT);

		VisualPropertyDependency<Double> vpDep = new VisualPropertyDependency<Double>("nodeSizeLocked", "Lock node width and height", nodeSizeVisualProperties, lexicon);
		vpDep.setDependency(true);
		return vpDep;
	}

}
