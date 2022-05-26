package org.cytoscape.view.vizmap.gui.internal.model;

import java.text.Collator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.gui.MappingFunctionFactoryManager;
import org.cytoscape.view.vizmap.gui.internal.GraphObjectType;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.puremvc.java.multicore.patterns.proxy.Proxy;

public class MappingFunctionFactoryProxy extends Proxy {
	
	public static final String NAME = "MappingFunctionFactoryProxy";
	
	private String currentColumnName;
	private GraphObjectType currentTargetDataType;
	
	private final ServicesUtil servicesUtil;

	public MappingFunctionFactoryProxy(final ServicesUtil servicesUtil) {
		super(NAME);
		this.servicesUtil = servicesUtil;
	}

	public void setCurrentColumnName(final String name) {
		this.currentColumnName = name;
	}
	
	public String getCurrentColumnName() {
		return currentColumnName;
	}
	
	public void setCurrentTargetDataType(GraphObjectType type) {
		this.currentTargetDataType = type;
	}
	
	public GraphObjectType getCurrentTargetDataType() {
		return currentTargetDataType;
	}
	
	public Set<VisualMappingFunctionFactory> getMappingFactories() {
		Collator collator = Collator.getInstance(Locale.getDefault()); // Locale-specific sorting
		collator.setStrength(Collator.PRIMARY);
		var set = new TreeSet<VisualMappingFunctionFactory>((f1,f2) -> collator.compare(f1.toString(), f2.toString()));
		
		MappingFunctionFactoryManager mappingFactoryMgr = servicesUtil.get(MappingFunctionFactoryManager.class);
		set.addAll(mappingFactoryMgr.getFactories());
		
		if (currentColumnName != null && currentTargetDataType != null) {
			// Remove the factories that don't make sense for the current column type
			var appMgr = servicesUtil.get(CyApplicationManager.class);
			var net = appMgr.getCurrentNetwork();
			
			if (net != null) {
				final CyTable table = net.getTable(currentTargetDataType.type(), CyNetwork.DEFAULT_ATTRS);
				final CyColumn column = table.getColumn(currentColumnName);
				
				if (column != null && !Number.class.isAssignableFrom(column.getType()))
					set.remove(mappingFactoryMgr.getFactory(ContinuousMapping.class));
			}
			
		}
		return set;
	}
	
}
