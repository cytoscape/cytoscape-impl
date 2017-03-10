package org.cytoscape.internal.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.slf4j.Logger;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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


public final class ViewUtil {

	public static final String CY_PROPERTY_NAME = "(cyPropertyName=cytoscape3.props)";
	public static final String SHOW_NODE_EDGE_COUNT_KEY = "showNodeEdgeCount";
	public static final String SHOW_NETWORK_PROVENANCE_HIERARCHY_KEY = "showNetworkProvenanceHierarchy";
	public static final String SHOW_NETWORK_TOOL_BAR = "showNetworkToolBar";
	
	public static final String PARENT_NETWORK_COLUMN = "__parentNetwork.SUID";
	
	public static String getName(final CyNetwork network) {
		String name = "";
		
		try {
			name = network.getRow(network).get(CyNetwork.NAME, String.class);
		} catch (Exception e) {
		}
		
		if (name == null || name.trim().isEmpty())
			name = "? (SUID: " + network.getSUID() + ")";
		
		return name;
	}
	
	public static String getTitle(final CyNetworkView view) {
		String title = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		
		if (title == null || title.trim().isEmpty())
			title = getName(view.getModel());
		
		return title;
	}
	
	public static int getHiddenNodeCount(final CyNetworkView view) {
		int count = 0;
		
		if (view != null) {
			for (View<CyNode> nv : view.getNodeViews()) {
				if (nv.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE) == Boolean.FALSE)
					count++;
			}
		}
		
		return count;
	}
	
	public static int getHiddenEdgeCount(final CyNetworkView view) {
		int count = 0;
		
		if (view != null) {
			for (View<CyEdge> ev : view.getEdgeViews()) {
				if (ev.getVisualProperty(BasicVisualLexicon.EDGE_VISIBLE) == Boolean.FALSE)
					count++;
			}
		}
		
		return count;
	}
	
	public static String createUniqueKey(final CyNetworkView view) {
		return "__CyNetworkView_" + view.getSUID();
	}
	
	public static String createUniqueKey(final CyNetwork net) {
		return "__CyNetwork_" + (net != null ? net.getSUID() : "null");
	}
	
	public static CySubNetwork getParent(final CySubNetwork net, final CyServiceRegistrar serviceRegistrar) {
		final CyTable hiddenTable = net.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		final CyRow row = hiddenTable != null ? hiddenTable.getRow(net.getSUID()) : null;
		final Long suid = row != null ? row.get(PARENT_NETWORK_COLUMN, Long.class) : null;
		
		if (suid != null) {
			final CyNetwork parent = serviceRegistrar.getService(CyNetworkManager.class).getNetwork(suid);
			
			if (parent instanceof CySubNetwork)
				return (CySubNetwork) parent;
		}
		
		return null;
	}
	
	public static void styleToolBarButton(final AbstractButton btn, final Font font) {
		styleToolBarButton(btn, font, true);
	}
	
	public static void styleToolBarButton(final AbstractButton btn, final Font font, final boolean addPadding) {
		btn.setFont(font);
		btn.setBorder(null);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setFocusable(false);
		
		if (addPadding) {
			final Dimension d = btn.getPreferredSize();
			btn.setPreferredSize(new Dimension(d.width + 10, d.height + 5));
		}
	}
	
	public static String getViewProperty(final String key, final CyServiceRegistrar serviceRegistrar) {
		return getViewProperty(key, null, serviceRegistrar);
	}
	
	@SuppressWarnings("unchecked")
	public static String getViewProperty(final String key, final String defaultValue,
			final CyServiceRegistrar serviceRegistrar) {
		final CyProperty<Properties> cyProps = serviceRegistrar.getService(CyProperty.class, CY_PROPERTY_NAME);

		return cyProps.getProperties().getProperty(key, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public static void setViewProperty(final String key, final String value,
			final CyServiceRegistrar serviceRegistrar) {
		final CyProperty<Properties> cyProps = serviceRegistrar.getService(CyProperty.class, CY_PROPERTY_NAME);
		cyProps.getProperties().setProperty(key, value);
	}
	
	public static Window getWindowAncestor(final ActionEvent evt, final CySwingApplication swingApplication) {
		Window window = null;
		
		if (evt.getSource() instanceof JMenuItem) {
			if (swingApplication.getJMenuBar() != null)
				window = SwingUtilities.getWindowAncestor(swingApplication.getJMenuBar());
		} else if (evt.getSource() instanceof Component) {
			window = SwingUtilities.getWindowAncestor((Component) evt.getSource());
		}
		
		if (window == null)
			window = swingApplication.getJFrame();
		
		return window;
	}
	
	/**
	 * Utility method that invokes the code in Runnable.run on the AWT Event Dispatch Thread.
	 * @param runnable
	 */
	public static void invokeOnEDT(final Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}
	
	public static void invokeOnEDTAndWait(final Runnable runnable) {
		invokeOnEDTAndWait(runnable, null);
	}
	
	public static void invokeOnEDTAndWait(final Runnable runnable, final Logger logger) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (Exception e) {
				if (logger != null)
					logger.error("Unexpected error", e);
				else
					e.printStackTrace();
			}
		}
	}
	
	private ViewUtil() {
	}
}
