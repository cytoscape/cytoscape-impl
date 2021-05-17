package org.cytoscape.view.table.internal;

import org.cytoscape.cg.model.CustomGraphicsRange;
import org.cytoscape.cg.model.CustomGraphicsVisualProperty;
import org.cytoscape.cg.model.NullCustomGraphics;
import org.cytoscape.model.CyColumn;
import org.cytoscape.view.model.NullDataType;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class BrowserTableVisualLexicon extends BasicTableVisualLexicon {

	// Root of Ding's VP tree.
	public static final VisualProperty<NullDataType> BROWSER_TABLE_ROOT = new NullVisualProperty(
			"BROWSER_TABLE_RENDERING_ENGINE_ROOT",
			"Browser Table Root Visual Property"
	);
	
	private static final CustomGraphicsRange CG_RANGE = CustomGraphicsRange.getInstance();
	
	public static final VisualProperty<CyCustomGraphics> CELL_CUSTOMGRAPHICS = new CustomGraphicsVisualProperty(
			NullCustomGraphics.getNullObject(), CG_RANGE, "CELL_CUSTOMGRAPHICS", "Cell Image/Sparkline", CyColumn.class);	
	
	public BrowserTableVisualLexicon() {
		super(BROWSER_TABLE_ROOT);
		
		// Register our properties
		addVisualProperty(CELL_CUSTOMGRAPHICS, CELL);
	}
}
