package org.cytoscape.view.vizmap.gui.internal.model;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.gui.MappingFunctionFactoryManager;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.puremvc.java.multicore.patterns.proxy.Proxy;

public class MappingFunctionFactoryProxy extends Proxy {
	
	public static final String NAME = "MappingFunctionFactoryProxy";
	
	private String currentColumnName;
	private Class<? extends CyIdentifiable> currentTargetDataType;
	
	private final ServicesUtil servicesUtil;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public MappingFunctionFactoryProxy(final ServicesUtil servicesUtil) {
		super(NAME);
		this.servicesUtil = servicesUtil;
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public void setCurrentColumnName(final String name) {
		this.currentColumnName = name;
	}
	
	public String getCurrentColumnName() {
		return currentColumnName;
	}
	
	public void setCurrentTargetDataType(final Class<? extends CyIdentifiable> type) {
		this.currentTargetDataType = type;
	}
	
	public Class<? extends CyIdentifiable> getCurrentTargetDataType() {
		return currentTargetDataType;
	}
	
	public Set<VisualMappingFunctionFactory> getMappingFactories() {
		final SortedSet<VisualMappingFunctionFactory> set = new TreeSet<VisualMappingFunctionFactory>(
				new Comparator<VisualMappingFunctionFactory>() {
					
					@Override
					public int compare(final VisualMappingFunctionFactory f1, final VisualMappingFunctionFactory f2) {
						// Locale-specific sorting
						final Collator collator = Collator.getInstance(Locale.getDefault());
						collator.setStrength(Collator.PRIMARY);
						
						return collator.compare(f1.toString(), f2.toString());
					}
				});
		
		final MappingFunctionFactoryManager mappingFactoryMgr = servicesUtil.get(MappingFunctionFactoryManager.class);
		set.addAll(mappingFactoryMgr.getFactories());
		
		if (currentColumnName != null && currentTargetDataType != null) {
			// Remove the factories that don't make sense for the current column type
			final CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
			final CyNetwork net = appMgr.getCurrentNetwork();
			
			if (net != null) {
				final CyTable table = net.getTable(currentTargetDataType, CyNetwork.DEFAULT_ATTRS);
				final CyColumn column = table.getColumn(currentColumnName);
				
				if (column != null && !Number.class.isAssignableFrom(column.getType()))
					set.remove(mappingFactoryMgr.getFactory(ContinuousMapping.class));
			}
		}
		
		return set;
	}
}
