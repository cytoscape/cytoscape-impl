package org.cytoscape.io.read.sif; 


import static org.cytoscape.property.CyProperty.SavePolicy.DO_NOT_SAVE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.read.sif.SIFNetworkReader;
import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.mockito.Mockito;


public class PerfTest {

	public static void main(String[] args) {
		new PerfTest().runTestLoop();
	}

    protected static final int DEF_THRESHOLD = 10000;
    
    protected TaskMonitor taskMonitor;
    protected CyNetworkFactory netFactory;
    protected CyNetworkViewFactory viewFactory;
    protected ReadUtils readUtil;
    protected CyLayoutAlgorithmManager layouts;
    protected CyRootNetworkManager rootMgr;
    protected CyNetworkManager netMgr;

	private Properties properties;

	public PerfTest() {
		taskMonitor = mock(TaskMonitor.class);

		CyLayoutAlgorithm def = mock(CyLayoutAlgorithm.class);
		when(def.createTaskIterator(Mockito.any(CyNetworkView.class), Mockito.any(Object.class), Mockito.anySet(), Mockito.any(String.class))).thenReturn(new TaskIterator(new SimpleTask()));

		layouts = mock(CyLayoutAlgorithmManager.class);
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
	}

	private  SIFNetworkReader readFile(String file) throws Exception {
		InputStream is = getClass().getResource("/testData/sif/" + file).openStream();
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		SIFNetworkReader snvp = new SIFNetworkReader(is, layouts, viewFactory, netFactory, eventHelper, netMgr, rootMgr);
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
