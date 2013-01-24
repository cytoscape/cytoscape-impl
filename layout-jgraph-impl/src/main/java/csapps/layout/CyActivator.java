package csapps.layout;

/*
 * #%L
 * Cytoscape JGraph Layout Impl (layout-jgraph-impl)
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

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.*;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {
		
		UndoSupport undo = getService(bc,UndoSupport.class);

		JGraphLayoutWrapper jGraphAnnealingLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.ANNEALING,undo);
		JGraphLayoutWrapper jGraphMoenLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.MOEN,undo);
		JGraphLayoutWrapper jGraphCircleGraphLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.CIRCLE_GRAPH,undo);
		JGraphLayoutWrapper jGraphRadialTreeLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.RADIAL_TREE,undo);
		JGraphLayoutWrapper jGraphGEMLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.GEM,undo);
		JGraphLayoutWrapper jGraphSpringEmbeddedLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.SPRING_EMBEDDED,undo);
		JGraphLayoutWrapper jGraphSugiyamaLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.SUGIYAMA,undo);
		JGraphLayoutWrapper jGraphTreeLayout = new JGraphLayoutWrapper(JGraphLayoutWrapper.TREE,undo);
		
		
		Properties jGraphAnnealingLayoutProps = new Properties();
		jGraphAnnealingLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphAnnealingLayout,CyLayoutAlgorithm.class, jGraphAnnealingLayoutProps);

		Properties jGraphMoenLayoutProps = new Properties();
		jGraphMoenLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphMoenLayout,CyLayoutAlgorithm.class, jGraphMoenLayoutProps);

		Properties jGraphCircleGraphLayoutProps = new Properties();
		jGraphCircleGraphLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphCircleGraphLayout,CyLayoutAlgorithm.class, jGraphCircleGraphLayoutProps);

		Properties jGraphRadialTreeLayoutProps = new Properties();
		jGraphRadialTreeLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphRadialTreeLayout,CyLayoutAlgorithm.class, jGraphRadialTreeLayoutProps);

		Properties jGraphGEMLayoutProps = new Properties();
		jGraphGEMLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphGEMLayout,CyLayoutAlgorithm.class, jGraphGEMLayoutProps);

		Properties jGraphSpringEmbeddedLayoutProps = new Properties();
		jGraphSpringEmbeddedLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphSpringEmbeddedLayout,CyLayoutAlgorithm.class, jGraphSpringEmbeddedLayoutProps);

		Properties jGraphSugiyamaLayoutProps = new Properties();
		jGraphSugiyamaLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphSugiyamaLayout,CyLayoutAlgorithm.class, jGraphSugiyamaLayoutProps);

		Properties jGraphTreeLayoutProps = new Properties();
		jGraphTreeLayoutProps.setProperty(PREFERRED_MENU,"JGraph Layouts");
		registerService(bc,jGraphTreeLayout,CyLayoutAlgorithm.class, jGraphTreeLayoutProps);

		

	}
}

