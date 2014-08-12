package org.cytoscape.ding.customgraphics;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.cytoscape.ding.customgraphicsmgr.internal.CGComparator;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;

@SuppressWarnings("rawtypes")
public class CustomGraphicsRange extends DiscreteRange<CyCustomGraphics>  {

	private CustomGraphicsManager manager;
	
	public CustomGraphicsRange() {
		super(CyCustomGraphics.class, new HashSet<CyCustomGraphics>());
	}

	public void setManager(final CustomGraphicsManager manager) {
		this.manager = manager;
	}
	
	@Override
	public Class<CyCustomGraphics> getType() {
		return CyCustomGraphics.class;
	}

	@Override
	public boolean isDiscrete() {
		return true;
	}

	@Override
	public Set<CyCustomGraphics> values() {
		Set<CyCustomGraphics> sortedSet = new TreeSet<CyCustomGraphics>(new CGComparator());
		sortedSet.addAll(manager.getAllCustomGraphics());
		return sortedSet;
	}

	@Override
	public void addRangeValue(CyCustomGraphics newValue) {
		manager.addCustomGraphics(newValue, null);
	}
	
	@Override
	public boolean inRange(CyCustomGraphics value) {
		// CyCharts don't have to be added to the manager
		return value instanceof CyCustomGraphics2
				|| value == NullCustomGraphics.getNullObject()
				|| manager.getAllCustomGraphics().contains(value);
	}
}
