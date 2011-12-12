/*
 Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.io.internal.read.session;

import static org.cytoscape.io.internal.util.session.SessionUtil.BOOKMARKS_FILE;
import static org.cytoscape.io.internal.util.session.SessionUtil.CYSESSION;
import static org.cytoscape.io.internal.util.session.SessionUtil.CY_PROPS;
import static org.cytoscape.io.internal.util.session.SessionUtil.NETWORK_ROOT;
import static org.cytoscape.io.internal.util.session.SessionUtil.APPS_FOLDER;
import static org.cytoscape.io.internal.util.session.SessionUtil.VIZMAP_PROPS;
import static org.cytoscape.io.internal.util.session.SessionUtil.XGMML_EXT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.cytoscape.io.internal.read.xgmml.XGMMLNetworkReader;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReader;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.VizmapReader;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.session.Child;
import org.cytoscape.property.session.Cysession;
import org.cytoscape.property.session.Desktop;
import org.cytoscape.property.session.Edge;
import org.cytoscape.property.session.Network;
import org.cytoscape.property.session.NetworkFrame;
import org.cytoscape.property.session.Node;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;

/**
 * Session reader implementation that handles the Cytoscape 2.x session format.
 */
public class Cy2SessionReaderImpl extends AbstractSessionReader {
	
	public static final Pattern NETWORK_PATTERN = Pattern.compile(".*/(([^/]+)[.]xgmml)");
	
	private final CyNetworkReaderManager networkReaderMgr;
	private final CyPropertyReaderManager propertyReaderMgr;
	private final VizmapReaderManager vizmapReaderMgr;
	private final CyRootNetworkManager rootNetworkManager;

	private final Map<String, CyNetwork> networkLookup = new HashMap<String, CyNetwork>();
	private final Map<String, CyNetworkView[]> networkViewLookup = new HashMap<String, CyNetworkView[]>();
	private Map<String, String> xgmmlEntries;

	/**
	 */
	public Cy2SessionReaderImpl(final InputStream sourceInputStream,
								final ReadCache cache,
								final CyNetworkReaderManager networkReaderMgr,
								final CyPropertyReaderManager propertyReaderMgr,
								final VizmapReaderManager vizmapReaderMgr,
								final CyRootNetworkManager rootNetworkManager) {
		super(sourceInputStream, cache);
		
		if (networkReaderMgr == null)
			throw new NullPointerException("network reader manager is null!");
		this.networkReaderMgr = networkReaderMgr;

		if (propertyReaderMgr == null)
			throw new NullPointerException("property reader manager is null!");
		this.propertyReaderMgr = propertyReaderMgr;

		if (vizmapReaderMgr == null)
			throw new NullPointerException("vizmap reader manager is null!");
		this.vizmapReaderMgr = vizmapReaderMgr;

		if (rootNetworkManager == null)
			throw new NullPointerException("root network factory is null!");
		this.rootNetworkManager = rootNetworkManager;

		xgmmlEntries = new HashMap<String, String>();
	}
	
	@Override
	protected void handleEntry(InputStream is, String entryName) throws Exception {
		if (entryName.contains("/" + APPS_FOLDER + "/")) {
			extractAppEntry(is, entryName);
		} else if (entryName.endsWith(CYSESSION)) {
			extractSessionState(is, entryName);
		} else if (entryName.endsWith(VIZMAP_PROPS)) {
			extractVizmap(is, entryName);
		} else if (entryName.endsWith(CY_PROPS)) {
			extractCytoscapeProps(is, entryName);
		} else if (entryName.endsWith(XGMML_EXT)) {
			// Don't extract the network now!
			// Just save the entry path, so it can be extracted
			// after the cysession file is parsed.
			Matcher matcher = NETWORK_PATTERN.matcher(entryName);

			if (matcher.matches()) {
				String fileName = matcher.group(1);
				xgmmlEntries.put(fileName, entryName);
			}
		} else if (entryName.endsWith(BOOKMARKS_FILE)) {
			extractBookmarks(is, entryName);
		} else {
			logger.warn("Unknown entry found in session zip file!\n" + entryName);
		}
	}
	
	@Override
	protected void complete() throws Exception {
		if (cysession == null) {
			throw new FileNotFoundException("Cannot find the " + CYSESSION + " file.");
		}

		tm.setProgress(0.4);
		tm.setTitle("Recreate networks");
		tm.setStatusMessage("Recreating networks...");
		extractNetworks();
		
		tm.setProgress(0.8);
		tm.setTitle("Process networks");
		tm.setStatusMessage("Processing networks...");
		processNetworks();
		
		super.complete();
	}
	
	private void extractNetworks() throws JAXBException, IOException {
		// Extract the XGMML files
		Map<String, Network> netMap = new HashMap<String, Network>();

		for (Network curNet : cysession.getNetworkTree().getNetwork()) {
			netMap.put(curNet.getId(), curNet);
		}

		walkNetworkTree(netMap.get(NETWORK_ROOT), null, netMap);
	}
	
	private void walkNetworkTree(final Network net, final CyRootNetwork parent, Map<String, Network> netMap) {
		// Get the list of children under this root
		final List<Child> children = net.getChild();

		// Traverse using recursive call
		final int numChildren = children.size();
		Child child = null;
		Network childNet = null;

		for (int i = 0; i < numChildren; i++) {
			child = children.get(i);
			childNet = netMap.get(child.getId());

			String entryName = xgmmlEntries.get(childNet.getFilename());
			CyRootNetwork rootNetwork = null;
			InputStream is = null;
			
			try {
				is = findEntry(entryName);
				
				if (is != null) {
					rootNetwork = extractNetworksAndViews(is, entryName, parent, childNet.isViewAvailable());
				} else {
					logger.error("Cannot find network file \"" + entryName + "\": ");
				}
			} catch (final Exception e) {
				final String message = "Unable to read XGMML file \"" + childNet.getFilename() + "\": "
						+ e.getMessage();
				logger.error(message, e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (final Exception ex) {
						logger.error("Unable to close XGMML input stream.", ex);
					}
					is = null;
				}

				// Always try to load child networks, even if the parent network is bad
				if (childNet.getChild().size() != 0)
					walkNetworkTree(childNet, rootNetwork, netMap);
			}
		}
	}

	/**
	 * @param is
	 * @param entryName
	 * @param parent
	 * @param createView
	 * @return The root-network of the extracted networks
	 * @throws Exception
	 */
	private CyRootNetwork extractNetworksAndViews(InputStream is, String entryName, CyRootNetwork parent,
			boolean createView) throws Exception {
		this.tm.setStatusMessage("Extracting network: " + entryName);
		
		CyRootNetwork rootNetwork = null;
		CyNetworkReader reader = networkReaderMgr.getReader(is, entryName);

		if (parent != null) {
			if (reader instanceof XGMMLNetworkReader) {
				((XGMMLNetworkReader) reader).setParent(parent);
			} else {
				logger.error("CyNetworkReader should be an instance of XGMMLNetworkReader! "
						+ "Cannot extract network as sub-nertwork of: " + entryName);
			}
		}

		reader.run(taskMonitor);

		final CyNetwork[] netArray = reader.getNetworks();
		CyNetworkView[] views = null;

		if (netArray != null && netArray.length > 0) {
			rootNetwork = rootNetworkManager.getRootNetwork(netArray[0]);
			
			if (createView)
				views = new CyNetworkView[netArray.length];

			int i = 0;
			String netName = null;

			for (CyNetwork net : netArray) {
				netName = net.getRow(net).get(CyNetwork.NAME, String.class);
				
				networkLookup.put(netName, net);
				networks.add(net);
				
				if (createView) {
					CyNetworkView netView = reader.buildCyNetworkView(net);
					views[i] = netView;
					i++;
				}
			}

			if (views != null) {
				networkViewLookup.put(netName, views);
				networkViews.addAll(Arrays.asList(views));
			}
		}
		
		return rootNetwork;
	}

	private void extractAppEntry(InputStream is, String entryName) {
		String[] items = entryName.split("/");

		if (items.length < 3) {
			// It's a directory name, not a file name
			return;
		}

		String appName = items[2];
		String fileName = items[items.length - 1];

		String tmpDir = System.getProperty("java.io.tmpdir");
		File theFile = new File(tmpDir, fileName);

		try {
			// Write input stream into tmp file
			BufferedWriter out = null;
			BufferedReader in = null;

			in = new BufferedReader(new InputStreamReader(is));
			out = new BufferedWriter(new FileWriter(theFile));

			// Write to tmp file
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				out.write(inputLine);
				out.newLine();
			}

			in.close();
			out.close();
		} catch (IOException e) {
			logger.error("Error: read from zip: " + entryName, e);
			return;
		}

		// Put the file into appFileListMap
		if (!appFileListMap.containsKey(appName))
			appFileListMap.put(appName, new ArrayList<File>());

		List<File> fileList = appFileListMap.get(appName);
		fileList.add(theFile);
	}

	private void extractVizmap(InputStream is, String entryName) throws Exception {
		VizmapReader reader = vizmapReaderMgr.getReader(is, entryName);
		reader.run(taskMonitor);
		visualStyles.addAll(reader.getVisualStyles());
	}

	private void extractCytoscapeProps(InputStream is, String entryName) throws Exception {
		CyPropertyReader reader = propertyReaderMgr.getReader(is, entryName);
		reader.run(taskMonitor);
		cytoscapeProps = (Properties) reader.getProperty();
	}

	private void extractBookmarks(InputStream is, String entryName) throws Exception {
		CyPropertyReader reader = propertyReaderMgr.getReader(is, entryName);
		reader.run(taskMonitor);
		bookmarks = (Bookmarks) reader.getProperty();
	}

	private void extractSessionState(InputStream is, String entryName) throws Exception {
		CyPropertyReader reader = propertyReaderMgr.getReader(is, entryName);
		reader.run(taskMonitor);
		cysession = (Cysession) reader.getProperty();
	}

	private void processNetworks() throws Exception {
		if (cysession == null) return;
		
		// Network attributes and visual styles
		if (cysession.getNetworkTree() != null) {
			for (final Network net : cysession.getNetworkTree().getNetwork()) {
				// We no longer have the concept of one top-level network root,
				// so let's ignore a network with that name.
				if (net.getId().equals(NETWORK_ROOT))
					continue;
	
				final String netName = net.getId();
				
				// Set attribute values that are saved in the cysession.xml
				final CyNetwork cyNet = getNetwork(netName);
	
				if (cyNet != null) {
					// TODO: check if should delete from sub-network
//					if (net.getHiddenNodes() != null)
//						setBooleanNodeAttr(cyNet, net.getHiddenNodes().getNode().iterator(), "hidden");
//					if (net.getHiddenEdges() != null)
//						setBooleanEdgeAttr(cyNet, net.getHiddenEdges().getEdge().iterator(), "hidden");
	
					// From Cytoscape 3.0, the selection info is stored inside CyTables,
					// but 2.x stores that info in the cysession.xml file.
					if (net.getSelectedNodes() != null)
						setBooleanNodeAttr(cyNet, net.getSelectedNodes().getNode().iterator(), CyNetwork.SELECTED);
					if (net.getSelectedEdges() != null)
						setBooleanEdgeAttr(cyNet, net.getSelectedEdges().getEdge().iterator(), CyNetwork.SELECTED);
				}
				
				// Populate the visual style map
				final CyNetworkView[] views = getNetworkViews(netName);
				
				if (views != null) {
					for (CyNetworkView nv : views) {
						String vsName = net.getVisualStyle();
		
						if (vsName != null)
							visualStyleMap.put(nv, vsName);
					}
				}
			}
		}
		
		// Network view sizes
		if (cysession.getSessionState() != null) {
			Desktop desktop = cysession.getSessionState().getDesktop();
			
			if (desktop != null && desktop.getNetworkFrames() != null) {
				List<NetworkFrame> frames = desktop.getNetworkFrames().getNetworkFrame();

				for (NetworkFrame nf : frames) {
					// Set sizes
					CyNetworkView[] views = getNetworkViews(nf.getFrameID());
					
					if (views != null) {
						for (CyNetworkView nv : views) {
							BigInteger w = nf.getWidth();
							BigInteger h = nf.getHeight();
	
							if (w != null)
								nv.setVisualProperty(MinimalVisualLexicon.NETWORK_WIDTH, w.doubleValue());
							if (h != null)
								nv.setVisualProperty(MinimalVisualLexicon.NETWORK_HEIGHT, h.doubleValue());
						}
					}
				}
			}
		}
	}

	private CyNetworkView[] getNetworkViews(final String name) {
		CyNetworkView[] views = null;
		
		for (String s : networkViewLookup.keySet()) {
			String decode = s;

			try {
				decode = URLDecoder.decode(s, "UTF-8");
				
				if (decode.equals(name)) {
					// this is OK since XGMML only ever reads one network
					views = networkViewLookup.get(s);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return views;
	}

	private CyNetwork getNetwork(final String name) {
		for (String s : networkLookup.keySet()) {
			String decode = s;

			try {
				decode = URLDecoder.decode(s, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}

			if (decode.equals(name)) {
				return networkLookup.get(s);
			}
		}

		return null;
	}

	private void setBooleanNodeAttr(final CyNetwork net, Iterator<?> it, final String attrName) {
		if (it == null)
			return;

		// create an id map
		Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();

		for (CyNode n : net.getNodeList()) {
			CyRow row = net.getRow(n);
			String name = row.get(CyNetwork.NAME, String.class);
			
			if (name == null) // try another column...
				name = row.get(CyRootNetwork.SHARED_NAME, String.class);
			
			if (name != null)
				nodeMap.put(name, n);
		}

		// set attr values based on ids
		while (it.hasNext()) {
			final Node nodeObject = (Node) it.next();
			String name = nodeObject.getId();
			CyNode n = nodeMap.get(name);

			if (n != null)
				net.getRow(n).set(attrName, true);
			else 
				logger.error("Cannot restore boolean node attr \"" + name + "\": node not found.");
		}
	}

	private void setBooleanEdgeAttr(final CyNetwork net, final Iterator<?> it, final String attrName) {
		if (it == null)
			return;

		// create an id map
		Map<String, CyEdge> edgeMap = new HashMap<String, CyEdge>();
		
		for (CyEdge e : net.getEdgeList()){
			CyRow row = net.getRow(e);
			String name = row.get(CyNetwork.NAME, String.class);
			
			if (name == null) // try another column...
				name = row.get(CyRootNetwork.SHARED_NAME, String.class);
			
			if (name != null)
				edgeMap.put(name, e);
		}

		// set attr values based on ids
		while (it.hasNext()) {
			final Edge edgeObject = (Edge) it.next();
			String name = edgeObject.getId();
			CyEdge e = edgeMap.get(name);

			if (e != null)
				net.getRow(e).set(attrName, true);
			else 
				logger.error("Cannot restore boolean edge attr \"" + name + "\": node not found.");
		}
	}
}
