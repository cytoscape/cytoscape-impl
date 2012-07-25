package org.cytoscape.ding.internal;


import static org.cytoscape.property.CyProperty.SavePolicy.DO_NOT_SAVE;
import static org.mockito.Mockito.mock;

import java.awt.Color;
import java.awt.Dimension;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.read.sif.SIFNetworkReader;
import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.layout.internal.CyLayoutsImpl;
import org.cytoscape.view.layout.internal.algorithms.GridNodeLayout;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;


public class PerfTest {

	public static void main(String[] args) {
		PerfTest pt = new PerfTest();
		//pt.runTestLoop();
		pt.visualizeNetworks();
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

		Properties properties = new Properties();
		CyProperty<Properties> cyProperties = new SimpleCyProperty<Properties>("Test",properties,Properties.class,DO_NOT_SAVE);	
		readUtil = new ReadUtils(new StreamUtilImpl(cyProperties));
	}

	private  SIFNetworkReader readFile(String file) throws Exception {
		InputStream is = getClass().getResource("/testData/sif/" + file).openStream();
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		SIFNetworkReader snvp = new SIFNetworkReader(is, layouts, viewFactory, netFactory, eventHelper);
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
		CyLayoutsImpl cyLayouts = new CyLayoutsImpl(props, gridNodeLayout);
		return cyLayouts;
	}
}
