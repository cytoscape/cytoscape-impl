
package org.cytoscape.ding;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingViewModelFactory;
import org.cytoscape.ding.impl.ViewTaskFactoryListener;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.spacial.internal.rtree.RTreeFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

public class NetworkViewTestSupport extends NetworkTestSupport {

	protected CyNetworkViewFactory viewFactory;

	public NetworkViewTestSupport() {
		DVisualLexicon dVisualLexicon = new DVisualLexicon(mock(CustomGraphicsManager.class));
		ViewTaskFactoryListener vtfListener = mock(ViewTaskFactoryListener.class);
		AnnotationFactoryManager annotationFactoryManager = mock(AnnotationFactoryManager.class);
		DingGraphLOD dingGraphLOD = mock(DingGraphLOD.class);
		HandleFactory handleFactory = mock(HandleFactory.class);
		
        TableTestSupport tableTestSupport = new TableTestSupport();
        
        CyTableFactory tableFactory = tableTestSupport.getTableFactory();
        CyRootNetworkManager rootNetManager = getRootNetworkFactory();
		UndoSupport undoSupport = mock(UndoSupport.class);
		SpacialIndex2DFactory spacialFactory = new RTreeFactory();
		DialogTaskManager dialogTaskManager = mock(DialogTaskManager.class);
		CyNetworkTableManager tableManager = getNetworkTableManager();
		CyEventHelper eventHelper = mock(CyEventHelper.class);
		IconManager iconManager = mock(IconManager.class);
		VisualMappingManager visualMappingManager = mock(VisualMappingManager.class);
		CyNetworkViewManager netViewManager = mock(CyNetworkViewManager.class);
		
		CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		
		when(serviceRegistrar.getService(CyTableFactory.class)).thenReturn(tableFactory);
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(rootNetManager);
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(SpacialIndex2DFactory.class)).thenReturn(spacialFactory);
		when(serviceRegistrar.getService(DialogTaskManager.class)).thenReturn(dialogTaskManager);
		when(serviceRegistrar.getService(CyNetworkTableManager.class)).thenReturn(tableManager);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(IconManager.class)).thenReturn(iconManager);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(visualMappingManager);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewManager);

        viewFactory = new DingViewModelFactory(
        		dVisualLexicon,
        		vtfListener,
        		annotationFactoryManager,
        		dingGraphLOD,
        		handleFactory,
        		serviceRegistrar
        );
	}
	
	public CyNetworkView getNetworkView() {
		return viewFactory.createNetworkView( getNetwork() );
	}

	public CyNetworkViewFactory getNetworkViewFactory() {
		return viewFactory;
	}
}
