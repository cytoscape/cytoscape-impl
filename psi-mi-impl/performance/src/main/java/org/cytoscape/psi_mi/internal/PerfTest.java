package org.cytoscape.psi_mi.internal;

/*
 * #%L
 * Cytoscape PSI-MI Impl Performance (psi-mi-impl-performance)
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


import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.psi_mi.internal.plugin.PsiMiTabReader;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.mockito.Mockito;

public class PerfTest {

	CyLayoutAlgorithmManager layouts;
	CyLayoutAlgorithm layout;
	TaskMonitor taskMonitor;
	
	CyProperty<Properties> props;

	private CyNetworkFactory networkFactory;
	private CyNetworkViewFactory networkViewFactory;
	private CyRootNetworkManager cyRootNetworkManager;
	private CyNetworkManager cyNetworkManager;
	
	public static void main(String[] args) {
		try {
			PerfTest pt = new PerfTest();
			pt.testPsiMiTabReader();
		} catch (Exception e) { e.printStackTrace(); }
	}

	public PerfTest() {
        layout = mock(CyLayoutAlgorithm.class);
		when(layout.createTaskIterator(Mockito.any(CyNetworkView.class), Mockito.any(Object.class), Mockito.anySet(), Mockito.any(String.class))).thenReturn(new TaskIterator(new SimpleTask()));

        layouts = mock(CyLayoutAlgorithmManager.class);
        when(layouts.getLayout(anyString())).thenReturn(layout);

		taskMonitor = mock(TaskMonitor.class);

		Properties properties = new Properties();
		properties.setProperty("viewThreshold", "1000000");
		props = new SimpleCyProperty<Properties>("Test", properties, Properties.class, SavePolicy.DO_NOT_SAVE);

		networkFactory = new NetworkTestSupport().getNetworkFactory();
		networkViewFactory = new NetworkViewTestSupport().getNetworkViewFactory();
		cyNetworkManager = new NetworkViewTestSupport().getNetworkManager();
		cyRootNetworkManager = new NetworkViewTestSupport().getRootNetworkFactory();
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
		PsiMiTabReader reader = new PsiMiTabReader(is, networkViewFactory, networkFactory, layouts, props, cyNetworkManager, cyRootNetworkManager);
		reader.setTaskIterator(new TaskIterator(reader));
		return reader;
	}

    static class SimpleTask extends AbstractTask {
        public void run(final TaskMonitor tm) { }
    }


}
