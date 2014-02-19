package org.cytoscape.filter.internal.view;

import java.awt.datatransfer.DataFlavor;
import java.util.List;

public class PathDataFlavor extends DataFlavor {
	public PathDataFlavor() {
		super(List.class, "Path");
	}
	
	static DataFlavor instance = new PathDataFlavor();
}