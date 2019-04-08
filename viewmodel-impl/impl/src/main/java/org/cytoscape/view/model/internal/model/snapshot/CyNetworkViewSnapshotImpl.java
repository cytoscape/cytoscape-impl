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
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.SnapshotNodeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.model.CyEdgeViewImpl;
import org.cytoscape.view.model.internal.model.CyNodeViewImpl;
import org.cytoscape.view.model.internal.model.VPNetworkStore;
import org.cytoscape.view.model.internal.model.VPStore;
import org.cytoscape.view.model.internal.model.spacial.SpacialIndex2DSnapshotImpl;
import org.cytoscape.view.model.spacial.SpacialIndex2D;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;

import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public class CyNetworkViewSnapshotImpl extends CyViewSnapshotBase<CyNetwork> implements CyNetworkViewSnapshot {
	
	private final String rendererId;
	private final CyNetworkView networkView;

	// View object is stored twice, using both the view suid and model suid as keys.
	private final Map<Long,CyNodeViewImpl> dataSuidToNode;
	private final Map<Long,CyNodeViewImpl> viewSuidToNode;
	private final Map<Long,CyEdgeViewImpl> dataSuidToEdge;
	private final Map<Long,CyEdgeViewImpl> viewSuidToEdge;
	
	// Key is SUID of View object
	private final Map<Long,Set<CyEdgeViewImpl>> adjacentEdgeMap;
	
	protected final VPStore nodeVPs;
	protected final VPStore edgeVPs;
	protected final VPNetworkStore netVPs;
	
	private final SpacialIndex2D<Long> spacialIndex;
	
	// Store of immutable node/edge objects
	// MKTODO these objects probably won't change much between snapshots, they don't actually store the VPs,
	// come up with a strategy to reuse them between snapshots.
	private final java.util.Map<Long,CyNodeViewSnapshotImpl> snapshotNodeViews = new java.util.HashMap<>();
	private final java.util.Map<Long,CyEdgeViewSnapshotImpl> snapshotEdgeViews = new java.util.HashMap<>();
	
	
	public CyNetworkViewSnapshotImpl(
			CyNetworkView networkView,
			String rendererId,
			Map<Long,CyNodeViewImpl> dataSuidToNode,
			Map<Long,CyNodeViewImpl> viewSuidToNode,
			Map<Long,CyEdgeViewImpl> dataSuidToEdge,
			Map<Long,CyEdgeViewImpl> viewSuidToEdge,
			Map<Long,Set<CyEdgeViewImpl>> adjacentEdgeMap,
			VPStore nodeVPs,
			VPStore edgeVPs,
			VPNetworkStore netVPs,
			RTree<Long,Rectangle> rtree,
			Map<Long,Rectangle> geometries
	) {
		super(networkView.getSUID());
		this.networkView = networkView;
		this.rendererId = rendererId;
		this.viewSuidToNode = viewSuidToNode;
		this.dataSuidToNode = dataSuidToNode;
		this.viewSuidToEdge = viewSuidToEdge;
		this.dataSuidToEdge = dataSuidToEdge;
		this.adjacentEdgeMap = adjacentEdgeMap;
		this.nodeVPs = nodeVPs;
		this.edgeVPs = edgeVPs;
		this.netVPs = netVPs;
		this.spacialIndex = new SpacialIndex2DSnapshotImpl(rtree, geometries);
	}
	
	@Override
	public VPStore getVPStore() {
		return netVPs;
	}
	
	private boolean isNodeVisible(Long nodeSuid) {
		return spacialIndex.exists(nodeSuid);
	}
	
	private boolean isEdgeVisible(CyEdgeViewImpl mutableEdgeView) {
		if(!Boolean.TRUE.equals(edgeVPs.getVisualProperty(mutableEdgeView.getSUID(), BasicVisualLexicon.EDGE_VISIBLE)))
			return false;
		if(!isNodeVisible(mutableEdgeView.getSourceSuid()))
			return false;
		if(!isNodeVisible(mutableEdgeView.getTargetSuid()))
			return false;
		return true;
	}
	

	@Override
	protected <T> T getSpecialVisualProperty(Long suid, VisualProperty<T> vp) {
		if(getSUID().equals(suid)) {
			return netVPs.getSpecialVisualProperty(suid, vp);
		}
		return null;
	}
	
	/**
	 * Returns null if the node is not visible.
	 */
	protected CyNodeViewSnapshotImpl getSnapshotNodeView(CyNodeViewImpl mutableNodeView) {
		Long suid = mutableNodeView.getSUID();
		if(snapshotNodeViews.containsKey(suid)) {
			return snapshotNodeViews.get(suid);
		}
		
		CyNodeViewSnapshotImpl view = null;
		if(isNodeVisible(suid)) {
			view = new CyNodeViewSnapshotImpl(this, mutableNodeView);
		}
		snapshotNodeViews.put(suid, view); // maps to null if not visible
		return view;
	}
	
	protected CyEdgeViewSnapshotImpl getSnapshotEdgeView(CyEdgeViewImpl mutableEdgeView) {
		Long suid = mutableEdgeView.getSUID();
		if(snapshotEdgeViews.containsKey(suid)) {
			return snapshotEdgeViews.get(suid);
		}
		
		CyEdgeViewSnapshotImpl view = null;
		if(isEdgeVisible(mutableEdgeView)) {
			view = new CyEdgeViewSnapshotImpl(this, mutableEdgeView);
		}
		snapshotEdgeViews.put(suid, view); // maps to null if not visible
		return view;
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
	public SpacialIndex2D<Long> getSpacialIndex2D() {
		return spacialIndex;
	}
	
	@Override
	public <T> T getViewDefault(VisualProperty<T> vp) {
		if(vp.getTargetDataType().equals(CyNode.class)) {
			return nodeVPs.getViewDefault(vp);
		} else if(vp.getTargetDataType().equals(CyEdge.class)) {
			return edgeVPs.getViewDefault(vp);
		} else if(vp.getTargetDataType().equals(CyNetwork.class)) {
			return netVPs.getViewDefault(vp);
		}
		return vp.getDefault();
	}
	
	// MKTODO
	// We cannot return the mutable version of the node and edge views, so we must create snapshot versions. 
	
	@Override
	public View<CyNode> getNodeView(CyNode node) {
		CyNodeViewImpl view = dataSuidToNode.getOrElse(node.getSUID(), null);
		return view == null ? null : getSnapshotNodeView(view);
	}

	@Override
	public View<CyEdge> getEdgeView(CyEdge edge) {
		CyEdgeViewImpl view = dataSuidToEdge.getOrElse(edge.getSUID(), null);
		return view == null ? null : getSnapshotEdgeView(view);
	}
	
	@Override
	public View<CyNode> getNodeView(long suid) {
		CyNodeViewImpl view = viewSuidToNode.getOrElse(suid, null);
		return view == null ? null : getSnapshotNodeView(view);
	}
	
	@Override
	public View<CyEdge> getEdgeView(long suid) {
		CyEdgeViewImpl view = viewSuidToEdge.getOrElse(suid, null);
		return view == null ? null : getSnapshotEdgeView(view);
	}

	@Override
	public Collection<View<CyNode>> getNodeViews() {
		List<View<CyNode>> nodeViews = new ArrayList<>(viewSuidToNode.size());
		for(CyNodeViewImpl view : viewSuidToNode.values()) {
			CyNodeViewSnapshotImpl nv = getSnapshotNodeView(view);
			if(nv != null) {
				nodeViews.add(nv);
			}
		}
		return nodeViews;
	}
	
	@Override
	public Collection<View<CyEdge>> getEdgeViews() {
		List<View<CyEdge>> edgeViews = new ArrayList<>(viewSuidToEdge.size());
		for(CyEdgeViewImpl view : viewSuidToEdge.values()) {
			CyEdgeViewSnapshotImpl ev = getSnapshotEdgeView(view);
			if(ev != null) {
				edgeViews.add(ev);
			}
		}
		return edgeViews;
	}
	
	@Override
	public Collection<View<? extends CyIdentifiable>> getAllViews() {
		ArrayList<View<? extends CyIdentifiable>> list = new ArrayList<>();
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
		return viewSuidToNode.size();
	}

	@Override
	public int getEdgeCount() {
		return viewSuidToEdge.size();
	}

	@Override
	public Iterable<View<CyEdge>> getAdjacentEdgeIterable(View<CyNode> nodeView) {
		return getAdjacentEdgeIterable(nodeView.getSUID());
	}
	
	@Override
	public Iterable<View<CyEdge>> getAdjacentEdgeIterable(long nodeSuid) {
		List<View<CyEdge>> result = new ArrayList<>();
		for(CyEdgeViewImpl edgeViewImpl : adjacentEdgeMap.getOrElse(nodeSuid,HashSet.empty())) {
			CyEdgeViewSnapshotImpl ev = getSnapshotEdgeView(edgeViewImpl);
			if(ev != null) {
				result.add(ev);
			}
		}
		return result;
	}
	
	@Override
	public SnapshotEdgeInfo getEdgeInfo(View<CyEdge> edge) {
		return (SnapshotEdgeInfo) edge;
	}
	
	@Override
	public SnapshotNodeInfo getNodeInfo(View<CyNode> node) {
		return (SnapshotNodeInfo) node;
	}
	
	@Override
	public Collection<View<CyNode>> getSelectedNodes() {
		java.util.HashSet<View<CyNode>> nodes = new java.util.HashSet<>();
		for(Long suid : nodeVPs.getSelected()) {
			View<CyNode> nv = getNodeView(suid);
			if(nv != null) {
				nodes.add(nv);
			}
		}
		return nodes;
	}

	@Override
	public Collection<View<CyEdge>> getSelectedEdges() {
		java.util.HashSet<View<CyEdge>> nodes = new java.util.HashSet<>();
		for(Long suid : edgeVPs.getSelected()) {
			View<CyEdge> ev = getEdgeView(suid);
			if(ev != null) {
				nodes.add(ev);
			}
		}
		return nodes;
	}

	@Override
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V defaultValue) {
		throw new UnsupportedOperationException("Cannot modify view snapshot");
	}

	@Override
	public void dispose() {
	}

}
