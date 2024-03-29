package org.cytoscape.internal;

import static org.cytoscape.internal.view.util.ViewUtil.invokeOnEDT;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.events.CyShutdownRequestedEvent;
import org.cytoscape.application.events.CyShutdownRequestedListener;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.internal.io.SessionIO;
import org.cytoscape.internal.io.networklist.Network;
import org.cytoscape.internal.io.networklist.NetworkList;
import org.cytoscape.internal.io.sessionstate.Cytopanel;
import org.cytoscape.internal.io.sessionstate.Cytopanels;
import org.cytoscape.internal.io.sessionstate.SessionState;
import org.cytoscape.internal.view.CytoPanelImpl;
import org.cytoscape.internal.view.CytoPanelStateInternal;
import org.cytoscape.internal.view.CytoscapeDesktop;
import org.cytoscape.internal.view.NetworkMainPanel;
import org.cytoscape.internal.view.NetworkViewMediator;
import org.cytoscape.internal.view.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.SynchronousTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class SessionHandler implements CyShutdownRequestedListener, SessionLoadedListener, SessionAboutToBeSavedListener {

	private static final String SESSION_STATE_DOC_VERSION = "1.1";
	
	private static final String APP_NAME = "org.cytoscape.swing-application";
	private static final String SESSION_STATE_FILENAME = "session_state.xml";
	private static final String NETWORK_LIST_FILENAME = "network_list.xml";
	
	private final Map<String, CytoPanelName> CYTOPANEL_NAMES = new LinkedHashMap<>();
	
	private final CytoscapeDesktop desktop;
	private final NetworkViewMediator netViewMediator;
	private final SessionIO sessionIO;
	private final NetworkMainPanel netPanel;
	private final CyServiceRegistrar serviceRegistrar;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	public SessionHandler(
			CytoscapeDesktop desktop,
			NetworkViewMediator netViewMediator,
			SessionIO sessionIO,
			NetworkMainPanel netPanel,
			CyServiceRegistrar serviceRegistrar
	) {
		this.desktop = desktop;
		this.netViewMediator = netViewMediator;
		this.sessionIO = sessionIO;
		this.netPanel = netPanel;
		this.serviceRegistrar = serviceRegistrar;
		
		CYTOPANEL_NAMES.put("CytoPanel1", CytoPanelName.WEST);
		CYTOPANEL_NAMES.put("CytoPanel2", CytoPanelName.SOUTH);
		CYTOPANEL_NAMES.put("CytoPanel3", CytoPanelName.EAST);
	}

	@Override
	public void handleEvent(CyShutdownRequestedEvent e) {
		var netMgr = serviceRegistrar.getService(CyNetworkManager.class);
		
		// If there are no networks, just quit.
		if (netMgr.getNetworkSet().isEmpty() || e.forceShutdown()) 
			return;

		// Ask user whether to save current session or not.
		var msg = "Do you want to save your session?";
		var header = "Save Networks Before Quitting?";
		Object[] options = { "Yes, save and quit", "No, just quit", "Cancel" };
		int n = JOptionPane.showOptionDialog(desktop.getJFrame(), msg, header,
		                                     JOptionPane.YES_NO_OPTION,
		                                     JOptionPane.QUESTION_MESSAGE, 
											 null, options, options[0]);

		if (n == JOptionPane.NO_OPTION) {
			return;
		} else if (n == JOptionPane.YES_OPTION) {
			var sessionMgr = serviceRegistrar.getService(CySessionManager.class);
			var sessionFileName = sessionMgr.getCurrentSessionFileName();
			final File file;
			
			if (sessionFileName == null || sessionFileName.isEmpty()) {
				var filter = new FileChooserFilter("Session File", "cys");
				var filterCollection = new ArrayList<FileChooserFilter>(1);
				filterCollection.add(filter);
				
				var fileUtil = serviceRegistrar.getService(FileUtil.class);
				file = fileUtil.getFile(desktop, "Save Session File", FileUtil.SAVE, filterCollection );
			} else {
				file = new File(sessionFileName);
			}
			
			if (file == null) { //just check the file again in case the file chooser dialoge task is canceled.
				e.abortShutdown("User canceled the shutdown request.");
				return;
			}
			
			var syncTaskMgr = serviceRegistrar.getService(SynchronousTaskManager.class);
			var saveTaskFactory = serviceRegistrar.getService(SaveSessionAsTaskFactory.class);
			
			syncTaskMgr.execute(saveTaskFactory.createTaskIterator(file));
			
			return;
		} else {
			e.abortShutdown("User canceled the shutdown request.");
			
			return; 
		}
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		// Do not use invokeLater() here.  It breaks session file.
		var f1 = saveSessionState(e);
		var f2 = saveNetworkList(e);
		
		var files = new ArrayList<File>();
		if (f1 != null) files.add(f1);
		if (f2 != null) files.add(f2);
		
		// Add it to the apps list
		try {
			if (!files.isEmpty())
				e.addAppFiles(APP_NAME, files);
		} catch (Exception ex) {
			logger.error("Error adding app files to be saved in the session.", ex);
		}
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		var sess = e.getLoadedSession();

		if (sess != null)
			invokeOnEDT(() -> postLoading(sess));
	}
	
	private File saveSessionState(SessionAboutToBeSavedEvent e) {
		var sessState = new SessionState();
		sessState.setDocumentVersion(SESSION_STATE_DOC_VERSION);

		// CytoPanels States
		var cytopanels = new Cytopanels();
		sessState.setCytopanels(cytopanels);
		
		for (var entry : CYTOPANEL_NAMES.entrySet()) {
			var p = desktop.getCytoPanel(entry.getValue());

			var cytopanel = new Cytopanel();
			cytopanel.setId(entry.getKey());
			cytopanel.setPanelState(p.getState().toString());
			cytopanel.setSelectedPanel(Integer.toString(p.getSelectedIndex()));
			
			if (p instanceof CytoPanelImpl)
				cytopanel.setPanelStateInternal(((CytoPanelImpl) p).getStateInternal().toString());

			cytopanels.getCytopanel().add(cytopanel);
		}

		// Create temp file
		var tmpFile = new File(System.getProperty("java.io.tmpdir"), SESSION_STATE_FILENAME);
		tmpFile.deleteOnExit();

		// Write to the file
		sessionIO.write(sessState, tmpFile);

		return tmpFile;
	}
	
	private File saveNetworkList(SessionAboutToBeSavedEvent e) {
		var netPos = netPanel.getNetworkListOrder();
		
		// Create the JAXB objects
		var netList = new NetworkList();
		
		for (var entry : netPos.entrySet()) {
			var suid = entry.getKey();
			var order = entry.getValue();
			
			if (order != null) {
				var n = new Network();
				n.setId(suid);
				n.setOrder(order);
				netList.getNetwork().add(n);
			}
		}
		
		// Create temp file
		var tmpFile = new File(System.getProperty("java.io.tmpdir"), NETWORK_LIST_FILENAME);
		tmpFile.deleteOnExit();

		// Write to the file
		sessionIO.write(netList, tmpFile);
		
		return tmpFile;
	}
	
	private final void postLoading(CySession sess) {
		NetworkList netList = null;
		var filesMap = sess.getAppFileListMap();

		if (filesMap != null) {
			var files = filesMap.get(APP_NAME);

			if (files != null) {
				SessionState sessState = null;
				
				for (var f : files) {
					if (f.getName().endsWith(SESSION_STATE_FILENAME))
						sessState = sessionIO.read(f, SessionState.class);
					else if (f.getName().endsWith(NETWORK_LIST_FILENAME))
						netList = sessionIO.read(f, NetworkList.class);
				}
				
				if (sessState != null)
					setCytoPanelStates(sessState.getCytopanels());
			}
		}
		
		if (netList == null) {
			// Probably a Cy2 session file, which does not provide a separate "network_list" file
			// so let's get the orders from the networks in the CySession file
			// (we just assume the Session Reader sent the networks in the correct order in a LinkedHashSet)
			var netSet = sess.getNetworks();
			var netPos = new HashMap<Long, Integer>();
			int count = 0;
			
			for (var n : netSet)
				netPos.put(n.getSUID(), count++);
				
			setSessionNetworks(netPos);
		} else {
			setSessionNetworks(netList.getNetwork(), sess);
		}
	}
	
	/**
	 * Restore the states of the CytoPanels.
	 * @param cytopanels
	 */
	private void setCytoPanelStates(Cytopanels cytopanels) {
		if (cytopanels != null) {
			var cytopanelsList = cytopanels.getCytopanel();

			for (var cytopanel : cytopanelsList) {
				var id = cytopanel.getId();
				var panelName = CYTOPANEL_NAMES.get(id);

				if (panelName != null) {
					var p = desktop.getCytoPanel(panelName);

					if (p instanceof CytoPanelImpl && cytopanel.getPanelStateInternal() != null) {
						try {
							CytoPanelImpl impl = (CytoPanelImpl) p;
							impl.setStateInternal(CytoPanelStateInternal.valueOf(cytopanel.getPanelStateInternal().toUpperCase().trim()));
						} catch (Exception ex) {
							logger.error("Cannot restore the internal state of panel \"" + panelName.getTitle() + "\"", ex);
						}
					} else {
						try {
							p.setState(CytoPanelState.valueOf(cytopanel.getPanelState().toUpperCase().trim()));
						} catch (Exception ex) {
							logger.error("Cannot restore the state of panel \"" + panelName.getTitle() + "\"", ex);
						}
					}

					try {
						int index = Integer.parseInt(cytopanel.getSelectedPanel());
						
						if (index >= 0 && index < p.getCytoPanelComponentCount())
							p.setSelectedIndex(index);
					} catch (Exception ex) {
						logger.error("Cannot restore the selected index of panel \"" + panelName.getTitle() + "\"", ex);
					}
				}
			}
		}
	}
	
	private void setSessionNetworks(List<Network> netInfoList, CySession sess) {
		var netOrder = new HashMap<Long, Integer>();
		
		for (var n : netInfoList) {
			var net = sess.getObject(n.getId(), CyNetwork.class); // in order to retrieve the new SUID
			
			if (net != null)
				netOrder.put(net.getSUID(), n.getOrder());
		}
		
		setSessionNetworks(netOrder);
	}
	
	/**
	 * @param netPos Maps CyNetwork SUID to the network position
	 */
	private void setSessionNetworks(Map<Long, Integer> netPos) {
		var sortedNetworks = ViewUtil.getSessionNetworks(serviceRegistrar);
		ViewUtil.sortNetworksByCreationPos(sortedNetworks, netPos);
		
		var applicationMgr = serviceRegistrar.getService(CyApplicationManager.class);
		
		var selectedNetworks = applicationMgr.getSelectedNetworks();
		var selectedViews = applicationMgr.getSelectedNetworkViews();
		
		invokeOnEDT(() -> {
			netPanel.setNetworks(sortedNetworks);
			
			netPanel.setSelectedNetworks(selectedNetworks);
			netViewMediator.getNetworkViewMainPanel().setSelectedNetworkViews(selectedViews);
		});
	}
}
