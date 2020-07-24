package org.cytoscape.browser.internal.task;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableColumnTask;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.work.TaskMonitor;

public class ColorColumnTestTask extends AbstractTableColumnTask {

	private final CyServiceRegistrar registrar;
	
	public ColorColumnTestTask(CyColumn column, CyServiceRegistrar registrar) {
		super(column);
		this.registrar = registrar;
	}
	
	
	@Override
	public void run(TaskMonitor tm) {
		var tableViewManager = registrar.getService(CyTableViewManager.class);
		var cmFactory = registrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");

		Class<?> colType = column.getType();
		if(!Number.class.isAssignableFrom(colType)) {
			return;
		}
		
		// create a function
		double minmax[] = getMinMax(column);
		Color minColor = Color.WHITE;
		Color maxColor = Color.RED.darker(); 
		
		ContinuousMapping<Object,Paint> mapping = (ContinuousMapping<Object,Paint>) cmFactory
				.createVisualMappingFunction(column.getName(), colType, BasicTableVisualLexicon.CELL_BACKGROUND_PAINT);
		mapping.addPoint(minmax[0], new BoundaryRangeValues<>(minColor, minColor, minColor));
		mapping.addPoint(minmax[1], new BoundaryRangeValues<>(maxColor, maxColor, maxColor));
		
		CyTable table = column.getTable();
		CyTableView tableView = tableViewManager.getTableView(table);
		CyColumnView colView = (CyColumnView) tableView.getColumnView(column);
		
		colView.setCellVisualProperty(BasicTableVisualLexicon.CELL_BACKGROUND_PAINT, mapping::getMappedValue);
	}

	
	private static double[] getMinMax(CyColumn column) {
		List<?> values = column.getValues(column.getType());
		if(values.isEmpty())
			return new double[] {0, 1};
		
		Number first = (Number) values.iterator().next();
		double min = first.doubleValue();
		double max = first.doubleValue();
		
		for(var value : values) {
			if(value != null) {
				double d = ((Number)value).doubleValue();
				min = Math.min(min, d);
				max = Math.max(max, d);
			}
		}
		if(min == max) {
			max += 1;
		}
		
		return new double[] { min, max };
	}
	
}
