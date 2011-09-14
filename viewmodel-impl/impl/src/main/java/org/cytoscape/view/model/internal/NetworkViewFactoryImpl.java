package org.cytoscape.view.model.internal;


import java.util.Properties;

import org.cytoscape.di.util.DIUtil;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;


public class NetworkViewFactoryImpl implements CyNetworkViewFactory {
	private final CyEventHelper eventHelper;
	private final CyServiceRegistrar registrar;

	/**
	 * For injection, use this constructor.
	 * 
	 * @param eventHelper
	 */
	public NetworkViewFactoryImpl(final CyEventHelper eventHelper, final CyServiceRegistrar registrar) {
		if (eventHelper == null)
			throw new NullPointerException("CyEventHelper is null");
		this.eventHelper = DIUtil.stripProxy(eventHelper);

		if (registrar == null)
			throw new NullPointerException("CyServiceRegistrar is null");
		this.registrar = DIUtil.stripProxy(registrar);
	}

	@Override
	public CyNetworkView getNetworkView(final CyNetwork network) {
		return getNetworkView(network, true);
	}

	@Override
	// FIXME useThreshold is no longer in use!
	public CyNetworkView getNetworkView(final CyNetwork network, final Boolean useThreshold) {
		final CyNetworkView view = new NetworkViewImpl(network, eventHelper);
		registrar.registerAllServices(view, new Properties());

		return view;
	}
}
