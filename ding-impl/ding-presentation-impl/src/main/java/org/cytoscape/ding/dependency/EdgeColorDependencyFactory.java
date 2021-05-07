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

import java.awt.Paint;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualPropertyDependencyFactory;

public class EdgeColorDependencyFactory implements VisualPropertyDependencyFactory<Paint> {

private final VisualLexicon lexicon;
	
	public EdgeColorDependencyFactory(final VisualLexicon lexicon) {
		this.lexicon = lexicon;
	}
	
	@Override
	public VisualPropertyDependency<Paint> createVisualPropertyDependency() {
		
		// Create Visual Property Dependencies
		final Set<VisualProperty<Paint>> edgeColorDependency = new HashSet<VisualProperty<Paint>>();
		
		edgeColorDependency.add(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		edgeColorDependency.add(DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT);
		edgeColorDependency.add(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT);
		
		return new VisualPropertyDependency<Paint>("arrowColorMatchesEdge", "Edge color to arrows", edgeColorDependency, lexicon);
	}

}
