package org.cytoscape.view.layout.internal;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.layout.internal.algorithms.GridNodeLayout;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Layout Impl (layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		final UndoSupport undoSupport = getService(bc, UndoSupport.class);

		GridNodeLayout gridNodeLayout = new GridNodeLayout(undoSupport);
		
		CyLayoutAlgorithmManagerImpl layoutManager = new CyLayoutAlgorithmManagerImpl(gridNodeLayout, serviceRegistrar);
		registerService(bc, layoutManager, CyLayoutAlgorithmManager.class, new Properties());

		{
			Properties props = new Properties();
			// gridNodeLayoutProps.setProperty(PREFERRED_MENU, "Layout.Cytoscape Layouts");
			props.setProperty("preferredTaskManager", "menu");
			props.setProperty(TITLE, gridNodeLayout.toString());
			props.setProperty(MENU_GRAVITY, "10.0");
			registerService(bc, gridNodeLayout, CyLayoutAlgorithm.class, props);
		}

		registerServiceListener(bc, layoutManager, "addLayout", "removeLayout", CyLayoutAlgorithm.class);
	}
}
