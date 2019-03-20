package org.cytoscape.view.model.internal.model;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewListener;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.AboutToRemoveEdgeViewsEvent;
import org.cytoscape.view.model.events.AboutToRemoveNodeViewsEvent;
import org.cytoscape.view.model.events.AddedEdgeViewsEvent;
import org.cytoscape.view.model.events.AddedNodeViewsEvent;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.internal.model.snapshot.CyNetworkViewSnapshotImpl;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Traversable;

public class CyNetworkViewImpl extends CyViewBase<CyNetwork> implements CyNetworkView {
	
	// Visual Properties that effect what's stored in the RTree
	public static final Set<VisualProperty<?>> NODE_GEOMETRIC_PROPS = HashSet.of(
		NODE_X_LOCATION, NODE_Y_LOCATION, NODE_HEIGHT, NODE_WIDTH, NODE_VISIBLE
	);
	
	// If you add more special case network properties make sure to update the JUnit test.
	public static final Set<VisualProperty<?>> NETWORK_PROPS = HashSet.of(
		NETWORK_CENTER_X_LOCATION, NETWORK_CENTER_Y_LOCATION, NETWORK_SCALE_FACTOR
	);
	
	private final CyEventHelper eventHelper;
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
	private Set<Long> selectedEdges = HashSet.empty();

	// Key is SUID of View object.
	private Map<Long,Map<VisualProperty<?>,Object>> visualProperties = HashMap.empty();
	private Map<Long,Map<VisualProperty<?>,Object>> allLocks = HashMap.empty();
	private Map<Long,Map<VisualProperty<?>,Object>> directLocks = HashMap.empty();
	private Map<VisualProperty<?>,Object> defaultValues = HashMap.empty();
	
	// Special case network visual properties that get updated a lot. This is an optimization.
	private double networkCenterXLocation = NETWORK_CENTER_X_LOCATION.getDefault();
	private double networkCenterYLocation = NETWORK_CENTER_Y_LOCATION.getDefault();
	private double networkScaleFactor     = NETWORK_SCALE_FACTOR.getDefault();
	
	// RTree
	// Need to store the bounds of each node so that they can be looked up in this RTree
	private Map<Long,Rectangle> geometries = HashMap.empty();
	private RTree<Long,Rectangle> rtree = RTree.create();
	

	public CyNetworkViewImpl(CyServiceRegistrar registrar, CyNetwork network, BasicVisualLexicon visualLexicon, String rendererId) {
		super(network);
		this.eventHelper = registrar.getService(CyEventHelper.class);
		this.rendererId = rendererId;
		this.visualLexicon = visualLexicon;
		
		// faster than calling addNode() and addEdge()
		for(CyNode node : network.getNodeList())
			addNode(node);
		for(CyEdge edge : network.getEdgeList())
			addEdge(edge);
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
					selectedEdges,
					defaultValues, 
					visualProperties, 
					allLocks, 
					directLocks, 
					rtree, 
					geometries,
					networkCenterXLocation,
					networkCenterYLocation,
					networkScaleFactor
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
	
	protected synchronized <T, V extends T> void updateNodeGeometry(View<CyNode> node, VisualProperty<? extends T> vp, V value) {
		Long suid = node.getSUID();
		Rectangle r = geometries.getOrElse(suid, null);
		Rectangle newGeom = null;
		
		if(r != null) {
			if(vp == NODE_X_LOCATION) {
				float x = ((Number)value).floatValue();
				float wDiv2 = (float) (r.x2() - r.x1()) / 2.0f;
				float xMin = x - wDiv2;
				float xMax = x + wDiv2;
				newGeom = RectangleFloat.create(xMin, (float)r.y1(), xMax, (float)r.y2());
			} 
			else if(vp == NODE_Y_LOCATION) {
				float y = ((Number)value).floatValue();
				float hDiv2 = (float) (r.y2() - r.y1()) / 2.0f;
				float yMin = y - hDiv2;
				float yMax = y + hDiv2;
				newGeom = RectangleFloat.create((float)r.x1(), yMin, (float)r.x2(), yMax);
			}
			else if(vp == NODE_WIDTH) {
				float w = ((Number)value).floatValue();
				float xMid = (float) (r.x1() + r.x2()) / 2.0f;
				float wDiv2 = w / 2.0f;
				float xMin = xMid - wDiv2;
				float xMax = xMid + wDiv2;
				newGeom = RectangleFloat.create(xMin, (float)r.y1(), xMax, (float)r.y2());
			}
			else if(vp == NODE_HEIGHT) {
				float h = ((Number)value).floatValue();
				float yMid = (float) (r.y1() + r.y2()) / 2.0f;
				float hDiv2 = h / 2.0f;
				float yMin = yMid - hDiv2;
				float yMax = yMid + hDiv2;
				newGeom = RectangleFloat.create((float)r.x1(), yMin, (float)r.x2(), yMax);
			} 
		}
		if(vp == NODE_VISIBLE) {
			if(Boolean.TRUE.equals(value)) {
				if(r == null) {
					float x = ((Number)getVisualProperty(suid, NODE_X_LOCATION)).floatValue();
					float y = ((Number)getVisualProperty(suid, NODE_Y_LOCATION)).floatValue();
					float w = ((Number)getVisualProperty(suid, NODE_WIDTH)).floatValue();
					float h = ((Number)getVisualProperty(suid, NODE_HEIGHT)).floatValue();
					r = vpToRTree(x, y, w, h);
					rtree = rtree.add(suid, r);
					geometries = geometries.put(suid, r);
				}
			} else {
				if(r != null) { // can be null if view is already hidden
					rtree = rtree.delete(suid, r);
					geometries = geometries.remove(suid);
				}
			}
		}
		
		if(newGeom != null) {
			rtree = rtree.delete(suid, r).add(suid, newGeom);
			geometries = geometries.put(suid, newGeom);
		}
	}
	
	
	private Rectangle getDefaultGeometry() {
		float x = ((Number)defaultValues.getOrElse(NODE_X_LOCATION, NODE_X_LOCATION.getDefault())).floatValue();
		float y = ((Number)defaultValues.getOrElse(NODE_Y_LOCATION, NODE_Y_LOCATION.getDefault())).floatValue();
		float w = ((Number)defaultValues.getOrElse(NODE_WIDTH, NODE_WIDTH.getDefault())).floatValue();
		float h = ((Number)defaultValues.getOrElse(NODE_HEIGHT, NODE_HEIGHT.getDefault())).floatValue();
		return vpToRTree(x, y, w, h);
	}
	
	/**
	 * Rtree stores (x1, x2, y1, y2) visual properties store (centerX, centerY, w, h)
	 */
	private static Rectangle vpToRTree(float x, float y, float w, float h) {
		float halfW = w / 2f;
		float halfH = h / 2f;
		return RectangleFloat.create(x - halfW, y - halfH, x + halfW, y + halfH);
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
			Rectangle r = getDefaultGeometry();
			rtree = rtree.add(view.getSUID(), r);
			geometries = geometries.put(view.getSUID(), r);
			setDirty();
		}
		
		eventHelper.addEventPayload(this, view, AddedNodeViewsEvent.class);
		return view;
	}
	
	public View<CyEdge> addEdge(CyEdge edge) {
		CyNodeViewImpl sourceView = dataSuidToNode.getOrElse(edge.getSource().getSUID(), null);
		CyNodeViewImpl targetView = dataSuidToNode.getOrElse(edge.getTarget().getSUID(), null);
		
		CyEdgeViewImpl view = new CyEdgeViewImpl(this, edge, sourceView.getSUID(), targetView.getSUID());
		synchronized (this) {
			dataSuidToEdge = dataSuidToEdge.put(edge.getSUID(), view);
			viewSuidToEdge = viewSuidToEdge.put(view.getSUID(), view);
			updateAdjacentEdgeMap(view, true);
			setDirty();
		}
		
		eventHelper.addEventPayload(this, view, AddedEdgeViewsEvent.class);
		return view;
	}
	
	public View<CyNode> removeNode(CyNode model) {
		View<CyNode> nodeView;
		synchronized (this) {
			nodeView = dataSuidToNode.getOrElse(model.getSUID(), null);
			if(nodeView != null) {
				
				// this is non-blocking, so its ok to call in the synchronized block
				eventHelper.addEventPayload(this, nodeView, AboutToRemoveNodeViewsEvent.class);
				
				dataSuidToNode = dataSuidToNode.remove(model.getSUID());
				viewSuidToNode = viewSuidToNode.remove(nodeView.getSUID());
				Set<CyEdgeViewImpl> adjacentEdges = adjacentEdgeMap.getOrElse(nodeView.getSUID(), HashSet.empty());
				for(CyEdgeViewImpl adjacentEdge : adjacentEdges) {
					removeEdge(adjacentEdge.getModel());
				}
				adjacentEdgeMap = adjacentEdgeMap.remove(nodeView.getSUID());
				clearVisualProperties(nodeView);
				
				Rectangle r = geometries.getOrElse(nodeView.getSUID(), null);
				rtree = rtree.delete(nodeView.getSUID(),r);
				geometries = geometries.remove(nodeView.getSUID());
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
				
				// this is non-blocking, so its ok to call in the synchronized block
				eventHelper.addEventPayload(this, edgeView, AboutToRemoveEdgeViewsEvent.class);
				
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
		// we actually can't clear certain VPs, the renderer expects node size and location to remain consistent
		java.util.HashMap<VisualProperty<?>,Object> values = new java.util.HashMap<>();
		for(VisualProperty<?> vp : NODE_GEOMETRIC_PROPS) {
			values.put(vp, get(map, suid, vp));
		}
		map = map.remove(suid);
		for(VisualProperty<?> vp : NODE_GEOMETRIC_PROPS) {
			map = put(map, suid, vp, values.get(vp));
		}
		return map;
	}
	
	
	@Override
	public View<CyNode> getNodeView(CyNode node) {
		return dataSuidToNode.getOrElse(node.getSUID(), null);
	}
	
	@Override
	public View<CyNode> getNodeView(long suid) {
		return viewSuidToNode.getOrElse(suid, null);
	}
	
	@Override
	public View<CyEdge> getEdgeView(CyEdge edge) {
		return dataSuidToEdge.getOrElse(edge.getSUID(), null);
	}
	
	@Override
	public View<CyEdge> getEdgeView(long suid) {
		return viewSuidToEdge.getOrElse(suid, null);
	}
	
	@Override
	public Collection<View<CyNode>> getNodeViews() {
		// The asJava() method returns a collection that is unbearably slow, so we create our own collection instead.
		java.util.List<View<CyNode>> nodeList = new ArrayList<>();
		for(CyNodeViewImpl node : dataSuidToNode.values()) {
			nodeList.add(node);
		}
		return nodeList;
	}
	
	@Override
	public Iterable<View<CyNode>> getNodeViewsIterable() {
		Traversable<CyNodeViewImpl> traversable = dataSuidToNode.values();
		return new Iterable<View<CyNode>>() {
			@Override public Iterator<View<CyNode>> iterator() {
				return new TraversableIterator<>(traversable);
			}
		};
	}
	
	@Override
	public Collection<View<CyEdge>> getEdgeViews() {
		java.util.List<View<CyEdge>> edgeList = new ArrayList<>();
		for(CyEdgeViewImpl edge : dataSuidToEdge.values()) {
			edgeList.add(edge);
		}
		return edgeList;
	}
	
	public Iterable<View<CyEdge>> getEdgeViewsIterable() {
		Traversable<CyEdgeViewImpl> traversable = dataSuidToEdge.values();
		return new Iterable<View<CyEdge>>() {
			@Override public Iterator<View<CyEdge>> iterator() {
				return new TraversableIterator<>(traversable);
			}
		};
	}
	
	
	private static class TraversableIterator<T> implements Iterator<T> {
		private Traversable<? extends T> traversable;

		TraversableIterator(Traversable<? extends T> traversable) {
			this.traversable = traversable;
		}

        @Override
        public boolean hasNext() {
            return !traversable.isEmpty();
        }

        @Override
        public T next() {
        	T element = traversable.head();
            traversable = traversable.tail();
            return element;
        }
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
			if(view == this && NETWORK_PROPS.contains(vp)) {
				setNetworkProp(vp, value);
				return;
			}
			
			visualProperties = put(visualProperties, suid, vp, value);
			// don't pass 'value' directly to updateSelectionAndVisibility(), it needs to check the locked values as well
			updateSelection(view, vp);
			setDirty();
		}
	}
	
	
	
	private double getNetworkProp(VisualProperty<?> vp) {
		if(vp == NETWORK_CENTER_X_LOCATION)
			return networkCenterXLocation;
		if(vp == NETWORK_CENTER_Y_LOCATION)
			return networkCenterYLocation;
		if(vp == NETWORK_SCALE_FACTOR)
			return networkScaleFactor;
		return 0; // should never happen
	}
	
	private void setNetworkProp(VisualProperty<?> vp, Object value) {
		if(vp == NETWORK_CENTER_X_LOCATION)
			networkCenterXLocation = ((Number)value).doubleValue();
		else if(vp == NETWORK_CENTER_Y_LOCATION)
			networkCenterYLocation = ((Number)value).doubleValue();
		if(vp == NETWORK_SCALE_FACTOR)
			networkScaleFactor = ((Number)value).doubleValue();
	}
	
	private <T> T getVisualPropertyStoredValue(Long suid, VisualProperty<T> vp) {
		Object value = get(directLocks, suid, vp);
		if(value != null)
			return (T) value;
		
		value = get(allLocks, suid, vp);
		if(value != null)
			return (T) value;
		
		if(suid.equals(this.getSUID()) && NETWORK_PROPS.contains(vp))
			return (T) Double.valueOf(getNetworkProp(vp));
		
		return (T) get(visualProperties, suid, vp);
	}

	public <T> T getVisualProperty(Long suid, VisualProperty<T> vp) {
		Object value = getVisualPropertyStoredValue(suid, vp);
		if(value != null)
			return (T) value;
		
		return (T) defaultValues.getOrElse(vp, vp.getDefault());
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
			
			updateSelection(view, vp);
			setDirty();
		}
	}
	
	
	private synchronized void updateSelection(CyIdentifiable view, VisualProperty<?> vp) {
		Long suid = view.getSUID();
		if(vp == NODE_SELECTED) {
			Object value = getVisualProperty(suid, vp);
			selectedNodes = Boolean.TRUE.equals(value) ? selectedNodes.add(suid) : selectedNodes.remove(suid);
		} else if(vp == EDGE_SELECTED) {
			Object value = getVisualProperty(suid, vp);
			selectedEdges = Boolean.TRUE.equals(value) ? selectedEdges.add(suid) : selectedEdges.remove(suid);
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
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V value) {
		if(vp.shouldIgnoreDefault())
			return;
		
		synchronized (this) {
			defaultValues = defaultValues.put(vp, value);
			if(NODE_GEOMETRIC_PROPS.contains(vp)) {
				for(CyNodeViewImpl node : dataSuidToNode.values()) {
					updateNodeGeometry(node, vp, value);
				}
			}
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
		// MKTODO this is temporary.
		// The old Ding view model used to fire this event when you call updateView()
		// If we want updateView() to be a no-op we need to move this.
		eventHelper.fireEvent(new UpdateNetworkPresentationEvent(this));
		
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
