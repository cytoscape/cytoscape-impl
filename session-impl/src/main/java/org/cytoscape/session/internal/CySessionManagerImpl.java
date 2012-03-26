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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionSavedEvent;
import org.cytoscape.session.events.SessionSavedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.undo.UndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link org.cytoscape.session.CySessionManager}.
 * 
 * @author Christian Lopes
 */
public class CySessionManagerImpl implements CySessionManager, SessionSavedListener {

	private String currentFileName;
	private CySession currentSession;

	private final CyEventHelper cyEventHelper;
	private final CyNetworkManager netMgr;
	private final CyTableManager tblMgr;
	private final CyNetworkTableManager netTblMgr;
	private final VisualMappingManager vmMgr;
	private final CyNetworkViewManager nvMgr;
	private final CyRootNetworkManager rootNetMgr;
	private final CyServiceRegistrar registrar;
	private final UndoSupport undo;

	private final Set<CyProperty<?>> sessionProperties;
	private CyProperty<Bookmarks> bookmarks;

	private static final Logger logger = LoggerFactory.getLogger(CySessionManagerImpl.class);

	public CySessionManagerImpl(final CyEventHelper cyEventHelper,
								final CyNetworkManager netMgr,
								final CyTableManager tblMgr,
								final CyNetworkTableManager netTblMgr,
								final VisualMappingManager vmMgr,
								final CyNetworkViewManager nvMgr,
								final CyRootNetworkManager rootNetMgr,
								final CyServiceRegistrar registrar,
								final UndoSupport undo) {
		this.cyEventHelper = cyEventHelper;
		this.netMgr = netMgr;
		this.tblMgr = tblMgr;
		this.netTblMgr = netTblMgr;
		this.vmMgr = vmMgr;
		this.nvMgr = nvMgr;
		this.rootNetMgr = rootNetMgr;
		this.registrar = registrar;
		this.sessionProperties = new HashSet<CyProperty<?>>();
		this.undo = undo;
	}

	@Override
	public CySession getCurrentSession() {
		// Apps who want to save anything to a session will have to listen for this event
		// and will then be responsible for adding files through SessionAboutToBeSavedEvent.addAppFiles(..)
		SessionAboutToBeSavedEvent savingEvent = new SessionAboutToBeSavedEvent(this);
		cyEventHelper.fireEvent(savingEvent);

		Set<CyNetwork> networks = netMgr.getNetworkSet();
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

		Map<String, List<File>> appMap = savingEvent.getAppFileListMap();
		Set<CyTable> tables = tblMgr.getAllTables(true);
		Set<VisualStyle> styles = vmMgr.getAllVisualStyles();
		Set<CyTableMetadata> metadata = buildMetadata(tables, networks);
		Set<CyProperty<?>> props = getAllProperties();
		
		// Build the session
		CySession sess = new CySession.Builder().properties(props).appFileListMap(appMap)
				.tables(metadata).networks(networks).networkViews(netViews).visualStyles(styles)
				.viewVisualStyleMap(stylesMap).build();

		return sess;
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends CyIdentifiable>[] TYPES = new Class[] { CyNetwork.class, CyNode.class, CyEdge.class };
	
	private Set<CyTableMetadata> buildMetadata(Set<CyTable> tables, Set<CyNetwork> networks) {
		Set<CyTableMetadata> result = new HashSet<CyTableMetadata>();
		
		// Clone the networks and tables to add the root-networks without changing the original sets:
		Set<CyNetwork> allNetworks = new HashSet<CyNetwork>(networks);
		Set<CyTable> allTables = new HashSet<CyTable>(tables);
		
		// Add the root-networks, which are not included in the original networks set:
		for (CyNetwork network : networks) {
			if (!(network instanceof CyRootNetwork)) {
				CyRootNetwork rootNet = rootNetMgr.getRootNetwork(network);
				
				if (!allNetworks.contains(rootNet)) {
					allNetworks.add(rootNet);
					
					// TODO: remove it once manager is fixed to return all tables
					for (Class<? extends CyIdentifiable> type : TYPES) {
						Map<String, CyTable> tableMap = netTblMgr.getTables(rootNet, type);
						allTables.addAll(tableMap.values());
					}
				}
			}
		}
		
		// Figure out which tables aren't associated with networks
		Map<CyTable, Set<CyTableMetadata>> networkTables = getNetworkTables(allNetworks);
		
		// Merge network/global metadata into a single set
		for (CyTable table : allTables) {
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

	private Map<CyTable, Set<CyTableMetadata>> getNetworkTables(final Set<CyNetwork> networks) {
		Map<CyTable, Set<CyTableMetadata>> result = new HashMap<CyTable, Set<CyTableMetadata>>();
		
		for (CyNetwork network : networks) {
			for (Class<? extends CyIdentifiable> type : TYPES) {
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
	public void setCurrentSession(CySession sess, final String fileName) {
		
		boolean emptySession = sess == null;

		// Always remove the current session first
		disposeCurrentSession(!emptySession);

		if (emptySession) {
			logger.debug("Creating empty session...");
			Set<VisualStyle> styles = vmMgr.getAllVisualStyles();
			Set<CyProperty<?>> props = getAllProperties();

			sess = new CySession.Builder().properties(props).visualStyles(styles).build();
		} else {
			logger.debug("Restoring the session...");
			restoreProperties(sess);
			restoreNetworks(sess);
			restoreNetworkViews(sess);
			restoreTables(sess);
			restoreVisualStyles(sess);
		}
		
		currentSession = sess;
		currentFileName = fileName;

		cyEventHelper.fireEvent(new SessionLoadedEvent(this, currentSession, getCurrentSessionFileName()));
	}

	private void restoreTables(CySession sess) {
		// Register global tables
		for (CyTableMetadata metadata : sess.getTables()) {
			CyNetwork network = metadata.getNetwork();
			
			if (network == null) {
				tblMgr.addTable(metadata.getTable());
			}
		}
	}

	@Override
	public String getCurrentSessionFileName() {
		return currentFileName;
	}

	@SuppressWarnings("unchecked")
	public void addCyProperty(final CyProperty<?> newCyProperty, final Map<String, String> properties) {
		CyProperty.SavePolicy sp = newCyProperty.getSavePolicy();

		if (sp == CyProperty.SavePolicy.SESSION_FILE || sp == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR) {
			if (Bookmarks.class.isAssignableFrom(newCyProperty.getPropertyType()))
				bookmarks = (CyProperty<Bookmarks>) newCyProperty;
			else
				sessionProperties.add(newCyProperty);
		}
	}

	public void removeCyProperty(final CyProperty<?> oldCyProperty, final Map<String, String> properties) {
		CyProperty.SavePolicy sp = oldCyProperty.getSavePolicy();

		if (sp == CyProperty.SavePolicy.SESSION_FILE || sp == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR) {
			if (Bookmarks.class.isAssignableFrom(oldCyProperty.getPropertyType()))
				bookmarks = null;
			else
				sessionProperties.remove(oldCyProperty);
		}
	}

	private Set<CyProperty<?>> getAllProperties() {
		Set<CyProperty<?>> set = new HashSet<CyProperty<?>>(sessionProperties);
		
		if (bookmarks != null)
			set.add(bookmarks);

		return set;
	}

	private void restoreProperties(CySession sess) {
		for (CyProperty<?> cyProps : sess.getProperties()) {
			final Properties serviceProps = new Properties();
			serviceProps.setProperty("cyPropertyName", cyProps.getName());
			serviceProps.setProperty("serviceType", "property");
	        
			registrar.registerAllServices(cyProps, serviceProps);
		}
	}
	
	private void restoreNetworks(CySession sess) {
		logger.debug("Restoring networks...");
		Set<CyNetwork> networks = sess.getNetworks();

		for (CyNetwork n : networks) {
			netMgr.addNetwork(n);
		}
	}
	
	private void restoreNetworkViews(CySession sess) {
		logger.debug("Restoring network views...");
		Set<CyNetworkView> netViews = sess.getNetworkViews();
		
		if (netViews != null) {
			for (CyNetworkView nv : netViews) {
				if (nv != null)
					nvMgr.addNetworkView(nv);
			}
		}
	}

	private void restoreVisualStyles(final CySession sess) {
		logger.debug("Restoring visual styles...");
		// Register visual styles 
		final Set<VisualStyle> styles = sess.getVisualStyles();
		final Map<String, VisualStyle> stylesMap = new HashMap<String, VisualStyle>();

		if (styles != null) {
			for (VisualStyle vs : styles) {
				vmMgr.addVisualStyle(vs);
				stylesMap.put(vs.getTitle(), vs);
			}
		}
		
		// Set visual styles to network views
		final Map<CyNetworkView, String> viewStyleMap = sess.getViewVisualStyleMap();
		
		if (viewStyleMap != null) {
			for (Entry<CyNetworkView, String> entry : viewStyleMap.entrySet()) {
				final CyNetworkView netView = entry.getKey();
				final String stName = entry.getValue();
				final VisualStyle vs = stylesMap.get(stName);

				if (vs != null) {
					vmMgr.setVisualStyle(vs, netView);
					vs.apply(netView);
					netView.updateView();
				}
			}
		}
	}

	private void disposeCurrentSession(boolean removeVisualStyles) {
		logger.debug("Disposing current session...");
		
		// Destroy network views
		Set<CyNetworkView> netViews = nvMgr.getNetworkViewSet();

		for (CyNetworkView nv : netViews) {
			nvMgr.destroyNetworkView(nv);
		}
		
		nvMgr.reset();
		
		// Destroy networks
		final Set<CyNetwork> networks = netMgr.getNetworkSet();
		
		for (CyNetwork n : networks) {
			netMgr.destroyNetwork(n);
		}
		
		netMgr.reset();

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

		// Destroy tables
		tblMgr.reset();
		
		// Unregister session properties
		final Set<CyProperty<?>> cyPropsClone = getAllProperties();
		
		for (CyProperty<?> cyProps : cyPropsClone) {
			if (cyProps.getSavePolicy().equals(CyProperty.SavePolicy.SESSION_FILE)) {
				registrar.unregisterAllServices(cyProps);
			}
		}
		
		// Clear undo stack
		undo.reset();
	}

	/**
	 * Update current session session object when session is saved.
	 */
	@Override
	public void handleEvent(SessionSavedEvent e) {
		
		if(currentSession != e.getSavedSession())
			currentSession = e.getSavedSession();
		
		if(currentFileName != e.getSavedFileName())
			currentFileName = e.getSavedFileName();
	}
}
