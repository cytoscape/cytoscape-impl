/*
 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.internal.read.xgmml;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.cytoscape.io.internal.read.AbstractNetworkReader;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.EdgeBendVisualProperty;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.ParserAdapter;

public class GenericXGMMLReader extends AbstractNetworkReader {

	public static final String REPAIR_BARE_AMPERSANDS_PROPERTY = "cytoscape.xgmml.repair.bare.ampersands";
	
	protected final ReadDataManager readDataMgr;
	protected final XGMMLParser parser;
	protected final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;
	protected final VisualLexicon visualLexicon;
	
	private static final Logger logger = LoggerFactory.getLogger(GenericXGMMLReader.class);
	
	public GenericXGMMLReader(final InputStream inputStream,
							  final CyNetworkViewFactory cyNetworkViewFactory,
							  final CyNetworkFactory cyNetworkFactory,
							  final RenderingEngineManager renderingEngineMgr,
							  final ReadDataManager readDataMgr,
							  final XGMMLParser parser,
							  final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
							  final CyNetworkManager cyNetworkManager, 
							  final CyRootNetworkManager cyRootNetworkManager
							  ) {
		super(inputStream, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		this.readDataMgr = readDataMgr;
		this.parser = parser;
		this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;
		this.visualLexicon = renderingEngineMgr.getDefaultVisualLexicon();
		
		// This should only be used when an XGMML file or session cannot be read due to improperly encoded ampersands,
		// as it slows down the reading process.
		final boolean attemptRepair = Boolean.getBoolean(REPAIR_BARE_AMPERSANDS_PROPERTY);
		
		if (attemptRepair)
			this.inputStream = new RepairBareAmpersandsInputStream(inputStream, 512);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		
		// Now user has the option to import network into different collection
		this.initNodeMap(name2RootMap.get(rootNetworkList.getSelectedValue()), this.targetColumnList.getSelectedValue());		
		this.readDataMgr.setNodeMap(this.nMap);
		this.readDataMgr.setRootNetwork(name2RootMap.get(rootNetworkList.getSelectedValue()));
		
		init(tm);
		
		try {
			readXGMML(tm);
			complete(tm);
		} catch (Exception e) {
			throw new IOException("Could not parse XGMML file.", e);
		} finally {
			readDataMgr.dispose();
		}

		tm.setProgress(1.0);
	}

	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network) {
		final CyNetworkView netView = cyNetworkViewFactory.createNetworkView(network);
		setNetworkViewProperties(netView);
		
		if (netView.getModel().getNodeCount() > 0) {
			final Collection<View<CyNode>> nodes = netView.getNodeViews();
			final Collection<View<CyEdge>> edges = netView.getEdgeViews();
			
			for (final View<CyNode> nodeView : nodes)
				setNodeViewProperties(netView, nodeView);
			for (final View<CyEdge> edgeView : edges)
				setEdgeViewProperties(netView, edgeView);
		}

		if (!readDataMgr.isSessionFormat())
			readDataMgr.updateGroupNodes(netView);
		
		netView.updateView();

		return netView;
	}
	
	protected void init(TaskMonitor tm) {
		readDataMgr.init();
		readDataMgr.setViewFormat(false); // TODO: refactor readDataMgr and delete this line
	}
	
	protected void complete(TaskMonitor tm) {
		Set<CyNetwork> netSet = readDataMgr.getPublicNetworks();
		this.cyNetworks = netSet.toArray(new CyNetwork[netSet.size()]);
	}
	
	/**
	 * Actual method to read XGMML documents.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	protected void readXGMML(TaskMonitor tm) throws SAXException, IOException {
		final SAXParserFactory spf = SAXParserFactory.newInstance();

		try {
			// Get our parser
			SAXParser sp = spf.newSAXParser();
			ParserAdapter pa = new ParserAdapter(sp.getParser());
			pa.setContentHandler(parser);
			pa.setErrorHandler(parser);
			pa.parse(new InputSource(inputStream));
		} catch (OutOfMemoryError oe) {
			// It's not generally a good idea to catch OutOfMemoryErrors, but in
			// this case, where we know the culprit (a file that is too large),
			// we can at least try to degrade gracefully.
			System.gc();
			throw new RuntimeException("Out of memory error caught. The network being loaded is too large for the current memory allocation.  Use the -Xmx flag for the java virtual machine to increase the amount of memory available, e.g. java -Xmx1G cytoscape.jar -p apps ....");
		} catch (ParserConfigurationException e) {
			logger.error("XGMMLParser: " + e.getMessage());
		} catch (SAXParseException e) {
			logger.error("XGMMLParser: fatal parsing error on line " + e.getLineNumber() + " -- '" + e.getMessage()
					+ "'");
			throw e;
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
	}
	
	protected void setNetworkViewProperties(CyNetworkView netView) {
		final CyNetwork network = netView.getModel();
		
		String name = network.getRow(network).get(CyNetwork.NAME, String.class);
		
		if (name != null)
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_TITLE, name);
		
		Map<String, String> atts = readDataMgr.getGraphicsAttributes(network);
		setVisualProperties(netView, netView, atts);
	}
	
	protected void setNodeViewProperties(CyNetworkView netView, View<CyNode> nodeView) {
		final CyNode node = nodeView.getModel();
		final Map<String, String> atts = readDataMgr.getGraphicsAttributes(node);
		setVisualProperties(netView, nodeView, atts);
	}

	protected void setEdgeViewProperties(CyNetworkView netView, View<CyEdge> edgeView) {
		final CyEdge edge = edgeView.getModel();
		final Map<String, String> atts = readDataMgr.getGraphicsAttributes(edge);
		setVisualProperties(netView, edgeView, atts);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setVisualProperties(final CyNetworkView netView, final View<? extends CyIdentifiable> view,
			Map<String, String> atts) {
		if (view != null && atts != null) {
			CyIdentifiable model = view.getModel();
			Class<?> type = CyNetwork.class;
			
			if (model instanceof CyNode)      type = CyNode.class;
			else if (model instanceof CyEdge) type = CyEdge.class;

			Set<String> attSet = atts.keySet();

			for (String attName : attSet) {
				String attValue = atts.get(attName);
				VisualProperty vp = visualLexicon.lookup(type, attName);
				
				if (vp != null) {
					if (isXGMMLTransparency(attName))
						attValue = convertXGMMLTransparencyValue(attValue);
					
					final Object parsedValue = vp.parseSerializableString(attValue);

					if (parsedValue != null) {
						if (isLockedVisualProperty(model, attName))
							view.setLockedValue(vp, parsedValue);
						else
							view.setVisualProperty(vp, parsedValue);
					}
				} else {
					unrecognizedVisualPropertyMgr.addUnrecognizedVisualProperty(netView, view, attName, attValue);
				}
			}
			
			// For 2.x compatibility
			final Bend bend = view.getVisualProperty(BasicVisualLexicon.EDGE_BEND);
			if (bend != null && bend != EdgeBendVisualProperty.DEFAULT_EDGE_BEND) {
				final List<Handle> handles = bend.getAllHandles();
				for (Handle handle : handles) {
					handle.defineHandle(netView, (View<CyEdge>) view, Double.NaN, Double.NaN);
				}
			}
		}
	}
	
	/**
	 * It tells which graphics attributes should be set as locked properties.
	 * @param element
	 * @param attName
	 * @return
	 */
	protected boolean isLockedVisualProperty(final CyIdentifiable element, String attName) {
		// These are NOT locked properties
		boolean b = !((element instanceof CyNode) && attName.matches("x|y|z"));
		b = b &&
			!((element instanceof CyNetwork) && 
					attName.matches("GRAPH_VIEW_(ZOOM|CENTER_(X|Y))|" + 
									"NETWORK_(WIDTH|HEIGHT|SCALE_FACTOR|CENTER_(X|Y|Z)_LOCATION)"));

		return b;
	}

	static boolean isXGMMLTransparency(String attName) {
		return attName.matches("(cy:)?(node|edge)Transparency");
	}

	static boolean isOldFont(String attName) {
		return attName.matches("(cy:)?(node|edge)LabelFont");
	}

	static String convertXGMMLTransparencyValue(String s) {
		// Opacity is saved in XGMML as a float from 0.0-1.0, but Cytoscape uses 0-255
		try {
			float f = Float.parseFloat(s);
			return "" + Math.round(f * 255);
		} catch (Exception e) {
			logger.warn("Cannot convert XGMML transparency value: " + s, e);
		}

		return "255";
	}

	static String convertOldFontValue(String s) {
		// e.g. from "ACaslonPro-Bold-0-18" to "ACaslonPro,bold,18"
		//      from "SansSerif-0-12"       to "SansSerif,plain,12"
		if (s.matches("(?i)[^\\-]+(-bold)?-\\d+(\\.\\d+)?-\\d+(\\.\\d+)?")) {
			String name = s.replaceAll("(?i)(\\.bold)?(-bold)?-\\d+(\\.\\d+)?-\\d+(\\.\\d+)?", "");
			String weight = s.matches("(?i)[^\\-]+(\\.|-)bold-.*") ? "bold" : "plain";
			String size = s.replaceAll("(?i)[^\\-]+(-bold)?-\\d+(\\.\\d+)?-", "").replaceAll("\\.\\d+", "");

			return name + "," + weight + "," + size;
		}

		return s;
	}
	
	private static class RepairBareAmpersandsInputStream extends PushbackInputStream {
		private final byte[] encodedAmpersand = new byte[] { 'a', 'm', 'p', ';' };

		public RepairBareAmpersandsInputStream(final InputStream in) {
			super(in);
		}

		public RepairBareAmpersandsInputStream(final InputStream in, final int size) {
			super(in, size);
		}

		@Override
		public int read() throws IOException {
			int c;

			c = super.read();
			if (c == (int) '&') {
				byte[] b = new byte[7];
				int cnt;

				cnt = read(b);
				if (cnt > 0) {
					boolean isEntity;
					int i;

					isEntity = false;
					i = 0;
					while ((i < cnt) && (!isEntity)) {
						isEntity = (b[i] == ';');
						i++;
					}

					byte[] pb = new byte[cnt];
					for (int p = 0; p < cnt; p++) {
						pb[p] = b[p];
					}
					unread(pb);

					if (!isEntity) {
						unread(encodedAmpersand);
					}
				}
			}

			return c;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (b == null) {
				throw new NullPointerException();
			} else if (off < 0 || len < 0 || len > b.length - off) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return 0;
			}

			int cnt;
			int c = -1;

			cnt = 0;
			while (cnt < len) {
				c = read();
				if (c == -1) {
					break;
				}
				b[off] = (byte) c;
				off++;
				cnt++;
			}

			if ((c == -1) && (cnt == 0)) {
				cnt = -1;
			}

			return cnt;
		}
	}
}
