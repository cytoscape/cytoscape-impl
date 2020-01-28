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
	
	// Need to track if these values are explicitly set, or if the view default is being used.
	private boolean networkCenterXLocationSet = false;
	private boolean networkCenterYLocationSet = false;
	private boolean networkScaleFactorSet = false;
	
	
	public VPNetworkStore(VisualLexicon visualLexicon, CyNetworkViewFactoryConfigImpl config) {
		super(CyNetwork.class, visualLexicon, config);
	}
	
	private VPNetworkStore(VPNetworkStore other) {
		super(other);
		this.networkCenterXLocation = other.networkCenterXLocation;
		this.networkCenterYLocation = other.networkCenterYLocation;
		this.networkScaleFactor = other.networkScaleFactor;
		this.networkCenterXLocationSet = other.networkCenterXLocationSet;
		this.networkCenterYLocationSet = other.networkCenterYLocationSet;
		this.networkScaleFactorSet = other.networkScaleFactorSet;
	}
	
	@Override
	public VPNetworkStore createSnapshot() {
		return new VPNetworkStore(this);
	}
	
	@Override
	protected <T, V extends T> boolean setSpecialVisualProperty(Long suid, VisualProperty<? extends T> vp, V value) {
		if(vp == NETWORK_CENTER_X_LOCATION) {
			if(value == null) {
				networkCenterXLocation = NETWORK_CENTER_X_LOCATION.getDefault();
				networkCenterXLocationSet = false;
			} else {
				networkCenterXLocation = ((Number)value).doubleValue();
				networkCenterXLocationSet = true;
			}
			return true;
		}
		if(vp == NETWORK_CENTER_Y_LOCATION) {
			if(value == null) {
				networkCenterYLocation = NETWORK_CENTER_Y_LOCATION.getDefault();
				networkCenterYLocationSet = false;
			} else {
				networkCenterYLocation = ((Number)value).doubleValue();
				networkCenterYLocationSet = true;
			}
			return true;
		}
		if(vp == NETWORK_SCALE_FACTOR) {
			if(value == null) {
				networkScaleFactor = NETWORK_SCALE_FACTOR.getDefault();
				networkScaleFactorSet = false;
			} else {
				networkScaleFactor = ((Number)value).doubleValue();
				networkScaleFactorSet = true;
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getSpecialVisualProperty(Long suid, VisualProperty<T> vp) {
		if(vp == NETWORK_CENTER_X_LOCATION && networkCenterXLocationSet)
			return (T) Double.valueOf(networkCenterXLocation);
		if(vp == NETWORK_CENTER_Y_LOCATION && networkCenterYLocationSet)
			return (T) Double.valueOf(networkCenterYLocation);
		if(vp == NETWORK_SCALE_FACTOR && networkScaleFactorSet)
			return (T) Double.valueOf(networkScaleFactor);
		return null;
	}
}
