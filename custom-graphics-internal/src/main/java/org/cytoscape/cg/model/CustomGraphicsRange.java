package org.cytoscape.cg.model;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;

@SuppressWarnings("rawtypes")
public final class CustomGraphicsRange extends DiscreteRange<CyCustomGraphics>  {

	private static CustomGraphicsManager manager;
	
	private static CustomGraphicsRange instance;
	
	private CustomGraphicsRange() {
		super(CyCustomGraphics.class, new HashSet<>());
	}
	
	public static CustomGraphicsRange getInstance() {
		if (instance == null)
			instance = new CustomGraphicsRange();
		
		return instance;
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
	
	public static void setManager(CustomGraphicsManager manager) {
		CustomGraphicsRange.manager = manager;
	}
}
