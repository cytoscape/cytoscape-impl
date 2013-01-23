package org.cytoscape.ding.dependency;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualPropertyDependencyFactory;

public class CustomGraphicsSizeDependencyFactory implements VisualPropertyDependencyFactory<Double> {

	private final VisualLexicon lexicon;
	
	public CustomGraphicsSizeDependencyFactory(final VisualLexicon lexicon) {
		this.lexicon = lexicon;
	}
	
	@Override
	public VisualPropertyDependency<Double> createVisualPropertyDependency() {
		
		final Set<VisualProperty<Double>> customGraphicsSizeDependency = new HashSet<VisualProperty<Double>>();
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_1);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_2);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_3);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_4);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_5);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_6);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_7);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_8);
		customGraphicsSizeDependency.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_9);
		
		VisualPropertyDependency<Double> vpDep = new VisualPropertyDependency<Double>("nodeCustomGraphicsSizeSync", "Fit Custom Graphics to node", customGraphicsSizeDependency, lexicon);
		vpDep.setDependency(true);
		
		return vpDep;
	}

}
