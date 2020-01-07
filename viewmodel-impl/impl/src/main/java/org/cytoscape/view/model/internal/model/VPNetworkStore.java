package org.cytoscape.view.model.internal.model;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_SCALE_FACTOR;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.CyNetworkViewFactoryConfigImpl;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class VPNetworkStore extends VPStore {

	// If you add more special case network properties make sure to update the JUnit test.
	public static final Set<VisualProperty<?>> NETWORK_PROPS = HashSet.of(
		NETWORK_CENTER_X_LOCATION, NETWORK_CENTER_Y_LOCATION, NETWORK_SCALE_FACTOR
	);
	
	// Special case network visual properties that get updated a lot. This is an optimization.
	private double networkCenterXLocation = NETWORK_CENTER_X_LOCATION.getDefault();
	private double networkCenterYLocation = NETWORK_CENTER_Y_LOCATION.getDefault();
	private double networkScaleFactor     = NETWORK_SCALE_FACTOR.getDefault();
	
	
	public VPNetworkStore(VisualLexicon visualLexicon, CyNetworkViewFactoryConfigImpl config) {
		super(CyNetwork.class, visualLexicon, config);
	}
	
	private VPNetworkStore(VPNetworkStore other) {
		super(other);
		this.networkCenterXLocation = other.networkCenterXLocation;
		this.networkCenterYLocation = other.networkCenterYLocation;
		this.networkScaleFactor = other.networkScaleFactor;
	}
	
	@Override
	public VPNetworkStore createSnapshot() {
		return new VPNetworkStore(this);
	}
	
	private double getSpecialNetworkProp(VisualProperty<?> vp) {
		if(vp == NETWORK_CENTER_X_LOCATION)
			return networkCenterXLocation;
		if(vp == NETWORK_CENTER_Y_LOCATION)
			return networkCenterYLocation;
		if(vp == NETWORK_SCALE_FACTOR)
			return networkScaleFactor;
		return 0; // should never happen
	}
	
	private void setSpecialNetworkProp(VisualProperty<?> vp, Object value) {
		if(vp == NETWORK_CENTER_X_LOCATION)
			networkCenterXLocation = ((Number)value).doubleValue();
		else if(vp == NETWORK_CENTER_Y_LOCATION)
			networkCenterYLocation = ((Number)value).doubleValue();
		if(vp == NETWORK_SCALE_FACTOR)
			networkScaleFactor = ((Number)value).doubleValue();
	}
	
	@Override
	public <T> T getSpecialVisualProperty(Long suid, VisualProperty<T> vp) {
		if(NETWORK_PROPS.contains(vp))
			return (T) Double.valueOf(getSpecialNetworkProp(vp));
		return null;
	}

	@Override
	protected <T, V extends T> boolean setSpecialVisualProperty(Long suid, VisualProperty<? extends T> vp, V value) {
		if(NETWORK_PROPS.contains(vp)) {
			setSpecialNetworkProp(vp, value);
			// don't set the dirty flag in this case
			return true;
		}
		return false;
	}
}
