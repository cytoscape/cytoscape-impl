package org.cytoscape.ding.internal;

import static org.cytoscape.property.CyProperty.SavePolicy.DO_NOT_SAVE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.io.internal.read.sif.SIFNetworkReader;
import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.layout.internal.CyLayoutAlgorithmManagerImpl;
import org.cytoscape.view.layout.internal.algorithms.GridNodeLayout;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl Performance (ding-impl-performance-debug)
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

public class PerfTest {

	public static void main(String[] args) {
		PerfTest pt = new PerfTest();
		//pt.runTestLoop();
		pt.visualizeNetworks();
	}

	@Mock protected TaskMonitor taskMonitor;
	@Mock protected CyServiceRegistrar serviceRegistrar;
	@Mock protected NetworkViewRenderer netViewRenderer;
	@Mock protected CyApplicationManager appMgr;
    protected CyNetworkFactory netFactory;
    protected CyNetworkViewFactory viewFactory;
    protected ReadUtils readUtil;
    protected CyNetworkManager netMgr;
    protected CyRootNetworkManager rootMgr;
    protected CyLayoutAlgorithmManager layouts;

	public PerfTest() {
		MockitoAnnotations.initMocks(this);

		layouts = getLayouts(); 

		NetworkTestSupport nts = new NetworkTestSupport();
		netFactory = nts.getNetworkFactory();
		netMgr = nts.getNetworkManager();
		rootMgr = nts.getRootNetworkFactory();

		NetworkViewTestSupport nvts = new NetworkViewTestSupport();
		viewFactory = nvts.getNetworkViewFactory();

		Properties properties = new Properties();
		CyProperty<Properties> cyProperties = new SimpleCyProperty<Properties>("Test",properties,Properties.class,DO_NOT_SAVE);	
		
		CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)")).thenReturn(cyProperties);
		
		readUtil = new ReadUtils(new StreamUtilImpl(serviceRegistrar));
		
		when(netViewRenderer.getNetworkViewFactory()).thenReturn(viewFactory);
		when(appMgr.getDefaultNetworkViewRenderer()).thenReturn(netViewRenderer);
		
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)")).thenReturn(cyProperties);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appMgr);
		when(serviceRegistrar.getService(NetworkViewRenderer.class)).thenReturn(netViewRenderer);
		when(serviceRegistrar.getService(CyNetworkFactory.class)).thenReturn(netFactory);
		when(serviceRegistrar.getService(CyNetworkViewFactory.class)).thenReturn(viewFactory);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netMgr);
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(rootMgr);
		when(serviceRegistrar.getService(CyLayoutAlgorithmManager.class)).thenReturn(layouts);
	}

	private SIFNetworkReader readFile(String file) throws Exception {
		InputStream is = getClass().getResource("/testData/sif/" + file).openStream();
		SIFNetworkReader snvp = new SIFNetworkReader(is, serviceRegistrar);
		new TaskIterator(snvp);
		snvp.run(taskMonitor);

		is.close();

		return snvp;
	}

	private CyNetwork[] getNetworks(String file) throws Exception {
		final SIFNetworkReader snvp = readFile(file);
		return snvp.getNetworks();
	}

	private CyNetworkView[] getViews(String file) throws Exception {
		final SIFNetworkReader snvp = readFile(file);
		final CyNetwork[] networks = snvp.getNetworks(); 
		final CyNetworkView[] views = new CyNetworkView[networks.length];
		int i = 0;
		for(CyNetwork network: networks) {
			views[i] = snvp.buildCyNetworkView(network);
			views[i].fitContent();
			i++;
		}
		
		return views;
	}

	public void runTestLoop() {
		try {
			networkAndViewPerf("A200-200.sif");
			networkAndViewPerf("A50-100.sif");
			networkAndViewPerf("A50-50.sif");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void justNetworkPerf(String name) throws Exception {
		long start = System.currentTimeMillis();
		CyNetwork[] nets = getNetworks(name);
		long end = System.currentTimeMillis();
		System.out.println("LOADING SIF file (" + name + ") no view duration: " + (end - start));
	}

	private void networkAndViewPerf(String name) throws Exception {
		long start = System.currentTimeMillis();
		CyNetworkView[] views = getViews(name);
		for ( CyNetworkView view : views )
			view.updateView();
		long end = System.currentTimeMillis();
		System.out.println("LOADING SIF file (" + name + ") with view duration: " + (end - start));
	}

	private void visualizeNetworks() {
		try {
		CyNetworkView[] views = getViews("A50-50.sif");
		JFrame frame = new JFrame();
		frame.setPreferredSize(new Dimension(800,800));
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(800,800));
		panel.setBackground(Color.blue);
		InnerCanvas jc = ((DGraphView)(views[0])).getCanvas();
		jc.setVisible(true);
		jc.setPreferredSize(new Dimension(800,800));
		panel.add( jc );
		frame.getContentPane().add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		} catch (Exception e) { e.printStackTrace(); }
	}

	private CyLayoutAlgorithmManager getLayouts() {
		Properties p = new Properties();
		CyProperty<Properties> props = new SimpleCyProperty<Properties>("Test",p,Properties.class,DO_NOT_SAVE);
		CyLayoutAlgorithm gridNodeLayout = new GridNodeLayout(null);
		CyLayoutAlgorithmManagerImpl cyLayouts = new CyLayoutAlgorithmManagerImpl(null, props, gridNodeLayout);
		return cyLayouts;
	}
}
