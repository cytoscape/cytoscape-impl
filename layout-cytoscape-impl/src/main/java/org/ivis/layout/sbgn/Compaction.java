package org.ivis.layout.sbgn;

import java.util.ArrayList;

/**
 * This class is used to apply compaction on a graph. First a visibility graph
 * is constructed from the given set of nodes. Since visibility graphs are
 * DAG's, apply topological sort. Then, try to translate each node's location
 * vertically/horizontally (compact them)
 * 
 * @author Begum Genc
 * 
 */
public class Compaction
{
	/**
	 * Stores the original provided list of vertices.
	 */
	private ArrayList<SbgnPDNode> vertices;

	/**
	 * Visibility graph
	 */
	private VisibilityGraph visGraph;

	/**
	 * Compaction direction: VERTICAL or HORIZONTAL
	 */
	private CompactionDirection direction;

	/**
	 * Stores the topological sort results of the nodes
	 */
	ArrayList<SbgnPDNode> orderedNodeList;

	/**
	 * Constructor
	 * 
	 * @param vertices
	 *            : list of vertices for visibility graph
	 */
	public Compaction(ArrayList<SbgnPDNode> vertices)
	{
		this.orderedNodeList = new ArrayList<SbgnPDNode>();
		this.vertices = new ArrayList<SbgnPDNode>();
		this.vertices = vertices;
	}

	/**
	 * Two times do the following: (first for vertical, second horizontal) First
	 * create a visibility graph for the given elements. The visibility graph is
	 * always a DAG, so perform a topological sort on the elements (the node
	 * that has in-degree 0 comes first), perform compaction.
	 */
	public void perform()
	{
		algorithmBody(CompactionDirection.VERTICAL);
		removeVisibilityEdges();
		
		algorithmBody(CompactionDirection.HORIZONTAL);
		removeVisibilityEdges();

	}

	private void removeVisibilityEdges()
	{
		for(Object objNode : vertices)
		{
			SbgnPDNode sbgnNode = (SbgnPDNode) objNode;
			
			for(int i = 0; i < sbgnNode.getEdges().size(); i++)
			{
				Object objEdge = sbgnNode.getEdges().get(i);
				
				if( objEdge instanceof VisibilityEdge)
				{
					sbgnNode.getEdges().remove(i);
					i--;
				}
			}
		}	
	}

	private void algorithmBody(CompactionDirection direction)
	{
		this.direction = direction;
		visGraph = new VisibilityGraph(null, null, null);

		// construct a visibility graph given the direction and vertices
		visGraph.construct(this.direction, vertices);

		if (visGraph.getEdges().size() > 0)
		{
			topologicallySort();

			compactElements();
		}

		// positions of the vertices has changed. Update them.
		vertices = (ArrayList<SbgnPDNode>) visGraph.getNodes();
	}

	/**
	 * Perform a DFS on the given graph nodes and then output the nodes in
	 * reverse order.
	 */
	private void topologicallySort()
	{
		// ensure that the vertices have not been marked as visited.
		for (int i = 0; i < visGraph.getNodes().size(); i++)
		{
			SbgnPDNode s = (SbgnPDNode) visGraph.getNodes().get(i);
			s.visited = false;
		}

		// ensure that the list is empty
		orderedNodeList.clear();

		DFS();

		orderedNodeList = reverseList(orderedNodeList);
	}

	private void DFS()
	{
		for (int i = 0; i < visGraph.getNodes().size(); i++)
		{
			SbgnPDNode s = (SbgnPDNode) visGraph.getNodes().get(i);
			if (!s.visited)
			{
				DFS_Visit(s);
			}
		}
	}
	
    private void DFS_Visit(SbgnPDNode s)
    {
            ArrayList<SbgnPDNode> neighbors = s.getChildrenNeighbors(null);

            if (neighbors.size() == 0)
            {
                    s.visited = true;
                    orderedNodeList.add(s);
                    return;
            }

            for (SbgnPDNode n : neighbors)
            {
                    if (!n.visited)
                            DFS_Visit(n);
            }

            s.visited = true;
            orderedNodeList.add(s);
    }

	/**
	 * Reverse the element order of a given list
	 */
	private ArrayList<SbgnPDNode> reverseList(ArrayList<SbgnPDNode> originalList)
	{
		ArrayList<SbgnPDNode> reverseOutput = new ArrayList<SbgnPDNode>();
		for (int i = originalList.size() - 1; i >= 0; i--)
			reverseOutput.add(originalList.get(i));

		return reverseOutput;
	}

	/**
	 * This method visits the list that is the result of topological sort. For
	 * each node in that list, it looks for its incoming edges and finds the
	 * shortest one. Translates the node wrt the shortest edge.
	 */
	private void compactElements()
	{
		double distance;

		for (SbgnPDNode s : orderedNodeList)
		{
			// find shortest incoming edge
			VisibilityEdge edge = visGraph.findShortestEdge(s);

			if (edge != null)
			{
				distance = edge.getLength();

				if (direction == CompactionDirection.VERTICAL)
				{
					// bring the node closer to the source node and respect the
					// buffer.
					if (distance > SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER)
					{
						s.setLocation(
								s.getLeft(),
								(s.getTop() - (distance - SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER)));
					}
					else
					{
						s.setLocation(s.getLeft(), edge.getOtherEnd(s)
								.getBottom()
								+ SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER);
					}
				}
				else if (direction == CompactionDirection.HORIZONTAL)
				{
					if (distance > SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER)
					{
						s.setLocation(
								(s.getLeft() - (distance - SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER)),
								s.getTop());
					}
					else
					{
						s.setLocation(
								edge.getOtherEnd(s).getRight()
										+ SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER,
								s.getTop());
					}
				}
			}
		}
	}

	/**
	 * This method prints the edges of the visibility graph.
	 */
	@SuppressWarnings("unused")
	private void printEdges()
	{
		System.out.println("# of edges: " + visGraph.getEdges().size());

		for (int i = 0; i < visGraph.getEdges().size(); i++)
		{
			VisibilityEdge e = (VisibilityEdge) visGraph.getEdges().get(i);
			System.out.println("between: " + e.getSource().label + "  "
					+ e.getTarget().label);
		}
	}

	/**
	 * This method prints the nodes of the visibility graph.
	 */
	@SuppressWarnings("unused")
	private void printGraphVertices()
	{
		for (int i = 0; i < visGraph.getNodes().size(); i++)
		{
			SbgnPDNode s = (SbgnPDNode) visGraph.getNodes().get(i);
			System.out.println(s.label + " (" + s.getLeft() + ", " + s.getTop()
					+ ")" + " || " + "(" + (s.getLeft() + s.getWidth()) + ", "
					+ (s.getTop() + s.getHeight()) + ")");
		}
		System.out.println();
	}

	public enum CompactionDirection
	{
		VERTICAL, HORIZONTAL
	};
}