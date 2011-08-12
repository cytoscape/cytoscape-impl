package org.cytoscape.task.internal.quickstart.subnetworkbuilder;


import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.internal.creation.NewNetworkSelectedNodesOnlyTask;
import org.cytoscape.task.internal.quickstart.ImportNetworkFromPublicDataSetTask;
import org.cytoscape.task.internal.quickstart.datasource.InteractionFilePreprocessor;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.undo.UndoSupport;


public class SubnetworkBuilderUtil {
	private CyNetworkReaderManager mgr;
	private CyNetworkManager netmgr;
	protected final CyNetworkViewManager networkViewManager;
	private Properties props;
	private StreamUtil streamUtil;

	protected final CyEventHelper eventHelper;
	protected final CyApplicationManager appManager;

	private CyNetworkNaming cyNetworkNaming;

	private final CyRootNetworkFactory crnf;
	private final CyNetworkViewFactory cnvf;

	private final Set<InteractionFilePreprocessor> processors;

	VisualStyleBuilder vsBuilder;
	final VisualMappingManager vmm;

	// For mapping generator
	final VisualStyleFactory vsFactory;
	private VisualMappingFunctionFactory discFactory;
	private VisualMappingFunctionFactory ptFactory;
	private final CyLayoutAlgorithmManager layouts;

	private final UndoSupport undoSupport;

	public SubnetworkBuilderUtil(CyNetworkReaderManager mgr, CyNetworkManager netmgr,
			final CyNetworkViewManager networkViewManager, CyProperty<Properties> cyProps,
			CyNetworkNaming cyNetworkNaming, StreamUtil streamUtil, final CyEventHelper eventHelper,
			final CyApplicationManager appManager, CyRootNetworkFactory crnf, CyNetworkViewFactory cnvf,
			final VisualMappingManager vmm, final VisualStyleFactory vsFactory,
			final CyLayoutAlgorithmManager layouts, final UndoSupport undoSupport)
	{
		this.mgr = mgr;
		this.netmgr = netmgr;
		this.networkViewManager = networkViewManager;
		this.crnf = crnf;
		this.cnvf = cnvf;
		this.vmm = vmm;
		this.props = cyProps.getProperties();
		this.cyNetworkNaming = cyNetworkNaming;
		this.streamUtil = streamUtil;
		this.processors = new HashSet<InteractionFilePreprocessor>();
		this.eventHelper = eventHelper;
		this.appManager = appManager;
		this.vsFactory = vsFactory;
		this.layouts = layouts;
		this.undoSupport = undoSupport;
	}

	public void addProcessor(InteractionFilePreprocessor processor, Map props) {
		if (processor != null)
			this.processors.add(processor);
	}

	public void removeProcessor(InteractionFilePreprocessor processor, Map props) {
		if (processor != null)
			this.processors.remove(processor);

	}

	UndoSupport getUndoSupport() { return undoSupport; }

	public Task getWebServiceImportTask() {
		return new ImportNetworkFromPublicDataSetTask(processors, mgr, netmgr, networkViewManager, props,
				cyNetworkNaming, streamUtil);
	}

	Task getNewNetworkSelectedNodesOnlyTask(final CyNetwork network) {
		return new NewNetworkSelectedNodesOnlyTask(undoSupport, network, crnf, cnvf, netmgr,
							   networkViewManager, cyNetworkNaming, vmm,
		                                           appManager, eventHelper);
	}

	Task getApplLayoutTask() {
		return new ApplyLayoutTask(this, layouts);
	}

	public void addFactory(VisualMappingFunctionFactory factory, Map props) {

		if (factory.toString().startsWith("Discrete Mapping"))
			discFactory = factory;
		else if (factory.toString().startsWith("Passthrough Mapping"))
			ptFactory = factory;

		if (discFactory != null && ptFactory != null) {
			this.vsBuilder = new VisualStyleBuilder(vsFactory, discFactory, ptFactory);
		}
	}

	void removeFactory(VisualMappingFunctionFactory factory, Map props) {

	}

}
