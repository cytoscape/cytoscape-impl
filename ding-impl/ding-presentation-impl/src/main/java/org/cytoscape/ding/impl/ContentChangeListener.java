package org.cytoscape.ding.impl;

import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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



@FunctionalInterface
public interface ContentChangeListener {
	/**
	 * This gets fired upon graph redraw when at least one of the following
	 * things change: node unselected, edge unselected, background paint
	 * change, node view added, edge view added, node view removed,
	 * edge view removed, node view hidden, edge view hidden, node view
	 * restored, edge view restored, graph lod changed, node visual property
	 * changed, edge visual property changed.
	 * @param updateType 
	 */
	public void contentChanged(UpdateType updateType);
}
