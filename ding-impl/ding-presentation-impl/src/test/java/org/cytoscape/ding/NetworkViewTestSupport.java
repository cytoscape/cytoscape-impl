
package org.cytoscape.ding;

import java.util.Properties;

import org.cytoscape.property.BasicCyProperty;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.spacial.internal.rtree.RTreeFactory;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.TaskManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.ding.impl.DingViewModelFactory;
import org.cytoscape.ding.impl.DVisualLexicon;

import static org.mockito.Mockito.*;

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
		                                       mock(TaskManager.class),
		                                       mock(CyServiceRegistrar.class),
		                                       mock(CyNetworkTableManager.class),
		                                       mock(CyEventHelper.class));
	}
	
	public CyNetworkView getNetworkView() {
		return viewFactory.getNetworkView( getNetwork() );
	}

	public CyNetworkViewFactory getNetworkViewFactory() {
		return viewFactory;
	}
}


