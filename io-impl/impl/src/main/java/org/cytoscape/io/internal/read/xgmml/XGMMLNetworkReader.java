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

import java.io.InputStream;
import java.util.Map;

import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.work.TaskMonitor;

/**
 * This XGMML reader is able to handle:
 * <ul><li>XGMML files contained in Cytoscape 2.x session files</li>
 *     <li>XGMML files that serialize CyNetworks in Cy3 session files</li>
 *     <li>Generic XGMML files that are not loaded from Cytoscape session files</li></ul>
 */
public class XGMMLNetworkReader extends AbstractXGMMLReader {

	protected static final String CY_NAMESPACE = "http://www.cytoscape.org";

	private final CyRootNetworkManager cyRootNetworkManager;
	private CyRootNetwork parent;

	public XGMMLNetworkReader(final InputStream inputStream,
							  final CyNetworkViewFactory cyNetworkViewFactory,
							  final CyNetworkFactory cyNetworkFactory,
							  final RenderingEngineManager renderingEngineMgr,
							  final CyRootNetworkManager cyRootNetworkManager,
							  final ReadDataManager readDataMgr,
							  final XGMMLParser parser,
							  final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr) {
		super(inputStream, cyNetworkViewFactory, cyNetworkFactory, renderingEngineMgr, readDataMgr, parser,
				unrecognizedVisualPropertyMgr);

		this.cyRootNetworkManager = cyRootNetworkManager;
	}

	public void setParent(CyNetwork n) {
		this.parent = n != null ? cyRootNetworkManager.getRootNetwork(n) : null;
	}
	
	@Override
	protected void init(TaskMonitor tm) {
		super.init(tm);
		
		readDataMgr.setViewFormat(false);
		readDataMgr.setParentNetwork(parent);
	}
	
	@Override
	protected void setNetworkViewProperties(CyNetworkView netView) {
		final CyNetwork network = netView.getModel();
		
		// View Title:
		// (only when directly importing an XGMML file or if as part of a 2.x CYS file;
		//  otherwise the title is read from the view xgmml file)
		if (!readDataMgr.isSessionFormat() || readDataMgr.getDocumentVersion() < 3.0) {
			String name = network.getRow(network).get(CyNetwork.NAME, String.class);
			
			if (name != null)
				netView.setVisualProperty(MinimalVisualLexicon.NETWORK_TITLE, name);
		}
		
		// Network visual properties
		Map<String, String> atts = readDataMgr.getGraphicsAttributes(network);
		setVisualProperties(netView, netView, atts);
	}

	@Override
	protected void setNodeViewProperties(CyNetworkView netView, View<CyNode> nodeView) {
		final CyNode node = nodeView.getModel();
		final Map<String, String> atts = readDataMgr.getGraphicsAttributes(node);
		setVisualProperties(netView, nodeView, atts);
	}

	@Override
	protected void setEdgeViewProperties(CyNetworkView netView, View<CyEdge> edgeView) {
		final CyEdge edge = edgeView.getModel();
		final Map<String, String> atts = readDataMgr.getGraphicsAttributes(edge);
		setVisualProperties(netView, edgeView, atts);
		
		// TODO Edge bend
//		if (readDataMgr.getAttributeNS(attr, "curved", CY_NAMESPACE) != null) {
//			String value = readDataMgr.getAttributeNS(attr, "curved", CY_NAMESPACE);
//			if (value.equals("STRAIGHT_LINES")) {
//				ev.setLineType(EdgeView.STRAIGHT_LINES);
//			} else if (value.equals("CURVED_LINES")) {
//				ev.setLineType(EdgeView.CURVED_LINES);
//			}
//		}
//		if (readDataMgr.getAttribute(attr, "edgeHandleList") != null) {
//			String handles[] = readDataMgr.getAttribute(attr, "edgeHandleList").split(";");
//			for (int i = 0; i < handles.length; i++) {
//				String points[] = handles[i].split(",");
//				double x = (new Double(points[0])).doubleValue();
//				double y = (new Double(points[1])).doubleValue();
//				Point2D.Double point = new Point2D.Double();
//				point.setLocation(x, y);
//				ev.getBend().addHandle(point);
//			}
//		}
	}
}
