package org.cytoscape.ding.internal;



import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import java.util.List;
import java.util.Properties;

import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.io.internal.read.sif.SIFNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.view.layout.internal.CyLayoutsImpl;
import org.cytoscape.view.layout.internal.algorithms.GridNodeLayout;


public class PerfTest {


	public static void main(String[] args) {
		new PerfTest().runTestLoop();
	}

    protected TaskMonitor taskMonitor;
    protected CyNetworkFactory netFactory;
    protected CyNetworkViewFactory viewFactory;
    protected ReadUtils readUtil;
    protected CyLayoutAlgorithmManager layouts;

	public PerfTest() {
		taskMonitor = mock(TaskMonitor.class);

		layouts = getLayouts(); 

		NetworkTestSupport nts = new NetworkTestSupport();
		netFactory = nts.getNetworkFactory();

		NetworkViewTestSupport nvts = new NetworkViewTestSupport();
	
		viewFactory = nvts.getNetworkViewFactory();

		readUtil = new ReadUtils(new StreamUtilImpl());
	}

	private  SIFNetworkReader readFile(String file) throws Exception {
		InputStream is = getClass().getResource("/testData/sif/" + file).openStream();
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		SIFNetworkReader snvp = new SIFNetworkReader(is, layouts, viewFactory, netFactory, eventHelper);
		new TaskIterator(snvp);
		snvp.run(taskMonitor);

		return snvp;
	}

	private CyNetwork[] getNetworks(String file) throws Exception {
		final SIFNetworkReader snvp = readFile(file);
		return snvp.getCyNetworks();
	}

	private CyNetworkView[] getViews(String file) throws Exception {
		final SIFNetworkReader snvp = readFile(file);
		final CyNetwork[] networks = snvp.getCyNetworks(); 
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
		long end = System.currentTimeMillis();
		System.out.println("LOADING SIF file (" + name + ") with view duration: " + (end - start));
	}

	private CyLayoutAlgorithmManager getLayouts() {
		Properties p = new Properties();
		CyProperty<Properties> props = new SimpleCyProperty(p,CyProperty.SavePolicy.DO_NOT_SAVE);
		CyLayoutsImpl cyLayouts = new CyLayoutsImpl(props);
		CyLayoutAlgorithm gridNodeLayout = new GridNodeLayout(mock(UndoSupport.class));
		cyLayouts.addLayout(gridNodeLayout,p);
		return cyLayouts;
	}
}
