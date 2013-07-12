package org.cytoscape.view.vizmap.gui.internal.view;

import static org.cytoscape.work.ServiceProperties.EDGE_EDIT_MENU;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.NETWORK_EDIT_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_EDIT_MENU;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNetworkViewContextMenuFactory;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.puremvc.java.multicore.interfaces.INotification;
import org.puremvc.java.multicore.patterns.mediator.Mediator;


public class VizMapperMenuMediator extends Mediator {

	public static final String NAME = "VizMapperMenuMediator";
	
	private static final String METADATA_MENU_KEY = "menu";
	
	private final ServicesUtil servicesUtil;
	private final VizMapperMainPanel vizMapperMainPanel;
	
	private VizMapperProxy proxy;

	private final Set<RenderingEngineFactory<?>> engineFactories = new HashSet<RenderingEngineFactory<?>>();
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VizMapperMenuMediator(final VizMapperMainPanel vizMapperMainPanel, final ServicesUtil servicesUtil) {
		super(NAME, vizMapperMainPanel);
		
		if (vizMapperMainPanel == null)
			throw new IllegalArgumentException("'vizMapperMainPanel' must not be null");
		if (servicesUtil == null)
			throw new IllegalArgumentException("'servicesUtil' must not be null");
		
		this.vizMapperMainPanel = vizMapperMainPanel;
		this.servicesUtil = servicesUtil;
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public final void onRegister() {
		proxy = (VizMapperProxy) getFacade().retrieveProxy(VizMapperProxy.NAME);
		registerMenuItems();
		super.onRegister();
	}
	
	@Override
	public String[] listNotificationInterests() {
		return new String[]{ };
	}
	
	@Override
	public void handleNotification(final INotification notification) {
		final String id = notification.getName();
		final Object body = notification.getBody();
		
//		if (id.equals(VISUAL_STYLE_SET_CHANGED)) {
//			updateVisualStyleList((SortedSet<VisualStyle>) body);
//		} else if (id.equals(CURRENT_VISUAL_STYLE_CHANGED)) {
//			selectCurrentVisualStyle((VisualStyle) body);
//			updateVisualPropertySheets((VisualStyle) body);
//		}
	}
	
	public void onRenderingEngineFactoryRegistered(final RenderingEngineFactory<?> factory,
			final Map<?, ?> properties) {
		this.engineFactories.add(factory);
	}
	
	public void onRenderingEngineFactoryUnregistered(final RenderingEngineFactory<?> factory,
			final Map<?, ?> properties) {
		this.engineFactories.remove(factory);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void registerMenuItems() {
		// Create context menu items
		final Properties nodeProp = new Properties();
		nodeProp.setProperty("preferredTaskManager", METADATA_MENU_KEY);
		nodeProp.setProperty(PREFERRED_MENU, NODE_EDIT_MENU);
		nodeProp.setProperty(MENU_GRAVITY, "-1");
		final NodeBypassContextMenuFactory nodeBypassContextMenuFactory = new NodeBypassContextMenuFactory();
		servicesUtil.registerAllServices(nodeBypassContextMenuFactory, nodeProp);

		final Properties edgeProp = new Properties();
		edgeProp.setProperty("preferredTaskManager", METADATA_MENU_KEY);
		edgeProp.setProperty(PREFERRED_MENU, EDGE_EDIT_MENU);
		edgeProp.setProperty(MENU_GRAVITY, "-1");
		final EdgeBypassContextMenuFactory edgeBypassContextMenuFactory = new EdgeBypassContextMenuFactory();
		servicesUtil.registerAllServices(edgeBypassContextMenuFactory, edgeProp);
		
		final Properties netProp = new Properties();
		netProp.setProperty("preferredTaskManager", METADATA_MENU_KEY);
		netProp.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
		netProp.setProperty(MENU_GRAVITY, "-1");
		final NetworkBypassContextMenuFactory netBypassContextMenuFactory = new NetworkBypassContextMenuFactory();
		servicesUtil.registerAllServices(netBypassContextMenuFactory, netProp);
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class NodeBypassContextMenuFactory implements CyNodeViewContextMenuFactory {

		@Override
		public CyMenuItem createMenuItem(final CyNetworkView netView, final View<CyNode> nodeView) {
			final BypassContextMenuBuilder menuBuilder = new BypassContextMenuBuilder(BasicVisualLexicon.NODE);
			return menuBuilder.build(netView, nodeView);
		}
	}
	
	private class EdgeBypassContextMenuFactory implements CyEdgeViewContextMenuFactory {
		
		@Override
		public CyMenuItem createMenuItem(final CyNetworkView netView, final View<CyEdge> edgeView) {
			final BypassContextMenuBuilder menuBuilder = new BypassContextMenuBuilder(BasicVisualLexicon.EDGE);
			return menuBuilder.build(netView, edgeView);
		}
	}
	
	private class NetworkBypassContextMenuFactory implements CyNetworkViewContextMenuFactory {
		
		@Override
		public CyMenuItem createMenuItem(final CyNetworkView netView) {
			final BypassContextMenuBuilder menuBuilder = new BypassContextMenuBuilder(BasicVisualLexicon.NETWORK);
			return menuBuilder.build(netView, netView);
		}
	}
	
	private class BypassContextMenuBuilder {
		
		private static final String ROOT = "Bypass Visual Style";
		private static final String REMOVE_ALL_FROM = "Remove All from this ";
		private static final String REMOVE_ALL_FROM_SELECTED_NODES = "Remove All from Selected Nodes";
		private static final String REMOVE_ALL_FROM_SELECTED_EDGES = "Remove All from Selected Edges";
		private static final String SET_TO_SELECTED_NODES = "Set Bypass to Selected Nodes";
		private static final String SET_TO_SELECTED_EDGES = "Set Bypass to Selected Edges";
		private static final String SET_TO_NETWORK = "Set Bypass to Network";
		
		private final VisualProperty<?> root;
		private final Set<VisualProperty<?>> vpSet = new HashSet<VisualProperty<?>>();
		
		public BypassContextMenuBuilder(final VisualProperty<?> root) {
			this.root = root;
		}
		
		/**
		 * @param netView
		 * @param view a View&lt;CyNode&gt;, View&lt;CyEdge&gt; or View&lt;CyNetwork&gt; object
		 * @return
		 */
		CyMenuItem build(final CyNetworkView netView, final View<? extends CyIdentifiable> view) {
			// Re-populate the Visual Property set 
			vpSet.clear();
			
			for (final RenderingEngineFactory<?> ef : engineFactories)
				vpSet.addAll(ef.getVisualLexicon().getAllDescendants(root));
			
			// Create menu items
			final JMenu rootMenu = new JMenu(ROOT);
			final Class<? extends CyIdentifiable> targetDataType = root.getTargetDataType();
			final String viewName = targetDataType.getSimpleName().replace("Cy", "");
			
			{
				final JMenuItem mi = new JMenuItem(REMOVE_ALL_FROM + viewName);
				mi.setEnabled(hasLockedValues(view));
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						clearAll(netView, (Set)Collections.singleton(view));
					}
				});
				rootMenu.add(mi);
			}
			
			if (targetDataType == CyNode.class || targetDataType == CyNetwork.class) {
				final Set selectedViews = proxy.getSelectedNodeViews(netView);
				
				final JMenuItem mi = new JMenuItem(REMOVE_ALL_FROM_SELECTED_NODES);
				mi.setEnabled(hasLockedValues(selectedViews));
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						clearAll(netView, selectedViews);
					}
				});
				rootMenu.add(mi);
			}
			
			if (targetDataType == CyEdge.class || targetDataType == CyNetwork.class) {
				final Set selectedViews = proxy.getSelectedEdgeViews(netView);
				
				final JMenuItem mi = new JMenuItem(REMOVE_ALL_FROM_SELECTED_EDGES);
				mi.setEnabled(hasLockedValues(selectedViews));
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						clearAll(netView, selectedViews);
					}
				});
				rootMenu.add(mi);
			}
			
			rootMenu.add(new JSeparator());
			
			{
				final JMenuItem mi = new JMenuItem(SET_TO_SELECTED_NODES);
				mi.setEnabled(!proxy.getSelectedNodeViews(netView).isEmpty());
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						showVizMapperPanel(CyNode.class);
					}
				});
				rootMenu.add(mi);
			}
			{
				final JMenuItem mi = new JMenuItem(SET_TO_SELECTED_EDGES);
				mi.setEnabled(!proxy.getSelectedEdgeViews(netView).isEmpty());
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						showVizMapperPanel(CyEdge.class);
					}
				});
				rootMenu.add(mi);
			}
			{
				final JMenuItem mi = new JMenuItem(SET_TO_NETWORK);
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						showVizMapperPanel(CyNetwork.class);
					}
				});
				rootMenu.add(mi);
			}
			
			return new CyMenuItem(rootMenu, 10000000.0f);
		}
		
		protected void clearAll(final CyNetworkView netView, final Set<View<? extends CyIdentifiable>> viewSet) {
			// TODO Move it to a Task?
			final Thread t = new Thread() {
				
				@Override
				public void run() {
					boolean changed = false;
					
					for (final View<? extends CyIdentifiable> view : viewSet) {
						for (VisualProperty<?> vp : vpSet) {
							if (view.isDirectlyLocked(vp)) {
								view.clearValueLock(vp);
								changed = true;
							}
						}
					}

					if (changed) {
						final VisualStyle style = proxy.getCurrentVisualStyle();
						style.apply(netView);
						netView.updateView();
					}
				};
			};
			t.start();
		}
		
		private void showVizMapperPanel(final Class<? extends CyIdentifiable> targetDataType) {
			final CySwingApplication swingApp = servicesUtil.get(CySwingApplication.class);
			final CytoPanel cytoPanel = swingApp.getCytoPanel(CytoPanelName.WEST);
			final int vizMapperIndex = cytoPanel.indexOfComponent(vizMapperMainPanel);
			
			if (vizMapperIndex > -1) {
				// Show the Control panel, if it's hidden
				if (cytoPanel.getState() == CytoPanelState.HIDE)
					cytoPanel.setState(CytoPanelState.DOCK);
				
				// Show the VizMapper panel
				if (cytoPanel.getSelectedIndex() != vizMapperIndex)
					cytoPanel.setSelectedIndex(vizMapperIndex);
				
				// Show the correspondent visual properties sheet
				final VisualPropertySheet vpSheet = vizMapperMainPanel.getVisualPropertySheet(targetDataType);
				
				if (vpSheet != null)
					vizMapperMainPanel.setSelectedVisualPropertySheet(vpSheet);
			}
		}
		
		private boolean hasLockedValues(final View<? extends CyIdentifiable> view) {
			for (VisualProperty<?> vp : vpSet) {
				if (view.isDirectlyLocked(vp))
					return true;
			}
			
			return false;
		}
		
		private boolean hasLockedValues(final Set<View<? extends CyIdentifiable>> viewSet) {
			for (final View<? extends CyIdentifiable> view : viewSet) {
				if (hasLockedValues(view))
					return true;
			}
			
			return false;
		}
	}
}
