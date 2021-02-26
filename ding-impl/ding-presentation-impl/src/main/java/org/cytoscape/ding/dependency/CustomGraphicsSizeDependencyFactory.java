package org.cytoscape.ding.dependency;

import java.util.HashSet;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualPropertyDependencyFactory;

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

public class CustomGraphicsSizeDependencyFactory implements VisualPropertyDependencyFactory<Double> {

	private final VisualLexicon lexicon;
	
	public CustomGraphicsSizeDependencyFactory(VisualLexicon lexicon) {
		this.lexicon = lexicon;
	}
	
	@Override
	public VisualPropertyDependency<Double> createVisualPropertyDependency() {
		var set = new HashSet<VisualProperty<Double>>();
		set.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_1);
		set.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_2);
		set.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_3);
		set.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_4);
		set.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_5);
		set.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_6);
		set.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_7);
		set.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_8);
		set.add(DVisualLexicon.NODE_CUSTOMGRAPHICS_SIZE_9);
		
		var vpDep = new VisualPropertyDependency<Double>("nodeCustomGraphicsSizeSync", "Fit Custom Graphics to node", set, lexicon);
		vpDep.setDependency(true);
		
		return vpDep;
	}
}
