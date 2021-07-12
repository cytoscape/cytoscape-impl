package org.cytoscape.view.model.internal.network;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;

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
import org.cytoscape.view.model.events.AddedEdgeViewsEvent;
import org.cytoscape.view.model.events.AddedNodeViewsEvent;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.internal.base.CyViewBase;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.base.ViewLock;
import org.cytoscape.view.model.internal.network.snapshot.CyNetworkViewSnapshotImpl;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

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
	private final VisualLexicon visualLexicon;
	private final boolean isBVL;
	
	private CopyOnWriteArrayList<CyNetworkViewListener> listeners = new CopyOnWriteArrayList<>();
	
	private boolean dirty = true; // set to true initially so that the first frame is rendered
	
	// View object is stored twice, using both the view suid and model suid as keys.
	private Map<Long,CyNodeViewImpl> dataSuidToNode = HashMap.empty();
	private Map<Long,CyNodeViewImpl> viewSuidToNode = HashMap.empty();
	private Map<Long,CyEdgeViewImpl> dataSuidToEdge = HashMap.empty();
	private Map<Long,CyEdgeViewImpl> viewSuidToEdge = HashMap.empty();
	
	// Key is SUID of View object
	private Map<Long,Set<CyEdgeViewImpl>> adjacentEdgeMap = HashMap.empty();
	
	protected final ViewLock netLock;
	protected final ViewLock nodeLock;
	protected final ViewLock edgeLock;
	
	protected final VPNodeStore nodeVPs;
	protected final VPEdgeStore edgeVPs;
	protected final VPNetworkStore netVPs;
	
	// minor optimization, true if no node or edge in this network has ever been hidden
	private boolean isNeverHidden = true;

	
	public CyNetworkViewImpl(CyServiceRegistrar registrar, CyNetwork network, VisualLexicon visualLexicon, String rendererId, CyNetworkViewFactoryConfigImpl config) {
		super(network);
		this.eventHelper = registrar.getService(CyEventHelper.class);
		this.rendererId = rendererId;
		this.visualLexicon = visualLexicon;
		this.isBVL = visualLexicon instanceof BasicVisualLexicon;
		
		this.netLock  = new ViewLock();
		this.nodeLock = new ViewLock(netLock);
		this.edgeLock = new ViewLock(netLock);
		
		this.edgeVPs = new VPEdgeStore(visualLexicon, config);
		this.nodeVPs = new VPNodeStore(visualLexicon, config);
		this.netVPs  = new VPNetworkStore(visualLexicon, config);
	}
	
	@Override
	public boolean supportsSnapshots() {
		return true;
	}
	
	@Override
	public CyNetworkViewSnapshot createSnapshot() {
		// MKTODO If we used ReentrantLock objects and the try() method we could have this
		// bail out early so as not to block the renderer.
		synchronized (nodeLock) {
			synchronized (edgeLock) {
				synchronized (netLock) {
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
						visualLexicon
					);
				}
			}
		}
	}
	
	public boolean isBVL() {
		return isBVL;
	}
	
	@Override
	public synchronized boolean dirty(boolean clear) {
		boolean d = dirty;
		if(clear) {
			dirty = false;
		}
		return d;
	}
	
	@Override
	public VPStore getVPStore() {
		return netVPs;
	}
	
	@Override
	public ViewLock getLock() {
		return netLock;
	}
	
	public void setDirty() {
		this.dirty = true;
	}
	
	public CyEventHelper getEventHelper() {
		return eventHelper;
	}
	
	public VisualLexicon getVisualLexicon() {
		return visualLexicon;
	}
	
	public boolean isNeverHidden() {
		return isNeverHidden;
	}
	
	public void setElementHidden() {
		isNeverHidden = false;
	}
	
	
	public CyNodeViewImpl addNode(CyNode model) {
		CyNodeViewImpl view;
		
		synchronized (nodeLock) {
			if(dataSuidToNode.containsKey(model.getSUID()))
				return null;
			
			view = new CyNodeViewImpl(this, model);
			
			dataSuidToNode = dataSuidToNode.put(model.getSUID(), view);
			viewSuidToNode = viewSuidToNode.put(view.getSUID(), view);
			setDirty();
		}
		
		eventHelper.addEventPayload(this, view, AddedNodeViewsEvent.class);
		return view;
	}
	
	
	public View<CyEdge> addEdge(CyEdge edge) {
		CyNode source = edge.getSource();
		CyNode target = edge.getTarget();
			
		CyNodeViewImpl sourceView = dataSuidToNode.getOrElse(source.getSUID(), null);
		CyNodeViewImpl targetView = dataSuidToNode.getOrElse(target.getSUID(), null);
		
		if(sourceView == null || targetView == null) {
			// corner case, its possible that the event for adding an edge came before the source/target node events
			addNode(source);
			addNode(target);
			// addNode() returns null if another thread added the node, so must lookup in map again
			sourceView = dataSuidToNode.getOrElse(source.getSUID(), null);
			targetView = dataSuidToNode.getOrElse(target.getSUID(), null);
		}
			
		CyEdgeViewImpl view;
		
		synchronized (edgeLock) {
			if(dataSuidToEdge.containsKey(edge.getSUID()))
				return null;
			
			view = new CyEdgeViewImpl(this, edge, sourceView.getSUID(), targetView.getSUID());
		
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
//					eventHelper.addEventPayload(this, nodeView, AboutToRemoveNodeViewsEvent.class);
					
					dataSuidToNode = dataSuidToNode.remove(model.getSUID());
					viewSuidToNode = viewSuidToNode.remove(nodeView.getSUID());
					Set<CyEdgeViewImpl> adjacentEdges = adjacentEdgeMap.getOrElse(nodeView.getSUID(), HashSet.empty());
					for(CyEdgeViewImpl adjacentEdge : adjacentEdges) {
						removeEdge(adjacentEdge.getModel());
					}
					adjacentEdgeMap = adjacentEdgeMap.remove(nodeView.getSUID());
					nodeVPs.remove(nodeView.getSUID());
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
//				eventHelper.addEventPayload(this, edgeView, AboutToRemoveEdgeViewsEvent.class);
				
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
	
	public CyEdgeViewImpl getEdgeView(long suid) {
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
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V defaultValue) {
		if(vp.shouldIgnoreDefault())
			return;
		
		if(vp.getTargetDataType().equals(CyNode.class)) {
			synchronized(nodeLock) {
				nodeVPs.setViewDefault(vp, defaultValue);
				if(nodeVPs.getConfig().isTracked(vp)) {
					netVPs.updateTrackedVP(getSUID(), vp);
				}
			}
		} else if(vp.getTargetDataType().equals(CyEdge.class)) {
			synchronized(edgeLock) {
				edgeVPs.setViewDefault(vp, defaultValue);
				if(edgeVPs.getConfig().isTracked(vp)) {
					for(Tuple2<Long,?> t : viewSuidToEdge) {
						edgeVPs.updateTrackedVP(t._1, vp);
					}
				}
				// This is hard-coded to mimic legacy Ding behaviour to prevent some build tests from failing.
				if(vp == BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT) {
					setViewDefault(BasicVisualLexicon.EDGE_SELECTED_PAINT, defaultValue);
				}
				else if(vp == BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT) {
					setViewDefault(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, defaultValue);
				}
			}
		} else if(vp.getTargetDataType().equals(CyNetwork.class)) {
			synchronized(netLock) {
				netVPs.setViewDefault(vp, defaultValue);
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
		setDirty();
	}

	@Override
	public void fitSelected() {
		for(CyNetworkViewListener listener : listeners) {
			listener.handleFitSelected();
		}
		setDirty();
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
		setDirty();
	}
	
	@Override
	public void dispose() {
		for(CyNetworkViewListener listener : listeners) {
			listener.handleDispose();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void fireViewChangedEvent(VisualProperty<?> vp, Object value, boolean lockedValue) {
		var record = new ViewChangeRecord<>(this, vp, value, lockedValue);
		getEventHelper().addEventPayload(this, record, ViewChangedEvent.class);
	}
}
