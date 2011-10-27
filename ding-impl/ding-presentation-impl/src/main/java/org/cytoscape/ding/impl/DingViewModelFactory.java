package org.cytoscape.ding.impl;

import java.util.Properties;

import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;

import org.cytoscape.di.util.DIUtil;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DingViewModelFactory implements CyNetworkViewFactory {

	private static final Logger logger = LoggerFactory.getLogger(DingViewModelFactory.class);

	private final CyTableFactory dataTableFactory;
	private final CyRootNetworkFactory rootNetworkFactory;
	private final SpacialIndex2DFactory spacialFactory;
	private final UndoSupport undo;
	private final VisualLexicon dingLexicon;
	private final CyServiceRegistrar registrar;

	private DialogTaskManager dialogTaskManager;
	private SubmenuTaskManager menuTaskManager;
	private final CyNetworkTableManager tableMgr;
	private final CyEventHelper eventHelper;
	private ViewTaskFactoryListener vtfListener;
	private final AnnotationFactoryManager annMgr;

	public DingViewModelFactory(CyTableFactory dataTableFactory, CyRootNetworkFactory rootNetworkFactory,
			UndoSupport undo, SpacialIndex2DFactory spacialFactory, VisualLexicon dingLexicon, 
			DialogTaskManager dialogTaskManager, SubmenuTaskManager menuTaskManager,
			CyServiceRegistrar registrar, CyNetworkTableManager tableMgr, CyEventHelper eventHelper, 
			ViewTaskFactoryListener vtfListener,
			AnnotationFactoryManager annMgr) {

		this.dataTableFactory = DIUtil.stripProxy(dataTableFactory);
		this.rootNetworkFactory = DIUtil.stripProxy(rootNetworkFactory);
		this.spacialFactory = DIUtil.stripProxy(spacialFactory);
		this.undo = DIUtil.stripProxy(undo);
		this.dingLexicon = DIUtil.stripProxy(dingLexicon);
		this.dialogTaskManager = DIUtil.stripProxy(dialogTaskManager);
		this.registrar = DIUtil.stripProxy(registrar);
		this.tableMgr = DIUtil.stripProxy(tableMgr);
		this.eventHelper = DIUtil.stripProxy(eventHelper);
		this.vtfListener = vtfListener;
		this.annMgr = annMgr;
		this.menuTaskManager = menuTaskManager;
	}

	@Override
	public CyNetworkView getNetworkView(final CyNetwork network) {
		return getNetworkView(network, false);
	}

	@Override
	public CyNetworkView getNetworkView(final CyNetwork network, final Boolean useThreshold) {

		if (network == null)
			throw new IllegalArgumentException("Cannot create view without model.");

		final DGraphView dgv = new DGraphView(network, dataTableFactory, rootNetworkFactory, undo, spacialFactory, dingLexicon,
				vtfListener.nodeViewTFs, vtfListener.edgeViewTFs, vtfListener.emptySpaceTFs, vtfListener.dropNodeViewTFs, 
				vtfListener.dropEmptySpaceTFs, dialogTaskManager, menuTaskManager, eventHelper, tableMgr, annMgr);

		registrar.registerAllServices(dgv, new Properties());

		return dgv;
	}
}
