package org.cytoscape.view.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.internal.network.CyNetworkViewFactoryProviderImpl;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;

public class NetworkViewFactoryTestSupport {
	
	private CyServiceRegistrar serviceRegistrar;
	
	public NetworkViewFactoryTestSupport() {
		serviceRegistrar = mock(CyServiceRegistrar.class, withSettings().stubOnly());
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(mock(CyEventHelper.class));
	}
	
	public CyNetworkViewFactoryProvider getNetworkViewFactoryFactory() {
		return new CyNetworkViewFactoryProviderImpl(serviceRegistrar);
	}
	
	public CyNetworkViewFactory getNetworkViewFactory() {
		VisualProperty<NullDataType> rootVp = new NullVisualProperty("ROOT", "root");
		BasicVisualLexicon lexicon = new BasicVisualLexicon(rootVp);
		return getNetworkViewFactory(lexicon);
	}
	
	public CyNetworkViewFactory getNetworkViewFactory(VisualLexicon lexicon) {
		CyNetworkViewFactoryProvider networkViewFactoryFactory = getNetworkViewFactoryFactory();
		return networkViewFactoryFactory.createNetworkViewFactory(lexicon, "test.renderer");
	}
}
