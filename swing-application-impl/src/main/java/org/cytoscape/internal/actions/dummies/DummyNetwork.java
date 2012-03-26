
package org.cytoscape.internal.actions.dummies;

import org.cytoscape.model.*;
import java.util.Collection;
import java.util.List;

public class DummyNetwork implements CyNetwork {
	public Long getSUID() { return null; }
	public CyNode addNode() {return null;}
	public boolean removeNodes(Collection<CyNode> node) {return false;}
	public CyEdge addEdge(CyNode source, CyNode target, boolean isDirected) {return null;}
	public boolean removeEdges(Collection<CyEdge> edges) {return false;}
	public int getNodeCount() {return 0;}
	public int getEdgeCount() {return 0;}
	public List<CyNode> getNodeList() {return null;}
	public List<CyEdge> getEdgeList() {return null;}
	public boolean containsNode(CyNode node) {return false;}
	public boolean containsEdge(CyEdge edge) {return false;}
	public boolean containsEdge(CyNode from, CyNode to) {return false;}
	public CyNode getNode(int index) {return null;}
	public CyEdge getEdge(int index) {return null;}
	public List<CyNode> getNeighborList(CyNode node, CyEdge.Type edgeType) {return null;}
	public List<CyEdge> getAdjacentEdgeList(CyNode node, CyEdge.Type edgeType) {return null;}
	public Iterable<CyEdge> getAdjacentEdgeIterable(CyNode node, CyEdge.Type edgeType) {return null;}
	public List<CyEdge> getConnectingEdgeList(CyNode source, CyNode target, CyEdge.Type edgeType) {return null;}
	public CyTable getDefaultNetworkTable() {return null;}
	public CyTable getDefaultNodeTable() {return null;}
	public CyTable getDefaultEdgeTable() {return null;}
	public CyRow getRow(CyIdentifiable entry, String tableName) {return null;}
	public CyRow getRow(CyIdentifiable entry) {return null;}
}
