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
	private boolean isDirty = false;
	
	// Key is SUID of underlying model object.
	private Map<Long,CyViewImpl<CyNode>> nodeViewMap = HashMap.empty();
	private Map<Long,CyViewImpl<CyEdge>> edgeViewMap = HashMap.empty();

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
			nodeViewMap = nodeViewMap.put(node.getSUID(), new CyNodeViewImpl(this, node));
		}
		for(CyEdge edge : network.getEdgeList()) {
			edgeViewMap = edgeViewMap.put(edge.getSUID(), new CyEdgeViewImpl(this, edge));
		}
	}
	
	
	@Override
	public CyNetworkView createSnapshot() {
		synchronized (this) {
			isDirty = false;
			return new CyNetworkViewSnapshotImpl(
					getSUID(), 
					getModel(), 
					rendererId, 
					nodeViewMap, 
					edgeViewMap, 
					defaultValues, 
					visualProperties, 
					allLocks, 
					directLocks, 
					rtree,
					geometries
			);
		}
	}
	
	@Override
	public CyNetworkViewImpl getNetworkView() {
		return this;
	}
	
	@Override
	public boolean isDirty() {
		return isDirty;
	}
	
	private Rectangle getGeometry(View<CyNode> view) {
		return geometries.getOrElse(view.getSUID(), DEFAULT_GEOMETRY);
	}
	
	
	public View<CyNode> addNode(CyNode model) {
		if(nodeViewMap.containsKey(getSUID()))
			return null;
		
		CyNodeViewImpl view = new CyNodeViewImpl(this, model);
		synchronized (this) {
			nodeViewMap = nodeViewMap.put(model.getSUID(), view);
			rtree = rtree.add(view.getSUID(), DEFAULT_GEOMETRY);
			isDirty = true;
		}
		return view;
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
			System.out.println(suid + ": " + newGeom);
			rtree = rtree.delete(suid, r).add(suid, newGeom);
			geometries = geometries.put(suid, newGeom);
		}
		System.out.println();
	}
	
	
	public View<CyEdge> addEdge(CyEdge model) {
		CyEdgeViewImpl view = new CyEdgeViewImpl(this, model);
		synchronized (this) {
			edgeViewMap = edgeViewMap.put(model.getSUID(), view);
			isDirty = true;
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
				rtree = rtree.delete(view.getSUID(), getGeometry(view));
				isDirty = true;
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
				isDirty = true;
			}
		}
		return view;
	}
	
	public void clearVisualProperties(CyIdentifiable view) {
		synchronized (this) {
			Long suid = view.getSUID();
			visualProperties = clear(visualProperties, suid);
//			allLocks = clear(allLocks, suid);
//			directLocks = clear(directLocks, suid);
			isDirty = true;
		}
	}
	
	private Map<Long,Map<VisualProperty<?>,Object>> clear(Map<Long,Map<VisualProperty<?>,Object>> map, Long suid) {
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
		Long suid = view.getSUID();
		synchronized (this) {
			visualProperties = put(visualProperties, suid, vp, value);
			isDirty = true;
		}
	}
	
	
	private <T> T getVisualPropertyStoredValue(CyIdentifiable view, VisualProperty<T> vp) {
		Long suid = view.getSUID();
		Object value = get(directLocks, suid, vp);
		if(value != null)
			return (T) value;
		
		value = get(allLocks, suid, vp);
		if(value != null)
			return (T) value;
		
		return (T) get(visualProperties, suid, vp);
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
		Long suid = view.getSUID();
		synchronized (this) {
			directLocks = put(directLocks, suid, vp, value);
			allLocks = put(allLocks, suid, vp, value);
			
			VisualLexiconNode node = visualLexicon.getVisualLexiconNode(vp);
			propagateLockedVisualProperty(suid, vp, node.getChildren(), value);
			isDirty = true;
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
			isDirty = true;
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
