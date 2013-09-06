package org.cytoscape.io.internal.read.xgmml;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
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
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.ParserAdapter;

public class GenericXGMMLReader extends AbstractCyNetworkReader {

	public static final String REPAIR_BARE_AMPERSANDS_PROPERTY = "cytoscape.xgmml.repair.bare.ampersands";

	protected final ReadDataManager readDataMgr;
	protected final XGMMLParser parser;
	protected final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;
	protected final VisualLexicon visualLexicon;
	
	private static final Map<String, String> legacyArrowShapes = new HashMap<String, String>();
	private static final Logger logger = LoggerFactory.getLogger(GenericXGMMLReader.class);
	
	static {
		legacyArrowShapes.put("0", "NONE"); // NO_END
		legacyArrowShapes.put("1", "DELTA"); // WHITE_DELTA
		legacyArrowShapes.put("2", "DELTA"); // BLACK_DELTA
		legacyArrowShapes.put("3", "DELTA"); // EDGE_COLOR_DELTA
		legacyArrowShapes.put("4", "ARROW"); // WHITE_ARROW
		legacyArrowShapes.put("5", "ARROW"); // BLACK_ARROW
		legacyArrowShapes.put("6", "ARROW"); // EDGE_COLOR_ARROW
		legacyArrowShapes.put("7", "DIAMOND"); // WHITE_DIAMOND
		legacyArrowShapes.put("8", "DIAMOND"); // BLACK_DIAMOND
		legacyArrowShapes.put("9", "DIAMOND"); // EDGE_COLOR_DIAMOND
		legacyArrowShapes.put("10", "CIRCLE"); // WHITE_CIRCLE
		legacyArrowShapes.put("11", "CIRCLE"); // BLACK_CIRCLE
		legacyArrowShapes.put("12", "CIRCLE"); // EDGE_COLOR_CIRCLE
		legacyArrowShapes.put("13", "T"); // WHITE_T
		legacyArrowShapes.put("14", "T"); // BLACK_T
		legacyArrowShapes.put("15", "T"); // EDGE_COLOR_T
		legacyArrowShapes.put("16", "HALF_TOP"); // EDGE_HALF_ARROW_TOP
		legacyArrowShapes.put("17", "HALF_BOTTOM"); // EDGE_HALF_ARROW_BOTTOM
		legacyArrowShapes.put("HALF_ARROW_TOP", "HALF_TOP"); // v2.8 bypass
		legacyArrowShapes.put("HALF_ARROW_BOTTOM", "HALF_BOTTOM"); // v2.8 bypass
	}
	
	public GenericXGMMLReader(final InputStream inputStream,
							  final CyNetworkViewFactory cyNetworkViewFactory,
							  final CyNetworkFactory cyNetworkFactory,
							  final RenderingEngineManager renderingEngineMgr,
							  final ReadDataManager readDataMgr,
							  final XGMMLParser parser,
							  final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
							  final CyNetworkManager cyNetworkManager, 
							  final CyRootNetworkManager cyRootNetworkManager,
							  final CyApplicationManager cyApplicationManager
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
		
		if (!SessionUtil.isReadingSessionFile()) {
			final List<CyNetwork> selectedNetworks = cyApplicationManager.getSelectedNetworks();
			if (selectedNetworks != null && selectedNetworks.size() > 0) {
				final CyNetwork selectedNetwork = cyApplicationManager.getSelectedNetworks().get(0);
				String rootName = "";
				if (selectedNetwork instanceof CySubNetwork) {
					CySubNetwork subnet = (CySubNetwork) selectedNetwork;
					CyRootNetwork rootNet = subnet.getRootNetwork();
					rootName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
				} else {
					// it is a root network
					rootName = selectedNetwork.getRow(selectedNetwork).get(CyNetwork.NAME, String.class);
				}
				getRootNetworkList().setSelectedValue(rootName);
			}
		}
	}

	@Override
	public void run(final TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
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
		
		return netView;
	}
	
	
	protected void init(final TaskMonitor tm) {
		readDataMgr.init();
		readDataMgr.setViewFormat(false); // TODO: refactor readDataMgr and delete this line
		
		// Now user has the option to import network into different collection
		final CyRootNetwork networkCollection = getRootNetwork();
		final Map<Object, CyNode> nMap = getNodeMap();
		
		readDataMgr.setNodeMap(nMap);
		readDataMgr.setParentNetwork(networkCollection);
	}


	protected void complete(TaskMonitor tm) {
		final Set<CyNetwork> netSet = readDataMgr.getPublicNetworks();
		this.networks = netSet.toArray(new CyNetwork[netSet.size()]);
	}


	/**
	 * Actual method to read XGMML documents.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	protected void readXGMML(final TaskMonitor tm) throws SAXException, IOException {
		final SAXParserFactory spf = SAXParserFactory.newInstance();

		try {
			// Get our parser
			final SAXParser sp = spf.newSAXParser();
			// Ignore the DTD declaration
			final XMLReader reader = sp.getXMLReader();
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			reader.setFeature("http://xml.org/sax/features/validation", false);
			// Make the SAX1 Parser act as a SAX2 XMLReader
			final ParserAdapter pa = new ParserAdapter(sp.getParser());
			pa.setContentHandler(parser);
			pa.setErrorHandler(parser);
			// Parse the XGMML input
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
				try {
					inputStream.close();
				} catch (Exception e) {
					logger.warn("Cannot close XGMML input stream", e);
				}
			}
		}
	}
	
	protected void setNetworkViewProperties(final CyNetworkView netView) {
		final CyNetwork network = netView.getModel();
		final String name = network.getRow(network).get(CyNetwork.NAME, String.class);
		
		if (name != null)
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_TITLE, name);
		
		final Map<String, String> atts = readDataMgr.getGraphicsAttributes(network);
		setVisualProperties(netView, netView, atts);
	}
	
	protected void setNodeViewProperties(final CyNetworkView netView, final View<CyNode> nodeView) {
		final CyNode node = nodeView.getModel();
		final Map<String, String> atts = readDataMgr.getGraphicsAttributes(node);
		setVisualProperties(netView, nodeView, atts);
	}

	protected void setEdgeViewProperties(final CyNetworkView netView, final View<CyEdge> edgeView) {
		final CyEdge edge = edgeView.getModel();
		final Map<String, String> atts = readDataMgr.getGraphicsAttributes(edge);
		setVisualProperties(netView, edgeView, atts);
		
		// For 2.x compatibility
		final Bend bend = edgeView.getVisualProperty(BasicVisualLexicon.EDGE_BEND);
		
		if (bend != null && bend != EdgeBendVisualProperty.DEFAULT_EDGE_BEND) {
			final List<Handle> handles = bend.getAllHandles();
			
			for (final Handle handle : handles)
				handle.defineHandle(netView, edgeView, Double.NaN, Double.NaN);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setVisualProperties(final CyNetworkView netView, final View<? extends CyIdentifiable> view,
			final Map<String, String> atts) {
		if (view != null && atts != null) {
			final CyIdentifiable model = view.getModel();
			Class<?> type = CyNetwork.class;
			
			if (model instanceof CyNode)      type = CyNode.class;
			else if (model instanceof CyEdge) type = CyEdge.class;

			final Set<String> attSet = atts.keySet();

			for (final String attName : attSet) {
				String attValue = atts.get(attName);
				final VisualProperty vp = visualLexicon.lookup(type, attName);
				
				if (vp != null) {
					if (isXGMMLTransparency(attName))
						attValue = convertXGMMLTransparencyValue(attValue);
					else if (isOldArrowShape(attName))
						attValue = convertOldArrowShapeValue(attValue);
					
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
		}
	}
	
	private static final Pattern DIRECT_NODE_PROPS_PATTERN = Pattern.compile("x|y|z");
	private static final Pattern DIRECT_NET_PROPS_PATTERN = Pattern.compile(
			"GRAPH_VIEW_(ZOOM|CENTER_(X|Y))|NETWORK_(WIDTH|HEIGHT|SCALE_FACTOR|CENTER_(X|Y|Z)_LOCATION)");
	
	/**
	 * It tells which graphics attributes should be set as locked properties.
	 * @param element
	 * @param attName
	 * @return
	 */
	protected boolean isLockedVisualProperty(final CyIdentifiable element, final String attName) {
		boolean locked = true;
		
		// These must NOT be set as locked properties
		if (element instanceof CyNetwork) {
			final Matcher netMatcher = DIRECT_NET_PROPS_PATTERN.matcher(attName);
			locked = !netMatcher.matches();
		} else if (element instanceof CyNode) {
			final Matcher nodeMatcher = DIRECT_NODE_PROPS_PATTERN.matcher(attName);
			locked = !nodeMatcher.matches();
		} else if (element instanceof CyEdge) {
			// Nothing to do here; all edge properties are locked by default.
		}

		return locked;
	}
	
	private static final Pattern XGMML_TRANSPARENCY_PATTERN = Pattern.compile("(cy:)?(node|edge)Transparency");
	
	static boolean isXGMMLTransparency(final String attName) {
		final Matcher matcher = XGMML_TRANSPARENCY_PATTERN.matcher(attName);
		return matcher.matches();
	}
	
	private static final Pattern OLD_ARROW_SHAPE_PATTERN = Pattern.compile("(?i)(cy:|edge)?(source|target)Arrow(Shape)?");
	
	static boolean isOldArrowShape(final String attName) {
		final Matcher matcher = OLD_ARROW_SHAPE_PATTERN.matcher(attName);
		return matcher.matches();
	}

	private static final Pattern OLD_FONT_PATTERN = Pattern.compile("(cy:)?(node|edge)LabelFont");
	
	static boolean isOldFont(final String attName) {
		final Matcher matcher = OLD_FONT_PATTERN.matcher(attName);
		return matcher.matches();
	}

	static String convertXGMMLTransparencyValue(final String s) {
		// Opacity is saved in XGMML as a float from 0.0-1.0, but Cytoscape uses 0-255
		try {
			final float f = Float.valueOf(s);
			return "" + Math.round(f * 255);
		} catch (Exception e) {
			logger.warn("Cannot convert XGMML transparency value: " + s, e);
		}

		return "255";
	}
	
	static String convertOldArrowShapeValue(final String s) {
		// Arrow shape is saved in Cy2 XGMML as integers
		String value = legacyArrowShapes.get(s);
		
		if (value == null)
			value = s;
		
		return value;
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
