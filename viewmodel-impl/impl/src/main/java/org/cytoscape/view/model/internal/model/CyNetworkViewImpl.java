package org.cytoscape.view.model.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewListener;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;


public class CyNetworkViewImpl extends CyView<CyNetwork> implements CyNetworkView {

	private final String rendererId;
	private final VisualLexicon visualLexicon;
	
	private CopyOnWriteArrayList<CyNetworkViewListener> listeners = new CopyOnWriteArrayList<>();
	
	// Key is SUID of underlying model object.
	private Map<Long,CyViewImpl<CyNode>> nodeViewMap = HashMap.empty();
	private Map<Long,CyViewImpl<CyEdge>> edgeViewMap = HashMap.empty();

	private Map<VisualProperty<?>,Object> defaultValues = HashMap.empty();
	private Map<CyIdentifiable,Map<VisualProperty<?>,Object>> visualProperties = HashMap.empty();
	private Map<CyIdentifiable,Map<VisualProperty<?>,Object>> allLocks = HashMap.empty();
	private Map<CyIdentifiable,Map<VisualProperty<?>,Object>> directLocks = HashMap.empty();
	
	
	/**
	 * Normal constructor to be called by factory.
	 */
	public CyNetworkViewImpl(CyNetwork network, VisualLexicon visualLexicon, String rendererId) {
		super(network);
		this.rendererId = rendererId;
		this.visualLexicon = visualLexicon;
		
		network.getNodeList().forEach(this::addNode);
		network.getEdgeList().forEach(this::addEdge);
	}
	
	/** 
	 * Copy constructor for snapshot.
	 */
	private CyNetworkViewImpl(CyNetworkViewImpl other) {
		super(other.getModel());
		synchronized (other) {
			this.rendererId = other.rendererId;
			this.visualLexicon = other.visualLexicon;
			this.nodeViewMap = other.nodeViewMap;
			this.edgeViewMap = other.edgeViewMap;
			this.defaultValues = other.defaultValues;
			this.visualProperties = other.visualProperties;
			this.allLocks = other.allLocks;
			this.directLocks = other.directLocks;
			// don't copy listeners, snapshot is not a "live" view
		}
	}
	
	
	@Override
	public CyNetworkViewImpl createSnapshot() {
		return new CyNetworkViewImpl(this);
	}
	
	@Override
	public CyNetworkViewImpl getNetworkView() {
		return this;
	}
	
	public View<CyNode> addNode(CyNode model) {
		CyViewImpl<CyNode> view = new CyViewImpl<>(this, model);
		synchronized (this) {
			nodeViewMap = nodeViewMap.put(model.getSUID(), view);
		}
		return view;
	}
	
	public View<CyEdge> addEdge(CyEdge model) {
		CyViewImpl<CyEdge> view = new CyViewImpl<CyEdge>(this, model);
		synchronized (this) {
			edgeViewMap = edgeViewMap.put(model.getSUID(), view);
		}
		return view;
	}
	
	public View<CyNode> removeNode(CyNode model) {
		View<CyNode> view;
		synchronized (this) {
			CyNetwork network = getModel();
			Iterable<CyEdge> edges = network.getAdjacentEdgeIterable(model, Type.ANY);
			for(CyEdge edge : edges) {
				removeEdge(edge);
			}
			view = nodeViewMap.getOrElse(model.getSUID(), null);
			if(view != null) {
				nodeViewMap = nodeViewMap.remove(model.getSUID());
				clearVisualProperties(view);
			}
		}
		return view;
	}
	
	public View<CyEdge> removeEdge(CyEdge model) {
		View<CyEdge> view;
		synchronized (this) {
			view = edgeViewMap.getOrElse(model.getSUID(), null);
			if(view != null) {
				edgeViewMap = edgeViewMap.remove(model.getSUID());
				clearVisualProperties(view);
			}
		}
		return view;
	}
	
	public void clearVisualProperties(CyIdentifiable view) {
		synchronized (this) {
			visualProperties = visualProperties.remove(view);
			allLocks = allLocks.remove(view);
			directLocks = directLocks.remove(view);
		}
	}
	
	@Override
	public View<CyNode> getNodeView(CyNode node) {
		return nodeViewMap.getOrElse(node.getSUID(), null);
	}
	
	@Override
	public View<CyEdge> getEdgeView(CyEdge edge) {
		return edgeViewMap.getOrElse(edge.getSUID(), null);
	}
	
	@Override
	public Collection<View<CyNode>> getNodeViews() {
		return (Collection<View<CyNode>>) (Collection<?>) nodeViewMap.values().asJava();
	}

	@Override
	public Collection<View<CyEdge>> getEdgeViews() {
		return (Collection<View<CyEdge>>) (Collection<?>) edgeViewMap.values().asJava();
	}

	@Override
	public Collection<View<? extends CyIdentifiable>> getAllViews() {
		ArrayList<View<? extends CyIdentifiable>> list = new ArrayList<>();
		list.addAll(getNodeViews());
		list.addAll(getEdgeViews());
		list.add(this);
		return list;
	}
	
	public <T, V extends T> void setVisualProperty(CyIdentifiable view, VisualProperty<? extends T> vp, V value) {
		synchronized (this) {
			visualProperties = put(visualProperties, view, vp, value);
		}
	}
	
	
	private <T> T getVisualPropertyStoredValue(CyIdentifiable view, VisualProperty<T> vp) {
		Object value = get(directLocks, view, vp);
		if(value != null)
			return (T) value;
		
		value = get(allLocks, view, vp);
		if(value != null)
			return (T) value;
		
		return (T) get(visualProperties, view, vp);
	}

	public <T> T getVisualProperty(CyIdentifiable view, VisualProperty<T> vp) {
		Object value = getVisualPropertyStoredValue(view, vp);
		if(value != null)
			return (T) value;
		
		value = defaultValues.getOrElse(vp,null);
		if(value != null)
			return (T) value;
		
		return vp.getDefault();
	}
	

	public boolean isSet(CyIdentifiable view, VisualProperty<?> vp) {
		return getVisualPropertyStoredValue(view, vp) != null;
	}

	public <T, V extends T> void setLockedValue(CyIdentifiable view, VisualProperty<? extends T> vp, V value) {
		synchronized (this) {
			directLocks = put(directLocks, view, vp, value);
			allLocks = put(allLocks, view, vp, value);
			
			VisualLexiconNode node = visualLexicon.getVisualLexiconNode(vp);
			propagateLockedVisualProperty(view, vp, node.getChildren(), value);
		}
	}
	
	private synchronized void propagateLockedVisualProperty(CyIdentifiable view, VisualProperty parent, Collection<VisualLexiconNode> roots, Object value) {
		LinkedList<VisualLexiconNode> nodes = new LinkedList<>(roots);
		
		while (!nodes.isEmpty()) {
			final VisualLexiconNode node = nodes.pop();
			final VisualProperty vp = node.getVisualProperty();
			
			if (!isDirectlyLocked(vp)) {
				if (parent.getClass() == vp.getClass()) { // Preventing ClassCastExceptions
					allLocks = put(allLocks, view, vp, value);
				}
				
				nodes.addAll(node.getChildren());
			}
		}
	}

	public boolean isValueLocked(CyIdentifiable view, VisualProperty<?> vp) {
		return get(allLocks, view, vp) != null;
	}

	public boolean isDirectlyLocked(CyIdentifiable view, VisualProperty<?> vp) {
		return get(directLocks, view, vp) != null;
	}
	
	public void clearValueLock(CyIdentifiable view, VisualProperty<?> vp) {
		setLockedValue(view, vp, null);
	}

	@Override
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V defaultValue) {
		synchronized (this) {
			defaultValues = defaultValues.put(vp, defaultValue);
		}
	}
	
	
	public static <T> T get(Map<CyIdentifiable,Map<VisualProperty<?>,Object>> map, CyIdentifiable view, VisualProperty<? extends T> vp) {
		return (T) map.getOrElse(view, HashMap.empty()).getOrElse(vp,null);
	}
	
	public static <T, V extends T> Map<CyIdentifiable,Map<VisualProperty<?>,Object>> put(Map<CyIdentifiable,Map<VisualProperty<?>,Object>> map, CyIdentifiable view, VisualProperty<? extends T> vp, V value) {
		Map<VisualProperty<?>, Object> values = map.getOrElse(view, HashMap.empty());
		values = (value == null) ? values.remove(vp) : values.put(vp, value);
		return map.put(view, values);
	}
	

	@Override
	public String getRendererId() {
		return rendererId;
	}
	
	public VisualLexicon getVisualLexicon() {
		return visualLexicon;
	}
	
	@Override
	public void addNetworkViewListener(CyNetworkViewListener listener) {
		listeners.addIfAbsent(listener);
	}
	
	@Override
	public void removeNetworkViewListener(CyNetworkViewListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void fitContent() {
		for(CyNetworkViewListener listener : listeners) {
			listener.handleFitContent();
		}
	}

	@Override
	public void fitSelected() {
		for(CyNetworkViewListener listener : listeners) {
			listener.handleFitSelected();
		}
	}

	@Override
	public void updateView() {
		for(CyNetworkViewListener listener : listeners) {
			listener.handleUpdateView();
		}
	}
	
	@Override
	public void dispose() {
		for(CyNetworkViewListener listener : listeners) {
			listener.handleDispose();
		}
	}
}
