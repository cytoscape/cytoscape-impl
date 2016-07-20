package org.ivis.layout.sbgn;

import org.ivis.layout.cose.CoSEEdge;

/**
 * This class implements SBGN Process Diagram specific data and functionality
 * for edges.
 * 
 * @author Begum Genc
 * 
 *         Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SbgnPDEdge extends CoSEEdge
{
	public int correspondingAngle;
	public boolean isProperlyOriented;

	/**
	 * Constructor
	 */
	public SbgnPDEdge(SbgnPDNode source, SbgnPDNode target, Object vEdge)
	{
		super(source, target, vEdge);
		correspondingAngle = 0;
		isProperlyOriented = false;
	}

	public SbgnPDEdge(SbgnPDNode source, SbgnPDNode target, Object vEdge, String type)
	{
		super(source, target, vEdge);
		this.type = type;
		correspondingAngle = 0;
		isProperlyOriented = false;
	}

	public void copy(SbgnPDEdge edge)
	{
		this.setSource(edge.getSource());
		this.setTarget(edge.getTarget());
		this.label = edge.label;
		this.type = edge.type;
		this.correspondingAngle = edge.correspondingAngle;
		this.isProperlyOriented = edge.isProperlyOriented;
		this.idealLength = edge.idealLength;
		this.isInterGraph = edge.isInterGraph;
		this.bendpoints = edge.bendpoints;
		this.isOverlapingSourceAndTarget = edge.isOverlapingSourceAndTarget;
		this.lca = edge.lca;
		this.length = edge.length;
		this.lengthX = edge.lengthX;
		this.lengthY = edge.lengthY;
		this.sourceInLca = edge.sourceInLca;
	}
	
	public boolean isEffector()
	{
		if(this.type.equals(SbgnPDConstants.MODULATION) || 
				this.type.equals(SbgnPDConstants.STIMULATION) || 
				this.type.equals(SbgnPDConstants.CATALYSIS) || 
				this.type.equals(SbgnPDConstants.INHIBITION) || 
				this.type.equals(SbgnPDConstants.NECESSARY_STIMULATION))
			return true;
		
		return false;
	}
	
	public boolean isRigidEdge()
	{
		if(this.type.equals(SbgnPDConstants.RIGID_EDGE))
			return true;
		else
			return false;
	}
}
