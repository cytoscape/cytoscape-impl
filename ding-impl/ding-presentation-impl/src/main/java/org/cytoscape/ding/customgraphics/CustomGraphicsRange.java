package org.cytoscape.ding.customgraphics;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

import org.cytoscape.ding.customgraphicsmgr.internal.CGComparator;

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
		Set<CyCustomGraphics> sortedSet = new TreeSet<CyCustomGraphics>(new CGComparator());
		sortedSet.addAll(manager.getAllCustomGraphics());
		return sortedSet;
	}

	@Override
	public void addRangeValue(CyCustomGraphics newValue) {
		manager.addCustomGraphics(newValue, null);
	}
	
	@Override
	public boolean inRange(CyCustomGraphics value) {
		if(manager.getAllCustomGraphics().contains(value))
			return true;
		else
			return false;
	}

}
