package org.cytoscape.internal.util;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

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
		return view.getSUID() + "__" + view.getRendererId() + "__" + view.hashCode();
	}
	
	public static CySubNetwork getParent(final CySubNetwork net, final CyServiceRegistrar serviceRegistrar) {
		final CyTable hiddenTable = net.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		final Long suid = hiddenTable.getRow(net.getSUID()).get(PARENT_NETWORK_COLUMN, Long.class);
		
		if (suid != null) {
			final CyNetwork parent = serviceRegistrar.getService(CyNetworkManager.class).getNetwork(suid);
			
			if (parent instanceof CySubNetwork)
				return (CySubNetwork) parent;
		}
		
		return null;
	}
	
	public static void styleToolBarButton(final AbstractButton btn, final Font font) {
		btn.setFont(font);
		btn.setBorder(null);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setFocusable(false);
		
		final Dimension d = btn.getPreferredSize();
		btn.setPreferredSize(new Dimension(d.width + 10, d.height + 5));
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
	
	private ViewUtil() {
	}
}
