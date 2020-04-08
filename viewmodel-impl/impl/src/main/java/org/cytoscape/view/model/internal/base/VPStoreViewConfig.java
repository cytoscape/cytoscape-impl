package org.cytoscape.view.model.internal.base;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import org.cytoscape.view.model.CyNetworkViewFactoryConfig;
import org.cytoscape.view.model.VisualProperty;

/**
 * Internal interface to configure the VPStore. 
 */
public interface VPStoreViewConfig extends CyNetworkViewFactoryConfig {

	Set<VisualProperty<?>> getNoClearVPs();
	
	Collection<VisualProperty<?>> getTrackedVPs(Class<?> type);

	Predicate getPredicate(Object key);

	boolean isTrackedKey(Object key);
	
	Set<Object> getTrackingKeys(VisualProperty<?> vp);

	boolean isTracked(VisualProperty<?> vp);
	
}
