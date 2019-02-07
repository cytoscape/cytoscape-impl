package org.cytoscape.view.model.internal.model.snapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.ReadableView;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.model.CyEdgeViewImpl;
import org.cytoscape.view.model.internal.model.CyNodeViewImpl;
import org.cytoscape.view.model.internal.model.CyViewImpl;
import org.cytoscape.view.model.spacial.SpacialIndex2D;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public class CyNetworkViewSnapshotImpl extends CyViewSnapshotBase<CyNetwork> implements CyNetworkViewSnapshot {
	
	private final String rendererId;
	private final CyNetworkView networkView;

	// Key is SUID of underlying model object.
	private final Map<Long,CyNodeViewImpl> nodeViewMap;
	private final Map<Long,CyEdgeViewImpl> edgeViewMap;
	
	// Key is SUID of View object
	private final Map<Long,Set<CyEdgeViewImpl>> adjacentEdgeMap;
		
	// Key is SUID of View object
	private final Map<Long,Map<VisualProperty<?>,Object>> visualProperties;
	private final Map<Long,Map<VisualProperty<?>,Object>> allLocks;
	private final Map<Long,Map<VisualProperty<?>,Object>> directLocks;
	private final Map<VisualProperty<?>,Object> defaultValues;
	
	private final SpacialIndex2D spacialIndex;
	
	
	public CyNetworkViewSnapshotImpl(
			CyNetworkView networkView,
			String rendererId, 
			Map<Long,CyNodeViewImpl> nodeViewMap,
			Map<Long,CyEdgeViewImpl> edgeViewMap,
			Map<Long,Set<CyEdgeViewImpl>> adjacentEdgeMap,
			Map<VisualProperty<?>, Object> defaultValues,
			Map<Long, Map<VisualProperty<?>, Object>> visualProperties,
			Map<Long, Map<VisualProperty<?>, Object>> allLocks,
			Map<Long, Map<VisualProperty<?>, Object>> directLocks,
			RTree<Long,Rectangle> rtree,
			Map<Long,Rectangle> geometries
	) {
		super(networkView.getSUID());
		this.networkView = networkView;
		this.rendererId = rendererId;
		this.nodeViewMap = nodeViewMap;
		this.edgeViewMap = edgeViewMap;
		this.adjacentEdgeMap = adjacentEdgeMap;
		this.defaultValues = defaultValues;
		this.visualProperties = visualProperties;
		this.allLocks = allLocks;
		this.directLocks = directLocks;
		this.spacialIndex = new SpacialIndex2DImpl(this, rtree, geometries);
	}
	
	
	@Override
	public CyNetworkViewSnapshotImpl getNetworkSnapshot() {
		return this;
	}
	
	@Override
	public CyNetworkView getMutableNetworkView() {
		return networkView;
	}
	
	@Override
	public SpacialIndex2D getSpacialIndex2D() {
		return spacialIndex;
	}
	
	public Map<VisualProperty<?>, Object> getVisualProperties(Long suid) {
		return visualProperties.getOrElse(suid, HashMap.empty());
	}
	
	public Map<VisualProperty<?>, Object> getAllLocks(Long suid) {
		return allLocks.getOrElse(suid, HashMap.empty());
	}
	
	public Map<VisualProperty<?>, Object> getDirectLocks(Long suid) {
		return directLocks.getOrElse(suid, HashMap.empty());
	}

	public Map<VisualProperty<?>,Object> getDefaultValues() {
		return defaultValues;
	}

	
	// MKTODO
	// We cannot return the mutable version of the node and edge views, so we must create
	// snapshot versions. Note they are not cached yet, perhaps caching will be needed, 
	// but no need to add it until necessary.
	
	@Override
	public ReadableView<CyNode> getNodeView(CyNode node) {
		CyViewImpl<CyNode> view = nodeViewMap.getOrElse(node.getSUID(), null);
		if(view == null)
			return null;
		return new CyViewSnapshotImpl<CyNode>(this, view);
	}

	@Override
	public Collection<ReadableView<CyNode>> getNodeViews() {
		List<ReadableView<CyNode>> nodeViews = new ArrayList<>(nodeViewMap.size());
		for(CyViewImpl<CyNode> view : nodeViewMap.values()) {
			nodeViews.add(new CyViewSnapshotImpl<CyNode>(this, view));
		}
		return nodeViews;
	}

	@Override
	public ReadableView<CyEdge> getEdgeView(CyEdge edge) {
		CyEdgeViewImpl view = edgeViewMap.getOrElse(edge.getSUID(), null);
		if(view == null)
			return null;
		return new CyEdgeViewSnapshotImpl(this, view);
	}

	@Override
	public Collection<ReadableView<CyEdge>> getEdgeViews() {
		List<ReadableView<CyEdge>> edgeViews = new ArrayList<>(edgeViewMap.size());
		for(CyEdgeViewImpl view : edgeViewMap.values()) {
			edgeViews.add(new CyEdgeViewSnapshotImpl(this, view));
		}
		return edgeViews;
	}

	@Override
	public Collection<ReadableView<? extends CyIdentifiable>> getAllViews() {
		ArrayList<ReadableView<? extends CyIdentifiable>> list = new ArrayList<>();
		list.addAll(getNodeViews());
		list.addAll(getEdgeViews());
		list.add(this);
		return list;
	}


	@Override
	public String getRendererId() {
		return rendererId;
	}

	@Override
	public int getNodeCount() {
		return nodeViewMap.size();
	}

	@Override
	public int getEdgeCount() {
		return edgeViewMap.size();
	}

	@Override
	public ReadableView<CyNode> getNodeView(long suid) {
		return nodeViewMap.getOrElse(suid, null);
	}

	@Override
	public ReadableView<CyEdge> getEdgeView(long suid) {
		return edgeViewMap.getOrElse(suid, null);
	}

	@Override
	public Iterable<ReadableView<CyEdge>> getAdjacentEdgeIterable(ReadableView<CyNode> nodeView) {
		return adjacentEdgeMap
				.getOrElse(nodeView.getSUID(), HashSet.empty())
				.map(view -> new CyEdgeViewSnapshotImpl(this, view));
	}
	
	
	@Override
	public SnapshotEdgeInfo getEdgeInfo(ReadableView<CyEdge> edge) {
		return (SnapshotEdgeInfo) edge;
	}
	
	@Override
	public Collection<ReadableView<CyNode>> getSelectedNodes() {
		// MKTODO
		return null;
	}
	

}
