package org.cytoscape.io.internal.write.json.serializer;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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
	
		// Special case handling:
		if(points.size() == 0) {
			// No mapping points.  Ignore.
			return "";
		}
		
		// Only one point: split into 3 selectors.
		if(points.size() == 1) {
			
		}
		
		// Sort points.
		SortedMap<Number, ContinuousMappingPoint<?, ?>> pointMap = new TreeMap<Number, ContinuousMappingPoint<?,?>>();
		for(Object point: points) {
			final ContinuousMappingPoint<?, ?> p = (ContinuousMappingPoint<?, ?>) point;
			Number val = (Number) p.getValue();
			pointMap.put(val, p);
		}
		
		System.out.println("\nMapingKey: " + columnName);
		for(Number key: pointMap.keySet()) {
			final ContinuousMappingPoint<?, ?> p = pointMap.get(key);
			Object val = p.getValue();
			System.out.println("P: " + val);
			BoundaryRangeValues<?> range = p.getRange();
			System.out.println("range1: " + range.lesserValue);
			System.out.println("range1: " + range.equalValue);
			System.out.println("range1: " + range.greaterValue);
//			range.equalValue;
		}
		
		// FIXME
		return "mapData(" + columnName + ", 0, 100, red, green)";
	}
}
