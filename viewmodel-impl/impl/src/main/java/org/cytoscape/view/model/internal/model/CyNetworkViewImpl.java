package org.cytoscape.view.model.internal.model;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.AboutToRemoveEdgeViewsEvent;
import org.cytoscape.view.model.events.AboutToRemoveNodeViewsEvent;
import org.cytoscape.view.model.events.AddedEdgeViewsEvent;
import org.cytoscape.view.model.events.AddedNodeViewsEvent;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.internal.CyNetworkViewConfigImpl;
import org.cytoscape.view.model.internal.model.snapshot.CyNetworkViewSnapshotImpl;
import org.cytoscape.view.model.internal.model.spacial.SpacialIndexStore;

import io.vavr.Tuple2;
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
	
	private final CyEventHelper eventHelper;
	private final String rendererId;
	
	private CopyOnWriteArrayList<CyNetworkViewListener> listeners = new CopyOnWriteArrayList<>();
	private boolean dirty = true;
	
	// View object is stored twice, using both the view suid and model suid as keys.
	private Map<Long,CyNodeViewImpl> dataSuidToNode = HashMap.empty();
	private Map<Long,CyNodeViewImpl> viewSuidToNode = HashMap.empty();
	private Map<Long,CyEdgeViewImpl> dataSuidToEdge = HashMap.empty();
	private Map<Long,CyEdgeViewImpl> viewSuidToEdge = HashMap.empty();
	
	// Key is SUID of View object
	private Map<Long,Set<CyEdgeViewImpl>> adjacentEdgeMap = HashMap.empty();
	
	protected final ViewLock nodeLock = new ViewLock();
	protected final ViewLock edgeLock = new ViewLock();
	protected final ViewLock netLock  = new ViewLock();
	
	protected final VPStore nodeVPs;
	protected final VPStore edgeVPs;
	protected final VPNetworkStore netVPs;
	
	private final SpacialIndexStore spacialIndex = new SpacialIndexStore();

	
	public CyNetworkViewImpl(CyServiceRegistrar registrar, CyNetwork network, VisualLexicon visualLexicon, String rendererId, CyNetworkViewConfigImpl config) {
		super(network);
		this.eventHelper = registrar.getService(CyEventHelper.class);
		this.rendererId = rendererId;
		
		this.edgeVPs = new VPStore(CyEdge.class, visualLexicon, config);
		this.nodeVPs = new VPStore(CyNode.class, visualLexicon, config);
		this.netVPs  = new VPNetworkStore(visualLexicon);
		
		for(CyNode node : network.getNodeList())
			addNode(node);
		for(CyEdge edge : network.getEdgeList())
			addEdge(edge);
	}
	
	
	@Override
	public CyNetworkViewSnapshot createSnapshot() {
		// MKTODO If we used ReentrantLock objects and the try() method we could have this
		// bail out early so as not to block the renderer.
		synchronized (nodeLock) {
			synchronized (edgeLock) {
				synchronized (netLock) {
					this.dirty = false;
					return new CyNetworkViewSnapshotImpl(
						this, 
						rendererId, 
						dataSuidToNode,
						viewSuidToNode,
						dataSuidToEdge,
						viewSuidToEdge,
						adjacentEdgeMap,
						nodeVPs.createSnapshot(),
						edgeVPs.createSnapshot(),
						netVPs.createSnapshot(),
						spacialIndex.createSnapshot()
					);
				}
			}
		}
	}
	
	@Override
	public CyNetworkViewImpl getNetworkView() {
		return this;
	}
	
	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	@Override
	public VPStore getVPStore() {
		return netVPs;
	}
	
	@Override
	public Object getLock() {
		return netLock;
	}
	
	public void setDirty() {
		this.dirty = true;
	}
	
	
	public View<CyNode> addNode(CyNode model) {
		if(dataSuidToNode.containsKey(model.getSUID()))
			return null;
		
		CyNodeViewImpl view = new CyNodeViewImpl(this, model);
		synchronized (nodeLock) {
			dataSuidToNode = dataSuidToNode.put(model.getSUID(), view);
			viewSuidToNode = viewSuidToNode.put(view.getSUID(), view);
			spacialIndex.addDefault(nodeVPs, view.getSUID());
			setDirty();
		}
		
		eventHelper.addEventPayload(this, view, AddedNodeViewsEvent.class);
		return view;
	}
	
	public View<CyEdge> addEdge(CyEdge edge) {
		CyNodeViewImpl sourceView = dataSuidToNode.getOrElse(edge.getSource().getSUID(), null);
		CyNodeViewImpl targetView = dataSuidToNode.getOrElse(edge.getTarget().getSUID(), null);
		CyEdgeViewImpl view = new CyEdgeViewImpl(this, edge, sourceView.getSUID(), targetView.getSUID());
		
		synchronized (edgeLock) {
			dataSuidToEdge = dataSuidToEdge.put(edge.getSUID(), view);
			viewSuidToEdge = viewSuidToEdge.put(view.getSUID(), view);
			updateAdjacentEdgeMap(view, true);
			setDirty();
		}
		
		eventHelper.addEventPayload(this, view, AddedEdgeViewsEvent.class);
		return view;
	}
	
	public View<CyNode> removeNode(CyNode model) {
		synchronized (nodeLock) {
			synchronized (edgeLock) {
				View<CyNode> nodeView = dataSuidToNode.getOrElse(model.getSUID(), null);
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
					
					nodeVPs.remove(nodeView.getSUID());
					spacialIndex.remove(nodeView.getSUID());
					
					setDirty();
				}
				return nodeView;
			}
		}
	}
	
	public View<CyEdge> removeEdge(CyEdge model) {
		synchronized (edgeLock) {
			CyEdgeViewImpl edgeView = dataSuidToEdge.getOrElse(model.getSUID(), null);
			if(edgeView != null) {
				// this is non-blocking, so its ok to call in the synchronized block
				eventHelper.addEventPayload(this, edgeView, AboutToRemoveEdgeViewsEvent.class);
				
				dataSuidToEdge = dataSuidToEdge.remove(model.getSUID());
				viewSuidToEdge = viewSuidToEdge.remove(edgeView.getSUID());
				updateAdjacentEdgeMap(edgeView, false);
				edgeVPs.remove(edgeView.getSUID());
				setDirty();
			}
			return edgeView;
		}
		
	}
	
	
	@Override
	public View<CyNode> getNodeView(CyNode node) {
		return dataSuidToNode.getOrElse(node.getSUID(), null);
	}
	
	@Override
	public View<CyNode> getNodeView(long suid) {
		return viewSuidToNode.getOrElse(suid, null);
	}
	
	public View<CyNode> getNodeViewByDataSuid(Long suid) {
		return dataSuidToNode.getOrElse(suid, null);
	}
	
	@Override
	public View<CyEdge> getEdgeView(CyEdge edge) {
		return dataSuidToEdge.getOrElse(edge.getSUID(), null);
	}
	
	@Override
	public View<CyEdge> getEdgeView(long suid) {
		return viewSuidToEdge.getOrElse(suid, null);
	}
	
	public View<CyEdge> getEdgeViewByDataSuid(Long suid) {
		return dataSuidToEdge.getOrElse(suid, null);
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
	

	protected <T, V extends T> void updateNodeGeometry(View<CyNode> node, VisualProperty<? extends T> vp) {
		synchronized (nodeLock) {
			spacialIndex.updateNodeGeometry(node.getSUID(), nodeVPs, vp);
		}
	}
	
	private void updateAdjacentEdgeMap(CyEdgeViewImpl edgeView, boolean add) {
		Set<CyEdgeViewImpl> edges;
		synchronized (edgeLock) {
			edges = adjacentEdgeMap.getOrElse(edgeView.getSourceSuid(), HashSet.empty());
			edges = add ? edges.add(edgeView) : edges.remove(edgeView);
			adjacentEdgeMap = adjacentEdgeMap.put(edgeView.getSourceSuid(), edges);
			
			edges = adjacentEdgeMap.getOrElse(edgeView.getTargetSuid(), HashSet.empty());
			edges = add ? edges.add(edgeView) : edges.remove(edgeView);
			adjacentEdgeMap = adjacentEdgeMap.put(edgeView.getTargetSuid(), edges);
		}
	}
	
	
	@Override
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V value) {
		if(vp.shouldIgnoreDefault())
			return;
		
		if(vp.getTargetDataType().equals(CyNode.class)) {
			synchronized(nodeLock) {
				nodeVPs.setViewDefault(vp, value);
				if(nodeVPs.getConfig().isTracked(vp)) {
					netVPs.updateTrackedVP(getSUID(), vp);
				}
				if(NODE_GEOMETRIC_PROPS.contains(vp)) {
					for(CyNodeViewImpl node : dataSuidToNode.values()) {
						spacialIndex.updateNodeGeometry(node.getSUID(), nodeVPs, vp);
					}
				}
			}
		} else if(vp.getTargetDataType().equals(CyEdge.class)) {
			synchronized(edgeLock) {
				edgeVPs.setViewDefault(vp, value);
				if(edgeVPs.getConfig().isTracked(vp)) {
					for(Tuple2<Long,?> t : viewSuidToEdge) {
						edgeVPs.updateTrackedVP(t._1, vp);
					}
				}
			}
		} else if(vp.getTargetDataType().equals(CyNetwork.class)) {
			synchronized(netLock) {
				netVPs.setViewDefault(vp, value);
				if(netVPs.getConfig().isTracked(vp)) {
					for(Tuple2<Long,?> t : viewSuidToNode) {
						nodeVPs.updateTrackedVP(t._1, vp);
					}
				}
			}
		}
		setDirty();
	}
	

	@Override
	public String getRendererId() {
		return rendererId;
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
