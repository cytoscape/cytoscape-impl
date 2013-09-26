package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LineStyleSerializer extends JsonSerializer<LineType> {

	private static final Map<LineType, String> LINE_MAP = new HashMap<LineType, String>();
	
	static {
		LINE_MAP.put(LineTypeVisualProperty.SOLID, "solid");
		LINE_MAP.put(LineTypeVisualProperty.DOT, "dotted");
		LINE_MAP.put(LineTypeVisualProperty.EQUAL_DASH, "dashed");
	}


	@Override
	public void serialize(LineType value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		if (value == null) {
			jgen.writeString(LineTypeVisualProperty.SOLID.getDisplayName().toLowerCase());
		} else {
			// Use default color
			String lineStyleString = LINE_MAP.get(value);
			if(lineStyleString == null) {
				lineStyleString =  LINE_MAP.get(LineTypeVisualProperty.SOLID);
			}
			jgen.writeString(lineStyleString);
		}
	}

	@Override
	public Class<LineType> handledType() {
		return LineType.class;
	}

}