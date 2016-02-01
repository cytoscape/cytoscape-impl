package org.cytoscape.io.internal.read.session;

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

import static org.cytoscape.io.internal.util.session.SessionUtil.BOOKMARKS_FILE;
import static org.cytoscape.io.internal.util.session.SessionUtil.CYSESSION_FILE;
import static org.cytoscape.io.internal.util.session.SessionUtil.IMAGES_FOLDER;
import static org.cytoscape.io.internal.util.session.SessionUtil.NETWORK_ROOT;
import static org.cytoscape.io.internal.util.session.SessionUtil.VIZMAP_PROPS_FILE;
import static org.cytoscape.io.internal.util.session.SessionUtil.XGMML_EXT;
import static org.cytoscape.model.CyNetwork.DEFAULT_ATTRS;
import static org.cytoscape.model.CyNetwork.SELECTED;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.cytoscape.io.internal.read.xgmml.SessionXGMMLNetworkReader;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.session.model.Child;
import org.cytoscape.io.internal.util.session.model.Cysession;
import org.cytoscape.io.internal.util.session.model.Cytopanel;
import org.cytoscape.io.internal.util.session.model.Cytopanels;
import org.cytoscape.io.internal.util.session.model.Desktop;
import org.cytoscape.io.internal.util.session.model.Edge;
import org.cytoscape.io.internal.util.session.model.Network;
import org.cytoscape.io.internal.util.session.model.NetworkFrame;
import org.cytoscape.io.internal.util.session.model.NetworkFrames;
import org.cytoscape.io.internal.util.session.model.Node;
import org.cytoscape.io.internal.util.session.model.SessionState;
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
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;

/**
 * Session reader implementation that handles the Cytoscape 2.x session format.
 * 
 * @see org.cytoscape.io.internal.read.session.Cy3SessionReaderImpl
 * @see org.cytoscape.io.internal.write.session.SessionWriterImpl
 */
public class Cy2SessionReaderImpl extends AbstractSessionReader {
	
	private static final String TEMP_DIR = "java.io.tmpdir";
	
	// In 3, CG Manager is treated as a regular app.  This is a unique name for it.
	private static final String DING_CG_MANAGER_NAME = "org.cytoscape.ding.customgraphicsmgr";
	
	public static final String CY_PROPS_FILE = "session_cytoscape.props";
	public static final Pattern NETWORK_PATTERN = Pattern.compile(".*/(([^/]+)[.]xgmml)");
	public static final Pattern PROPERTIES_PATTERN = Pattern.compile(".*/(([^/]+)[.]xgmml)");
	public static final String IGNORED_PROPS =
			"(cytoscape|proxy|logger|render|undo|vizmapper|nodelinkouturl|edgelinkouturl)\\.[^\\.]+.*" +
			"|canonicalizeNames|defaultPluginDownloadUrl|defaultVisualStyle|defaultWebBrowser" +
			"|exportTextAsShape|Linkout\\.externalLinkName|maximizeViewOnCreate|moduleNetworkViewCreationThreshold" +
			"|nestedNetwork\\.imageScaleFactor|nestedNetworkSnapshotSize|preferredLayoutAlgorithm" +
			"|secondaryViewThreshold|showQuickStartAtStartup|viewThreshold";
	public static final String PLUGINS_FOLDER = "plugins/";
	
	private final CyNetworkReaderManager networkReaderMgr;
	private final CyPropertyReaderManager propertyReaderMgr;
	private final VizmapReaderManager vizmapReaderMgr;

	protected Cysession cysession;
	private final Map<String, CyNetwork> networkLookup = new HashMap<>();
	private final Map<String, CyNetworkView> networkViewLookup = new HashMap<>();
	private final Map<String, List<Node>> nodeSelectionLookup = new HashMap<>();
	private final Map<String, List<Edge>> edgeSelectionLookup = new HashMap<>();
	private Map<String, String> xgmmlEntries;

	public Cy2SessionReaderImpl(final InputStream sourceInputStream,
								final ReadCache cache,
								final GroupUtil groupUtil,
								final CyNetworkReaderManager networkReaderMgr,
								final CyPropertyReaderManager propertyReaderMgr,
								final VizmapReaderManager vizmapReaderMgr,
								final CyRootNetworkManager rootNetworkManager) {
		super(sourceInputStream, cache, groupUtil, rootNetworkManager);
		
		if (networkReaderMgr == null)
			throw new NullPointerException("network reader manager is null.");
		this.networkReaderMgr = networkReaderMgr;

		if (propertyReaderMgr == null)
			throw new NullPointerException("property reader manager is null.");
		this.propertyReaderMgr = propertyReaderMgr;

		if (vizmapReaderMgr == null)
			throw new NullPointerException("vizmap reader manager is null.");
		this.vizmapReaderMgr = vizmapReaderMgr;
		
		xgmmlEntries = new HashMap<>();
	}
	
	@Override
	protected void handleEntry(final InputStream is, final String entryName) throws Exception {		
		if (entryName.contains("/" + PLUGINS_FOLDER)) {
			extractPluginEntry(is, entryName);
		} else if (entryName.endsWith(CYSESSION_FILE)) {
			extractSessionState(is, entryName);
		} else if (entryName.endsWith(VIZMAP_PROPS_FILE)) {
			extractVizmap(is, entryName);
		} else if (entryName.endsWith(CY_PROPS_FILE)) {
			extractProperties(is, entryName);
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
		} else if (entryName.contains("/" + IMAGES_FOLDER)) {
			extractImages(is, entryName);
		} else {
			logger.warn("Unknown entry found in session zip file!\n" + entryName);
		}
	}
	
	@Override
	protected void complete(TaskMonitor tm) throws Exception {
		if (cancelled) return;
		
		if (cysession == null)
			throw new FileNotFoundException("Cannot find the " + CYSESSION_FILE + " file.");

		tm.setProgress(0.4);
		tm.setTitle("Recreate networks");
		tm.setStatusMessage("Recreating networks...");
		extractNetworks(tm);
		
		tm.setProgress(0.8);
		tm.setTitle("Process networks");
		tm.setStatusMessage("Processing networks...");
		processNetworks();
		
		super.complete(tm);
	}
	
	@Override
	protected void createObjectMap() {
		objectMap.put(CyNetwork.class, cache.getNetworkByIdMap());
		objectMap.put(CyNetworkView.class, cache.getNetworkViewByIdMap());
		objectMap.put(CyNode.class, cache.getNodeByNameMap()); // In 2.x, the node name is the ID (not the XGMML id)
		objectMap.put(CyEdge.class, cache.getEdgeByIdMap());
	}
	
	private void extractNetworks(TaskMonitor tm) throws JAXBException, IOException {
		// Extract the XGMML files
		Map<String, Network> netMap = new HashMap<>();

		for (Network curNet : cysession.getNetworkTree().getNetwork()) {
			netMap.put(curNet.getId(), curNet);
		}

		walkNetworkTree(netMap.get(NETWORK_ROOT), null, netMap, tm);
	}
	
	private void walkNetworkTree(final Network net, CyNetwork parent, final Map<String, Network> netMap,
			final TaskMonitor tm) {
		// Get the list of children under this root
		final List<Child> children = net.getChild();

		// Traverse using recursive call
		final int numChildren = children.size();
		Child child = null;
		Network childNet = null;
		
		for (int i = 0; i < numChildren; i++) {
			if (cancelled) return;
			
			child = children.get(i);
			childNet = netMap.get(child.getId());

			String entryName = xgmmlEntries.get(childNet.getFilename());
			InputStream is = null;
			CyNetwork cy2Parent = null; // This is the original Cytoscape 2 parent.
			
			try {
				is = findEntry(entryName);
				
				if (is != null) {
					tm.setStatusMessage("Extracting network: " + entryName);
					cy2Parent = extractNetworksAndViews(is, entryName, parent, childNet.isViewAvailable());
					
					// Every 2.x network should be a child of the same root-network.
					if (parent == null)
						parent = rootNetworkManager.getRootNetwork(cy2Parent);
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
				if (!cancelled && childNet.getChild().size() != 0)
					walkNetworkTree(childNet, cy2Parent, netMap, tm);
			}
		}
	}

	/**
	 * @param is
	 * @param entryName
	 * @param parent
	 * @param createView
	 * @return The top-level network that was extracted from the XGMML file.
	 * @throws Exception
	 */
	private CyNetwork extractNetworksAndViews(final InputStream is, final String entryName, final CyNetwork parent,
			final boolean createView) throws Exception {
		CyNetwork topNetwork = null;
		final CyNetworkReader reader = networkReaderMgr.getReader(is, entryName);

		if (parent != null) {
			if (reader instanceof SessionXGMMLNetworkReader) {
				((SessionXGMMLNetworkReader) reader).setParent(parent);
			} else {
				logger.error("CyNetworkReader should be an instance of XGMMLNetworkReader. "
						+ "Cannot extract network as sub-nertwork of: " + entryName);
			}
		}

		reader.run(taskMonitor);

		final CyNetwork[] netArray = reader.getNetworks();
		
		if (netArray != null && netArray.length > 0) {
			topNetwork = netArray[0];

			for (int i = 0; i < netArray.length; i++) {
				// Process each CyNetwork
				final CyNetwork net = netArray[i];
				
				final String netName = net.getRow(net).get(CyNetwork.NAME, String.class);
				networkLookup.put(netName, net);
				
				// Add parent network attribute to a column in the hidden table,
				// to preserve the network hierarchy info from Cytoscape 2.x
				final CyRow hRow = net.getRow(net, CyNetwork.HIDDEN_ATTRS);
				final CyTable hTbl = hRow.getTable();
				
				if (parent instanceof CySubNetwork) {
					if (hTbl.getColumn(CY3_PARENT_NETWORK_COLUMN) == null)
						hTbl.createColumn(CY3_PARENT_NETWORK_COLUMN, Long.class, false);
				
					hRow.set(CY3_PARENT_NETWORK_COLUMN, parent.getSUID());
				}
				
				final CyTable tbl = net.getRow(net, CyNetwork.LOCAL_ATTRS).getTable();
				
				// Remove this old column (used until v3.3) to prevent stale values
				// (e.g. the user imported a Cy3 XGMML that contains this attribute into Cy2)
				if (tbl.getColumn(CY2_PARENT_NETWORK_COLUMN) != null && parent instanceof CySubNetwork == false)
					tbl.deleteColumn(CY2_PARENT_NETWORK_COLUMN);

				// Restore node/edge selection
				List<Node> selNodes = nodeSelectionLookup.get(netName);
				List<Edge> selEdges = edgeSelectionLookup.get(netName);
				
				if (selNodes != null)
					setBooleanNodeAttr(net, selNodes, SELECTED, DEFAULT_ATTRS);
				if (selEdges != null)
					setBooleanEdgeAttr(net, selEdges, SELECTED, DEFAULT_ATTRS);
				
				networks.add(net);
				
				if (!cancelled && i == 0 && createView) {
					// Create a network view for the first network only,
					// which is supposed to be the top-level one
					final CyNetworkView view = reader.buildCyNetworkView(net);
					networkViewLookup.put(netName, view);
					networkViews.add(view);
					cache.cache(netName, view);
				}
			}
		}
		
		return topNetwork;
	}

	private void extractPluginEntry(InputStream is, String entryName) {
		String[] items = entryName.split("/");

		if (items.length < 3) {
			// It's a directory name, not a file name
			return;
		}

		String appName = items[2];
		String fileName = items[items.length - 1];

		String tmpDir = System.getProperty("java.io.tmpdir");
		File file = new File(tmpDir, fileName);
		
		try {
			file.deleteOnExit();
		} catch (Exception e) {
			logger.warn("This temporary app file may not be deleted on exit: " + file.getAbsolutePath(), e);
		}

		try {
			// Write input stream into tmp file
			BufferedWriter out = null;
			BufferedReader in = null;

			in = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8").newDecoder()));
			out = new BufferedWriter(new FileWriter(file));

			// Write to tmp file
			String inputLine;

			while ((inputLine = in.readLine()) != null && !cancelled) {
				out.write(inputLine);
				out.newLine();
			}

			in.close();
			out.close();
		} catch (IOException e) {
			logger.error("Error: read from zip: " + entryName, e);
			return;
		}

		if (cancelled) return;
		
		// Put the file into appFileListMap
		if (!appFileListMap.containsKey(appName))
			appFileListMap.put(appName, new ArrayList<>());

		List<File> fileList = appFileListMap.get(appName);
		fileList.add(file);
	}

	private void extractVizmap(InputStream is, String entryName) throws Exception {
		VizmapReader reader = vizmapReaderMgr.getReader(is, entryName);
		reader.run(taskMonitor);
		visualStyles.addAll(reader.getVisualStyles());
	}

	private void extractProperties(InputStream is, String entryName) throws Exception {
		CyPropertyReader reader = propertyReaderMgr.getReader(is, entryName);
		
		if (reader == null)
			return;
		
		reader.run(taskMonitor);
		
		final Properties props = (Properties) reader.getProperty();
		final Properties newProps = new Properties();
		
		if (props != null) {
			// Only add properties that should have the SESSION_FILE save policy
			for (String key : props.stringPropertyNames()) {
				if (isSessionProperty(key)) {
					String value = props.getProperty(key);
					newProps.put(key, value);
				}
			}

			final CyProperty<Properties> cyProps = new SimpleCyProperty<Properties>("session", newProps,
					Properties.class, CyProperty.SavePolicy.SESSION_FILE);
			properties.add(cyProps);
		}
	}

	private void extractBookmarks(InputStream is, String entryName) throws Exception {
		CyPropertyReader reader = propertyReaderMgr.getReader(is, entryName);
		reader.run(taskMonitor);
		
		final Bookmarks bookmarks = (Bookmarks) reader.getProperty();
		final CyProperty<Bookmarks> cyProps = new SimpleCyProperty<Bookmarks>("bookmarks", bookmarks, Bookmarks.class,
				CyProperty.SavePolicy.SESSION_FILE);
		properties.add(cyProps);
	}
	
	private void extractImages(final InputStream is, final String entryName) {
		final String[] items = entryName.split("/");

		if (items.length < 3) {
			// It's a directory name, not a file name
			return;
		}

		final String fileName = items[items.length - 1];		
		final String tmpDir = System.getProperty(TEMP_DIR);
		final File file = new File(tmpDir, fileName);

		try {
			// Write input stream into temp file (Use binary streams to support images/movies/etc.)
			final BufferedInputStream bin = new BufferedInputStream(is);
			final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
			final byte buf[] = new byte[256];
			
			int len;
			while ((len = bin.read(buf)) != -1)
				output.write(buf, 0, len);
			
			output.flush();
			output.close();
			bin.close();
		} catch (IOException e) {
			logger.error("Error: read from zip: " + entryName, e);
			return;
		}

		// Put the file into appFileListMap
		if (!appFileListMap.containsKey(DING_CG_MANAGER_NAME))
			appFileListMap.put(DING_CG_MANAGER_NAME, new ArrayList<>());
		
		List<File> fileList = appFileListMap.get(DING_CG_MANAGER_NAME);
		fileList.add(file);
	}

	private void extractSessionState(InputStream is, String entryName) throws Exception {
		CyPropertyReader reader = propertyReaderMgr.getReader(is, entryName);
		reader.run(taskMonitor);
		cysession = (Cysession) reader.getProperty();
		
		if (cysession != null) {
			// Create map of selected elements:
			// Network attributes and visual styles
			if (cysession.getNetworkTree() != null) {
				for (final Network net : cysession.getNetworkTree().getNetwork()) {
					// We no longer have the concept of one top-level network root,
					// so let's ignore a network with that name.
					if (net.getId().equals(NETWORK_ROOT))
						continue;
					
					final String netName = net.getId();
					List<Node> selNodes = null;
					List<Edge> selEdges = null;
					
					if (net.getSelectedNodes() != null)
						selNodes = net.getSelectedNodes().getNode();
					if (net.getSelectedEdges() != null)
						selEdges = net.getSelectedEdges().getEdge();
					
					nodeSelectionLookup.put(netName, selNodes);
					edgeSelectionLookup.put(netName, selEdges);
				}
			}
			
			// Convert the old cysession to core the required 3.0 core plugin files:
			// Actually we just need to extract the "networkFrames" and "cytopanels" data from the Cysession object
			// and write an XML file that will be parsed by swing-application.
			StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
			sb.append("<sessionState documentVersion=\"1.0\">\n");
			sb.append("    <networkFrames>\n");
			
			if (cysession.getSessionState() != null && cysession.getSessionState().getDesktop() != null) {
				NetworkFrames netFrames = cysession.getSessionState().getDesktop().getNetworkFrames();
				
				if (netFrames != null) {
					for (NetworkFrame nf : netFrames.getNetworkFrame()) {
						String id = nf.getFrameID();
						String x = nf.getX() != null ? nf.getX().toString() : "0";
						String y = nf.getY() != null ? nf.getY().toString() : "0";
						sb.append("        <networkFrame networkViewID=\""+id+"\" x=\""+x+"\" y=\""+y+"\"/>\n");
					}
				}
			}
			
			sb.append("    </networkFrames>\n");
			sb.append("    <cytopanels>\n");
			
			SessionState sessionState = cysession.getSessionState();
			
			if (sessionState != null) {
				Cytopanels cytopanels = sessionState.getCytopanels();
				
				if (cytopanels != null) {
					List<Cytopanel> cytopanelsList = cytopanels.getCytopanel();
					
					for (Cytopanel cytopanel : cytopanelsList) {
						String id = cytopanel.getId();
						String state = cytopanel.getPanelState();
						String selection = cytopanel.getSelectedPanel();
						
						sb.append("        <cytopanel id=\""+id+"\">\n");
						sb.append("            <panelState>"+state+"</panelState>\n");
						sb.append("            <selectedPanel>"+selection+"</selectedPanel>\n");
						sb.append("        </cytopanel>\n");
					}
				}
			}
			
			sb.append("    </cytopanels>\n");
			sb.append("</sessionState>");
			
			// Extract it as an app file now:
			ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
			extractPluginEntry(bais, cysession.getId() + "/" + PLUGINS_FOLDER + "org.cytoscape.swing-application/session_state.xml");
		}
	}

	private void processNetworks() throws Exception {
		if (cysession == null)
			return;

		// Network attributes and visual styles
		if (cysession.getNetworkTree() != null) {
			for (final Network net : cysession.getNetworkTree().getNetwork()) {
				if (cancelled) return;
				
				// We no longer have the concept of one top-level network root,
				// so let's ignore a network with that name.
				if (net.getId().equals(NETWORK_ROOT))
					continue;
				
				final String netName = net.getId();
				final CyNetworkView view = getNetworkView(netName);
				
				if (view != null) {
					// Populate the visual style map
					String vsName = net.getVisualStyle();
					
					if (vsName != null)
						visualStyleMap.put(view, vsName);
					
					// Convert 2.x hidden state (cysession.xml) to 3.x visual properties
					if (net.getHiddenEdges() != null) {
						for (final Edge edgeObject : net.getHiddenEdges().getEdge()) {
							if (cancelled) return;
							
							final String name = edgeObject.getId();
							final CyEdge e = cache.getEdge(name);

							if (e != null) {
								final View<CyEdge> ev = view.getEdgeView(e);
								
								if (ev != null)
									ev.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, false);
								else
									logger.error("Cannot restore hidden state of edge \"" + name
											+ "\": Edge view not found.");
							} else {
								logger.error("Cannot restore hidden state of edge \"" + name + "\": Edge not found.");
							}
						}
					}
					
					if (net.getHiddenNodes() != null) {
						for (final Node nodeObject : net.getHiddenNodes().getNode()) {
							if (cancelled) return;
							
							final String name = nodeObject.getId();
							final CyNode n = cache.getNodeByName(name);

							if (n != null) {
								final View<CyNode> nv = view.getNodeView(n);

								if (nv != null)
									nv.setLockedValue(BasicVisualLexicon.NODE_VISIBLE, false);
								else
									logger.error("Cannot restore hidden state of node \"" + name
											+ "\": Node view not found.");
							} else {
								logger.error("Cannot restore hidden state of node \"" + name + "\": Node not found.");
							}
						}
					}
				}
			}
		}
		
		// Network view sizes
		if (cysession.getSessionState() != null) {
			Desktop desktop = cysession.getSessionState().getDesktop();
			
			if (desktop != null && desktop.getNetworkFrames() != null) {
				List<NetworkFrame> frames = desktop.getNetworkFrames().getNetworkFrame();

				for (final NetworkFrame nf : frames) {
					if (cancelled) return;
					
					// Set sizes
					final CyNetworkView view = getNetworkView(nf.getFrameID());
					
					if (view != null) {
						BigInteger w = nf.getWidth();
						BigInteger h = nf.getHeight();

						if (w != null)
							view.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH, w.doubleValue());
						if (h != null)
							view.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, h.doubleValue());
					}
				}
			}
		}
	}

	private CyNetworkView getNetworkView(final String name) {
		return networkViewLookup.get(name);
	}

	private void setBooleanNodeAttr(final CyNetwork net, final List<Node> nodes, final String attrName,
			final String tableName) {
		if (net != null && nodes != null) {
			// set attr values based on ids
			for (final Node nodeObject : nodes) {
				if (cancelled) return;
				
				String name = nodeObject.getId();
				// The XGMML node "id" is only used internally, by the XGMML parser--the name is the real ID.
				CyNode n = cache.getNodeByName(name);

				if (n != null)
					net.getRow(n, tableName).set(attrName, true);
				else
					logger.error("Cannot restore boolean node attr \"" + name + "\": node not found.");
			}
		}
	}

	private void setBooleanEdgeAttr(final CyNetwork net, final List<Edge> edges, final String attrName,
			final String tableName) {
		if (net != null && edges != null) {
			// set attr values based on ids
			for (final Edge edgeObject : edges) {
				if (cancelled) return;
				
				String name = edgeObject.getId();
				// In 2.x, XGMML edge elements have no "id" attribute--the label is the id.
				CyEdge e = cache.getEdge(name); 

				if (e != null)
					net.getRow(e, tableName).set(attrName, true);
				else
					logger.error("Cannot restore boolean edge attr \"" + name + "\": node not found.");
			}
		}
	}
	
	boolean isSessionProperty(String key) {
		return !key.matches(IGNORED_PROPS);
	}
}
