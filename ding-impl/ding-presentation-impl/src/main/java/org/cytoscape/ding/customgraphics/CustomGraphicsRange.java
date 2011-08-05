package org.cytoscape.ding.customgraphics;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.view.model.DiscreteRange;

public class CustomGraphicsRange extends DiscreteRange<CyCustomGraphics>  {

	public CustomGraphicsRange() {
		super(CyCustomGraphics.class, new HashSet<CyCustomGraphics>());
	}

	private CustomGraphicsManager manager;
	
	
	public void setManager(final CustomGraphicsManager manager) {
		this.manager = manager;
	}
	
	@Override
	public Class<CyCustomGraphics> getType() {
		return CyCustomGraphics.class;
	}

	@Override
	public boolean isDiscrete() {
		return true;
	}

	@Override
	public Set<CyCustomGraphics> values() {
		
		return new HashSet<CyCustomGraphics>(manager.getAllCustomGraphics());
	}

	@Override
	public void addRangeValue(CyCustomGraphics newValue) {
		manager.addCustomGraphics(newValue, null);
	}

}
