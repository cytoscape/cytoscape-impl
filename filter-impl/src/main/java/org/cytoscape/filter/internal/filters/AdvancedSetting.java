package org.cytoscape.filter.internal.filters;


//Advanced settings
public class AdvancedSetting {
	// default settings
	private boolean session = true, global=false; //scope
	private boolean node=false, edge=false; // selectionType
	private boolean source=true, target=true; // EdgeType
	private Relation relation = Relation.AND;
		
	public String toString() {
		String retStr = "<AdvancedSetting>\n";
		retStr += "scope.global=";
		if (global) {
			retStr += "true\n";
		}
		else {
			retStr += "false\n";
		}
		retStr += "scope.session=";
		if (session) {
			retStr += "true\n";
		}
		else {
			retStr += "false\n";
		}
		retStr += "selection.node=";
		if (node) {
			retStr += "true\n";
		}
		else {
			retStr += "false\n";
		}
		retStr += "selection.edge=";
		if (edge) {
			retStr += "true\n";
		}
		else {
			retStr += "false\n";
		}
		
		retStr += "edge.source=";
		if (source) {
			retStr += "true\n";
		}
		else {
			retStr += "false\n";
		}
		retStr += "edge.target=";
		if (target) {
			retStr += "true\n";
		}
		else {
			retStr += "false\n";
		}

		retStr += "Relation="+relation+ "\n";
		retStr += "</AdvancedSetting>";
		return retStr;
	}

	public Relation getRelation()
	{
		return relation;
	}
	
	public void setRelation(Relation pRelation)
	{
		relation = pRelation;
	}

	public boolean isSessionChecked()
	{
		return session;
	}
	public void setSession(boolean pSession)
	{
		session = pSession;
	}
	
	public boolean isGlobalChecked()
	{
		return global;
	}
	public void setGlobal(boolean pGlobal)
	{
		global = pGlobal;
	}

	public boolean isNodeChecked()
	{
		return node;
	}
	public void setNode(boolean pNode)
	{
		node = pNode;
	}
	public boolean isEdgeChecked()
	{
		return edge;
	}
	public void setEdge(boolean pEdge)
	{
		edge = pEdge;
	}
	public void setSource(boolean pSource)
	{
		source = pSource;
	}
	public void setTarget(boolean pTarget)
	{
		target = pTarget;
	}
	
	public boolean isSourceChecked()
	{
		return source;
	}
	public boolean isTargetChecked()
	{
		return target;
	}
	
	
}//End of Advanced settings
