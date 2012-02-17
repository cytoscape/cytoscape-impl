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
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyHelpBroker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.property.CyProperty;
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
		NetworkViewChangedListener {

	private static final Logger logger = LoggerFactory.getLogger(NetworkViewManager.class);

	// TODO Where should we store these property constants?
	private static final String VIEW_THRESHOLD = "viewThreshold";
	private static final int DEF_VIEW_THRESHOLD = 10000;

	private static final int MINIMUM_WIN_WIDTH = 200;
	private static final int MINIMUM_WIN_HEIGHT = 200;

	private final JDesktopPane desktopPane;

	// Key is MODEL ID
	private final Map<CyNetworkView, JInternalFrame> presentationContainerMap;
	private final Map<CyNetworkView, RenderingEngine<CyNetwork>> presentationMap;

	private final Map<JInternalFrame, CyNetworkView> iFrameMap;
	private final Properties props;

	private CyNetworkView currentView;

	// Supports multiple presentations
	private final Map<String, RenderingEngineFactory<CyNetwork>> factories;
	private RenderingEngineFactory<CyNetwork> currentRenderingEngineFactory;

	// TODO: discuss the name and key of props.
	private static final String ID = "id";

	// TODO: for now, use this as default. But in the future, we should provide
	// UI to select presentation.
	private static final String DEFAULT_PRESENTATION = "ding";

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

		presentationContainerMap = new HashMap<CyNetworkView, JInternalFrame>();
		presentationMap = new HashMap<CyNetworkView, RenderingEngine<CyNetwork>>();
		iFrameMap = new HashMap<JInternalFrame, CyNetworkView>();
		currentView = null;
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
		if (view == null) {
			throw new IllegalArgumentException("NetworkViewManager.getInternalFrame(), argument is null");
		}

		return presentationContainerMap.get(view);
	}
	
	public CyNetworkView getNetworkView(JInternalFrame frame) throws IllegalArgumentException {
		if (frame == null) {
			throw new IllegalArgumentException("NetworkViewManager.getNetworkView(), argument is null");
		}
		
		return iFrameMap.get(frame);
	}

	/**
	 * View switched
	 */
	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		final CyNetworkView view = iFrameMap.get(e.getInternalFrame());
		if (view == null)
			return;

		final RenderingEngine<CyNetwork> currentEngine = applicationManager.getCurrentRenderingEngine();
		
		if (!view.equals(applicationManager.getCurrentNetworkView()))
			applicationManager.setCurrentNetworkView(view);

		if (currentEngine == null || currentEngine.getViewModel() != view)
			applicationManager.setCurrentRenderingEngine(presentationMap.get(view));
	}

	/**
	 * Fire Events when a Managed Network View gets the Focus.
	 */
	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		internalFrameActivated(e);
	}

	// // Event Handlers ////
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		if (e.getNetworkView() == null) {
			logger.info("Attempting to set current network view model: null view ");
			return;
		}

		logger.info("Attempting to set current network view model: View Model ID = " + e.getNetworkView().getSUID());
		setFocus(e.getNetworkView());
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {
		if (e.getNetwork() == null) {
			logger.info("Attempting to set current network : null network ");
			return;
		}
		
		CyNetworkView view = networkViewManager.getNetworkView(e.getNetwork());

		if (view != null) {
			setFocus(view);
		}
	}

	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent nvde) {
		logger.info("Network view destroyed: View ID = " + nvde.getNetworkView());
		removeView(nvde.getNetworkView());
	}

	/**
	 * Adding new network view model to this manager. Then, render presentation.
	 */
	@Override
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
			final JInternalFrame frame = presentationContainerMap.get(view);
			if (frame != null) {
				RenderingEngine<CyNetwork> removed = this.presentationMap.remove(view);
				logger.debug("#### Removing rendering engine: " + removed);
				removed = null;
				frame.dispose();
			}
		} catch (Exception e) {
			logger.error("Network View unable to be killed", e);
		}

		presentationContainerMap.remove(view);
		logger.debug("Network View Model removed.");
	}

	/**
	 * Create a visualization container and add presentation to it.
	 * 
	 */
	private void render(final CyNetworkView view) {
		// If already registered in this manager, do not render.
		if (presentationContainerMap.containsKey(view))
			return;

		// Create a new InternalFrame and put the CyNetworkView Component into it
		final String title = getTitle(view);
		final JInternalFrame iframe = new JInternalFrame(title, true, true, true, true);
		
		// This is for force move title bar to the desktop if it's out of range.
		iframe.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				final Point originalPoint = iframe.getLocation();
				if (originalPoint.y < 0)
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
		presentationContainerMap.put(view, iframe);
		iFrameMap.put(iframe, view);

		final long start = System.currentTimeMillis();
		logger.debug("Rendering start: view model = " + view.getSUID());
		final RenderingEngine<CyNetwork> renderingEngine = currentRenderingEngineFactory.createRenderingEngine(iframe, view);
		logger.debug("Rendering finished in " + (System.currentTimeMillis() - start) + " m sec.");
		presentationMap.put(view, renderingEngine);

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
			updateNetworkSize(view, w, h);
		}

		// Display it and add listeners
		iframe.setVisible(true);
		iframe.addInternalFrameListener(this);
	}
	
	@Override
	public void handleEvent(NetworkViewChangedEvent e) {
		for ( ViewChangeRecord<CyNetwork> record : e.getPayloadCollection()) {
			CyNetworkView view = (CyNetworkView)(record.getView());
			JInternalFrame iframe = presentationContainerMap.get(view);
			if ( iframe == null )
				return;
			
			if (record.getVisualProperty().equals(MinimalVisualLexicon.NETWORK_WIDTH)) {
				int w = ((Double) record.getValue()).intValue();
				int h = iframe.getSize().height;
				updateNetworkSize(view, w, h);
			} else if (record.getVisualProperty().equals(MinimalVisualLexicon.NETWORK_HEIGHT)) {
				int w = iframe.getSize().width;
				int h = ((Double) record.getValue()).intValue();
				updateNetworkSize(view, w, h);
			} else if (record.getVisualProperty().equals(MinimalVisualLexicon.NETWORK_TITLE)) {
				updateNetworkTitle(view);
			}
		}
	}
	
	private void updateNetworkTitle(CyNetworkView view) {
		JInternalFrame frame = presentationContainerMap.get(view);

		if (frame != null) {
			final String title = getTitle(view);
			frame.setTitle(title);
			frame.repaint();
		}
	}

	private String getTitle(CyNetworkView view) {
		String title = view.getVisualProperty(MinimalVisualLexicon.NETWORK_TITLE);
		
		if (title == null || title.isEmpty())
			title = view.getModel().getRow(view.getModel()).get(CyTableEntry.NAME, String.class);
		
		return title;
	}
	
	private void updateNetworkSize(final CyNetworkView view, int width, int height) {
		final JInternalFrame frame = presentationContainerMap.get(view);

		if (frame == null)
			return;

		if (width > 0 && height > 0)
			frame.setSize(new Dimension(width, height));
	}

	private void setFocus(CyNetworkView targetViewModel) {
		if (targetViewModel == null) {
			logger.warn("Set Focus method got a null view.");
			return;
		}

		// make sure we're not redundant
		if (currentView != null && currentView.equals(targetViewModel)) {
			logger.debug("Same as current focus.  No need to update focus view model: " + targetViewModel);
			return;
		}

		currentView = targetViewModel;

		// Reset focus on frames
		for (JInternalFrame f : presentationContainerMap.values()) {
			try {
				f.setSelected(false);
			} catch (PropertyVetoException pve) {
				logger.error("Couldn't reset focus for internal frames.", pve);
			}
		}

		// Set focus
		final JInternalFrame curr = presentationContainerMap.get(targetViewModel);
		if (curr != null) {
			try {
				logger.debug("Updating JInternalFrame selection");

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
