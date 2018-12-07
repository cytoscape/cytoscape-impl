package org.cytoscape.view.model.internal.model;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewListener;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.pcollections.HashTreePSet;

import cyclops.collections.immutable.PersistentMapX;
import cyclops.collections.scala.ScalaHashMapX;

public class CyNetworkViewImpl extends CyView<CyNetwork> implements CyNetworkView {

	private final String rendererId;
	
	private Runnable disposeListener;
	private CopyOnWriteArrayList<CyNetworkViewListener> listeners = new CopyOnWriteArrayList<>();
	
	// Key is SUID of underlying model object.
	private PersistentMapX<Long,CyNodeViewImpl> nodeViewMap = ScalaHashMapX.empty();
	private PersistentMapX<Long,CyEdgeViewImpl> edgeViewMap = ScalaHashMapX.empty();

	private PersistentMapX<VisualProperty<?>,Object> defaultValues = ScalaHashMapX.empty();
	private PersistentMapX<CyIdentifiable,PersistentMapX<VisualProperty<?>,Object>> vpValues = ScalaHashMapX.empty();
	private PersistentMapX<CyIdentifiable,PersistentMapX<VisualProperty<?>,Object>> lockedValues = ScalaHashMapX.empty();
	
	
	public CyNetworkViewImpl(CyNetwork network, VisualLexicon visualLexicon, String rendererId) {
		super(network);
		this.rendererId = rendererId;
		
		for(VisualProperty<?> vp : visualLexicon.getAllVisualProperties()) {
			defaultValues = defaultValues.plus(vp, vp.getDefault());
		}
		
		for(CyNode node : network.getNodeList()) {
			addNode(node);
		}
		for(CyEdge edge : network.getEdgeList()) {
			addEdge(edge);
		}
	}
	
	@Override
	public CyNetworkViewImpl getNetworkView() {
		return this;
	}
	
	void onDispose(Runnable listener) {
		this.disposeListener = listener;
	}
	
	@Override
	public void dispose() {
		if(disposeListener != null)
			disposeListener.run();
	}

	@Override
	public void addNetworkViewListener(CyNetworkViewListener listener) {
		listeners.addIfAbsent(listener);
	}
	
	@Override
	public void removeNetworkViewListener(CyNetworkViewListener listener) {
		listeners.remove(listener);
	}
	
	public View<CyNode> addNode(CyNode model) {
		CyNodeViewImpl view = new CyNodeViewImpl(this, model);
		synchronized (this) {
			nodeViewMap = nodeViewMap.plus(model.getSUID(), view);
		}
		return view;
	}
	
	public View<CyEdge> addEdge(CyEdge model) {
		CyEdgeViewImpl view = new CyEdgeViewImpl(this, model);
		synchronized (this) {
			edgeViewMap = edgeViewMap.plus(model.getSUID(), view);
		}
		return view;
	}
	
	public View<CyNode> removeNode(CyNode model) {
		View<CyNode> view;
		synchronized (this) {
			view = get(nodeViewMap, model.getSUID());
			nodeViewMap = nodeViewMap.minus(model.getSUID());
			clearVisualProperties(view);
		}
		return view;
	}
	
	public View<CyEdge> removeEdge(CyEdge model) {
		View<CyEdge> view;
		synchronized (this) {
			view = get(edgeViewMap, model.getSUID());
			edgeViewMap = edgeViewMap.minus(model.getSUID());
			clearVisualProperties(view);
		}
		return view;
	}
	
	@Override
	public View<CyNode> getNodeView(CyNode node) {
		return get(nodeViewMap, node.getSUID());
	}
	
	@Override
	public View<CyEdge> getEdgeView(CyEdge edge) {
		return get(edgeViewMap, edge.getSUID());
	}
	
	@Override
	public Collection<View<CyNode>> getNodeViews() {
		// This is safe because 'nodes' is immutable, there's no way for calling code to add a node of the wrong type.
		return (Collection<View<CyNode>>) (Collection<?>) nodeViewMap.values();
	}

	@Override
	public Collection<View<CyEdge>> getEdgeViews() {
		return (Collection<View<CyEdge>>) (Collection<?>) edgeViewMap.values();
	}

	@Override
	public Collection<View<? extends CyIdentifiable>> getAllViews() {
		return HashTreePSet.<View<? extends CyIdentifiable>>from(getNodeViews()).plusAll(getEdgeViews()).plus(this);
	}
	
	
	private static PersistentMapX<VisualProperty<?>,Object> getValues(PersistentMapX<CyIdentifiable,PersistentMapX<VisualProperty<?>,Object>> valueMap, CyIdentifiable view) {
		// Need this because the getOrDefault() method doesn't work on a scala map
		return valueMap.containsKey(view) ? valueMap.get(view) : ScalaHashMapX.empty();
	}
	
	private static <K,V> V get(PersistentMapX<K, V> map, K key) {
		return map.containsKey(key) ? map.get(key) : null;
	}
	
	public <T, V extends T> void setVisualProperty(CyIdentifiable view, VisualProperty<? extends T> vp, V value) {
		PersistentMapX<VisualProperty<?>, Object> values = getValues(vpValues, view);
		values = values.plus(vp, value);
		vpValues = vpValues.plus(view, values);
	}

	public <T> T getVisualProperty(CyIdentifiable view, VisualProperty<T> vp) {
		PersistentMapX<VisualProperty<?>, Object> values = getValues(vpValues, view);
		Object value = get(values, vp);
		if(value == null)
			return (T) get(defaultValues, vp);
		return (T) value;
	}

	public boolean isSet(CyIdentifiable view, VisualProperty<?> vp) {
		return getValues(vpValues, view).containsKey(vp);
	}

	public <T, V extends T> void setLockedValue(CyIdentifiable view, VisualProperty<? extends T> vp, V value) {
		PersistentMapX<VisualProperty<?>, Object> values = getValues(lockedValues, view);
		values = values.plus(vp, value);
		lockedValues = lockedValues.plus(view, values);
	}

	public boolean isValueLocked(CyIdentifiable view, VisualProperty<?> vp) {
		return getValues(lockedValues, view).containsKey(vp);
	}

	public void clearValueLock(CyIdentifiable view, VisualProperty<?> vp) {
		PersistentMapX<VisualProperty<?>, Object> values = getValues(lockedValues, view);
		values = values.minus(vp);
		lockedValues = lockedValues.plus(view, values);
	}

	public boolean isDirectlyLocked(CyIdentifiable view, VisualProperty<?> vp) {
		// TODO Auto-generated method stub
		return false;
	}

	public void clearVisualProperties(CyIdentifiable view) {
		synchronized (this) {
			vpValues = vpValues.minus(view);
			lockedValues = lockedValues.minus(view);
		}
	}

	@Override
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V defaultValue) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getRendererId() {
		return rendererId;
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

}
