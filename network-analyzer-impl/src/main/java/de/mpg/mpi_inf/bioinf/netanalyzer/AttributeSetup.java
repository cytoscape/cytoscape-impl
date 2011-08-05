package de.mpg.mpi_inf.bioinf.netanalyzer;


import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;


final class AttributeSetup {

	private AttributeSetup() {}

	static void createDirectedNodeAttributes(CyTable nodeTable) {
		createCommonNodeAttributes(nodeTable);
		createAttr(nodeTable,"dal", Integer.class);
		createAttr(nodeTable,"din", Integer.class);
		createAttr(nodeTable,"dou", Integer.class);
		createAttr(nodeTable,"nbt", Double.class);
		createAttr(nodeTable,"nco", Double.class);
	}

	static void createUndirectedNodeAttributes(CyTable nodeTable) {
		createCommonNodeAttributes(nodeTable);
		createAttr(nodeTable,"deg", Integer.class);
		createAttr(nodeTable,"nbt", Double.class); 
		createAttr(nodeTable,"nco", Double.class);
		createAttr(nodeTable,"nde", Integer.class);
		createAttr(nodeTable,"nue", Integer.class);
		createAttr(nodeTable,"rad", Double.class);
		createAttr(nodeTable,"tco", Double.class);
	}

	private static void createCommonNodeAttributes(CyTable nodeTable) {
		createAttr(nodeTable,"apl", Double.class); 
		createAttr(nodeTable,"cco", Double.class);
		createAttr(nodeTable,"clc", Double.class);
		createAttr(nodeTable,"isn", Boolean.class);
		createAttr(nodeTable,"pmn", Integer.class);
		createAttr(nodeTable,"slo", Integer.class);
		createAttr(nodeTable,"spl", Integer.class);
		createAttr(nodeTable,"stress", Long.class);
	}

	static void createEdgeAttributes(CyTable edgeTable) {
		createAttr(edgeTable,"ebt",Double.class);
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
