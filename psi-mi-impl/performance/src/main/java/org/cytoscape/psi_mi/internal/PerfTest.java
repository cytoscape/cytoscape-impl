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
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import static org.mockito.Mockito.*;

import org.cytoscape.psi_mi.internal.plugin.PsiMiTabReader;

public class PerfTest {

	CyLayoutAlgorithmManager layouts;
	CyLayoutAlgorithm layout;
	TaskMonitor taskMonitor;
	
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
        layout = mock(CyLayoutAlgorithm.class);
        when(layout.createTaskIterator()).thenReturn(new TaskIterator(new SimpleTask()));

        layouts = mock(CyLayoutAlgorithmManager.class);
        when(layouts.getLayout(anyString())).thenReturn(layout);

		taskMonitor = mock(TaskMonitor.class);

		Properties properties = new Properties();
		properties.setProperty("viewThreshold", "1000000");
		props = new SimpleCyProperty<Properties>("Test", properties, Properties.class, SavePolicy.DO_NOT_SAVE);

		networkFactory = new NetworkTestSupport().getNetworkFactory();
		networkViewFactory = new NetworkViewTestSupport().getNetworkViewFactory();
	}

	private void testPsiMiTabReader() throws Exception {
		long start = System.currentTimeMillis();
		//final CyNetworkReader reader = createReader("BIOGRID-ORGANISM-Saccharomyces_cerevisiae-3.1.83.mitab");
		final CyNetworkReader reader = createReader("BIOGRID-ORGANISM-Schizosaccharomyces_pombe-3.1.83.mitab");
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

    static class SimpleTask extends AbstractTask {
        public void run(final TaskMonitor tm) { }
    }


}
