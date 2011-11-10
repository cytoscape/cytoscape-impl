/*
 * @author kozo.nishida
 */

package org.cytoscape.io.internal.read.graphml;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.ParserAdapter;

public class GraphMLReader extends AbstractTask implements CyNetworkReader {

	private VisualStyle[] visualstyles;
	private InputStream inputStream;

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkFactory cyNetworkFactory;
	
	private final CyLayoutAlgorithmManager layouts;

	private URL targetURL;

	// GraphML file name to be loaded.
	private String networkName;
	
	private GraphMLParser parser;
	private TaskMonitor taskMonitor;

	private final CyRootNetworkFactory cyRootNetworkFactory;

	public GraphMLReader(InputStream inputStream, final CyLayoutAlgorithmManager layouts,
			final CyNetworkFactory cyNetworkFactory, final CyNetworkViewFactory cyNetworkViewFactory, final CyRootNetworkFactory cyRootNetworkFactory) {
		if (inputStream == null)
			throw new NullPointerException("Input stream is null");
		if (cyNetworkViewFactory == null)
			throw new NullPointerException("CyNetworkViewFactory is null");
		if (cyNetworkFactory == null)
			throw new NullPointerException("CyNetworkFactory is null");

		this.inputStream = inputStream;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyRootNetworkFactory = cyRootNetworkFactory;
		this.layouts = layouts;
	}

	@Override
	public CyNetwork[] getCyNetworks() {
		if(parser == null)
			throw new IllegalStateException("Parser is not initialized.");
		
		return parser.getCyNetworks();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		try {
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			final SAXParser sp = spf.newSAXParser();
			final ParserAdapter pa = new ParserAdapter(sp.getParser());
			
			parser = new GraphMLParser(taskMonitor, cyNetworkFactory, cyRootNetworkFactory);

			pa.setContentHandler(parser);
			pa.setErrorHandler(parser);
			pa.parse(new InputSource(inputStream));

		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView view = cyNetworkViewFactory.getNetworkView(network);

		final CyLayoutAlgorithm layout = layouts.getDefaultLayout();
		layout.setNetworkView(view);
		
		// Force to run this task here to avoid concurrency problem.
		TaskIterator itr = layout.getTaskIterator();
		Task nextTask = itr.next();
		try {
			nextTask.run(taskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		return view;
	}
}
