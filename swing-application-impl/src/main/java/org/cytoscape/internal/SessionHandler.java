/*
 Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.internal;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.internal.io.Cytopanel;
import org.cytoscape.internal.io.Cytopanels;
import org.cytoscape.internal.io.NetworkFrame;
import org.cytoscape.internal.io.NetworkFrames;
import org.cytoscape.internal.io.SessionState;
import org.cytoscape.internal.io.SessionStateIO;
import org.cytoscape.internal.view.CytoscapeDesktop;
import org.cytoscape.internal.view.NetworkViewManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionHandler implements CyShutdownListener, SessionLoadedListener, SessionAboutToBeSavedListener {

	private static final String APP_NAME = "org.cytoscape.swing-application";
	private static final String SESSION_STATE_FILENAME = "session_state.xml";
	
	private final CytoscapeDesktop desktop;
	private final CyNetworkManager netMgr;
	private final CyApplicationManager appManager;
	private final NetworkViewManager netViewMgr;
	private final SynchronousTaskManager<?> syncTaskMgr;
	private final TaskFactory saveTaskFactory;
	private final SessionStateIO sessionStateIO;
	
	private final Map<String, CytoPanelName> CYTOPANEL_NAMES = new LinkedHashMap<String, CytoPanelName>();
	
	private static final Logger logger = LoggerFactory.getLogger(SessionHandler.class);
	
	public SessionHandler(final CytoscapeDesktop desktop,
						  final CyNetworkManager netMgr,
						  final CyApplicationManager appManager,
						  final NetworkViewManager netViewMgr,
						  final SynchronousTaskManager<?> syncTaskMgr,
						  final TaskFactory saveTaskFactory,
						  final SessionStateIO sessionStateIO) {
		this.desktop = desktop;
		this.netMgr = netMgr;
		this.appManager = appManager;
		this.netViewMgr = netViewMgr;
		this.syncTaskMgr = syncTaskMgr;
		this.saveTaskFactory = saveTaskFactory;
		this.sessionStateIO = sessionStateIO;
		
		CYTOPANEL_NAMES.put("CytoPanel1", CytoPanelName.WEST);
		CYTOPANEL_NAMES.put("CytoPanel2", CytoPanelName.SOUTH);
		CYTOPANEL_NAMES.put("CytoPanel3", CytoPanelName.EAST);
	}

	@Override
	public void handleEvent(CyShutdownEvent e) {
		// If there are no networks, just quit.
		if (netMgr.getNetworkSet().size() == 0) 
			return;

		// Ask user whether to save current session or not.
		final String msg = "Do you want to save your session?";
		final String header = "Save Networks Before Quitting?";
		final Object[] options = { "Yes, save and quit", "No, just quit", "Cancel" };
		final int n = JOptionPane.showOptionDialog(desktop.getJFrame(), msg, header,
		                                     JOptionPane.YES_NO_OPTION,
		                                     JOptionPane.QUESTION_MESSAGE, 
											 null, options, options[0]);

		if (n == JOptionPane.NO_OPTION) {
			return;
		} else if (n == JOptionPane.YES_OPTION) {
			syncTaskMgr.execute(saveTaskFactory.createTaskIterator());
			return;
		} else {
			e.abortShutdown("User canceled the shutdown request.");
			return; 
		}
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		final SessionState sessState = new SessionState();
		
		// Network Frames
		final NetworkFrames netFrames = new NetworkFrames();
		sessState.setNetworkFrames(netFrames);
		
		final JInternalFrame[] internalFrames = netViewMgr.getDesktopPane().getAllFrames();
    	
    	for (JInternalFrame iframe : internalFrames) {
    		final CyNetworkView view = netViewMgr.getNetworkView(iframe);
    		
    		if (view == null) {
				logger.error("Cannot save position of network frame \"" + iframe.getTitle()
						+ "\": Network View is null.");
    			continue;
    		}
    		
    		final NetworkFrame nf = new NetworkFrame();
            nf.setNetworkViewID(view.getSUID().toString());
            nf.setX(BigInteger.valueOf(iframe.getX()));
            nf.setY(BigInteger.valueOf(iframe.getY()));
    		
            netFrames.getNetworkFrame().add(nf);
    	}
		
        // CytoPanels States
    	final Cytopanels cytopanels = new Cytopanels();
		sessState.setCytopanels(cytopanels);
		
		for (Map.Entry<String, CytoPanelName> entry : CYTOPANEL_NAMES.entrySet()) {
			final CytoPanel p = desktop.getCytoPanel(entry.getValue());
			
			final Cytopanel cytopanel = new Cytopanel();
			cytopanel.setId(entry.getKey());
			cytopanel.setPanelState(p.getState().toString());
			cytopanel.setSelectedPanel(Integer.toString(p.getSelectedIndex()));
			
			cytopanels.getCytopanel().add(cytopanel);
		}
		
		// Create temp file
		File tmpFile = new File(System.getProperty("java.io.tmpdir"), SESSION_STATE_FILENAME);
		tmpFile.deleteOnExit();
		
		// Write to the file
		sessionStateIO.write(sessState, tmpFile);
		
		// Add it to the apps list
		List<File> fileList = new ArrayList<File>();
		fileList.add(tmpFile);
		
		try {
			e.addAppFiles(APP_NAME, fileList);
		} catch (Exception ex) {
			logger.error("Error adding "+SESSION_STATE_FILENAME+" file to be saved in the session.", ex);
		}
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		final CySession sess = e.getLoadedSession();

		if (sess != null) {
			final Map<String, List<File>> filesMap = sess.getAppFileListMap();

			if (filesMap != null) {
				final List<File> files = filesMap.get(APP_NAME);

				if (files != null) {
					SessionState sessState = null;
					
					for (File f : files) {
						if (f.getName().endsWith(SESSION_STATE_FILENAME)) {
							// There should be only one file!
							sessState = sessionStateIO.read(f);
							break;
						}
					}
					
					if (sessState != null) {
						setNetworkFrameLocations(sessState.getNetworkFrames(), sess);
						setCytoPanelStates(sessState.getCytopanels());
					}
				}
			}
		}
	}

	/**
	 * Restore each network frame's location.
	 * @param frames
	 */
	private void setNetworkFrameLocations(final NetworkFrames frames, final CySession sess) {
		if (frames != null) {
			final List<NetworkFrame> framesList = frames.getNetworkFrame();
			CyNetworkView currentNetView = null;
			
			for (NetworkFrame nf : framesList) {
				final String oldIdStr = nf.getNetworkViewID(); // ID in the original session--it's probably different now
				CyNetworkView view = null;
				
				// Try to convert the old ID to Long--only works if the loaded session is a 3.0 format
				try {
					final Long oldSuid = Long.valueOf(oldIdStr);
					view = sess.getObject(oldSuid, CyNetworkView.class);
				} catch (NumberFormatException nfe) {
					logger.debug("The old network view id is not a number: " + oldIdStr);
					view = sess.getObject(oldIdStr, CyNetworkView.class);
				}
				
				if (view != null) {
					final JInternalFrame iframe = netViewMgr.getInternalFrame(view);
					
					if (iframe != null) {
						iframe.moveToBack(); // In order to restore its z-index
						
						if (nf.getX() != null && nf.getY() != null) {
							int x = nf.getX().intValue();
							int y = nf.getY().intValue();
							iframe.setLocation(x, y);
						}
					}
					
					// The first frame should be the current one
					if (currentNetView == null) {
						currentNetView = view;
					}
				} else {
					logger.warn("Cannot restore network frame's position: Network View not found for former ID \""
							+ oldIdStr + "\".");
				}
			}
			
			// Restore the current network view
			if (currentNetView != null) {
				appManager.setCurrentNetworkView(currentNetView);
			}
		}
	}
	
	/**
	 * Restore the states of the CytoPanels.
	 * @param cytopanels
	 */
	private void setCytoPanelStates(final Cytopanels cytopanels) {
		if (cytopanels != null) {
			final List<Cytopanel> cytopanelsList = cytopanels.getCytopanel();

			for (Cytopanel cytopanel : cytopanelsList) {
				String id = cytopanel.getId();
				final CytoPanelName panelName = CYTOPANEL_NAMES.get(id);

				if (panelName != null) {
					final CytoPanel p = desktop.getCytoPanel(panelName);

					try {
						p.setState(CytoPanelState.valueOf(cytopanel.getPanelState().toUpperCase().trim()));
					} catch (Exception ex) {
						logger.error("Cannot restore the state of panel \"" + panelName.getTitle() + "\"", ex);
					}

					try {
						p.setSelectedIndex(Integer.parseInt(cytopanel.getSelectedPanel()));
					} catch (Exception ex) {
						logger.error("Cannot restore the selected index of panel \"" + panelName.getTitle() + "\"", ex);
					}
				}
			}
		}
	}
}
