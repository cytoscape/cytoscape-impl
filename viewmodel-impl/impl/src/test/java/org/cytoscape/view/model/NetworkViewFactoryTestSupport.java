package org.cytoscape.view.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.internal.model.CyNetworkViewFactoryFactoryImpl;
import org.cytoscape.view.model.internal.model.spacial.SpacialIndex2DFactoryImpl;
import org.cytoscape.view.model.spacial.SpacialIndex2DFactory;
import org.cytoscape.view.presentation.CyNetworkViewFactoryFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;

public class NetworkViewFactoryTestSupport {
	
	private CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class, withSettings().stubOnly());
	
	public CyNetworkViewFactoryFactory getNetworkViewFactoryFactory() {
		return new CyNetworkViewFactoryFactoryImpl(serviceRegistrar);
	}
	
	public CyNetworkViewFactory getNetworkViewFactory() {
		VisualProperty<NullDataType> rootVp = new NullVisualProperty("ROOT", "root");
		BasicVisualLexicon lexicon = new BasicVisualLexicon(rootVp);
		CyNetworkViewFactoryFactory networkViewFactoryFactory = getNetworkViewFactoryFactory();
		return networkViewFactoryFactory.createNetworkViewFactory(lexicon, "test.renderer");
	}

	public SpacialIndex2DFactory getSpacialIndex2DFactory() {
		return new SpacialIndex2DFactoryImpl();
	}
	
}
