package org.cytoscape.app.internal;


import static org.mockito.Mockito.mock;

import java.util.Properties;

import org.cytoscape.app.internal.CyAppAdapterImpl;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
//import org.cytoscape.app.CyAppAdapterTest;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;

public class CyAppAdapterImplTest /*extends CyAppAdapterTest*/ {

	@Before
	public void setUp() {
		CyAppAdapter adapter = new CyAppAdapterImpl( 
			mock(CyApplicationManager.class),
			mock(CyEventHelper.class),
			mock(CyLayoutAlgorithmManager.class),
			mock(CyNetworkFactory.class),
			mock(CyNetworkManager.class),
			mock(CyNetworkViewFactory.class),
			mock(CyNetworkViewManager.class),
			mock(CyNetworkReaderManager.class),
			mock(CyNetworkViewWriterManager.class),
			(CyProperty<Properties>)mock(CyProperty.class),
			mock(CyPropertyReaderManager.class),
			mock(CyPropertyWriterManager.class),
			mock(CyRootNetworkManager.class),
			mock(CyServiceRegistrar.class),
			mock(CySessionManager.class),
			mock(CySessionReaderManager.class),
			mock(CySessionWriterManager.class),
			mock(CySwingApplication.class),
			mock(CyTableFactory.class),
			mock(CyTableManager.class),
			mock(CyTableReaderManager.class),
			mock(CyVersion.class),
//			mock(CyTableWriterManager.class),
			mock(DialogTaskManager.class),
			mock(PanelTaskManager.class),
			mock(SubmenuTaskManager.class),
			mock(PresentationWriterManager.class),
			mock(RenderingEngineManager.class),
			mock(TaskManager.class),
			mock(UndoSupport.class),
			mock(VisualMappingFunctionFactory.class),
			mock(VisualMappingFunctionFactory.class),
			mock(VisualMappingFunctionFactory.class),
			mock(VisualMappingManager.class),
			mock(VisualStyleFactory.class)
		    );
	}
	
}
