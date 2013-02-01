package org.cytoscape.ding.impl;

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

import java.util.Properties;

import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DingViewModelFactory implements CyNetworkViewFactory {

	private static final Logger logger = LoggerFactory.getLogger(DingViewModelFactory.class);

	private final CyTableFactory dataTableFactory;
	private final CyRootNetworkManager rootNetworkManager;
	private final SpacialIndex2DFactory spacialFactory;
	private final UndoSupport undo;
	private final VisualLexicon dingLexicon;
	private final CyServiceRegistrar registrar;

	private DialogTaskManager dialogTaskManager;
	private final CyNetworkTableManager tableMgr;
	private final CyEventHelper eventHelper;
	private ViewTaskFactoryListener vtfListener;
	private final AnnotationFactoryManager annMgr;
	
	private final DingGraphLOD dingGraphLOD;
	private final VisualMappingManager vmm;

	private final CyNetworkViewManager netViewMgr;
	private final HandleFactory handleFactory;

	public DingViewModelFactory(CyTableFactory dataTableFactory, CyRootNetworkManager rootNetworkManager,
			UndoSupport undo, SpacialIndex2DFactory spacialFactory, VisualLexicon dingLexicon, 
			DialogTaskManager dialogTaskManager,
			CyServiceRegistrar registrar, CyNetworkTableManager tableMgr, CyEventHelper eventHelper, 
			ViewTaskFactoryListener vtfListener,
			AnnotationFactoryManager annMgr, DingGraphLOD dingGraphLOD, final VisualMappingManager vmm,
			final CyNetworkViewManager netViewMgr, final HandleFactory handleFactory) {

		this.dataTableFactory = dataTableFactory;
		this.rootNetworkManager = rootNetworkManager;
		this.spacialFactory = spacialFactory;
		this.undo = undo;
		this.dingLexicon = dingLexicon;
		this.dialogTaskManager = dialogTaskManager;
		this.registrar = registrar;
		this.tableMgr = tableMgr;
		this.eventHelper = eventHelper;
		this.vtfListener = vtfListener;
		this.annMgr = annMgr;
		this.dingGraphLOD = dingGraphLOD;
		this.vmm=vmm;
		
		this.netViewMgr = netViewMgr;
		this.handleFactory = handleFactory;
	}

	@Override
	public CyNetworkView createNetworkView(final CyNetwork network) {
		if (network == null)
			throw new IllegalArgumentException("Cannot create view without model.");

		final DGraphView dgv = new DGraphView(network, rootNetworkManager, undo, spacialFactory, dingLexicon,
				vtfListener, dialogTaskManager, eventHelper, annMgr, dingGraphLOD, vmm, netViewMgr, handleFactory, registrar);

		dgv.registerServices();

		return dgv;
	}
}
