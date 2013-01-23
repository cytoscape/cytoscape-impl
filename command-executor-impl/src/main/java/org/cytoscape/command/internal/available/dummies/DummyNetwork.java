package org.cytoscape.command.internal.available.dummies;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.cytoscape.model.*;

import java.util.Collection;
import java.util.List;

public class DummyNetwork implements CyNetwork {
	@Override public Long getSUID() { return null; }
	@Override public CyNode addNode() {return null;}
	@Override public boolean removeNodes(Collection<CyNode> node) {return false;}
	@Override public CyEdge addEdge(CyNode source, CyNode target, boolean isDirected) {return null;}
	@Override public boolean removeEdges(Collection<CyEdge> edges) {return false;}
	@Override public int getNodeCount() {return 0;}
	@Override public int getEdgeCount() {return 0;}
	@Override public List<CyNode> getNodeList() {return null;}
	@Override public List<CyEdge> getEdgeList() {return null;}
	@Override public boolean containsNode(CyNode node) {return false;}
	@Override public boolean containsEdge(CyEdge edge) {return false;}
	@Override public boolean containsEdge(CyNode from, CyNode to) {return false;}
	@Override public CyNode getNode(long index) {return null;}
	@Override public CyEdge getEdge(long index) {return null;}
	@Override public List<CyNode> getNeighborList(CyNode node, CyEdge.Type edgeType) {return null;}
	@Override public List<CyEdge> getAdjacentEdgeList(CyNode node, CyEdge.Type edgeType) {return null;}
	@Override public Iterable<CyEdge> getAdjacentEdgeIterable(CyNode node, CyEdge.Type edgeType) {return null;}
	@Override public List<CyEdge> getConnectingEdgeList(CyNode source, CyNode target, CyEdge.Type edgeType) {return null;}
	@Override public CyTable getDefaultNetworkTable() {return null;}
	@Override public CyTable getDefaultNodeTable() {return null;}
	@Override public CyTable getDefaultEdgeTable() {return null;}
	@Override public CyTable getTable(Class <?extends CyIdentifiable> type, String namespace) {return null;}
	@Override public CyRow getRow(CyIdentifiable entry, String tableName) {return null;}
	@Override public CyRow getRow(CyIdentifiable entry) {return null;}
	@Override public SavePolicy getSavePolicy() {return SavePolicy.DO_NOT_SAVE;}
	@Override public void dispose() {}
}
