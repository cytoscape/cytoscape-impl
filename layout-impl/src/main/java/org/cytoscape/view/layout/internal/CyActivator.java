package org.cytoscape.view.layout.internal;

/*
 * #%L
 * Cytoscape Layout Impl (layout-impl)
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

import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.layout.internal.algorithms.GridNodeLayout;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.*;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyProperty cyPropertyServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		UndoSupport undoSupportServiceRef = getService(bc,UndoSupport.class);
		CyServiceRegistrar cyServiceRegistrar = getService(bc,CyServiceRegistrar.class);
		
		GridNodeLayout gridNodeLayout = new GridNodeLayout(undoSupportServiceRef);
		CyLayoutsImpl cyLayouts = new CyLayoutsImpl(cyServiceRegistrar, cyPropertyServiceRef, gridNodeLayout);
		
		registerService(bc,cyLayouts,CyLayoutAlgorithmManager.class, new Properties());

		Properties gridNodeLayoutProps = new Properties();
		// gridNodeLayoutProps.setProperty(PREFERRED_MENU,"Layout.Cytoscape Layouts");
		gridNodeLayoutProps.setProperty("preferredTaskManager","menu");
		gridNodeLayoutProps.setProperty(TITLE,gridNodeLayout.toString());
		gridNodeLayoutProps.setProperty(MENU_GRAVITY,"10.0");
		registerService(bc,gridNodeLayout,CyLayoutAlgorithm.class, gridNodeLayoutProps);

		registerServiceListener(bc,cyLayouts,"addLayout","removeLayout",CyLayoutAlgorithm.class);
	}
}

