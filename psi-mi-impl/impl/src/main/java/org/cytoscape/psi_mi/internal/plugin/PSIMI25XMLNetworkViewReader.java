package org.cytoscape.psi_mi.internal.plugin;

import java.io.InputStream;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.psi_mi.internal.data_mapper.PSIMI25EntryMapper;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.EntrySet;

public class PSIMI25XMLNetworkViewReader extends AbstractTask implements CyNetworkReader {
	
	private static final Logger logger = LoggerFactory.getLogger(PSIMI25XMLNetworkViewReader.class);
	
	
	private final CyNetworkViewFactory networkViewFactory;
	private final CyNetworkFactory networkFactory;
	
	private InputStream inputStream;
	private CyNetwork network;

	private CyLayoutAlgorithmManager layouts;
	
	private TaskMonitor parentTaskMonitor;
	
	private PSIMI25EntryMapper mapper;
	
	private boolean cancelFlag = false;

	public PSIMI25XMLNetworkViewReader(InputStream inputStream, CyNetworkFactory networkFactory, CyNetworkViewFactory networkViewFactory, CyLayoutAlgorithmManager layouts) {
		this.inputStream = inputStream;
		this.networkFactory = networkFactory;
		this.networkViewFactory = networkViewFactory;
		this.layouts = layouts;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		parentTaskMonitor = taskMonitor;
		long start = System.currentTimeMillis();
		
		taskMonitor.setProgress(0.01d);
		taskMonitor.setTitle("Loading PSI-MI 2.5.x XML File ");
		taskMonitor.setStatusMessage("Loading data file in PSI-MI 2.5 XML format.");
		
		PsimiXmlReader reader = new PsimiXmlReader();
		EntrySet result = reader.read(inputStream);		
		taskMonitor.setProgress(0.4d);
		taskMonitor.setStatusMessage("Data Loaded.  Mapping Data to Network...");

		if(cancelFlag) {
			inputStream.close();
			reader = null;
			result = null;
			return;
		}
		
		network = networkFactory.createNetwork();
		mapper = new PSIMI25EntryMapper(network, result);
		mapper.map();
		
		taskMonitor.setProgress(1.0d);
		logger.info("PSI-MI XML Data Import finihsed in " + (System.currentTimeMillis() - start) + " msec.");
	}

	@Override
	public CyNetwork[] getNetworks() {
		return new CyNetwork[] { network };
	}

	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network) {
		final CyNetworkView view = networkViewFactory.createNetworkView(network);
		final CyLayoutAlgorithm layout = layouts.getDefaultLayout();
		layout.setNetworkView(view);
		// Force to run this task here to avoid concurrency problem.
		TaskIterator itr = layout.createTaskIterator();
		Task nextTask = itr.next();
		try {
			nextTask.run(parentTaskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		parentTaskMonitor.setProgress(1.0d);
		return view;		
	}
	
	@Override
	public void cancel() {
		cancelFlag = true;
		mapper.cancel();
	}
}
