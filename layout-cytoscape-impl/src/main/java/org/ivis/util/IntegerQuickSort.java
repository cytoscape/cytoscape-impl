package org.ivis.util;

import java.util.List;

/**
 * This class implements a quick sort for integers.
 *
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class IntegerQuickSort extends QuickSort
{
	public IntegerQuickSort(List<Object> objectList)
	{
		super(objectList);
	}

	public boolean compare(Object a, Object b)
	{
		int i = ((Integer) a).intValue();
		int j = ((Integer) b).intValue();

		return j > i;
	}
}