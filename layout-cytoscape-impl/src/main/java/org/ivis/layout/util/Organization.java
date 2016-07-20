package org.ivis.layout.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ivis.layout.sbgn.SbgnPDConstants;
import org.ivis.layout.sbgn.SbgnPDNode;

public class Organization
{
	/**
	 * Width of the container
	 */
	private double width;

	/**
	 * Height of the container
	 */
	private double height;

	private List<Double> rowWidth;
	private List<Double> rowHeight;

	private List<LinkedList<SbgnPDNode>> rows;

	/**
	 * Creates a container whose width and height is only the margins
	 */
	public Organization()
	{
		this.width = SbgnPDConstants.COMPLEX_MEM_MARGIN * 2;
		this.height = SbgnPDConstants.COMPLEX_MEM_MARGIN * 2 ;

		rowWidth = new ArrayList<Double>();
		rowHeight = new ArrayList<Double>();

		rows = new ArrayList<LinkedList<SbgnPDNode>>();
	}

	public double getWidth()
	{
		shiftToLastRow();
		return width;
	}

	public double getHeight()
	{
		return height;
	}

	/**
	 * Scans the rowWidth array list and returns the index of the row that has
	 * the minimum width.
	 */
	private int getShortestRowIndex()
	{
		int r = -1;
		double min = Double.MAX_VALUE;

		for (int i = 0; i < rows.size(); i++)
		{
			if (rowWidth.get(i) < min)
			{
				r = i;
				min = rowWidth.get(i);
			}
		}

		return r;
	}

	/**
	 * Scans the rowWidth array list and returns the index of the row that has
	 * the maximum width.
	 */
	private int getLongestRowIndex()
	{
		int r = -1;
		double max = Double.MIN_VALUE;

		for (int i = 0; i < rows.size(); i++)
		{
			if (rowWidth.get(i) > max)
			{
				r = i;
				max = rowWidth.get(i);
			}
		}

		return r;
	}

	public void insertNode(SbgnPDNode node)
	{
		if (rows.isEmpty())
		{
			insertNodeToRow(node, 0);
		}
		else if (canAddHorizontal(node.getWidth(), node.getHeight()))
		{
			insertNodeToRow(node, getShortestRowIndex());
		}
		else
		{
			insertNodeToRow(node, rows.size());
		}
	}

	/**
	 * This method performs tiling. If a new row is needed, it creates the row
	 * and places the new node there. Otherwise, it places the node to the end
	 * of the given row.
	 * 
	 * @param node
	 * @param rowIndex
	 */
	private void insertNodeToRow(SbgnPDNode node, int rowIndex)
	{
		// Add new row if needed
		if (rowIndex == rows.size())
		{
			rows.add(new LinkedList<SbgnPDNode>());
			
			rowWidth.add(SbgnPDConstants.COMPLEX_MIN_WIDTH);
			rowHeight.add(0.0);

			assert rows.size() == rowWidth.size();
		}

		// Update row width
		double w = rowWidth.get(rowIndex) + node.getWidth();

		if (!rows.get(rowIndex).isEmpty())
		{
			w += SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER;
		}
		rowWidth.set(rowIndex, w);

		// Update complex width
		if (width < w)
		{
			width = w;
		}

		// Update height
		double h = node.getHeight();
		if (rowIndex > 0)
			h += SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER;

		double extraHeight = 0;
		if (h > rowHeight.get(rowIndex))
		{
			extraHeight = rowHeight.get(rowIndex);
			rowHeight.set(rowIndex, h);
			extraHeight = rowHeight.get(rowIndex) - extraHeight;
		}

		height += extraHeight;

		// Insert node
		rows.get(rowIndex).add(node);
	}

	/**
	 * If moving the last node from the longest row and adding it to the last
	 * row makes the bounding box smaller, do it.
	 */
	private void shiftToLastRow()
	{
		int longest = getLongestRowIndex();
		int last = rowWidth.size() - 1;
		LinkedList<SbgnPDNode> row = rows.get(longest);
		SbgnPDNode node = row.getLast();

		double diff = node.getWidth()
				+ SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER;

		if (width - rowWidth.get(last) > diff && rowHeight.get(last) > node.getHeight())
		{
			row.removeLast();
			rows.get(last).add(node);
			rowWidth.set(longest, rowWidth.get(longest) - diff);
			rowWidth.set(last, rowWidth.get(last) + diff);

			width = rowWidth.get(getLongestRowIndex());

			// Update height of the organization
			double maxHeight = Double.MIN_VALUE;
			for (int i = 0; i < row.size(); i++)
			{
				if (row.get(i).getHeight() > maxHeight)
					maxHeight = row.get(i).getHeight();
			}
			if (longest > 0)
				maxHeight += SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER;

			double prevTotal = rowHeight.get(longest) + rowHeight.get(last);

			rowHeight.set(longest, maxHeight);
			if (rowHeight.get(last) < node.getHeight()
					+ SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER)
			{
				rowHeight.set(last, node.getHeight()
						+ SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER);
			}
			
			double finalTotal = rowHeight.get(longest) + rowHeight.get(last);
			height += (finalTotal - prevTotal);

			shiftToLastRow();
		}
	}

	private boolean canAddHorizontal(double extraWidth, double extraHeight)
	{
		int sri = getShortestRowIndex();

		if (sri < 0)
		{
			return true;
		}
		double min = rowWidth.get(sri);

		double hDiff = 0;
		if (rowHeight.get(sri) < extraHeight)
		{
			if (sri > 0)
				hDiff = extraHeight
						+ SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER
						- rowHeight.get(sri);
		}
		if (width - min >= extraWidth
				+ SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER)
		{
			return true;
		}

		return height + hDiff > min + extraWidth
				+ SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER;
	}

	public void adjustLocations(double x, double y)
	{
		x += SbgnPDConstants.COMPLEX_MEM_MARGIN;
		y += SbgnPDConstants.COMPLEX_MEM_MARGIN;

		double left = x;

		for (LinkedList<SbgnPDNode> row : rows)
		{
			x = left;
			double maxHeight = 0;
			for (SbgnPDNode node : row)
			{
				node.setLocation(x, y);

				x += node.getWidth()
						+ SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER;

				if (node.getHeight() > maxHeight)
					maxHeight = node.getHeight();
			}

			y += maxHeight + SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER;
		}
	}
}