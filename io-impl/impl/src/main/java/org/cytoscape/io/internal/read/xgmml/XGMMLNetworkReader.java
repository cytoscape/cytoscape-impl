/*
 File: XGMMLReader.java

 Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute of Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.ParserAdapter;

/**
 * XGMML file reader.<br>
 * This version is Metanode-compatible.
 * 
 * @version 1.0
 * @since Cytoscape 2.3
 * @see cytoscape.data.writers.XGMMLWriter
 * @author kono
 */
public class XGMMLNetworkReader extends AbstractNetworkReader {

	protected static final String CY_NAMESPACE = "http://www.cytoscape.org";

	private final CyRootNetworkManager cyRootNetworkManager;
	private final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;
	private final XGMMLParser parser;
	private final ReadDataManager readDataMgr;
	private final VisualLexicon visualLexicon;
	private boolean viewFormat;
	private CyNetworkView netView;
	private CyRootNetwork parent;

	private static final Logger logger = LoggerFactory.getLogger(XGMMLNetworkReader.class);

	public XGMMLNetworkReader(final InputStream inputStream,
							  final CyNetworkViewFactory cyNetworkViewFactory,
							  final CyNetworkFactory cyNetworkFactory,
							  final CyRootNetworkManager cyRootNetworkManager,
							  final RenderingEngineManager renderingEngineMgr,
							  final ReadDataManager readDataMgr,
							  final XGMMLParser parser,
							  final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr) {
		super(inputStream, cyNetworkViewFactory, cyNetworkFactory);

		this.cyRootNetworkManager = cyRootNetworkManager;
		this.readDataMgr = readDataMgr;
		this.parser = parser;
		this.visualLexicon = renderingEngineMgr.getDefaultVisualLexicon();
		this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;
	}

	@Override
	public void run(TaskMonitor tm) throws IOException {
		tm.setProgress(0.0);
		
		readDataMgr.init();
		readDataMgr.setViewFormat(viewFormat);
		
		if (!viewFormat && parent != null)
			readDataMgr.setParentNetwork(parent);
		
		tm.setProgress(0.1);
		
		try {
			readXGMML();
			tm.setProgress(0.3);
			
			Set<CyNetwork> netSet = readDataMgr.getNetworks();
			this.cyNetworks = netSet.toArray(new CyNetwork[netSet.size()]);
		} catch (Exception e) {
			throw new IOException("Could not parse XGMML file.", e);
		} finally {
			readDataMgr.dispose();
		}

		tm.setProgress(1.0);
	}

	@Override
	public void cancel() {
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		netView = cyNetworkViewFactory.createNetworkView(network);

		if (netView != null) {
			// View Title:
			// (only when directly importing an XGMML file or if as part of a 2.x CYS file;
			//  otherwise the title is read from the view xgmml file)
			if (!readDataMgr.isSessionFormat() || readDataMgr.getDocumentVersion() < 3.0) {
				String name = network.getRow(network).get(CyNetwork.NAME, String.class);
				
				if (name != null)
					netView.setVisualProperty(MinimalVisualLexicon.NETWORK_TITLE, name);
			}

			// Nodes and edges
			if (netView.getModel().getNodeCount() > 0) {
				layoutNodeViews(netView.getModel().getNodeList());
				layoutEdgeViews(netView.getModel().getEdgeList());
			}

			// Network visual properties
			Map<String, String> atts = null;
			
			if (viewFormat) {
				// Direct visual properties
				atts = readDataMgr.getViewGraphicsAttributes(readDataMgr.getNetworkId(), false);
				layoutViewGraphics(netView, atts, false);
				// Locked visual properties
				atts = readDataMgr.getViewGraphicsAttributes(readDataMgr.getNetworkId(), true);
				layoutViewGraphics(netView, atts, true);
			} else {
				atts = readDataMgr.getGraphicsAttributes(network);
				layoutGraphics(netView, atts);
			}
			
			netView.updateView();
		}

		return netView;
	}

	public void setParent(CyNetwork n) {
		if (n != null) {
			this.parent = (n instanceof CyRootNetwork) ? (CyRootNetwork) n : cyRootNetworkManager.getRootNetwork(n);
		} else {
			this.parent = null;
		}
	}
	
	/**
	 * Only if reading network views from 3.0+ session formats.
	 * @param viewFormat
	 */
	public void setViewFormat(boolean viewFormat) {
		this.viewFormat = viewFormat;  // TODO: remove flag from here and add it to ReadDataManager only or a XGMMLConfig object
	}
	
	private void layoutNodeViews(final List<CyNode> nodes) {
		// Graphics (defaults & mappings)
		for (CyNode node : nodes) {
			final View<CyNode> nv = netView.getNodeView(node);
			Map<String, String> atts = null;
			
			if (viewFormat) {
				// When parsing view-format XGMML, the manager does not have the network model
				// to create a map by CyNode objects, so the graphics mapping is indexed by the old element id.
				String oldId = readDataMgr.getCache().getOldId(node.getSUID());
				// Direct visual properties
				atts = readDataMgr.getViewGraphicsAttributes(oldId, false);
				layoutViewGraphics(nv, atts, false);
				// Locked visual properties
				atts = readDataMgr.getViewGraphicsAttributes(oldId, true);
				layoutViewGraphics(nv, atts, true);
			} else {
				atts = readDataMgr.getGraphicsAttributes(node);
				layoutGraphics(nv, atts);
			}
		}
	}

	private void layoutEdgeViews(final List<CyEdge> edges) {
		for (CyEdge edge : edges) {
			final View<CyEdge> ev = netView.getEdgeView(edge);
			Map<String, String> atts = null;
			
			if (viewFormat) {
				// When parsing view-format XGMML, the manager does not have the network model
				// to create a map by CyEdge objects, so the graphics mapping is indexed by the old element id.
				String oldId = readDataMgr.getCache().getOldId(edge.getSUID());
				// Direct visual properties
				atts = readDataMgr.getViewGraphicsAttributes(oldId, false);
				layoutViewGraphics(ev, atts, false);
				// Locked visual properties
				atts = readDataMgr.getViewGraphicsAttributes(oldId, true);
				layoutViewGraphics(ev, atts, true);
			} else {
				atts = readDataMgr.getGraphicsAttributes(edge);
				layoutGraphics(ev, atts);
			}
			
			// TODO Edge bend
//			if (readDataMgr.getAttributeNS(attr, "curved", CY_NAMESPACE) != null) {
//				String value = readDataMgr.getAttributeNS(attr, "curved", CY_NAMESPACE);
//				if (value.equals("STRAIGHT_LINES")) {
//					ev.setLineType(EdgeView.STRAIGHT_LINES);
//				} else if (value.equals("CURVED_LINES")) {
//					ev.setLineType(EdgeView.CURVED_LINES);
//				}
//			}
//			if (readDataMgr.getAttribute(attr, "edgeHandleList") != null) {
//				String handles[] = readDataMgr.getAttribute(attr, "edgeHandleList").split(";");
//				for (int i = 0; i < handles.length; i++) {
//					String points[] = handles[i].split(",");
//					double x = (new Double(points[0])).doubleValue();
//					double y = (new Double(points[1])).doubleValue();
//					Point2D.Double point = new Point2D.Double();
//					point.setLocation(x, y);
//					ev.getBend().addHandle(point);
//				}
//			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void layoutGraphics(final View<? extends CyTableEntry> view, Map<String, String> atts) {
		if (view != null && atts != null) {
			CyTableEntry model = view.getModel();
			Class<?> type = CyNetwork.class;
			
			if (model instanceof CyNode)      type = CyNode.class;
			else if (model instanceof CyEdge) type = CyEdge.class;

			Set<String> attSet = atts.keySet();

			for (String attName : attSet) {
				VisualProperty vp = visualLexicon.lookup(type, attName);
				String attValue = atts.get(attName);

				if (vp != null) {
					if (isXGMMLTransparency(attName))
						attValue = convertXGMMLTransparencyValue(attValue);

					Object value = vp.parseSerializableString(attValue);

					if (value != null) {
						if (isLockedVisualProperty(model, attName))
							view.setLockedValue(vp, value);
						else
							view.setVisualProperty(vp, value);
					}
				} else {
					unrecognizedVisualPropertyMgr.addUnrecognizedVisualProperty(netView, view, attName, attValue);
				}
			}
		}
	}
	
	/**
	 * Only used by Cy3 view-format.
	 * @param view
	 * @param atts
	 * @param locked true is creating vizmap bypass properties.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void layoutViewGraphics(final View<? extends CyTableEntry> view, Map<String, String> atts, boolean locked) {
		// TODO: refactor--see layoutGraphics()
		if (view != null && atts != null) {
			CyTableEntry model = view.getModel();
			Class<?> type = CyNetwork.class;
			
			if (model instanceof CyNode)      type = CyNode.class;
			else if (model instanceof CyEdge) type = CyEdge.class;
			
			Set<String> attSet = atts.keySet();
			
			for (String attName : attSet) {
				VisualProperty vp = visualLexicon.lookup(type, attName);
				String attValue = atts.get(attName);
				
				if (vp != null) {
					Object value = vp.parseSerializableString(attValue);
					
					if (value != null) {
						if (locked)
							view.setLockedValue(vp, value);
						else
							view.setVisualProperty(vp, value);
					}
				} else {
					unrecognizedVisualPropertyMgr.addUnrecognizedVisualProperty(netView, view, attName, attValue);
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
	protected boolean isLockedVisualProperty(final CyTableEntry element, String attName) {
		// These are NOT locked properties
		boolean b = !((element instanceof CyNode) && attName.matches("x|y|z"));
		b = b &&
			!((element instanceof CyNetwork) && attName
					.matches("GRAPH_VIEW_ZOOM|GRAPH_VIEW_CENTER_X|GRAPH_VIEW_CENTER_Y"));

		return b;
	}

	/**
	 * Actual method to read XGMML documents.
	 * 
	 * @throws IOException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private void readXGMML() throws SAXException, IOException {
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
			throw new RuntimeException(
									   "Out of memory error caught! The network being loaded is too large for the current memory allocation.  Use the -Xmx flag for the java virtual machine to increase the amount of memory available, e.g. java -Xmx1G cytoscape.jar -p apps ....");
		} catch (ParserConfigurationException e) {
		} catch (SAXParseException e) {
			System.err.println("XGMMLParser: fatal parsing error on line " + e.getLineNumber() + " -- '" +
							   e.getMessage() + "'");
			throw e;
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
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
}
