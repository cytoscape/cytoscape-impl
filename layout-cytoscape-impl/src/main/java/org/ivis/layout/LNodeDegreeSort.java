package org.ivis.layout;

import java.util.List;

import org.ivis.util.QuickSort;

/**
 * This class implements sorting with respect to degrees of LNodes.
 *
 * @author Alptug Dilek
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LNodeDegreeSort extends QuickSort
{
	public LNodeDegreeSort(List<Object> objectList)
	{
		super(objectList);
	}

	public LNodeDegreeSort(Object[] objectArray)
	{
		super(objectArray);
	}

	public boolean compare(Object a, Object b)
	{
		LNode node1 = (LNode)a;
		LNode node2 = (LNode)b;

		return (node2.getEdges().size() > node1.getEdges().size());
	}
}