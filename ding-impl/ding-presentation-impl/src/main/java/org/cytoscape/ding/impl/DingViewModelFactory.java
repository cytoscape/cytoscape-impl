package org.cytoscape.ding.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.di.util.DIUtil;
import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.dnd.DropNodeViewTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.work.TaskManager;
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


	private Map<NodeViewTaskFactory, Map> nodeViewTFs;
	private Map<EdgeViewTaskFactory, Map> edgeViewTFs;
	private Map<NetworkViewTaskFactory, Map> emptySpaceTFs;
	private Map<DropNodeViewTaskFactory, Map> dropNodeViewTFs;
	private Map<DropNetworkViewTaskFactory, Map> dropEmptySpaceTFs;

	private TaskManager tm;
	private final CyTableManager tableMgr;
	private final CyEventHelper eventHelper;

	public DingViewModelFactory(CyTableFactory dataTableFactory, CyRootNetworkFactory rootNetworkFactory,
			UndoSupport undo, SpacialIndex2DFactory spacialFactory, VisualLexicon dingLexicon, TaskManager tm,
			CyServiceRegistrar registrar, CyTableManager tableMgr, CyEventHelper eventHelper) {

		this.dataTableFactory = DIUtil.stripProxy(dataTableFactory);
		this.rootNetworkFactory = DIUtil.stripProxy(rootNetworkFactory);
		this.spacialFactory = DIUtil.stripProxy(spacialFactory);
		this.undo = DIUtil.stripProxy(undo);
		this.dingLexicon = DIUtil.stripProxy(dingLexicon);
		this.tm = DIUtil.stripProxy(tm);
		this.registrar = DIUtil.stripProxy(registrar);
		this.tableMgr = DIUtil.stripProxy(tableMgr);
		this.eventHelper = DIUtil.stripProxy(eventHelper);

		nodeViewTFs = new HashMap<NodeViewTaskFactory, Map>();
		edgeViewTFs = new HashMap<EdgeViewTaskFactory, Map>();
		emptySpaceTFs = new HashMap<NetworkViewTaskFactory, Map>();
		dropNodeViewTFs = new HashMap<DropNodeViewTaskFactory, Map>();
		dropEmptySpaceTFs = new HashMap<DropNetworkViewTaskFactory, Map>();
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
				nodeViewTFs, edgeViewTFs, emptySpaceTFs, dropNodeViewTFs, dropEmptySpaceTFs, tm, eventHelper, tableMgr);

		registrar.registerAllServices(dgv, new Properties());

		return dgv;
	}
}
