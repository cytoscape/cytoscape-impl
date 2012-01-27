package org.cytoscape.psi_mi.internal;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;

import org.cytoscape.psi_mi.internal.plugin.PsiMiTabReader;

public class PerfTest {

	@Mock
	CyLayoutAlgorithmManager layouts;
	@Mock
	CyLayoutAlgorithm layout;
	@Mock
	TaskMonitor taskMonitor;
	@Mock
	Task task;
	
	@Mock
	CyProperty<Properties> props;

	private CyNetworkFactory networkFactory;
	private CyNetworkViewFactory networkViewFactory;

	public static void main(String[] args) {
		try {
			PerfTest pt = new PerfTest();
			pt.testPsiMiTabReader();
		} catch (Exception e) { e.printStackTrace(); }
	}

	public PerfTest() {
		MockitoAnnotations.initMocks(this);

		when(layouts.getDefaultLayout()).thenReturn(layout);
		when(layout.createTaskIterator()).thenReturn(new TaskIterator(task));
		
		networkFactory = new NetworkTestSupport().getNetworkFactory();
		networkViewFactory = new NetworkViewTestSupport().getNetworkViewFactory();
	}

	private void testPsiMiTabReader() throws Exception {
		long start = System.currentTimeMillis();
		final CyNetworkReader reader = createReader("BIOGRID-ORGANISM-Saccharomyces_cerevisiae-3.1.83.mitab");
		reader.run(taskMonitor);
		CyNetwork[] networks = reader.getNetworks();
		long end = System.currentTimeMillis();
		System.out.println("read networks: " + (end - start));

		start = System.currentTimeMillis();
        for(CyNetwork network: networks) {
            reader.buildCyNetworkView(network);
        }
		end = System.currentTimeMillis();
		System.out.println("create views : " + (end - start));
	}
	
	private CyNetworkReader createReader(String file) throws IOException {
		final InputStream is = getClass().getResource("/testData/mitab/" + file).openStream(); 
		PsiMiTabReader reader = new PsiMiTabReader(is, networkViewFactory, networkFactory, layouts, props);
		reader.setTaskIterator(new TaskIterator(reader));
		return reader;
	}

}
