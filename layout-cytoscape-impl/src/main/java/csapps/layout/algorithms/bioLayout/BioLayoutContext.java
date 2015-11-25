package csapps.layout.algorithms.bioLayout;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import org.cytoscape.view.layout.EdgeWeighter;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.ContainsTunables;

public abstract class BioLayoutContext {
	
	@ContainsTunables
	public EdgeWeighter edgeWeighter = new EdgeWeighter();
	
	/**
	 * Whether or not to initialize by randomizing all points
	 */
	@Tunable(description="Randomize graph before layout:", groups="Standard Settings")
	public boolean randomize = true;
}
