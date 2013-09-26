package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ArrowShapeSerializer extends JsonSerializer<ArrowShape> {

	private static final Map<ArrowShape, String> SHAPE_MAP = new HashMap<ArrowShape, String>();
	
	static {
		SHAPE_MAP.put(ArrowShapeVisualProperty.T, "tee");
		SHAPE_MAP.put(ArrowShapeVisualProperty.DELTA, "triangle");
		SHAPE_MAP.put(ArrowShapeVisualProperty.CIRCLE, "circle");
		SHAPE_MAP.put(ArrowShapeVisualProperty.DIAMOND, "diamond");
		SHAPE_MAP.put(ArrowShapeVisualProperty.NONE, "none");
	}
	
	@Override
	public void serialize(ArrowShape value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
	
		if(value == null) {
			// Use default
			jgen.writeString(ArrowShapeVisualProperty.NONE.getDisplayName().toLowerCase());
		} else {
			String shapeString = SHAPE_MAP.get(value);
			if(shapeString == null) {
				shapeString = SHAPE_MAP.get(ArrowShapeVisualProperty.NONE);
			}
			jgen.writeString(shapeString);
		}
	}
	@Override
	public Class<ArrowShape> handledType() {
		return ArrowShape.class;
	}
}