package org.cytoscape.view.vizmap.gui.internal.model;

import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.CURRENT_NETWORK_VIEW_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.CURRENT_VISUAL_STYLE_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.LOAD_DEFAULT_VISUAL_STYLES;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.VISUAL_STYLE_SET_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.VISUAL_STYLE_UPDATED;

import java.text.Collator;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.SetCurrentVisualStyleEvent;
import org.cytoscape.view.vizmap.events.SetCurrentVisualStyleListener;
import org.cytoscape.view.vizmap.events.VisualStyleAboutToBeRemovedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleAboutToBeRemovedListener;
import org.cytoscape.view.vizmap.events.VisualStyleAddedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleAddedListener;
import org.cytoscape.view.vizmap.events.VisualStyleChangedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleChangedListener;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.puremvc.java.multicore.patterns.proxy.Proxy;

@SuppressWarnings("unchecked")
public class VizMapperProxy extends Proxy
							implements VisualStyleAddedListener, VisualStyleAboutToBeRemovedListener,
							  		   VisualStyleChangedListener, SetCurrentVisualStyleListener,
							  		   SessionLoadedListener, SetCurrentNetworkViewListener {

	public static final String NAME = "VisualStyleProxy";
	
	private final SortedSet<VisualStyle> visualStyles;
	private final ServicesUtil servicesUtil;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VizMapperProxy(final ServicesUtil servicesUtil) {
		// Create the data object--a SortedSet that will store all the Visual Styles
		super(NAME, new TreeSet<VisualStyle>(
				new Comparator<VisualStyle>() {
					
					@Override
					public int compare(final VisualStyle vs1, final VisualStyle vs2) {
						// Locale-specific sorting
						final Collator collator = Collator.getInstance(Locale.getDefault());
						collator.setStrength(Collator.PRIMARY);
						
						return collator.compare(vs1.getTitle(), vs2.getTitle());
					}
				}
		));

		this.visualStyles = (SortedSet<VisualStyle>) getData();
		this.servicesUtil = servicesUtil;
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public synchronized SortedSet<VisualStyle> getVisualStyles() {
		return new TreeSet<VisualStyle>(visualStyles);
	}
	
	public void loadVisualStyles() {
		boolean changed = false;
		SortedSet<VisualStyle> updatedStyles = null;
		
		synchronized (this) {
			// Load the styles
			final Set<VisualStyle> allStyles = getAllVisualStyles();
			
			if (! (allStyles.isEmpty() && visualStyles.isEmpty())) {
				visualStyles.clear();
				visualStyles.addAll(allStyles);
				updatedStyles = getVisualStyles();
				changed = true;
			}
		}
		
		if (changed)
			sendNotification(VISUAL_STYLE_SET_CHANGED, updatedStyles);
	}

	public synchronized void addVisualStyle(final VisualStyle vs) {
		if (vs != null)
			servicesUtil.get(VisualMappingManager.class).addVisualStyle(vs);
	}
	
	public synchronized void removeVisualStyle(final VisualStyle vs) {
		if (vs != null)
			servicesUtil.get(VisualMappingManager.class).removeVisualStyle(vs);
	}
	
	public synchronized VisualStyle getDefaultVisualStyle() {
		return servicesUtil.get(VisualMappingManager.class).getDefaultVisualStyle();
	}
	
	public VisualStyle getCurrentVisualStyle() {
		return servicesUtil.get(VisualMappingManager.class).getCurrentVisualStyle();
	}

	public void setCurrentVisualStyle(final VisualStyle vs) {
		final VisualStyle curVs = getCurrentVisualStyle();
		final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
		
		if (vs != null && !vs.equals(curVs))
			vmMgr.setCurrentVisualStyle(vs);
	}

	public CyNetwork getCurrentNetwork() {
		return servicesUtil.get(CyApplicationManager.class).getCurrentNetwork();
	}
	
	public CyNetworkView getCurrentNetworkView() {
		return servicesUtil.get(CyApplicationManager.class).getCurrentNetworkView();
	}
	
	public RenderingEngine<CyNetwork> getCurrentRenderingEngine() {
		RenderingEngine<CyNetwork> engine = servicesUtil.get(CyApplicationManager.class).getCurrentRenderingEngine();
		
		if (engine == null)
			getDefaultRenderingEngine(getCurrentVisualStyle());
		
		return engine;
	}
	
	public RenderingEngineFactory<CyNetwork> getCurrentRenderingEngineFactory() {// TODO How to get the current one?
		return servicesUtil.get(RenderingEngineFactory.class);
	}
	
	public void getDefaultRenderingEngine(final VisualStyle style) {
		// TODO Auto-generated method stub
		
	}
	
	public VisualLexicon getCurrentVisualLexicon() {
		final RenderingEngine<CyNetwork> engine = getCurrentRenderingEngine();
		final VisualLexicon lexicon = engine != null ? 
				engine.getVisualLexicon() : servicesUtil.get(RenderingEngineManager.class).getDefaultVisualLexicon();
		
		return lexicon;
	}
	
	public Set<View<CyNode>> getSelectedNodeViews(final CyNetworkView netView) {
		final Set<View<CyNode>> views = new HashSet<View<CyNode>>();
		
		if (netView != null) {
			final List<CyNode> nodes = CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true);
			
			for (final CyNode n : nodes) {
				final View<CyNode> nv = netView.getNodeView(n);
				
				if (nv != null)
					views.add(nv);
			}
		}
		
		return views;
	}
	
	public Set<View<CyEdge>> getSelectedEdgeViews(final CyNetworkView netView) {
		final Set<View<CyEdge>> views = new HashSet<View<CyEdge>>();
		
		if (netView != null) {
			final List<CyEdge> edges = CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true);
			
			for (final CyEdge e : edges) {
				final View<CyEdge> ev = netView.getEdgeView(e);
				
				if (ev != null)
					views.add(ev);
			}
		}
		
		return views;
	}
	
	public Set<CyNetworkView> getNetworkViewsWithStyle(final VisualStyle style) {
		final Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		
		if (style != null) {
			final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
			final Set<CyNetworkView> allNetViews = servicesUtil.get(CyNetworkViewManager.class).getNetworkViewSet();
			
			for (final CyNetworkView nv : allNetViews) {
				final VisualStyle vs = vmMgr.getVisualStyle(nv);
				
				if (style.equals(vs))
					views.add(nv);
			}
		}
		
		return views;
	}
	
	// --- Cytoscape EVENTS ---
	
	@Override
	public void handleEvent(final VisualStyleAddedEvent e) {
		synchronized (this) {
			visualStyles.add(e.getVisualStyleAdded());
		}
		
		sendNotification(VISUAL_STYLE_SET_CHANGED, getVisualStyles());
	}
	
	@Override
	public void handleEvent(final VisualStyleAboutToBeRemovedEvent e) {
		final VisualStyle vs = e.getVisualStyleToBeRemoved();
		boolean changed = false;
		SortedSet<VisualStyle> updatedStyles = null;
		
		synchronized (this) {
			if (visualStyles.remove(vs)) {
				updatedStyles = getVisualStyles(); 
				changed = true;
			}
		}
		
		if (changed)
			sendNotification(VISUAL_STYLE_SET_CHANGED, updatedStyles);
	}
	
	@Override
	public void handleEvent(VisualStyleChangedEvent e) {
		sendNotification(VISUAL_STYLE_UPDATED, e.getSource());
	}
	
	@Override
	public void handleEvent(final SetCurrentVisualStyleEvent e) {
		sendNotification(CURRENT_VISUAL_STYLE_CHANGED, e.getVisualStyle());
	}
	
	@Override
	public void handleEvent(final SetCurrentNetworkViewEvent e) {
		sendNotification(CURRENT_NETWORK_VIEW_CHANGED, e.getNetworkView());
	}
	
	@Override
	public void handleEvent(final SessionLoadedEvent e) {
		if (e.getLoadedFileName() == null) // New empty session
			getFacade().sendNotification(LOAD_DEFAULT_VISUAL_STYLES);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private Set<VisualStyle> getAllVisualStyles() {
		return servicesUtil.get(VisualMappingManager.class).getAllVisualStyles();
	}
}
