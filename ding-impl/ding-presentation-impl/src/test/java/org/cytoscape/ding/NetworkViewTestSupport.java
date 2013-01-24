
package org.cytoscape.ding;

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

import static org.mockito.Mockito.mock;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingViewModelFactory;
import org.cytoscape.ding.impl.ViewTaskFactoryListener;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.internal.rtree.RTreeFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.UndoSupport;

public class NetworkViewTestSupport extends NetworkTestSupport {

	protected CyNetworkViewFactory viewFactory;

	public NetworkViewTestSupport() {
		super();
		
        DVisualLexicon dVisualLexicon = new DVisualLexicon(mock(CustomGraphicsManager.class));

		TableTestSupport tableTestSupport = new TableTestSupport();

        viewFactory = new DingViewModelFactory(tableTestSupport.getTableFactory(),
		                                       getRootNetworkFactory(),
		                                       mock(UndoSupport.class),
		                                       new RTreeFactory(),
		                                       dVisualLexicon,
		                                       mock(DialogTaskManager.class),
		                                       mock(CyServiceRegistrar.class),
		                                       networkTableMgr,
		                                       mock(CyEventHelper.class),
		                                       mock(ViewTaskFactoryListener.class),
											   mock(AnnotationFactoryManager.class),
											   mock(DingGraphLOD.class), mock(VisualMappingManager.class),
											   mock(CyNetworkViewManager.class), mock(HandleFactory.class));
	}
	
	public CyNetworkView getNetworkView() {
		return viewFactory.createNetworkView( getNetwork() );
	}

	public CyNetworkViewFactory getNetworkViewFactory() {
		return viewFactory;
	}
}


