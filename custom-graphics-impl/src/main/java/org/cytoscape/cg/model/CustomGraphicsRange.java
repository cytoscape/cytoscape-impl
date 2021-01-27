package org.cytoscape.cg.model;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;

@SuppressWarnings("rawtypes")
public class CustomGraphicsRange extends DiscreteRange<CyCustomGraphics>  {

	private CustomGraphicsManager manager;
	
	public CustomGraphicsRange() {
		super(CyCustomGraphics.class, new HashSet<>());
	}

	public void setManager(CustomGraphicsManager manager) {
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
		var sortedSet = new TreeSet<>(new CGComparator());
		sortedSet.addAll(manager.getAllCustomGraphics());
		
		return sortedSet;
	}

	@Override
	public void addRangeValue(CyCustomGraphics newValue) {
		manager.addCustomGraphics(newValue, null);
	}
	
	@Override
	public boolean inRange(CyCustomGraphics value) {
		// CyCharts don't have to be added to the manager
		return value instanceof CyCustomGraphics2
				|| value == NullCustomGraphics.getNullObject()
				|| manager.getAllCustomGraphics().contains(value);
	}
}
