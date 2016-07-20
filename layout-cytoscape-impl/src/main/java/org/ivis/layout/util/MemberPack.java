package org.ivis.layout.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ivis.layout.LGraph;
import org.ivis.layout.sbgn.SbgnPDNode;

public class MemberPack
{
	private List<SbgnPDNode> members;
	public Organization org;

	public MemberPack(LGraph childG)
	{
		members = new ArrayList<SbgnPDNode>();
		members.addAll(childG.getNodes());
		org = new Organization();

		layout();

		SbgnPDNode[] nodes = new SbgnPDNode[childG.getNodes().size()];

		for (int i = 0; i < childG.getNodes().size(); i++)
		{
			nodes[i] = (SbgnPDNode) childG.getNodes().get(i);
		}
	}

	public void layout()
	{
		ComparableNode[] compar = new ComparableNode[members.size()];

		int i = 0;
		for (SbgnPDNode node : members)
		{
			compar[i++] = new ComparableNode(node);

		}

		Arrays.sort(compar);

		members.clear();

		for (ComparableNode com : compar)
		{
			members.add(com.getNode());
		}

		for (SbgnPDNode node : members)
		{
			org.insertNode(node);
		}

		// Compaction c = new Compaction(
		// (ArrayList<SbgnPDNode>) members);
		// c.perform();

	}

	public double getWidth()
	{
		return org.getWidth();
	}

	public double getHeight()
	{
		return org.getHeight();
	}

	public void adjustLocations(double x, double y)
	{
		org.adjustLocations(x, y);
	}

	public List<SbgnPDNode> getMembers()
	{
		return members;
	}

}
