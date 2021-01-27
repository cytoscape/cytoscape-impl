package org.cytoscape.view.table.internal;

import org.cytoscape.cg.internal.model.CustomGraphicsVisualProperty;
import org.cytoscape.cg.model.CustomGraphicsRange;
import org.cytoscape.cg.model.NullCustomGraphics;
import org.cytoscape.model.CyColumn;
import org.cytoscape.view.model.NullDataType;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;

public class BrowserTableVisualLexicon extends BasicTableVisualLexicon {

	// Root of Ding's VP tree.
	public static final VisualProperty<NullDataType> BROWSER_TABLE_ROOT = new NullVisualProperty(
			"BROWSER_TABLE_RENDERING_ENGINE_ROOT",
			"Browser Table Root Visual Property"
	);
	
	private static final CustomGraphicsRange CG_RANGE = new CustomGraphicsRange();
	
	public static final VisualProperty<CyCustomGraphics> CELL_CUSTOMGRAPHICS = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE, "CELL_CUSTOMGRAPHICS", "Cell Image/Sparkline", CyColumn.class);	
	
	public BrowserTableVisualLexicon() {
		super(BROWSER_TABLE_ROOT);
		
		// Register our properties
		addVisualProperty(CELL_CUSTOMGRAPHICS, CELL);
	}
}
