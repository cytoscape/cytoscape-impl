
package org.cytoscape.ding;

import static org.mockito.Mockito.mock;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingViewModelFactory;
import org.cytoscape.ding.impl.ViewTaskFactoryListener;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.internal.rtree.RTreeFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.UndoSupport;

public class NetworkViewTestSupport extends NetworkTestSupport {

	protected CyNetworkViewFactory viewFactory;

	public NetworkViewTestSupport() {
		super();
		
        DVisualLexicon dVisualLexicon = new DVisualLexicon(mock(CustomGraphicsManager.class));

		TableTestSupport tableTestSupport = new TableTestSupport();

        viewFactory = new DingViewModelFactory(tableTestSupport.getTableFactory(),
		                                       getRootNetworkFactory(),
		                                       mock(UndoSupport.class),
		                                       new RTreeFactory(),
		                                       dVisualLexicon,
		                                       mock(DialogTaskManager.class),
		                                       mock(CyServiceRegistrar.class),
		                                       networkTableMgr,
		                                       mock(CyEventHelper.class),
		                                       mock(ViewTaskFactoryListener.class),
											   mock(AnnotationFactoryManager.class),
											   mock(DingGraphLOD.class), mock(VisualMappingManager.class),
											   mock(CyNetworkViewManager.class));
	}
	
	public CyNetworkView getNetworkView() {
		return viewFactory.createNetworkView( getNetwork() );
	}

	public CyNetworkViewFactory getNetworkViewFactory() {
		return viewFactory;
	}
}


