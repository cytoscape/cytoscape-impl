package org.cytoscape.io.read.sif;

/*
 * #%L
 * Cytoscape IO Impl Performance (io-impl-performance)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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



import static org.cytoscape.property.CyProperty.SavePolicy.DO_NOT_SAVE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.internal.read.sif.SIFNetworkReader;
import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


public class PerfTest {

	public static void main(String[] args) {
		new PerfTest().runTestLoop();
	}

    protected static final int DEF_THRESHOLD = 10000;
    
    @Mock protected TaskMonitor taskMonitor;
    @Mock protected CyApplicationManager cyApplicationManager;
    @Mock protected NetworkViewRenderer netViewRenderer;
    @Mock protected CyLayoutAlgorithmManager layouts;
    protected CyNetworkFactory netFactory;
    protected CyNetworkViewFactory viewFactory;
    protected ReadUtils readUtil;
    protected CyRootNetworkManager rootMgr;
    protected CyNetworkManager netMgr;
	
	private Properties properties;

	public PerfTest() {
		MockitoAnnotations.initMocks(this);

		CyLayoutAlgorithm def = mock(CyLayoutAlgorithm.class);
		when(def.createTaskIterator(Mockito.any(CyNetworkView.class), Mockito.any(Object.class), Mockito.anySet(), Mockito.any(String.class))).thenReturn(new TaskIterator(new SimpleTask()));

		when(layouts.getDefaultLayout()).thenReturn(def);

		NetworkTestSupport nts = new NetworkTestSupport();
		netFactory = nts.getNetworkFactory();
		rootMgr = nts.getRootNetworkFactory();
		netMgr = nts.getNetworkManager();
		
		properties = new Properties();
		CyProperty<Properties> cyProperties = new SimpleCyProperty<Properties>("Test", properties, Properties.class, DO_NOT_SAVE);	
		NetworkViewTestSupport nvts = new NetworkViewTestSupport();
		setViewThreshold(DEF_THRESHOLD);
	
		viewFactory = nvts.getNetworkViewFactory();

		readUtil = new ReadUtils(new StreamUtilImpl(cyProperties));
		
		when(netViewRenderer.getNetworkViewFactory()).thenReturn(viewFactory);
		when(cyApplicationManager.getDefaultNetworkViewRenderer()).thenReturn(netViewRenderer);
	}

	private  SIFNetworkReader readFile(String file) throws Exception {
		InputStream is = getClass().getResource("/testData/sif/" + file).openStream();
		SIFNetworkReader snvp = new SIFNetworkReader(is, layouts, cyApplicationManager, netFactory, netMgr, rootMgr);
		new TaskIterator(snvp);
		snvp.run(taskMonitor);

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
			i++;
		}
		
		return views;
	}

	public void runTestLoop() {
		try {
			testNetworkCapability("biogrid-fly.sif");
//			justNetworkPerf("A200-200.sif");
//			justNetworkPerf("A50-100.sif");
//			justNetworkPerf("A50-50.sif");
			//networkAndViewPerf("A200-200.sif");
			//networkAndViewPerf("A50-100.sif");
			//networkAndViewPerf("A50-50.sif");
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
		long end = System.currentTimeMillis();
		System.out.println("LOADING SIF file (" + name + ") with view duration: " + (end - start));
	}

	static class SimpleTask extends AbstractTask {
		public void run(final TaskMonitor tm) { }
	}

	protected void setViewThreshold(int threshold) {
		properties.setProperty("viewThreshold", String.valueOf(threshold));
	}

	private void testNetworkCapability(String name) throws Exception {
		CyNetwork[] nets = getNetworks(name);
		CyNetwork net = nets[0];
		List<CyNode> nodeList = net.getNodeList();

		long start = System.currentTimeMillis();
		for ( CyNode n : nodeList ) {
			List<CyEdge> edges = net.getAdjacentEdgeList(n,CyEdge.Type.ANY);
		}
		long end = System.currentTimeMillis();
		System.out.println("Getting all adjacent edges: " + (end - start));
/*
		start = System.currentTimeMillis();
		for ( CyNode n : nodeList ) {
			for ( CyNode nn : nodeList ) {
				List<CyEdge> edges = net.getConnectingEdgeList(n,nn,CyEdge.Type.ANY);
			}
		}
		end = System.currentTimeMillis();
		System.out.println("Getting all connecting edges: " + (end - start));
		*/

		start = System.currentTimeMillis();
		for ( CyNode n : nodeList ) {
			List<CyNode> nodes = net.getNeighborList(n,CyEdge.Type.ANY);
		}
		end = System.currentTimeMillis();
		System.out.println("Getting all neighbor nodes: " + (end - start));

        // create subnetworks
        CyRootNetwork root = rootMgr.getRootNetwork(net);
        int i = 0;
        for ( CyNode n : net.getNodeList() ) {
            if ( i++ > 1000 ) break;
            List<CyNode> nl = net.getNeighborList(n,CyEdge.Type.ANY);
            Set<CyEdge> es = new HashSet<CyEdge>();
            for ( CyNode nn : nl ) {
                List<CyEdge> ee = net.getConnectingEdgeList(n,nn,CyEdge.Type.ANY);
                es.addAll(ee);
            }
            root.addSubNetwork(nl,es);
        }

	}

}
