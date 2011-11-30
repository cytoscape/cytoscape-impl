
package org.cytoscape.ding;

import static org.mockito.Mockito.mock;

import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.DVisualLexicon;
import org.cytoscape.ding.impl.DingViewModelFactory;
import org.cytoscape.ding.impl.ViewTaskFactoryListener;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.internal.rtree.RTreeFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.cytoscape.work.undo.UndoSupport;

public class NetworkViewTestSupport extends NetworkTestSupport {

	protected CyNetworkViewFactory viewFactory;

	public NetworkViewTestSupport() {
		
        DVisualLexicon dVisualLexicon = new DVisualLexicon(mock(CustomGraphicsManager.class));

		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		TableTestSupport tableTestSupport = new TableTestSupport();

        viewFactory = new DingViewModelFactory(tableTestSupport.getTableFactory(),
		                                       networkTestSupport.getRootNetworkFactory(),
		                                       mock(UndoSupport.class),
		                                       new RTreeFactory(),
		                                       dVisualLexicon,
		                                       mock(DialogTaskManager.class),
		                                       mock(SubmenuTaskManager.class),
		                                       mock(CyServiceRegistrar.class),
		                                       mock(CyNetworkTableManager.class),
		                                       mock(CyEventHelper.class),
		                                       mock(ViewTaskFactoryListener.class),
											   mock(AnnotationFactoryManager.class));
	}
	
	public CyNetworkView getNetworkView() {
		return viewFactory.createNetworkView( getNetwork() );
	}

	public CyNetworkViewFactory getNetworkViewFactory() {
		return viewFactory;
	}
}


