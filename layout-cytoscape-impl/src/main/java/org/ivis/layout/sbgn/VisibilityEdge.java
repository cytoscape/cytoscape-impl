package org.ivis.layout.sbgn;

import org.ivis.layout.cose.CoSEEdge;

/**
 * This class implements Visibility graph specific length update process for
 * edges.
 * 
 * @author Begum Genc
 * 
 *         Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class VisibilityEdge extends CoSEEdge
{

	/**
	 * Constructor
	 */
	public VisibilityEdge(SbgnPDNode source, SbgnPDNode target, Object vEdge)
	{
		super(source, target, vEdge);
	}

	/**
	 * We want the length of a visibility edge calculated as the distance
	 * between borders of two nodes, not the distance between center points.
	 * Edges have to be vertical or horizontal.
	 */
	@Override
	public void updateLength()
	{
		if (this.source.getBottom() <= this.target.getTop())
		{
			this.lengthX = 0;
			this.lengthY = this.source.getBottom() - this.target.getTop();
		} else if (this.source.getRight() <= this.target.getLeft())
		{
			this.lengthX = this.source.getRight() - this.target.getLeft();
			this.lengthY = 0;
		}
		// else
		// System.out.println("unexpected edge");

		this.length = Math.sqrt(this.lengthX * this.lengthX + this.lengthY
				* this.lengthY);
	}
}
