package org.cytoscape.view.manual.internal.control.actions.dist;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
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

import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.manual.internal.control.actions.AbstractControlAction;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 *
 */
public class HDistCenter extends AbstractControlAction {

	public HDistCenter(Icon i,CyApplicationManager appMgr) {
		super("",i,appMgr);
	}

	protected void control(List<View<CyNode>> nodes) {
		if (nodes.size() <= 1)
			return;

		Collections.sort(nodes, new XComparator());

		double d = X_max - X_min;
		d = d / (nodes.size() - 1);

		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,X_min + (i * d));
		}
	}
}
