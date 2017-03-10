package org.ivis.layout.sbgn;

import java.util.ArrayList;

import org.ivis.layout.LGraph;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.sbgn.SbgnPDNode;
import org.ivis.layout.sbgn.Compaction.CompactionDirection;
import org.ivis.util.RectangleD;

/**
 * This class implements CoSE specific data and functionality for graphs.
 * 
 * @author Begum Genc
 * 
 */
public class VisibilityGraph extends LGraph
{
	/**
	 * CompactionDirection.VERTICAL or CompactionDirection.HORIZONTAL
	 */
	public CompactionDirection direction;

	/*
	 * Constructor
	 */
	public VisibilityGraph(SbgnPDNode parent, LGraphManager graphMgr,
			Object vGraph)
	{
		super(parent, graphMgr, vGraph);
	}

	/**
	 * Create a new visibility graph. Compare each vertices in the graph to see
	 * if they are visible to each other. If two vertices can see each other and
	 * they see each other in the desired direction, add an edge between them.
	 */
	public void construct(CompactionDirection d, ArrayList<SbgnPDNode> vertices)
	{
		init(vertices);

		ArrayList<SbgnPDNode> nodes = (ArrayList<SbgnPDNode>) this.getNodes();
		this.direction = d;

		// check the visibility between each two vertex
		for (int i = 0; i < nodes.size(); i++)
		{
			for (int j = i + 1; j < nodes.size(); j++)
			{
				SbgnPDNode node1 = nodes.get(i);
				SbgnPDNode node2 = nodes.get(j);

				int result = findVisibilityDirection(node1, node2);

				// if they are visible, create edge.
				if (result != 0)
				{
					createEdge(node1, node2);
				}
			}
		}
	}

	/**
	 * This method adds the given nodes to the graph.
	 */
	private void init(ArrayList<SbgnPDNode> vertices)
	{
		// create the new graph with given vertices
		for (SbgnPDNode s : vertices)
			this.add(s);
	}

	/**
	 * Given two nodes, check their visibility. Two nodes are visible to each
	 * other if there exists an infinite ray that intersects them without
	 * intersecting any other nodes in between those two.
	 * 
	 * @return 1: vertical, 2: horizontal (if any edge found). Otherwise, return
	 *         0
	 */
	private int findVisibilityDirection(SbgnPDNode p, SbgnPDNode q)
	{
		if (direction == CompactionDirection.VERTICAL)
		{
			// ensure that p points to the leftmost element
			if (q.getLeft() < p.getLeft()
					&& p.getLeft() < q.getLeft() + q.getWidth())
			{
				SbgnPDNode temp = p;
				p = q;
				q = temp;
			}

			// check if there exists a ray
			if (p.getLeft() <= q.getLeft()
					&& q.getLeft() <= p.getLeft() + p.getWidth())
			{
				if (sweepIntersectedArea(p, q))
					return 1;
			}
		}

		else if (direction == CompactionDirection.HORIZONTAL)
		{
			// ensure that p points to the upper element
			if (q.getTop() < p.getTop()
					&& p.getTop() < q.getTop() + q.getHeight())
			{
				SbgnPDNode temp = p;
				p = q;
				q = temp;
			}

			// check if there exists a ray
			if (p.getTop() <= q.getTop()
					&& q.getTop() <= p.getTop() + p.getHeight())
			{
				if (sweepIntersectedArea(p, q))
					return 2;
			}
		}
		return 0;
	}

	/**
	 * Starting from the intersection area between p and q, walk on a line
	 * perpendicular to the desired direction. If there is an edge that does not
	 * intersect with any other nodes, this is a valid edge.
	 * 
	 * @return true if an edge exists. false otherwise.
	 */
	private boolean sweepIntersectedArea(SbgnPDNode p, SbgnPDNode q)
	{
		RectangleD edge;
		boolean isValid;
		int start = 0, end = 0, result;

		// find the sweep line borders
		if (direction == CompactionDirection.VERTICAL)
		{
			start = (int) q.getLeft();
			end = (int) Math.min(p.getRight(), q.getRight());
		}
		else if (direction == CompactionDirection.HORIZONTAL)
		{
			start = (int) q.getTop();
			end = (int) Math.min(p.getBottom(), q.getBottom());
		}

		// if they intersect only on the borders, immediately return false.
		if (start == end)
			return false;

		// check for all intersected area
		for (int sweepPoint = start; sweepPoint <= end; sweepPoint++)
		{
			isValid = true;
			edge = tryConstructingEdge(p, q, sweepPoint);

			// if an edge is constructed, check its validity
			if (edge != null)
			{
				result = checkIntermediateNodes(p, q, edge, sweepPoint);

				if (sweepPoint == result)
					isValid = true;
				else
				{
					sweepPoint = result;
					isValid = false;
				}
			}
			if (isValid)
				return true;
		}

		return false;
	}

	/**
	 * This method tries to construct an edge(RectangleD shape) between two
	 * nodes. The parameter i indicates the starting point of the edge. For
	 * example, if a vertical edge to be constructed, i is the top coordinate.
	 * For horizontal, i is the leftmost coordinate.
	 * 
	 * @return edge. if no edge can be constructed, returns null.
	 */
	private RectangleD tryConstructingEdge(SbgnPDNode p, SbgnPDNode q, int i)
	{
		if (direction == CompactionDirection.VERTICAL)
		{
			// create an edge from upper to lower or return false:does not
			// exist
			if (p.getTop() < q.getTop() && p.getBottom() <= q.getTop())
			{
				return new RectangleD(i, (int) (p.getBottom()), 1,
						(int) (q.getTop() - p.getBottom()));
			}
			else if (q.getTop() < p.getTop()
					&& q.getTop() + q.getHeight() <= p.getTop())
			{
				return new RectangleD(i, (int) (q.getBottom()), 1,
						(int) (p.getTop() - q.getBottom()));
			}
			else
				return null;
		}
		else if (direction == CompactionDirection.HORIZONTAL)
		{
			// create an edge from leftmost to right or return false:does
			// not exist
			if (p.getLeft() < q.getLeft() && p.getRight() <= q.getLeft())
			{
				return new RectangleD((int) (p.getRight()), i,
						(int) (q.getLeft() - p.getRight()), 1);
			}
			else if (q.getLeft() < p.getLeft() && q.getRight() <= p.getLeft())

			{
				return new RectangleD((int) (q.getRight()), i,
						(int) (p.getLeft() - q.getRight()), 1);
			}
			else
				return null;
		}

		return null;
	}

	/**
	 * This method checks if the given edge intersects any nodes except the
	 * source and target nodes. If an intersection is found, update the sweep
	 * point to the end of the intersected node. Otherwise, do not change the
	 * point.
	 */
	private int checkIntermediateNodes(SbgnPDNode p, SbgnPDNode q,
			RectangleD edge, int sweepPoint)
	{
		for (int j = 0; j < this.getNodes().size(); j++)
		{
			SbgnPDNode intermediateNode = (SbgnPDNode) this.getNodes().get(j);

			if (!intermediateNode.equals(p) && !intermediateNode.equals(q))
			{
				// if there is an intersection, edge is not valid
				if (edge.intersects(intermediateNode.getRect()))
				{
					// jump to the end of intersected node
					if (direction == CompactionDirection.VERTICAL)
						sweepPoint = (int) (intermediateNode.getRight() + 1);
					else if (direction == CompactionDirection.HORIZONTAL)
						sweepPoint = (int) (intermediateNode.getBottom() + 1);

					break;
				}
			}
		}

		return sweepPoint;
	}

	/**
	 * This class creates an edge between the given nodes using the given
	 * direction. While adding the edge, be careful about the source and target
	 * i.e. if we want to get a vertical visibility graph, (A -> B) A should
	 * have a lower y coordinate (upper). Similarly, for horizontal visibility
	 * graph (A -> B): A is on the left, has lower x coordinate.
	 */
	private void createEdge(SbgnPDNode node1, SbgnPDNode node2)
	{
		if (direction == CompactionDirection.VERTICAL)
		{
			if (node1.getTop() < node2.getTop())
				this.add(new VisibilityEdge(node1, node2, null), node1, node2);
			else
				this.add(new VisibilityEdge(node2, node1, null), node2, node1);
		}

		else if (direction == CompactionDirection.HORIZONTAL)
		{
			if (node1.getLeft() < node2.getLeft())
				this.add(new VisibilityEdge(node1, node2, null), node1, node2);
			else
				this.add(new VisibilityEdge(node2, node1, null), node2, node1);
		}

		// calculate newly added edge's length.
		((VisibilityEdge) this.getEdges().get(this.getEdges().size() - 1))
				.updateLength();
	}

	/**
	 * For each edge having s as its target node, find and return the shortest
	 * one. Returns null if could not find an edge.
	 */
	public VisibilityEdge findShortestEdge(SbgnPDNode s)
	{
		VisibilityEdge shortestEdge = null;
		int minLength = Integer.MAX_VALUE;

		for (int i = 0; i < this.getEdges().size(); i++)
		{
			VisibilityEdge e = (VisibilityEdge) this.getEdges().get(i);

			e.updateLength();
			if (e.getTarget().equals(s) && e.getLength() < minLength)
			{
				shortestEdge = e;
				minLength = (int) e.getLength();
			}
		}

		return shortestEdge;
	}
}