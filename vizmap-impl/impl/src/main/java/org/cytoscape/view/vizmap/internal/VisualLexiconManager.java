package org.cytoscape.view.vizmap.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		this.lexiconSet = new HashSet<VisualLexicon>();
		
		nodeVPs = new HashSet<VisualProperty<?>>();
		edgeVPs = new HashSet<VisualProperty<?>>();
		networkVPs = new HashSet<VisualProperty<?>>();
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
