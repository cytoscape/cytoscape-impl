package org.cytoscape.io.internal.write.json.serializer;

import java.awt.Color;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ColorSerializer extends JsonSerializer<Color> {

	private static final Color DEF_COLOR = Color.WHITE;
	
	@Override
	public void serialize(Color value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {

		if (value != null) {
			jgen.writeString(decodeColor(value));
		} else {
			// Use default color
			jgen.writeString(decodeColor(DEF_COLOR));
		}
	}

	private final String decodeColor(final Color color) {
		final StringBuilder builder = new StringBuilder();

		builder.append("rgb(");
		builder.append(color.getRed() + ",");
		builder.append(color.getGreen() + ",");
		builder.append(color.getBlue() + ")");

		return builder.toString();
	}

	@Override
	public Class<Color> handledType() {
		return Color.class;
	}
}
