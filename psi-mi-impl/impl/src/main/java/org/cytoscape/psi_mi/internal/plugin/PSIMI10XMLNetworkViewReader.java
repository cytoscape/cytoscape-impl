package org.cytoscape.psi_mi.internal.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;

import java.util.List;


import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.psi_mi.internal.cyto_mapper.MapToCytoscape;
import org.cytoscape.psi_mi.internal.data_mapper.MapPsiOneToInteractions;
import org.cytoscape.psi_mi.internal.model.Interaction;
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

public class PSIMI10XMLNetworkViewReader extends AbstractTask implements CyNetworkReader {
	
	private static final Logger logger = LoggerFactory.getLogger(PSIMI10XMLNetworkViewReader.class);
	
	private static final int BUFFER_SIZE = 16384;
	
	private final CyNetworkViewFactory networkViewFactory;
	private final CyNetworkFactory networkFactory;
	
	private InputStream inputStream;
	private CyNetwork network;

	private CyLayoutAlgorithmManager layouts;
	
	private TaskMonitor parentTaskMonitor;

	public PSIMI10XMLNetworkViewReader(InputStream inputStream, CyNetworkFactory networkFactory, CyNetworkViewFactory networkViewFactory, CyLayoutAlgorithmManager layouts) {
		this.inputStream = inputStream;
		this.networkFactory = networkFactory;
		this.networkViewFactory = networkViewFactory;
		this.layouts = layouts;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		parentTaskMonitor = taskMonitor;
		long start = System.currentTimeMillis();
		
		taskMonitor.setStatusMessage("Loading PSI-MI 1.x XML file...");
		taskMonitor.setProgress(0.05d);
		String xml = readString(inputStream);

		List<Interaction> interactions = new ArrayList<Interaction>();

		final MapPsiOneToInteractions mapper = new MapPsiOneToInteractions(xml, interactions);
		mapper.doMapping();
		
		taskMonitor.setProgress(0.4d);

		//  Now map to Cytoscape network objects.
		network = networkFactory.createNetwork();
		final MapToCytoscape mapper2 = new MapToCytoscape(network, interactions, MapToCytoscape.SPOKE_VIEW);
		mapper2.doMapping();

		taskMonitor.setProgress(1.0d);
		logger.info("PSI-MI XML Data Import finihsed in " + (System.currentTimeMillis() - start) + " msec.");
	}


	/**
	 * Create big String object from the entire XML file
	 * TODO: is this OK for huge data files?
	 * 
	 * @param source
	 * @return
	 * @throws IOException
	 */
	private static String readString(InputStream source) throws IOException {
		final StringWriter writer = new StringWriter();
	
		final BufferedReader reader = new BufferedReader(new InputStreamReader(source));
		try {
			char[] buffer = new char[BUFFER_SIZE];
			int charactersRead = reader.read(buffer, 0, buffer.length);
			while (charactersRead != -1) {
				writer.write(buffer, 0, charactersRead);
				charactersRead = reader.read(buffer, 0, buffer.length);
			}
		} finally {
			reader.close();
		}
		return writer.toString();
	}

	@Override
	public CyNetwork[] getNetworks() {
		return new CyNetwork[] { network };
	}

	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network) {
		final CyNetworkView view = networkViewFactory.createNetworkView(network);
		final CyLayoutAlgorithm layout = layouts.getDefaultLayout();
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS,"");
		Task nextTask = itr.next();
		try {
			nextTask.run(parentTaskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		parentTaskMonitor.setProgress(1.0d);
		return view;		
	}
}
