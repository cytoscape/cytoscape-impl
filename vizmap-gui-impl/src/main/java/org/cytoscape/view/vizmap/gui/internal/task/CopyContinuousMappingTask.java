package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

public class CopyContinuousMappingTask extends AbstractTask {

	private final VisualStyle sourceStyle;
	private final VisualProperty<?> sourceVP; 
	private final VisualStyle targetStyle;
	private final VisualProperty<?> targetVP;
	
	private final ServicesUtil servicesUtil;
	
	
	public CopyContinuousMappingTask(
			VisualStyle sourceStyle, 
			VisualProperty<?> sourceVP, 
			VisualStyle targetStyle,
			VisualProperty<?> targetVP, 
			ServicesUtil servicesUtil) 
	{
		this.sourceStyle = sourceStyle;
		this.sourceVP = sourceVP;
		this.targetStyle = targetStyle;
		this.targetVP = targetVP;
		this.servicesUtil = servicesUtil;
	}


	@Override
	public void run(TaskMonitor tm) {
		var sourceMapping = sourceStyle.getVisualMappingFunction(sourceVP);
		
		if(sourceMapping instanceof ContinuousMapping<?,?> cm) {
			var existingMapping = targetStyle.getVisualMappingFunction(targetVP);
			
			var newMapping = cloneMapping(cm);
			targetStyle.addVisualMappingFunction(newMapping);
			
			servicesUtil.get(UndoSupport.class).postEdit(new CopyContinuousMappingEdit(targetStyle, existingMapping, newMapping));
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private <K,V> VisualMappingFunction<K,V> cloneMapping(ContinuousMapping<K,V> mapping) {
		var factory = servicesUtil.get(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		var attrName = mapping.getMappingColumnName();
		var attrType = mapping.getMappingColumnType();
		var newMapping = (ContinuousMapping<K,V>) factory.createVisualMappingFunction(attrName, attrType, targetVP);
		
		for(var point : mapping.getAllPoints()) {
			newMapping.addPoint(point.getValue(), new BoundaryRangeValues<>(point.getRange()));
		}
		
		return newMapping;
 	}

	
	private static class CopyContinuousMappingEdit extends AbstractCyEdit {
		
		private final VisualStyle targetStyle;
		
		private final VisualMappingFunction<?,?> originalMapping;
		private final VisualMappingFunction<?,?> newMapping;
		
		
		public CopyContinuousMappingEdit(
				VisualStyle targetStyle, 
				VisualMappingFunction<?, ?> originalMapping, 
				VisualMappingFunction<?, ?> newMapping
		) {
			super("Copy Mapping");
			this.originalMapping = originalMapping;
			this.newMapping = newMapping;
			this.targetStyle = targetStyle;
		}

		@Override
		public void undo() {
			if(originalMapping == null) {
				targetStyle.removeVisualMappingFunction(newMapping.getVisualProperty());
			} else {
				targetStyle.addVisualMappingFunction(originalMapping);
			}
		}

		@Override
		public void redo() {
			targetStyle.addVisualMappingFunction(newMapping);
		}
		
	}
}
