/*
 File: NetworkViewManager.java

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
package org.cytoscape.internal.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyHelpBroker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.session.Cysession;
import org.cytoscape.property.session.Desktop;
import org.cytoscape.property.session.NetworkFrame;
import org.cytoscape.property.session.NetworkFrames;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.NetworkViewChangedEvent;
import org.cytoscape.view.model.events.NetworkViewChangedListener;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managing views (presentations) in current session.
 * 
 */
public class NetworkViewManager extends InternalFrameAdapter implements NetworkViewAddedListener,
		NetworkViewAboutToBeDestroyedListener, SetCurrentNetworkViewListener, SetCurrentNetworkListener,
		SessionLoadedListener, SessionAboutToBeSavedListener, NetworkViewChangedListener, RowsSetListener {

	private static final Logger logger = LoggerFactory.getLogger(NetworkViewManager.class);

	// TODO Where should we store these property constants?
	private static final String VIEW_THRESHOLD = "viewThreshold";
	private static final int DEF_VIEW_THRESHOLD = 10000;

	private static final int MINIMUM_WIN_WIDTH = 200;
	private static final int MINIMUM_WIN_HEIGHT = 200;

	private final JDesktopPane desktopPane;

	// Key is MODEL ID
	private final Map<Long, JInternalFrame> presentationContainerMap;
	private final Map<Long, RenderingEngine<CyNetwork>> presentationMap;

	private final Map<JInternalFrame, Long> iFrameMap;
	private final Properties props;

	private Long currentPresentationContainerID;

	// Supports multiple presentations
	private final Map<String, RenderingEngineFactory<CyNetwork>> factories;
	private RenderingEngineFactory<CyNetwork> currentRenderingEngineFactory;

	// TODO: discuss the name and key of props.
	private static final String ID = "id";

	// TODO: for now, use this as default. But in the future, we should provide
	// UI to select presentation.
	private static final String DEFAULT_PRESENTATION = "ding";

	private final Map<CyTable, CyNetwork> nameTables;
	private final CyNetworkViewManager networkViewManager;
	private final CyApplicationManager applicationManager;

	/**
	 * Creates a new NetworkViewManager object.
	 * 
	 * @param desktop
	 *            DOCUMENT ME!
	 */
	public NetworkViewManager(CyApplicationManager appMgr, CyNetworkViewManager netViewMgr,
			CyProperty<Properties> cyProps, CyHelpBroker help) {

		if (appMgr == null)
			throw new NullPointerException("CyApplicationManager is null.");
		if (netViewMgr == null)
			throw new NullPointerException("CyNetworkViewManager is null.");

		this.factories = new HashMap<String, RenderingEngineFactory<CyNetwork>>();

		this.networkViewManager = netViewMgr;
		this.applicationManager = appMgr;
		this.props = cyProps.getProperties();

		this.desktopPane = new JDesktopPane();

		// add Help hooks
		help.getHelpBroker().enableHelp(desktopPane, "network-view-manager", null);

		presentationContainerMap = new HashMap<Long, JInternalFrame>();
		presentationMap = new HashMap<Long, RenderingEngine<CyNetwork>>();
		iFrameMap = new HashMap<JInternalFrame, Long>();
		currentPresentationContainerID = null;

		nameTables = new HashMap<CyTable, CyNetwork>();
	}

	/**
	 * Dynamically add rendering engine factories. Will be used by Spring DM.
	 * 
	 * @param factory
	 * @param props
	 */
	public void addPresentationFactory(RenderingEngineFactory<CyNetwork> factory,
			@SuppressWarnings("rawtypes") Map props) {
		logger.info("Adding New Rendering Engine Factory...");

		Object rendererID = props.get(ID);
		if (rendererID == null)
			throw new IllegalArgumentException("Renderer ID is null.");

		factories.put(rendererID.toString(), factory);
		if (currentRenderingEngineFactory == null && rendererID.equals(DEFAULT_PRESENTATION)) {
			currentRenderingEngineFactory = factory;
			logger.info(rendererID + " is registered as the default rendering engine.");
		}

		logger.info("New Rendering Engine is Available: " + rendererID);
	}

	public void removePresentationFactory(RenderingEngineFactory<CyNetwork> factory,
			@SuppressWarnings("rawtypes") Map props) {
		factories.remove(props.get(ID));
	}

	/**
	 * Desktop for JInternalFrames which contains actual network presentations.
	 * 
	 * @return DOCUMENT ME!
	 */
	public JDesktopPane getDesktopPane() {
		return desktopPane;
	}

	/**
	 * Given a CyNetworkView, returns the internal frame.
	 * 
	 * @param view
	 *            CyNetworkView
	 * @return JInternalFrame
	 * @throws IllegalArgumentException
	 */
	public JInternalFrame getInternalFrame(CyNetworkView view) throws IllegalArgumentException {
		// check args
		if (view == null) {
			throw new IllegalArgumentException("NetworkViewManager.getInternalFrame(), argument is null");
		}

		// outta here
		return presentationContainerMap.get(view.getModel().getSUID());
	}

	/**
	 * View switched
	 */
	public void internalFrameActivated(InternalFrameEvent e) {
		final Long networkId = iFrameMap.get(e.getInternalFrame());
		if (networkId == null)
			return;

		final RenderingEngine<CyNetwork> currentEngine = applicationManager.getCurrentRenderingEngine();
		applicationManager.setCurrentNetworkView(networkId);

		if (currentEngine == null || currentEngine.getViewModel().getModel().getSUID() != networkId)
			applicationManager.setCurrentRenderingEngine(presentationMap.get(networkId));
	}

	/**
	 * Fire Events when a Managed Network View gets the Focus.
	 */
	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		internalFrameActivated(e);
	}

	// // Event Handlers ////
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		if (e.getNetworkView() == null) {
			logger.info("Attempting to set current network view model: null view ");
			return;
		}

		logger.info("Attempting to set current network view model: View Model ID = " + e.getNetworkView().getSUID());
		setFocus(e.getNetworkView().getModel().getSUID());
	}

	public void handleEvent(SetCurrentNetworkEvent e) {
		if (e.getNetwork() == null) {
			logger.info("Attempting to set current network : null network ");
			return;
		}

		logger.info("Attempting to set current network model: Model ID = " + e.getNetwork().getSUID());
		setFocus(e.getNetwork().getSUID());
	}

	public void handleEvent(NetworkViewAboutToBeDestroyedEvent nvde) {
		logger.info("Network view destroyed: View ID = " + nvde.getNetworkView());
		removeView(nvde.getNetworkView());
	}

	/**
	 * Adding new network view model to this manager. Then, render presentation.
	 */
	public void handleEvent(final NetworkViewAddedEvent nvae) {

		logger.info("\n\n\nView Manager got Network view added event.  Adding view to manager: NetworkViewManager: View ID = "
				+ nvae.getNetworkView().getSUID() + "\n\n\n");

		final String viewThresholdString = props.getProperty(VIEW_THRESHOLD);
		int viewThreshold;
		try {
			viewThreshold = Integer.parseInt(viewThresholdString);
		} catch (Exception e) {
			viewThreshold = DEF_VIEW_THRESHOLD;
			logger.warn("Could not parse view threshold property.  Use default value: " + DEF_VIEW_THRESHOLD);
		}

		CyNetworkView networkView = nvae.getNetworkView();
		final CyNetwork model = networkView.getModel();
		final int graphObjectCount = model.getNodeCount() + model.getEdgeCount();
		
//		// Render only when graph size is smaller than threshold.
//		if (graphObjectCount > viewThreshold) {
//			int createFlag = JOptionPane
//					.showConfirmDialog(
//							null,
//							"Network contains "
//									+ graphObjectCount
//									+ " objects.\nDo you still want to create visualization?\nThis is not recommended for machines with small amount of memory.",
//							"Large Network Data Loaded", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
//			if (createFlag == JOptionPane.NO_OPTION) {
//				networkViewManager.destroyNetworkView(networkView);
//				networkView = null;
//				return;
//			}
//		}

		render(nvae.getNetworkView());
	}

	protected void removeView(final CyNetworkView view) {
		try {
			final JInternalFrame frame = presentationContainerMap.get(view.getModel().getSUID());
			if (frame != null) {
				RenderingEngine<CyNetwork> removed = this.presentationMap.remove(view.getModel().getSUID());
				logger.debug("#### Removing rendering engine: " + removed);
				removed = null;
				frame.dispose();
			}
		} catch (Exception e) {
			logger.error("Network View unable to be killed", e);
		}

		presentationContainerMap.remove(view.getModel().getSUID());
		nameTables.remove(view.getModel().getDefaultNetworkTable());

		logger.debug("Network View Model removed.");
	}

	/**
	 * Create a visualization container and add presentation to it.
	 * 
	 */
	private void render(final CyNetworkView view) {
		final Long modelID = view.getModel().getSUID();

		// If already registered in this manager, do not render.
		if (presentationContainerMap.containsKey(modelID))
			return;

		// Create a new InternalFrame and put the CyNetworkView Component into
		// it
		final String title = view.getModel().getCyRow().get(CyTableEntry.NAME, String.class);
		final JInternalFrame iframe = new JInternalFrame(title, true, true, true, true);
		
		// This is for force move title bar to the desktop if it's out of range.
		iframe.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				final Point originalPoint = iframe.getLocation();
				if(originalPoint.y < 0)
					iframe.setLocation(originalPoint.x, 0);
			}
		});
		

		iframe.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				networkViewManager.destroyNetworkView(view);

				Component[] components = iframe.getComponents();
				for (Component cp : components) {
					logger.debug("Removing: " + cp);
					cp = null;
				}
				components = null;

			}
		});

		desktopPane.add(iframe);
		presentationContainerMap.put(modelID, iframe);
		iFrameMap.put(iframe, modelID);

		final long start = System.currentTimeMillis();
		logger.debug("Rendering start: view model = " + view.getSUID());
		final RenderingEngine<CyNetwork> renderingEngine = currentRenderingEngineFactory.createRenderingEngine(iframe, view);
		logger.debug("Rendering finished in " + (System.currentTimeMillis() - start) + " m sec.");
		presentationMap.put(modelID, renderingEngine);

		iframe.pack();

		// create cascade iframe
		int x = 0;
		int y = 0;
		JInternalFrame refFrame = null;
		JInternalFrame[] allFrames = desktopPane.getAllFrames();

		// frame Location
		if (allFrames.length > 1)
			refFrame = allFrames[0];

		if (refFrame != null) {
			x = refFrame.getLocation().x + 20;
			y = refFrame.getLocation().y + 20;
		}

		if (x > (desktopPane.getWidth() - MINIMUM_WIN_WIDTH))
			x = desktopPane.getWidth() - MINIMUM_WIN_WIDTH;
		if (y > (desktopPane.getHeight() - MINIMUM_WIN_HEIGHT))
			y = desktopPane.getHeight() - MINIMUM_WIN_HEIGHT;
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;

		iframe.setLocation(x, y);

		// maximize the frame if the specified property is set

		final String max = props.getProperty("maximizeViewOnCreate");

		if ((max != null) && Boolean.parseBoolean(max)) {
			try {
				iframe.setMaximum(true);
			} catch (PropertyVetoException pve) {
				logger.warn("Could not maximize frame.", pve);
			}
		} else {
			int w = view.getVisualProperty(MinimalVisualLexicon.NETWORK_WIDTH).intValue();
			int h = view.getVisualProperty(MinimalVisualLexicon.NETWORK_HEIGHT).intValue();
			updateNetworkSize(modelID, w, h);
		}

		// Display it and add listeners
		iframe.setVisible(true);
		iframe.addInternalFrameListener(this);

		nameTables.put(view.getModel().getDefaultNetworkTable(), view.getModel());
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		CyNetwork n = nameTables.get( e.getSource() );
		if ( n == null )
			return;
		
		final String title = n.getCyRow().get(CyTableEntry.NAME, String.class);
		updateNetworkTitle(n.getSUID(), title);
	}
	
	@Override
	public void handleEvent(NetworkViewChangedEvent e) {
		for ( ViewChangeRecord<CyNetwork> record : e.getPayloadCollection()) {
			Long id = record.getView().getModel().getSUID();
			JInternalFrame iframe = presentationContainerMap.get(id);
			if ( iframe == null )
				return;
			
			if (record.getVisualProperty().equals(MinimalVisualLexicon.NETWORK_WIDTH)) {
				int w = ((Double) record.getValue()).intValue();
				int h = iframe.getSize().height;
				updateNetworkSize(id, w, h);
			} else if (record.getVisualProperty().equals(MinimalVisualLexicon.NETWORK_HEIGHT)) {
				int w = iframe.getSize().width;
				int h = ((Double) record.getValue()).intValue();
				updateNetworkSize(id, w, h);
			}
		}
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		CySession sess = e.getLoadedSession();

		if (sess != null) {
			Cysession cs = sess.getCysession();

			if (cs != null) {
				// Restore frames positions
				if (cs.getSessionState().getDesktop().getNetworkFrames() != null) {
					List<NetworkFrame> frames = cs.getSessionState().getDesktop().getNetworkFrames().getNetworkFrame();

					for (NetworkFrame nf : frames) {
						String frameName = nf.getFrameID();
						JInternalFrame[] internalFrames = desktopPane.getAllFrames();

						for (JInternalFrame iframe : internalFrames) {
							if (iframe.getTitle() != null && iframe.getTitle().equals(frameName) && nf.getX() != null
									&& nf.getY() != null) {

								int x = nf.getX().intValue();
								int y = nf.getY().intValue();
								iframe.setLocation(x, y);
							}
						}
					}
				}
			}
		}
	}

    @Override
    public void handleEvent(SessionAboutToBeSavedEvent e) {
    	// Save Network Frames
    	Desktop desktop = e.getDesktop();
        
        if (desktop == null) {
            desktop = new Desktop();
            e.setDesktop(desktop);
        }
        
        NetworkFrames netFrames = new NetworkFrames();
        desktop.setNetworkFrames(netFrames);
    	
    	JInternalFrame[] internalFrames = desktopPane.getAllFrames();
    	
    	for (JInternalFrame iframe : internalFrames) {
    		NetworkFrame nf = new NetworkFrame();
    		
            nf.setFrameID(iframe.getTitle());
            nf.setHeight(BigInteger.valueOf(iframe.getHeight()));
            nf.setWidth(BigInteger.valueOf(iframe.getWidth()));
            nf.setX(BigInteger.valueOf(iframe.getX()));
            nf.setY(BigInteger.valueOf(iframe.getY()));
    		
            netFrames.getNetworkFrame().add(nf);
    	}
    }
	
	private void updateNetworkTitle(Long networkModelID, String title) {
		JInternalFrame frame = presentationContainerMap.get(networkModelID);

		if (frame != null) {
			frame.setTitle(title);
			frame.repaint();
		}
	}

	private void updateNetworkSize(final Long networkModelID, int width, int height) {
		final JInternalFrame frame = presentationContainerMap.get(networkModelID);

		if (frame == null)
			return;

		if (width > 0 && height > 0)
			frame.setSize(new Dimension(width, height));
	}

	private void setFocus(Long networkModelID) {
		if (networkModelID == null) {
			logger.warn("Set Focus method got a null as target ID.");
			return;
		}

		final CyNetworkView targetViewModel = networkViewManager.getNetworkView(networkModelID);
		if (targetViewModel == null) {
			logger.debug("View model does not exist for model ID: " + networkModelID);
			return;
		}

		// make sure we're not redundant
		if (currentPresentationContainerID != null && currentPresentationContainerID.equals(networkModelID)) {
			logger.debug("Same as current focus.  No need to update focus: model ID = " + networkModelID);
			return;
		}

		currentPresentationContainerID = networkModelID;

		// Reset focus on frames
		for (JInternalFrame f : presentationContainerMap.values()) {
			try {
				f.setSelected(false);
			} catch (PropertyVetoException pve) {
				logger.error("Couldn't reset focus for internal frames.", pve);
			}
		}

		// Set focus
		if (presentationContainerMap.containsKey(networkModelID)) {
			try {
				logger.debug("Updating JInternalFrame selection");
				final JInternalFrame curr = presentationContainerMap.get(networkModelID);

				curr.setIcon(false);
				curr.show();
				// fires internalFrameActivated
				curr.setSelected(true);

			} catch (Exception ex) {
				logger.error("Could not update focus: ", ex);
			}
		} else {
			logger.debug("Frame was not found. Need to create new frame for presentation.");
		}
	}
}
