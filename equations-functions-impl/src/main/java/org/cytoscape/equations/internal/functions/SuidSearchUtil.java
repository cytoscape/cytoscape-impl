package org.cytoscape.equations.internal.functions;

import java.util.Set;
import java.util.function.BiFunction;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;

public class SuidSearchUtil {
	
	public static <T,R> R lookup(CyServiceRegistrar registrar, Class<T> type, Long suid, BiFunction<CyNetwork,T,R> func) {
		final CyNetwork currentNetwork = registrar.getService(CyApplicationManager.class).getCurrentNetwork();
		if(currentNetwork != null) {
			T elem = getIdentifiable(type, currentNetwork, suid);
			if(elem != null) {
				return func.apply(currentNetwork, elem);
			}
		}
		
		// Either there is no current network, or the SUID was not in the current network, need to search all networks.
		Set<CyNetwork> allNetworks = registrar.getService(CyNetworkManager.class).getNetworkSet();
		for(CyNetwork network : allNetworks) {
			if(network != currentNetwork) {
				T elem = getIdentifiable(type, network, suid);
				if(elem != null) {
					return func.apply(network, elem);
				}
			}
		}
		
		return null;
	}
	
	private static <T> T getIdentifiable(Class<T> type, CyNetwork network, Long suid) {
		if(type == CyNode.class)
			return type.cast(network.getNode(suid));
		if(type == CyEdge.class)
			return type.cast(network.getEdge(suid));
		return null;
	}

}
