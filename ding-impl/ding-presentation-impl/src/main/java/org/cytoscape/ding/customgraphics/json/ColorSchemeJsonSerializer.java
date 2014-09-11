package org.cytoscape.ding.customgraphics.json;

import java.io.IOException;

import org.cytoscape.ding.customgraphics.ColorScheme;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ColorSchemeJsonSerializer extends JsonSerializer<ColorScheme> {

	@Override
	public void serialize(final ColorScheme value, final JsonGenerator jgen, final SerializerProvider provider)
			throws IOException, JsonProcessingException {
		if (value != null)
			jgen.writeString(((ColorScheme)value).getKey());
		else
			jgen.writeNull();
	}
	
	@Override
	public Class<ColorScheme> handledType() {
		return ColorScheme.class;
	}
}
