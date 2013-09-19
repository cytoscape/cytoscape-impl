package org.cytoscape.io.internal.write.json.serializer;

import java.util.List;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;

public class ContinuousMappingSerializer implements VisualMappingSerializer<ContinuousMapping<?, ?>>{

	@Override
	public String serialize(final ContinuousMapping<?, ?> mapping) {
		
		final VisualProperty<?> vp = mapping.getVisualProperty();

		final String columnName = mapping.getMappingColumnName();
		final List<?> points = mapping.getAllPoints();
		
		for(Object point: points) {
			final ContinuousMappingPoint<?, ?> p = (ContinuousMappingPoint<?, ?>) point;
			Object val = p.getValue();
			BoundaryRangeValues<?> range = p.getRange();
//			range.equalValue;
		}
		
		// FIXME
		return "mapData(" + columnName + ", 0, 100, red, green)";
	}
}
