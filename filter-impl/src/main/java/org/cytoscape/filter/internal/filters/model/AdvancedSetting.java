package org.cytoscape.filter.internal.filters.model;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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


//Advanced settings
public class AdvancedSetting {
	// default settings
	private boolean session = true, global = false; // scope
	private boolean node = false, edge = false; // selectionType
	private boolean source = true, target = true; // EdgeType
	private Relation relation = Relation.AND;

	public String toString() {
		String retStr = "<AdvancedSetting>\n";
		retStr += "scope.global=" + global + "\n";
		retStr += "scope.session=" + session + "\n";
		retStr += "selection.node=" + node + "\n";
		retStr += "selection.edge=" + edge + "\n";
		retStr += "edge.source=" + source + "\n";
		retStr += "edge.target=" + target + "\n";
		retStr += "Relation=" + relation + "\n";
		retStr += "</AdvancedSetting>";
		return retStr;
	}

	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation pRelation) {
		relation = pRelation;
	}

	public boolean isSessionChecked() {
		return session;
	}

	public void setSession(boolean pSession) {
		session = pSession;
	}

	public boolean isGlobalChecked() {
		return global;
	}

	public void setGlobal(boolean pGlobal) {
		global = pGlobal;
	}

	public boolean isNodeChecked() {
		return node;
	}

	public void setNode(boolean pNode) {
		node = pNode;
	}

	public boolean isEdgeChecked() {
		return edge;
	}

	public void setEdge(boolean pEdge) {
		edge = pEdge;
	}

	public void setSource(boolean pSource) {
		source = pSource;
	}

	public void setTarget(boolean pTarget) {
		target = pTarget;
	}

	public boolean isSourceChecked() {
		return source;
	}

	public boolean isTargetChecked() {
		return target;
	}

}// End of Advanced settings
