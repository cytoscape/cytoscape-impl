package de.mpg.mpi_inf.bioinf.netanalyzer;


import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;


final class AttributeSetup {

	private AttributeSetup() {}

	static void createDirectedNodeAttributes(CyTable nodeTable) {
		createCommonNodeAttributes(nodeTable);
		createAttr(nodeTable,Messages.getAttr("dal"), Integer.class);
		createAttr(nodeTable,Messages.getAttr("din"), Integer.class);
		createAttr(nodeTable,Messages.getAttr("dou"), Integer.class);
		createAttr(nodeTable,Messages.getAttr("nbt"), Double.class);
		createAttr(nodeTable,Messages.getAttr("nco"), Double.class);
	}

	static void createUndirectedNodeAttributes(CyTable nodeTable) {
		createCommonNodeAttributes(nodeTable);
		createAttr(nodeTable,Messages.getAttr("deg"), Integer.class);
		createAttr(nodeTable,Messages.getAttr("nbt"), Double.class); 
		createAttr(nodeTable,Messages.getAttr("nco"), Double.class);
		createAttr(nodeTable,Messages.getAttr("nde"), Integer.class);
		createAttr(nodeTable,Messages.getAttr("nue"), Integer.class);
		createAttr(nodeTable,Messages.getAttr("rad"), Double.class);
		createAttr(nodeTable,Messages.getAttr("tco"), Double.class);
	}

	private static void createCommonNodeAttributes(CyTable nodeTable) {
		createAttr(nodeTable,Messages.getAttr("apl"), Double.class); 
		createAttr(nodeTable,Messages.getAttr("cco"), Double.class);
		createAttr(nodeTable,Messages.getAttr("clc"), Double.class);
		createAttr(nodeTable,Messages.getAttr("isn"), Boolean.class);
		createAttr(nodeTable,Messages.getAttr("pmn"), Integer.class);
		createAttr(nodeTable,Messages.getAttr("slo"), Integer.class);
		createAttr(nodeTable,Messages.getAttr("spl"), Integer.class);
		createAttr(nodeTable,Messages.getAttr("stress"), Long.class);
	}

	static void createEdgeAttributes(CyTable edgeTable) {
		createAttr(edgeTable,Messages.getAttr("ebt"),Double.class);
	}

	private static void createAttr(final CyTable table, final String col, final Class<?> newType) {
		final CyColumn column = table.getColumn(col);
		if (column == null)
			table.createColumn(col, newType, false);
		else if (column.getType() == newType)
			return;
		else
			throw new IllegalArgumentException("trying to set table column: " + col + 
			                                   " to type: " + newType.getName() + " when it already " +
							   " has a type of: " + column.getType().getName());
	}
}
