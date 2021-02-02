package org.cytoscape.cg.internal.charts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

public abstract class AbstractChartFactory<T extends CustomGraphicLayer> implements CyCustomGraphics2Factory<T> {

	private final Set<Class<? extends CyIdentifiable>> targetTypes = new HashSet<>();
	protected final CyServiceRegistrar serviceRegistrar;
	
	protected AbstractChartFactory(
			Collection<Class<? extends CyIdentifiable>> targetTypes,
			CyServiceRegistrar serviceRegistrar
	) {
		if (targetTypes == null || targetTypes.isEmpty())
			throw new IllegalArgumentException("At least one supported target type must be informed.");
		
		this.targetTypes.addAll(targetTypes);
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public Set<Class<? extends CyIdentifiable>> getSupportedTargetTypes() {
		return new HashSet<>(targetTypes);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
