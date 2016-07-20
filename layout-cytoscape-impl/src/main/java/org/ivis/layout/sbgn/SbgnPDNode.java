package org.ivis.layout.sbgn;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraph;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;
import org.ivis.layout.LayoutConstants;
import org.ivis.layout.cose.CoSENode;
import org.ivis.util.PointD;

/**
 * This class implements SBGN specific data and functionality for nodes.
 * 
 * @author Begum Genc
 * 
 *         Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SbgnPDNode extends CoSENode
{
	/**
	 * This parameter is used in DFS to find ordering of the complex members.
	 */
	public boolean visited;

	/**
	 * This parameter
	 */
	public boolean isDummyCompound;

	/**
	 * Constructor
	 */
	public SbgnPDNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
		this.visited = false;
		this.isDummyCompound = false;
	}

	/**
	 * Alternative constructor
	 */
	public SbgnPDNode(LGraphManager gm, Point loc, Dimension size, LNode vNode,
			String type)
	{
		super(gm, loc, size, vNode);
		this.type = type;
		this.visited = false;
		this.label = vNode.label;
		this.isDummyCompound = false;
	}

	public void copyNode(SbgnPDNode s, LGraphManager graphManager)
	{
		this.type = s.type;
		this.label = s.label;
		this.setCenter(s.getCenterX(), s.getCenterY());
		this.setChild(s.getChild());
		this.setHeight(s.getHeight());
		this.setLocation(s.getLocation().x, s.getLocation().y);
		this.setNext(s.getNext());
		this.setOwner(s.getOwner());
		this.setPred1(s.getPred1());
		this.setPred2(s.getPred2());
		this.setWidth(s.getWidth());
	}

	public double getSpringForceX()
	{
		return this.springForceX;
	}

	public boolean isComplex()
	{
		return type.equalsIgnoreCase(SbgnPDConstants.COMPLEX);
	}
	
	public boolean isInputPort()
	{
		return type.equalsIgnoreCase(SbgnPDConstants.INPUT_PORT);
	}
	
	public boolean isOutputPort()
	{
		return type.equalsIgnoreCase(SbgnPDConstants.OUTPUT_PORT);
	}
	
		

	/**
	 * This method checks if the given node contains any unmarked complex nodes
	 * in its child graph.
	 * 
	 * @return true - if there are unmarked complex nodes false - otherwise
	 */
	public boolean containsUnmarkedComplex()
	{
		if (this.getChild() == null)
			return false;
		else
		{
			for (Object child : this.getChild().getNodes())
			{
				SbgnPDNode sbgnChild = (SbgnPDNode) child;

				if (sbgnChild.isComplex() && !sbgnChild.visited)
					return true;
			}
			return false;
		}
	}

	public void resetForces()
	{
		this.springForceX = 0;
		this.springForceY = 0;
		this.repulsionForceX = 0;
		this.repulsionForceY = 0;
	}

	protected void rotateNode(PointD origin, int rotationDegree)
	{
		PointD relativePt = new PointD(this.getCenterX() - origin.x,
				this.getCenterY() - origin.y);
		PointD rotatedPt = new PointD(-Math.signum(rotationDegree)
				* relativePt.y, Math.signum(rotationDegree) * relativePt.x);

		this.setCenter(rotatedPt.x + origin.x, rotatedPt.y + origin.y);

		double newHeight = this.getWidth();
		double newWidth = this.getHeight();
		this.setWidth(newWidth);
		this.setHeight(newHeight);
	}

	/**
	 * This method is used for port nodes only
	 */
	public PointD calcAveragePoint()
	{
		PointD averagePnt = new PointD();

		for (Object o : this.getEdges())
		{
			SbgnPDEdge edge = (SbgnPDEdge) o;
			if (edge.type.equals(SbgnPDConstants.RIGID_EDGE))
				continue;

			averagePnt.x += edge.getOtherEnd(this).getCenterX();
			averagePnt.y += edge.getOtherEnd(this).getCenterY();
		}

		averagePnt.x /= (this.getEdges().size() - 1);
		averagePnt.y /= (this.getEdges().size() - 1);

		return averagePnt;
	}

	public void printForces()
	{
		System.out.println("springForceX: " + this.springForceX
				+ " springForceY: " + this.springForceY + "\nrepulsionForceX: "
				+ this.repulsionForceX + " repulsionForceY: "
				+ this.repulsionForceY + "\ngravitationForceX: "
				+ this.gravitationForceX + " gravitationForceY: "
				+ this.gravitationForceY);
	}

	/**
	 * This method returns the neighbors of a given node. Notice that the graph
	 * is directed. Therefore edges should have the given node as the source
	 * node.
	 */
	public ArrayList<SbgnPDNode> getChildrenNeighbors(String edgeType)
	{
		ArrayList<SbgnPDNode> neighbors = new ArrayList<SbgnPDNode>();

		for (int i = 0; i < this.getEdges().size(); i++)
		{
			LEdge e = (LEdge) this.getEdges().get(i);

			if (e.getSource().equals(this) && !e.getTarget().equals(this))
			{
				SbgnPDNode s = (SbgnPDNode) e.getTarget();

				if (edgeType != null && e.equals(edgeType))
				{
					neighbors.add(s);
				}
				if (edgeType == null)
					neighbors.add(s);

			}
		}
		return neighbors;
	}

	/**
	 * This method updates the bounds of this compound node. If the node is a
	 * dummy compound, do not include label and extra margins.
	 */
	@Override
	public void updateBounds()
	{
		assert this.getChild() != null;

		if (this.getChild().getNodes().size() != 0)
		{
			// wrap the children nodes by re-arranging the boundaries
			LGraph childGraph = this.getChild();
			childGraph.updateBounds(true);

			this.rect.x = childGraph.getLeft();
			this.rect.y = childGraph.getTop();

			if (this.type != null && this.type.equals(SbgnPDConstants.DUMMY_COMPOUND))
			{
				this.setWidth(childGraph.getRight() - childGraph.getLeft());
				this.setHeight(childGraph.getBottom() - childGraph.getTop());
			}
			else
			{
				this.setWidth(childGraph.getRight() - childGraph.getLeft() + 2
						* LayoutConstants.COMPOUND_NODE_MARGIN);
				this.setHeight(childGraph.getBottom() - childGraph.getTop() + 2
						* LayoutConstants.COMPOUND_NODE_MARGIN
						+ LayoutConstants.LABEL_HEIGHT);
			}
		}
	}
}
