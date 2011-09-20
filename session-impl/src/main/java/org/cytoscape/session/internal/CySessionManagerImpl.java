/*
  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
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
package org.cytoscape.session.internal;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.session.Cysession;
import org.cytoscape.property.session.Edge;
import org.cytoscape.property.session.Network;
import org.cytoscape.property.session.NetworkFrame;
import org.cytoscape.property.session.Node;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link org.cytoscape.session.CySessionManager}.
 * 
 * @author Christian Lopes
 */
public class CySessionManagerImpl implements CySessionManager {

	private String currentFileName;
	private CySession currentSession;

	private final CyEventHelper cyEventHelper;
	private final CyNetworkManager netMgr;
	private final CyTableManager tblMgr;
	private final CyNetworkTableManager netTblMgr;
	private final VisualMappingManager vmMgr;
	private final CyNetworkViewManager nvMgr;

	private final Map<CyProperty<?>, Map<String, String>> sessionProperties;

	private static final Logger logger = LoggerFactory.getLogger(CySessionManagerImpl.class);

	public CySessionManagerImpl(final CyEventHelper cyEventHelper,
								final CyNetworkManager netMgr,
								final CyTableManager tblMgr,
								final CyNetworkTableManager netTblMgr,
								final VisualMappingManager vmMgr,
								final CyNetworkViewManager nvMgr) {
		this.cyEventHelper = cyEventHelper;
		this.netMgr = netMgr;
		this.tblMgr = tblMgr;
		this.netTblMgr = netTblMgr;
		this.vmMgr = vmMgr;
		this.nvMgr = nvMgr;
		sessionProperties = new HashMap<CyProperty<?>, Map<String, String>>();
	}

	@Override
	public CySession getCurrentSession() {
		// Plugins who want to save anything to a session will have to listen for this event
		// and will then be responsible for adding files through SessionAboutToBeSavedEvent.addPluginFiles(..)
		SessionAboutToBeSavedEvent savingEvent = new SessionAboutToBeSavedEvent(this);
		cyEventHelper.fireEvent(savingEvent);

		CysessionFactory cysessFactory = new CysessionFactory(netMgr, nvMgr, vmMgr);
		Set<CyNetworkView> netViews = nvMgr.getNetworkViewSet();

		// Visual Styles Map
		Map<CyNetworkView, String> stylesMap = new HashMap<CyNetworkView, String>();

		if (netViews != null) {
			for (CyNetworkView nv : netViews) {
				VisualStyle style = vmMgr.getVisualStyle(nv);

				if (style != null) {
					stylesMap.put(nv, style.getTitle());
				}
			}
		}

		// Cysession
		Cysession cysess = cysessFactory.createCysession(savingEvent.getDesktop(), savingEvent.getCytopanels(), null);

		Map<String, List<File>> pluginMap = savingEvent.getPluginFileListMap();
		Set<CyTable> tables = tblMgr.getAllTables(true);
		Set<VisualStyle> styles = vmMgr.getAllVisualStyles();
		Properties props = getProperties();
		Bookmarks bkmarks = getBookmarks();

		Set<CyTableMetadata> metadata = buildMetadata(tables, netViews);
		// Build the session
		CySession sess = new CySession.Builder().cytoscapeProperties(props).bookmarks(bkmarks).cysession(cysess)
				.pluginFileListMap(pluginMap).tables(metadata).networkViews(netViews).visualStyles(styles)
				.viewVisualStyleMap(stylesMap).build();

		return sess;
	}

	private Set<CyTableMetadata> buildMetadata(Set<CyTable> tables, Set<CyNetworkView> netViews) {
		Set<CyTableMetadata> result = new HashSet<CyTableMetadata>();
		
		// Figure out which tables aren't associated with networks
		Map<CyTable, Set<CyTableMetadata>> networkTables = getNetworkTables(netViews);
		
		// Merge network/global metadata into a single set
		for (CyTable table : tables) {
			Set<CyTableMetadata> metadataSet = networkTables.get(table);
			if (metadataSet == null || metadataSet.size() == 0) {
				result.add(new CyTableMetadataImpl.CyTableMetadataBuilder().setCyTable(table));
			} else {
				for (CyTableMetadata metadata : metadataSet) {
					result.add(metadata);
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends CyTableEntry>[] TYPES = new Class[] { CyNetwork.class, CyNode.class, CyEdge.class };
	
	private Map<CyTable, Set<CyTableMetadata>> getNetworkTables(Set<CyNetworkView> views) {
		Map<CyTable, Set<CyTableMetadata>> result = new HashMap<CyTable, Set<CyTableMetadata>>();
		for (CyNetworkView view : views) {
			CyNetwork network = view.getModel();
			for (Class<? extends CyTableEntry> type : TYPES) {
				Map<String, CyTable> tableMap = netTblMgr.getTables(network, type);
				for (Entry<String, CyTable> entry : tableMap.entrySet()) {
					CyTable table = entry.getValue();
					
					Set<CyTableMetadata> metadataSet = result.get(table);
					if (metadataSet == null) {
						metadataSet = new HashSet<CyTableMetadata>();
						result.put(table, metadataSet);
					}
					String namespace = entry.getKey();
					metadataSet.add(new CyTableMetadataImpl.CyTableMetadataBuilder()
										.setCyTable(table)
										.setNamespace(namespace)
										.setType(type)
										.setNetwork(network)
										.build());
				}
			}
		}
		return result ;
	}

	@Override
	public void setCurrentSession(CySession sess, String fileName) {
		boolean emptySession = sess == null;

		// Always remove the current session first
		disposeCurrentSession(!emptySession);

		if (emptySession) {
			logger.debug("Creating empty session...");
			Set<VisualStyle> styles = vmMgr.getAllVisualStyles();

			// Cysession info
			Cysession cysess = new CysessionFactory(netMgr, nvMgr, vmMgr).createDefaultCysession();

			Properties props = getProperties();
			Bookmarks bkmarks = getBookmarks();

			sess = new CySession.Builder().cytoscapeProperties(props).bookmarks(bkmarks).cysession(cysess)
					.visualStyles(styles).build();
		} else {
			logger.debug("Restoring the session...");
			restoreNetworks(sess);
			restoreTables(sess);
			restoreVisualStyles(sess);
			restoreSelection(sess);
		}

		currentSession = sess;
		currentFileName = fileName;

		cyEventHelper.fireEvent(new SessionLoadedEvent(this, currentSession, getCurrentSessionFileName()));
	}

	private void restoreTables(CySession sess) {
		// Register global tables
		for (CyTableMetadata metadata : sess.getTables()) {
			CyNetwork network = metadata.getCyNetwork();
			if (network == null) {
				tblMgr.addTable(metadata.getCyTable());
			}
		}
	}

	@Override
	public String getCurrentSessionFileName() {
		return currentFileName;
	}

	public void addCyProperty(final CyProperty<?> newCyProperty, final Map<String, String> properties) {
		CyProperty.SavePolicy sp = newCyProperty.getSavePolicy();

		if (sp == CyProperty.SavePolicy.SESSION_FILE || sp == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR)
			sessionProperties.put(newCyProperty, properties);
	}

	public void removeCyProperty(final CyProperty<?> oldCyProperty, final Map<String, String> properties) {
		CyProperty.SavePolicy sp = oldCyProperty.getSavePolicy();

		if (sp == CyProperty.SavePolicy.SESSION_FILE || sp == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR)
			sessionProperties.remove(oldCyProperty);
	}

	private Bookmarks getBookmarks() {
		Bookmarks bookmarks = null;

		for (CyProperty<?> cyProps : sessionProperties.keySet()) {
			if (cyProps.getProperties() instanceof Bookmarks) {
				bookmarks = (Bookmarks) cyProps.getProperties();
				break;
			}
		}

		return bookmarks;
	}

	private Properties getProperties() {
		Properties props = new Properties();

		for (CyProperty<?> cyProps : sessionProperties.keySet()) {
			if (cyProps.getProperties() instanceof Properties) {
				Properties p = (Properties) cyProps.getProperties();
				props.putAll(p);
			}
		}

		return props;
	}

	private void restoreNetworks(CySession sess) {
		logger.debug("Restoring networks...");
		Set<CyNetworkView> netViews = sess.getNetworkViews();

		for (CyNetworkView nv : netViews) {
			netMgr.addNetwork(nv.getModel());
			nvMgr.addNetworkView(nv);
		}
	}

	private void restoreVisualStyles(final CySession sess) {
		logger.debug("Restoring visual styles...");
		final Set<VisualStyle> styles = sess.getVisualStyles();
		final Map<String, VisualStyle> stylesMap = new HashMap<String, VisualStyle>();

		if (styles != null) {
			for (VisualStyle vs : styles) {
				vmMgr.addVisualStyle(vs);
				stylesMap.put(vs.getTitle(), vs);
				// TODO: what if a style with the same name already exists?
			}
		}

		final Cysession cysess = sess.getCysession();

		// Get network frames info
		if (cysess.getSessionState().getDesktop().getNetworkFrames() != null) {
			List<NetworkFrame> frames = cysess.getSessionState().getDesktop().getNetworkFrames().getNetworkFrame();
			Map<String, NetworkFrame> framesLookup = new HashMap<String, NetworkFrame>();

			for (NetworkFrame nf : frames)
				framesLookup.put(nf.getFrameID(), nf);

			// Set visual styles to network views

			// This is a map from network view to Visual Style TITLE (may
			// not be unique. TODO: use ID?)
			final Map<CyNetworkView, String> netStyleMap = sess.getViewVisualStyleMap();

			for (Entry<CyNetworkView, String> entry : netStyleMap.entrySet()) {
				final CyNetworkView netView = entry.getKey();
				final String stName = entry.getValue();
				final VisualStyle vs = stylesMap.get(stName);

				if (vs != null) {
					vmMgr.setVisualStyle(vs, netView);
					vs.apply(netView);
				}

				// Set network width/height
				String name = netView.getModel().getCyRow().get(CyNetwork.NAME, String.class);

				if (name != null && name.length() > 0) {
					NetworkFrame nf = framesLookup.get(name);

					if (nf != null) {
						BigInteger w = nf.getWidth();
						BigInteger h = nf.getHeight();

						if (w != null) netView.setVisualProperty(MinimalVisualLexicon.NETWORK_WIDTH, w.doubleValue());
						if (h != null) netView.setVisualProperty(MinimalVisualLexicon.NETWORK_HEIGHT, h.doubleValue());
					}
				}

				netView.updateView();
			}
		}
	}

	private void restoreSelection(CySession sess) {
		final Cysession cysess = sess.getCysession();
		float version = 0;

		try {
			version = Float.valueOf(cysess.getDocumentVersion());
		} catch (Exception e) {
		}

		if (version < 3.0) {
			logger.debug("Restoring node/edge selection...");

			// First create network_title -> element_name lookup maps
			final Map<String, Set<String>> selectedNodesMap = new HashMap<String, Set<String>>();
			final Map<String, Set<String>> selectedEdgesMap = new HashMap<String, Set<String>>();
			final List<Network> networks = cysess.getNetworkTree().getNetwork();

			for (Network net : networks) {
				String netTitle = net.getId();

				if (net.getSelectedNodes() != null) {
					// Store selected node names for future reference
					Set<String> selectedNodes = new HashSet<String>();
					selectedNodesMap.put(netTitle, selectedNodes);

					for (Node n : net.getSelectedNodes().getNode()) {
						selectedNodes.add(n.getId());
					}
				}

				if (net.getSelectedEdges() != null) {
					// Store selected edge names for future reference
					Set<String> selectedEdges = new HashSet<String>();
					selectedEdgesMap.put(netTitle, selectedEdges);

					for (Edge e : net.getSelectedEdges().getEdge()) {
						selectedEdges.add(e.getId());
					}
				}
			}

			// Now iterate through all CyNodes/Edges and select the ones that are found in the lookup maps
			Set<CyNetwork> cyNetworks = netMgr.getNetworkSet();

			if (cyNetworks != null) {
				for (CyNetwork cyNet : cyNetworks) {
					String netTitle = cyNet.getCyRow().get(CyNetwork.NAME, String.class);

					selectElementsByName(cyNet.getNodeList(), selectedNodesMap.get(netTitle));
					selectElementsByName(cyNet.getEdgeList(), selectedEdgesMap.get(netTitle));
				}
			}
		}
	}

	private <T extends CyTableEntry> void selectElementsByName(List<T> entries, Set<String> names) {
		if (entries != null && names != null) {
			for (T entry : entries) {
				CyRow row = entry.getCyRow();

				if (names.contains(row.get(CyNetwork.NAME, String.class))) {
					row.set(CyNetwork.SELECTED, true);
				}
			}
		}
	}

	private void disposeCurrentSession(boolean removeVisualStyles) {
		logger.debug("Disposing current session...");

		// Destroy network views and models
		Set<CyNetworkView> netViews = nvMgr.getNetworkViewSet();

		for (CyNetworkView nv : netViews) {
			nvMgr.destroyNetworkView(nv);
			netMgr.destroyNetwork(nv.getModel());
		}

		// Destroy styles
		if (removeVisualStyles) {
			logger.debug("Removing current visual styles...");
			VisualStyle defaultStyle = vmMgr.getDefaultVisualStyle();
			List<VisualStyle> allStyles = new ArrayList<VisualStyle>(vmMgr.getAllVisualStyles());

			for (int i = 0; i < allStyles.size(); i++) {
				VisualStyle vs = allStyles.get(i);

				if (!vs.equals(defaultStyle)) {
					vmMgr.removeVisualStyle(vs);
				}
			}
		}

		// TODO: destroy unattached tables--how?
		tblMgr.reset();
	}
}
