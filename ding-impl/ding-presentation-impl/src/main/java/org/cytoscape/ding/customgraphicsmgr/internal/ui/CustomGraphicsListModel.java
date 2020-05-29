package org.cytoscape.ding.customgraphicsmgr.internal.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;

import org.cytoscape.ding.customgraphicsmgr.internal.CGComparator;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

/**
 * Extend the default list model by insuring that the items in the list
 * are sorted by type, then by displayName....
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class CustomGraphicsListModel extends AbstractListModel<CyCustomGraphics> {
	
	private List<CyCustomGraphics> graphics;
	private Comparator<CyCustomGraphics> comp;

	public CustomGraphicsListModel() {
		graphics = new ArrayList<>();
		comp = new CGComparator();
	}

	public void addElement(CyCustomGraphics cg) {
		graphics.add(cg);
		Collections.sort(graphics, comp);
		fireContentsChanged(this, 0, graphics.size());
	}

	public void removeElement(CyCustomGraphics cg) {
		graphics.remove(cg);
		fireContentsChanged(this, 0, graphics.size());
	}

	public void removeAllElements() {
		graphics.clear();
		fireContentsChanged(this, 0, graphics.size());
	}

	public void clear() {
		graphics.clear();
		fireContentsChanged(this, 0, graphics.size());
	}

	@Override
	public int getSize() {
		return graphics.size();
	}

	@Override
	public CyCustomGraphics getElementAt(int index) {
		return graphics.get(index);
	}
}
