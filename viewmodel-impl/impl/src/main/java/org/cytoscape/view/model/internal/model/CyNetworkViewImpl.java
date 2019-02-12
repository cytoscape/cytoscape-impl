package org.cytoscape.view.model.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewListener;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.model.snapshot.CyNetworkViewSnapshotImpl;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;


public class CyNetworkViewImpl extends CyViewBase<CyNetwork> implements CyNetworkView {

	private static final float DEFAULT_X = 0;
	private static final float DEFAULT_Y = 0;
	private static final float DEFAULT_WIDTH = 30;
	private static final float DEFAULT_HEIGHT = 30;
	
	private static final Rectangle DEFAULT_GEOMETRY = 
			RectangleFloat.create(DEFAULT_X, DEFAULT_Y, DEFAULT_X + DEFAULT_WIDTH, DEFAULT_Y + DEFAULT_HEIGHT);
	
	public static final Set<VisualProperty<?>> NODE_GEOMETRIC_PROPERTIES = 
			HashSet.of(BasicVisualLexicon.NODE_X_LOCATION, BasicVisualLexicon.NODE_Y_LOCATION,
					   BasicVisualLexicon.NODE_HEIGHT, BasicVisualLexicon.NODE_WIDTH);
	
	
	
	private final String rendererId;
	private final BasicVisualLexicon visualLexicon;
	
	private CopyOnWriteArrayList<CyNetworkViewListener> listeners = new CopyOnWriteArrayList<>();
	private CyNetworkViewSnapshot snapshot = null;
	
	// View object is stored twice, using both the view suid and model suid as keys.
	private Map<Long,CyNodeViewImpl> dataSuidToNode = HashMap.empty();
	private Map<Long,CyNodeViewImpl> viewSuidToNode = HashMap.empty();
	private Map<Long,CyEdgeViewImpl> dataSuidToEdge = HashMap.empty();
	private Map<Long,CyEdgeViewImpl> viewSuidToEdge = HashMap.empty();
	
	// Key is SUID of View object
	private Map<Long,Set<CyEdgeViewImpl>> adjacentEdgeMap = HashMap.empty();
	private Set<Long> selectedNodes = HashSet.empty();

	// Key is SUID of View object.
	private Map<Long,Map<VisualProperty<?>,Object>> visualProperties = HashMap.empty();
	private Map<Long,Map<VisualProperty<?>,Object>> allLocks = HashMap.empty();
	private Map<Long,Map<VisualProperty<?>,Object>> directLocks = HashMap.empty();
	private Map<VisualProperty<?>,Object> defaultValues = HashMap.empty();
	
	// RTree
	// Need to store the bounds of each node so that they can be looked up in this RTree
	private Map<Long,Rectangle> geometries = HashMap.empty();
	private RTree<Long,Rectangle> rtree = RTree.create();
	

	public CyNetworkViewImpl(CyNetwork network, BasicVisualLexicon visualLexicon, String rendererId) {
		super(network);
		this.rendererId = rendererId;
		this.visualLexicon = visualLexicon;
		
		// faster than calling addNode() and addEdge()
		for(CyNode node : network.getNodeList()) {
			CyNodeViewImpl nodeView = new CyNodeViewImpl(this, node);
			dataSuidToNode = dataSuidToNode.put(node.getSUID(), nodeView);
			viewSuidToNode = viewSuidToNode.put(nodeView.getSUID(), nodeView);
		}
		for(CyEdge edge : network.getEdgeList()) {
			CyNodeViewImpl sourceView = dataSuidToNode.getOrElse(edge.getSource().getSUID(), null);
			CyNodeViewImpl targetView = dataSuidToNode.getOrElse(edge.getTarget().getSUID(), null);
			
			CyEdgeViewImpl edgeView = new CyEdgeViewImpl(this, edge, sourceView.getSUID(), targetView.getSUID());
			dataSuidToEdge = dataSuidToEdge.put(edge.getSUID(), edgeView);
			viewSuidToEdge = viewSuidToEdge.put(edgeView.getSUID(), edgeView);
			
			updateAdjacentEdgeMap(edgeView, true);
		}
	}
	
	
	@Override
	public CyNetworkViewSnapshot createSnapshot() {
		synchronized (this) {
			if(snapshot == null) {
				snapshot = new CyNetworkViewSnapshotImpl(
					this, 
					rendererId, 
					dataSuidToNode,
					viewSuidToNode,
					dataSuidToEdge,
					viewSuidToEdge,
					adjacentEdgeMap,
					selectedNodes,
					defaultValues, 
					visualProperties, 
					allLocks, 
					directLocks, 
					rtree, 
					geometries
				);
			}
			return snapshot;
		}
	}
	
	@Override
	public CyNetworkViewImpl getNetworkView() {
		return this;
	}
	
	@Override
	public boolean isDirty() {
		return snapshot == null;
	}
	
	private void setDirty() {
		snapshot = null;
	}
	
	private Rectangle getGeometry(View<CyNode> view) {
		return geometries.getOrElse(view.getSUID(), DEFAULT_GEOMETRY);
	}
	
	protected synchronized <T, V extends T> void updateNodeGeometry(View<CyNode> node, VisualProperty<? extends T> vp, V value) {
		Long suid = node.getSUID();
		Rectangle r = getGeometry(node);
		Rectangle newGeom = null;
		
		if(vp == BasicVisualLexicon.NODE_X_LOCATION) {
			float x = ((Number)value).floatValue();
			float wDiv2 = (float) (r.x2() - r.x1()) / 2.0f;
			float xMin = x - wDiv2;
			float xMax = x + wDiv2;
			newGeom = RectangleFloat.create(xMin, (float)r.y1(), xMax, (float)r.y2());
		} 
		else if(vp == BasicVisualLexicon.NODE_Y_LOCATION) {
			float y = ((Number)value).floatValue();
			float hDiv2 = (float) (r.y2() - r.y1()) / 2.0f;
			float yMin = y - hDiv2;
			float yMax = y + hDiv2;
			newGeom = RectangleFloat.create((float)r.x1(), yMin, (float)r.x2(), yMax);
		}
		else if(vp == BasicVisualLexicon.NODE_WIDTH) {
			float w = ((Number)value).floatValue();
			float xMid = (float) (r.x1() + r.x2()) / 2.0f;
			float wDiv2 = w / 2.0f;
			float xMin = xMid - wDiv2;
			float xMax = xMid + wDiv2;
			newGeom = RectangleFloat.create(xMin, (float)r.y1(), xMax, (float)r.y2());
		}
		else if(vp == BasicVisualLexicon.NODE_HEIGHT) {
			float h = ((Number)value).floatValue();
			float yMid = (float) (r.y1() + r.y2()) / 2.0f;
			float hDiv2 = h / 2.0f;
			float yMin = yMid - hDiv2;
			float yMax = yMid + hDiv2;
			newGeom = RectangleFloat.create((float)r.x1(), yMin, (float)r.x2(), yMax);
		}
		
		if(newGeom != null) {
			rtree = rtree.delete(suid, r).add(suid, newGeom);
			geometries = geometries.put(suid, newGeom);
		}
	}
	
	private void updateAdjacentEdgeMap(CyEdgeViewImpl edgeView, boolean add) {
		Set<CyEdgeViewImpl> edges;
		synchronized (this) {
			edges = adjacentEdgeMap.getOrElse(edgeView.getSourceSuid(), HashSet.empty());
			edges = add ? edges.add(edgeView) : edges.remove(edgeView);
			adjacentEdgeMap = adjacentEdgeMap.put(edgeView.getSourceSuid(), edges);
			
			edges = adjacentEdgeMap.getOrElse(edgeView.getTargetSuid(), HashSet.empty());
			edges = add ? edges.add(edgeView) : edges.remove(edgeView);
			adjacentEdgeMap = adjacentEdgeMap.put(edgeView.getTargetSuid(), edges);
		}
	}
	
	
	public View<CyNode> addNode(CyNode model) {
		if(dataSuidToNode.containsKey(getSUID()))
			return null;
		
		CyNodeViewImpl view = new CyNodeViewImpl(this, model);
		synchronized (this) {
			dataSuidToNode = dataSuidToNode.put(model.getSUID(), view);
			viewSuidToNode = viewSuidToNode.put(view.getSUID(), view);
			rtree = rtree.add(view.getSUID(), DEFAULT_GEOMETRY);
			setDirty();
		}
		return view;
	}
	
	public View<CyEdge> addEdge(CyEdge edge) {
		CyNodeViewImpl sourceView = dataSuidToNode.getOrElse(edge.getSource().getSUID(), null);
		CyNodeViewImpl targetView = dataSuidToNode.getOrElse(edge.getTarget().getSUID(), null);
		
		CyEdgeViewImpl edgeView = new CyEdgeViewImpl(this, edge, sourceView.getSUID(), targetView.getSUID());
		synchronized (this) {
			dataSuidToEdge = dataSuidToEdge.put(edge.getSUID(), edgeView);
			viewSuidToEdge = viewSuidToEdge.put(edgeView.getSUID(), edgeView);
			updateAdjacentEdgeMap(edgeView, true);
			setDirty();
		}
		return edgeView;
	}
	
	public View<CyNode> removeNode(CyNode model) {
		View<CyNode> nodeView;
		synchronized (this) {
			nodeView = dataSuidToNode.getOrElse(model.getSUID(), null);
			if(nodeView != null) {
				dataSuidToNode = dataSuidToNode.remove(model.getSUID());
				viewSuidToNode = viewSuidToNode.remove(nodeView.getSUID());
				Set<CyEdgeViewImpl> adjacentEdges = adjacentEdgeMap.getOrElse(nodeView.getSUID(), HashSet.empty());
				for(CyEdgeViewImpl adjacentEdge : adjacentEdges) {
					removeEdge(adjacentEdge.getModel());
				}
				adjacentEdgeMap = adjacentEdgeMap.remove(nodeView.getSUID());
				clearVisualProperties(nodeView);
				rtree = rtree.delete(nodeView.getSUID(), getGeometry(nodeView));
				setDirty();
			}
		}
		return nodeView;
	}
	
	public View<CyEdge> removeEdge(CyEdge model) {
		CyEdgeViewImpl edgeView;
		synchronized (this) {
			edgeView = dataSuidToEdge.getOrElse(model.getSUID(), null);
			if(edgeView != null) {
				dataSuidToEdge = dataSuidToEdge.remove(model.getSUID());
				viewSuidToEdge = viewSuidToEdge.remove(edgeView.getSUID());
				updateAdjacentEdgeMap(edgeView, false);
				clearVisualProperties(edgeView);
				setDirty();
			}
		}
		return edgeView;
	}
	
	public void clearVisualProperties(CyIdentifiable view) {
		synchronized (this) {
			Long suid = view.getSUID();
			visualProperties = clear(visualProperties, suid);
//			allLocks = clear(allLocks, suid);
//			directLocks = clear(directLocks, suid);
			setDirty();
		}
	}
	
	private Map<Long,Map<VisualProperty<?>,Object>> clear(Map<Long,Map<VisualProperty<?>,Object>> map, Long suid) {
		// we actually can't clear certain VPs, the renderer expects node size and location to remain
		java.util.HashMap<VisualProperty<?>,Object> values = new java.util.HashMap<>();
		for(VisualProperty<?> vp : NODE_GEOMETRIC_PROPERTIES) {
			values.put(vp, get(map, suid, vp));
		}
		map = map.remove(suid);
		for(VisualProperty<?> vp : NODE_GEOMETRIC_PROPERTIES) {
			map = put(map, suid, vp, values.get(vp));
		}
		return map;
	}
	
	
	@Override
	public View<CyNode> getNodeView(CyNode node) {
		return dataSuidToNode.getOrElse(node.getSUID(), null);
	}
	
	@Override
	public View<CyEdge> getEdgeView(CyEdge edge) {
		return dataSuidToEdge.getOrElse(edge.getSUID(), null);
	}
	
	@Override
	public Collection<View<CyNode>> getNodeViews() {
		return (Collection<View<CyNode>>) (Collection<?>) dataSuidToNode.values().asJava();
	}

	@Override
	public Collection<View<CyEdge>> getEdgeViews() {
		return (Collection<View<CyEdge>>) (Collection<?>) dataSuidToEdge.values().asJava();
	}
	
	@Override
	public Collection<View<? extends CyIdentifiable>> getAllViews() {
		ArrayList<View<? extends CyIdentifiable>> list = new ArrayList<>();
		list.addAll(getNodeViews());
		list.addAll(getEdgeViews());
		list.add(this);
		return list;
	}
	
	protected <T, V extends T> void setVisualProperty(CyIdentifiable view, VisualProperty<? extends T> vp, V value) {
		Long suid = view.getSUID();
		synchronized (this) {
			visualProperties = put(visualProperties, suid, vp, value);
			if(vp == BasicVisualLexicon.NODE_SELECTED) {
				// ignoring locked vp for now
				selectedNodes = Boolean.TRUE.equals(value) ? selectedNodes.add(suid) : selectedNodes.remove(suid);
			}
			setDirty();
		}
	}
	
	
	private <T> T getVisualPropertyStoredValue(Long suid, VisualProperty<T> vp) {
		Object value = get(directLocks, suid, vp);
		if(value != null)
			return (T) value;
		
		value = get(allLocks, suid, vp);
		if(value != null)
			return (T) value;
		
		return (T) get(visualProperties, suid, vp);
	}

	public <T> T getVisualProperty(Long suid, VisualProperty<T> vp) {
		Object value = getVisualPropertyStoredValue(suid, vp);
		if(value != null)
			return (T) value;
		
		value = defaultValues.getOrElse(vp,null);
		if(value != null)
			return (T) value;
		
		return vp.getDefault();
	}
	

	public boolean isSet(CyIdentifiable view, VisualProperty<?> vp) {
		return getVisualPropertyStoredValue(view.getSUID(), vp) != null;
	}

	public <T, V extends T> void setLockedValue(CyIdentifiable view, VisualProperty<? extends T> vp, V value) {
		Long suid = view.getSUID();
		synchronized (this) {
			directLocks = put(directLocks, suid, vp, value);
			allLocks = put(allLocks, suid, vp, value);
			
			VisualLexiconNode node = visualLexicon.getVisualLexiconNode(vp);
			propagateLockedVisualProperty(suid, vp, node.getChildren(), value);
			setDirty();
		}
	}
	
	private synchronized void propagateLockedVisualProperty(Long suid, VisualProperty parent, Collection<VisualLexiconNode> roots, Object value) {
		LinkedList<VisualLexiconNode> nodes = new LinkedList<>(roots);
		
		while (!nodes.isEmpty()) {
			final VisualLexiconNode node = nodes.pop();
			final VisualProperty vp = node.getVisualProperty();
			
			if (!isDirectlyLocked(vp)) {
				if (parent.getClass() == vp.getClass()) { // Preventing ClassCastExceptions
					allLocks = put(allLocks, suid, vp, value);
				}
				
				nodes.addAll(node.getChildren());
			}
		}
	}

	public boolean isValueLocked(CyIdentifiable view, VisualProperty<?> vp) {
		return get(allLocks, view.getSUID(), vp) != null;
	}

	public boolean isDirectlyLocked(CyIdentifiable view, VisualProperty<?> vp) {
		return get(directLocks, view.getSUID(), vp) != null;
	}
	
	public void clearValueLock(CyIdentifiable view, VisualProperty<?> vp) {
		setLockedValue(view, vp, null);
	}

	@Override
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V defaultValue) {
		synchronized (this) {
			defaultValues = defaultValues.put(vp, defaultValue);
			setDirty();
		}
	}
	
	
	public static <T> T get(Map<Long,Map<VisualProperty<?>,Object>> map, Long suid, VisualProperty<? extends T> vp) {
		return (T) map.getOrElse(suid, HashMap.empty()).getOrElse(vp, null);
	}
	
	public static <T, V extends T> Map<Long,Map<VisualProperty<?>,Object>> put(Map<Long,Map<VisualProperty<?>,Object>> map, Long suid, VisualProperty<? extends T> vp, V value) {
		Map<VisualProperty<?>, Object> values = map.getOrElse(suid, HashMap.empty());
		values = (value == null) ? values.remove(vp) : values.put(vp, value);
		if(values.isEmpty())
			return map.remove(suid);
		else
			return map.put(suid, values);
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
