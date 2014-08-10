package org.cytoscape.view.vizmap.internal;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class VisualLexiconManager {

	private final Set<VisualLexicon> lexiconSet;
	
	private final Collection<VisualProperty<?>> nodeVPs;
	private final Collection<VisualProperty<?>> edgeVPs;
	private final Collection<VisualProperty<?>> networkVPs;

	public VisualLexiconManager() {
		this.lexiconSet = new CopyOnWriteArraySet<VisualLexicon>();
		
		nodeVPs = new CopyOnWriteArraySet<VisualProperty<?>>();
		edgeVPs = new CopyOnWriteArraySet<VisualProperty<?>>();
		networkVPs = new CopyOnWriteArraySet<VisualProperty<?>>();
	}
	
	public Collection<VisualProperty<?>> getNodeVisualProperties() {
		return this.nodeVPs;
	}
	
	public Collection<VisualProperty<?>> getEdgeVisualProperties() {
		return this.edgeVPs;
	}
	
	public Collection<VisualProperty<?>> getNetworkVisualProperties() {
		return this.networkVPs;
	}
	
	public Set<VisualLexicon> getAllVisualLexicon() {
		return this.lexiconSet;
	}

	
	public void addRenderingEngineFactory(RenderingEngineFactory<?> factory, Map props) {
		final VisualLexicon lexicon = factory.getVisualLexicon();
		lexiconSet.add(lexicon);
		
		// Node-related Visual Properties are linked as a children of NODE VP.
		nodeVPs.addAll(lexicon.getAllDescendants(BasicVisualLexicon.NODE));
		// Node-related Visual Properties are linked as a children of NODE VP.
		edgeVPs.addAll(lexicon.getAllDescendants(BasicVisualLexicon.EDGE));

		for (VisualProperty<?> vp : lexicon.getAllVisualProperties()) {
			if (!nodeVPs.contains(vp) && !edgeVPs.contains(vp))
				networkVPs.add(vp);
		}
	}
	
	public void removeRenderingEngineFactory(RenderingEngineFactory<?> factory, Map props) {
		// TODO: cleanup
	}

}
