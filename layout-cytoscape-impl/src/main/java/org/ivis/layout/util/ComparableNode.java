package org.ivis.layout.util;

import org.ivis.layout.sbgn.SbgnPDNode;

/**
 * Sort the given nodes according to their area calculations.
 * 
 */
public class ComparableNode implements Comparable
{
	private SbgnPDNode node;

	public ComparableNode(SbgnPDNode node)
	{
		this.node = node;
	}

	public SbgnPDNode getNode()
	{
		return node;
	}

	/**
	 * Descending order of areas
	 */
	public int compareTo(Object o)
	{
		return (new Double(((ComparableNode) o).getNode().getWidth()
				* ((ComparableNode) o).getNode().getHeight())
				.compareTo(node.getWidth() * node.getHeight()));
	}
}