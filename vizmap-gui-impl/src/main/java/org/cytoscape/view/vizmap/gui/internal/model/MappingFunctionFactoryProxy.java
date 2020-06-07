package org.cytoscape.view.vizmap.gui.internal.model;

import java.text.Collator;
import java.util.Locale;
import java.util.Set;
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
		Collator collator = Collator.getInstance(Locale.getDefault()); // Locale-specific sorting
		collator.setStrength(Collator.PRIMARY);
		var set = new TreeSet<VisualMappingFunctionFactory>((f1,f2) -> collator.compare(f1.toString(), f2.toString()));
		
		MappingFunctionFactoryManager mappingFactoryMgr = servicesUtil.get(MappingFunctionFactoryManager.class);
		set.addAll(mappingFactoryMgr.getFactories());
		
		if (currentColumnName != null && currentTargetDataType != null) {
			// Remove the factories that don't make sense for the current column type
			CyTable table = null;
			
			CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
			if(currentTargetDataType == CyColumn.class) {
				table = appMgr.getCurrentTable();
			} else {
				CyNetwork net = appMgr.getCurrentNetwork();
				if (net != null) {
					table = net.getTable(currentTargetDataType, CyNetwork.DEFAULT_ATTRS);
				}
			}
			
			if(table != null) {
				CyColumn column = table.getColumn(currentColumnName);
				if (column != null && !Number.class.isAssignableFrom(column.getType())) {
					set.remove(mappingFactoryMgr.getFactory(ContinuousMapping.class));
				}
			}
			
		}
		return set;
	}
	
}
