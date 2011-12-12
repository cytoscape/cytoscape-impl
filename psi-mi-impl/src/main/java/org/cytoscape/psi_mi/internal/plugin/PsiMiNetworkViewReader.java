package org.cytoscape.psi_mi.internal.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.psi_mi.internal.cyto_mapper.MapToCytoscape;
import org.cytoscape.psi_mi.internal.data_mapper.MapPsiOneToInteractions;
import org.cytoscape.psi_mi.internal.data_mapper.MapPsiTwoFiveToInteractions;
import org.cytoscape.psi_mi.internal.model.Interaction;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class PsiMiNetworkViewReader extends AbstractTask implements CyNetworkReader {
	
	private static final int BUFFER_SIZE = 16384;
	
	private final CyNetworkViewFactory networkViewFactory;
	private final CyNetworkFactory networkFactory;
	
	private InputStream inputStream;

	private CyNetwork network;

	private CyLayoutAlgorithmManager layouts;

	public PsiMiNetworkViewReader(InputStream inputStream, CyNetworkFactory networkFactory, CyNetworkViewFactory networkViewFactory, CyLayoutAlgorithmManager layouts) {
		this.inputStream = inputStream;
		this.networkFactory = networkFactory;
		this.networkViewFactory = networkViewFactory;
		this.layouts = layouts;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		String xml = readString(inputStream);

		ArrayList<Interaction> interactions = new ArrayList<Interaction>();

		//  Pick one of two mappers
		int level2 = xml.indexOf("level=\"2\"");

		if ((level2 > 0) && (level2 < 500)) {
			MapPsiTwoFiveToInteractions mapper = new MapPsiTwoFiveToInteractions(xml, interactions);
			mapper.doMapping();
		} else {
			MapPsiOneToInteractions mapper = new MapPsiOneToInteractions(xml, interactions);
			mapper.doMapping();
		}
		taskMonitor.setProgress(0.25);

		//  Now map to Cytoscape network objects.
		network = networkFactory.createNetwork();
		MapToCytoscape mapper2 = new MapToCytoscape(network, interactions, MapToCytoscape.SPOKE_VIEW);
		mapper2.doMapping();

		taskMonitor.setProgress(1.0);
	}



	private static String readString(InputStream source) throws IOException {
		StringWriter writer = new StringWriter();
		BufferedReader reader = new BufferedReader(new InputStreamReader(source));
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
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		
		final CyNetworkView networkView = networkViewFactory.createNetworkView(network);
		
		CyLayoutAlgorithm taskFactory = layouts.getDefaultLayout();
		taskFactory.setNetworkView(networkView);
		TaskIterator taskIterator = taskFactory.createTaskIterator();
		Task task = taskIterator.next();
		insertTasksAfterCurrentTask(task);
		
		return networkView;
	}
}
