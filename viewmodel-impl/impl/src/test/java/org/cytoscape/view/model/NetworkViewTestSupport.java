
package org.cytoscape.view.model;

import java.util.Properties;

import org.cytoscape.property.BasicCyProperty;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.internal.NetworkViewFactoryImpl;
import org.cytoscape.service.util.CyServiceRegistrar;

import static org.mockito.Mockito.*;

public class NetworkViewTestSupport extends NetworkTestSupport {

	protected CyNetworkViewFactory viewFactory;

	public NetworkViewTestSupport() {
		this(new BasicCyProperty(new Properties(), SavePolicy.DO_NOT_SAVE));
	}

	public NetworkViewTestSupport(CyProperty<Properties> properties) {
		viewFactory = new NetworkViewFactoryImpl( eventHelper, mock(CyServiceRegistrar.class));
	}
	
	public CyNetworkView getNetworkView() {
		return viewFactory.getNetworkView( getNetwork() );
	}

	public CyNetworkViewFactory getNetworkViewFactory() {
		return viewFactory;
	}
}


