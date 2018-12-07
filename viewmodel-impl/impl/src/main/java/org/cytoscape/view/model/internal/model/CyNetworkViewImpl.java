package org.cytoscape.view.model.internal.model;

import java.util.ArrayList;
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

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class CyNetworkViewImpl extends CyView<CyNetwork> implements CyNetworkView {

	private final String rendererId;
	
	private CopyOnWriteArrayList<CyNetworkViewListener> listeners = new CopyOnWriteArrayList<>();
	
	// Key is SUID of underlying model object.
	private Map<Long,CyNodeViewImpl> nodeViewMap = HashMap.empty();
	private Map<Long,CyEdgeViewImpl> edgeViewMap = HashMap.empty();

	private Map<VisualProperty<?>,Object> defaultValues = HashMap.empty();
	private Map<CyIdentifiable,Map<VisualProperty<?>,Object>> vpValues = HashMap.empty();
	private Map<CyIdentifiable,Map<VisualProperty<?>,Object>> lockedValues = HashMap.empty();
	
	
	/**
	 * Normal constructor to be called by factory.
	 */
	public CyNetworkViewImpl(CyNetwork network, VisualLexicon visualLexicon, String rendererId) {
		super(network);
		this.rendererId = rendererId;
		
		if(visualLexicon != null) {
			for(VisualProperty<?> vp : visualLexicon.getAllVisualProperties()) {
				defaultValues = defaultValues.put(vp, vp.getDefault());
			}
		}
		
		if(network != null) {
			network.getNodeList().forEach(this::addNode);
			network.getEdgeList().forEach(this::addEdge);
		}
	}
	
	/** 
	 * Copy constructor for snapshot.
	 */
	private CyNetworkViewImpl(CyNetworkViewImpl other) {
		super(other.getModel());
		this.rendererId = other.rendererId;
		this.nodeViewMap = other.nodeViewMap;
		this.edgeViewMap = other.edgeViewMap;
		this.defaultValues = other.defaultValues;
		this.vpValues = other.vpValues;
		this.lockedValues = other.lockedValues;
		// don't copy listeners, snapshot is not a "live" view
	}
	
	
	@Override
	public CyNetworkViewImpl createSnapshot() {
		return new CyNetworkViewImpl(this);
	}
	
	
	@Override
	public CyNetworkViewImpl getNetworkView() {
		return this;
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
			nodeViewMap = nodeViewMap.put(model.getSUID(), view);
		}
		return view;
	}
	
	public View<CyEdge> addEdge(CyEdge model) {
		CyEdgeViewImpl view = new CyEdgeViewImpl(this, model);
		synchronized (this) {
			edgeViewMap = edgeViewMap.put(model.getSUID(), view);
		}
		return view;
	}
	
	public View<CyNode> removeNode(CyNode model) {
		View<CyNode> view;
		synchronized (this) {
			view = nodeViewMap.getOrElse(model.getSUID(), null);
			nodeViewMap = nodeViewMap.remove(model.getSUID());
			clearVisualProperties(view);
		}
		return view;
	}
	
	public View<CyEdge> removeEdge(CyEdge model) {
		View<CyEdge> view;
		synchronized (this) {
			view = edgeViewMap.getOrElse(model.getSUID(), null); // MKTODO should I be using null???
			edgeViewMap = edgeViewMap.remove(model.getSUID());
			clearVisualProperties(view);
		}
		return view;
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
			Map<VisualProperty<?>, Object> values = vpValues.getOrElse(view, HashMap.empty());
			values = values.put(vp, value);
			vpValues = vpValues.put(view, values);
		}
	}

	public <T> T getVisualProperty(CyIdentifiable view, VisualProperty<T> vp) {
		Map<VisualProperty<?>, Object> values = vpValues.getOrElse(view, HashMap.empty());
		Object value = values.getOrElse(vp, null);
		if(value == null)
			return (T) defaultValues.getOrElse(vp, null);
		return (T) value;
	}

	public boolean isSet(CyIdentifiable view, VisualProperty<?> vp) {
		return vpValues.getOrElse(view, HashMap.empty()).containsKey(vp);
	}

	public <T, V extends T> void setLockedValue(CyIdentifiable view, VisualProperty<? extends T> vp, V value) {
		synchronized (this) {
			Map<VisualProperty<?>, Object> values = lockedValues.getOrElse(view, HashMap.empty());
			values = values.put(vp, value);
			lockedValues = lockedValues.put(view, values);
		}
	}

	public boolean isValueLocked(CyIdentifiable view, VisualProperty<?> vp) {
		return lockedValues.getOrElse(view, HashMap.empty()).containsKey(vp);
	}

	public void clearValueLock(CyIdentifiable view, VisualProperty<?> vp) {
		synchronized (this) {
			Map<VisualProperty<?>, Object> values = lockedValues.getOrElse(view, HashMap.empty());
			values = values.remove(vp);
			lockedValues = lockedValues.put(view, values);
		}
	}

	public boolean isDirectlyLocked(CyIdentifiable view, VisualProperty<?> vp) {
		// TODO Auto-generated method stub
		return false;
	}

	public void clearVisualProperties(CyIdentifiable view) {
		synchronized (this) {
			vpValues = vpValues.remove(view);
			lockedValues = lockedValues.remove(view);
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
	
	@Override
	public void dispose() {
		for(CyNetworkViewListener listener : listeners) {
			listener.handleDispose();
		}
	}
}
