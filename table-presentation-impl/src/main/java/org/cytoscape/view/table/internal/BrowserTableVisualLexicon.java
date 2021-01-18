package org.cytoscape.view.table.internal;

import org.cytoscape.model.CyColumn;
import org.cytoscape.view.model.NullDataType;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.presentation.property.table.CellCustomGraphics;
import org.cytoscape.view.table.internal.cg.CellCGVisualProperty;
import org.cytoscape.view.table.internal.cg.NullCellCustomGraphics;

public class BrowserTableVisualLexicon extends BasicTableVisualLexicon {

	// Root of Ding's VP tree.
	public static final VisualProperty<NullDataType> BROWSER_TABLE_ROOT = new NullVisualProperty(
			"BROWSER_TABLE_RENDERING_ENGINE_ROOT",
			"Browser Table Root Visual Property"
	);
	
	public static final VisualProperty<CellCustomGraphics> CELL_CUSTOMGRAPHICS = new CellCGVisualProperty(
			NullCellCustomGraphics.getNullObject(), "CELL_CUSTOMGRAPHICS", "Cell Image/Sparkline", CyColumn.class);	
	
	public BrowserTableVisualLexicon() {
		super(BROWSER_TABLE_ROOT);
		
		// Register our properties
		addVisualProperty(CELL_CUSTOMGRAPHICS, CELL);
	}
}
