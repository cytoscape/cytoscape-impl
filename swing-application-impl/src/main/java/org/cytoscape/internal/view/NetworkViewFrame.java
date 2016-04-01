package org.cytoscape.internal.view;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JToolBar;

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;

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


@SuppressWarnings("serial")
public class NetworkViewFrame extends JFrame {

	private final JToolBar toolBar;
	private final NetworkViewContainer networkViewContainer;
	private final JRootPane containerRootPane;
	
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewFrame(final NetworkViewContainer vc, final GraphicsConfiguration gc,
			final JToolBar toolBar, final CyServiceRegistrar serviceRegistrar) {
		super(ViewUtil.getTitle(vc.getNetworkView()), gc);
		
		this.toolBar = toolBar;
		this.networkViewContainer = vc;
		this.serviceRegistrar = serviceRegistrar;
		containerRootPane = vc.getRootPane();
		
		init();
	}
	
	private void init() {
		setName("Frame." + networkViewContainer.getName());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		// To prevent error this error when using multiple monitors:
		// "IllegalArgumentException: adding a container to a container on a different GraphicsDevice".
		networkViewContainer.setRootPane(new JRootPane());
		
		getContentPane().add(containerRootPane, BorderLayout.CENTER);
		
		if (toolBar != null) {
			toolBar.setFloatable(false);
			
			final Properties props = (Properties) 
					serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
			setToolBarVisible(props.getProperty("showDetachedViewToolBars", "true").equalsIgnoreCase("true"));
			
			getContentPane().add(toolBar, BorderLayout.NORTH);
		}
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				networkViewContainer.update();
				networkViewContainer.updateViewSize();
			}
		});
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				networkViewContainer.updateViewSize();
			};
		});
		
		update();
	}

	public JToolBar getToolBar() {
		return toolBar;
	}
	
	public void setToolBarVisible(final boolean b) {
		getToolBar().setVisible(b);
	}
	
	protected NetworkViewContainer getNetworkViewContainer() {
		return networkViewContainer;
	}
	
	/**
	 * Use this method to get the original NetworkViewContainer's JRootPane
	 * instead of {@link #getNetworkViewContainer()#getRootPane()}.
	 * @return The JRootPane that contains the rendered view.
	 */
	protected JRootPane getContainerRootPane() {
		return containerRootPane;
	}
	
	protected RenderingEngine<CyNetwork> getRenderingEngine() {
		return networkViewContainer.getRenderingEngine();
	}
	
	public CyNetworkView getNetworkView() {
		return networkViewContainer.getNetworkView();
	}
	
	public void update() {
		getNetworkViewContainer().update();
	}
	
	@Override
	public void dispose() {
		// To prevent error this error when using multiple monitors:
		// "IllegalArgumentException: adding a container to a container on a different GraphicsDevice".
		getContentPane().removeAll();
		remove(getRootPane());
		
		super.dispose();
		
		networkViewContainer.setRootPane(containerRootPane);
	}
	
	@Override
	public String toString() {
		return "NetworkViewFrame: " + getNetworkView();
	}
}
