package org.cytoscape.graph.render.stateful;

import java.awt.geom.Rectangle2D;

import org.cytoscape.ding.impl.NetworkTransform;
import org.cytoscape.model.CyEdge;
import org.cytoscape.util.intr.LongHash;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;

/**
 * The GraphLOD combined with the number of visible nodes/edges tells us exactly what level of
 * detail to actually render.
 */
public class RenderDetailFlags {
	
	public final static int LOD_HIGH_DETAIL  = 0x1;
	public final static int LOD_NODE_BORDERS = 0x2;
	public final static int LOD_NODE_LABELS  = 0x4;
	public final static int LOD_EDGE_ARROWS  = 0x8;
	public final static int LOD_DASHED_EDGES = 0x10;
	public final static int LOD_EDGE_ANCHORS = 0x20;
	public final static int LOD_EDGE_LABELS  = 0x40;
	public final static int LOD_TEXT_AS_SHAPE = 0x80;
	public final static int LOD_CUSTOM_GRAPHICS = 0x100;

	
	private final int lodBits;
	private final int renderEdges;
	
	private RenderDetailFlags(int lodBits, int renderEdges) {
		this.lodBits = lodBits;
		this.renderEdges = renderEdges;
	}
	
	public int renderEdges() {
		return renderEdges;
	}
	
	public boolean not(int flag) {
		return (lodBits & flag) == 0;
	}
	
	public boolean has(int flag) {
		return (lodBits & flag) != 0;
	}
	
	
	public static RenderDetailFlags create(CyNetworkViewSnapshot netView, NetworkTransform transform, GraphLOD lod, EdgeDetails edgeDetails) {
		Rectangle2D.Float area = transform.getNetworkVisibleAreaInNodeCoords();
		SpacialIndex2DEnumerator<Long> nodeHits = netView.getSpacialIndex2D().queryOverlap(area.x, area.y, area.x + area.width, area.y + area.height);
		
		final int renderNodeCount;
		final int renderEdgeCount;
		final int visibleNodeCount = nodeHits.size();
		final int totalNodeCount = netView.getNodeCount();
		final int totalEdgeCount = netView.getEdgeCount();
		final byte renderEdges = lod.renderEdges(visibleNodeCount, totalNodeCount, totalEdgeCount);
		
		final float[] floatBuff = new float[4];
		
		if (renderEdges > 0) {
			int runningNodeCount = 0;
			while (nodeHits.hasNext()) {
				nodeHits.nextExtents(floatBuff);
				if ((floatBuff[0] != floatBuff[2]) && (floatBuff[1] != floatBuff[3]))
					runningNodeCount++;
			}

			renderNodeCount = runningNodeCount;
			renderEdgeCount = totalEdgeCount;
		} else if (renderEdges < 0) {
			int runningNodeCount = 0;
			while (nodeHits.hasNext()) {
				nodeHits.nextExtents(floatBuff);
				if ((floatBuff[0] != floatBuff[2]) && (floatBuff[1] != floatBuff[3]))
					runningNodeCount++;
			}

			renderNodeCount = runningNodeCount;
			renderEdgeCount = 0;
		} else {
			int runningNodeCount = 0;
			int runningEdgeCount = 0;
			LongHash nodeBuff = new LongHash();
			
			while (nodeHits.hasNext()) {
				final long nodeSuid = nodeHits.nextExtents(floatBuff);

				if ((floatBuff[0] != floatBuff[2]) && (floatBuff[1] != floatBuff[3]))
					runningNodeCount++;

				Iterable<View<CyEdge>> touchingEdges = netView.getAdjacentEdgeIterable(nodeSuid);

				for ( View<CyEdge> e : touchingEdges ) {
					SnapshotEdgeInfo edgeInfo = netView.getEdgeInfo(e);
					if (!edgeDetails.isVisible(e))
						continue;
					long otherNode = nodeSuid ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();
					if (nodeBuff.get(otherNode) < 0)
						runningEdgeCount++;
				}
				nodeBuff.put(nodeSuid);
			}

			renderNodeCount = runningNodeCount;
			renderEdgeCount = runningEdgeCount;
			nodeBuff.empty();
		}
		
		int lodBits = lodToBits(renderNodeCount, renderEdgeCount, lod);
		return new RenderDetailFlags(lodBits, renderEdges);
	}
	
	
	private static int lodToBits(int renderNodeCount, int renderEdgeCount, GraphLOD lod) {
		int lodTemp = 0;
		if (lod.detail(renderNodeCount, renderEdgeCount)) {
			lodTemp |= LOD_HIGH_DETAIL;
			if (lod.nodeBorders(renderNodeCount, renderEdgeCount))
				lodTemp |= LOD_NODE_BORDERS;
			if (lod.nodeLabels(renderNodeCount, renderEdgeCount))
				lodTemp |= LOD_NODE_LABELS;
			if (lod.edgeArrows(renderNodeCount, renderEdgeCount))
				lodTemp |= LOD_EDGE_ARROWS;
			if (lod.dashedEdges(renderNodeCount, renderEdgeCount))
				lodTemp |= LOD_DASHED_EDGES;
			if (lod.edgeAnchors(renderNodeCount, renderEdgeCount))
				lodTemp |= LOD_EDGE_ANCHORS;
			if (lod.edgeLabels(renderNodeCount, renderEdgeCount))
				lodTemp |= LOD_EDGE_LABELS;
			if ((((lodTemp & LOD_NODE_LABELS) != 0) || ((lodTemp & LOD_EDGE_LABELS) != 0)) && lod.textAsShape(renderNodeCount, renderEdgeCount))
				lodTemp |= LOD_TEXT_AS_SHAPE;
			if (lod.customGraphics(renderNodeCount, renderEdgeCount))
				lodTemp |= LOD_CUSTOM_GRAPHICS;
		}
		return lodTemp;
	}

}
