package org.cytoscape.view.table.internal;

import org.cytoscape.view.model.NullDataType;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;

public class BrowserTableVisualLexicon extends BasicTableVisualLexicon {

	// Root of Ding's VP tree.
	public static final VisualProperty<NullDataType> BROWSER_TABLE_ROOT = new NullVisualProperty(
			"BROWSER_TABLE_RENDERING_ENGINE_ROOT",
			"Browser Table Root Visual Property");
	
	public BrowserTableVisualLexicon() {
		super(BROWSER_TABLE_ROOT);
	}

}
