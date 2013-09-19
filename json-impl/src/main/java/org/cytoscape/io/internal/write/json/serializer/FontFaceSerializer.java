package org.cytoscape.io.internal.write.json.serializer;

import java.awt.Font;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class FontFaceSerializer extends JsonSerializer<Font> {

	@Override
	public void serialize(Font value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {

		if (value == null) {
			// Use default
			jgen.writeString(Font.SANS_SERIF);
		} else {
			jgen.writeString(value.getFontName());
		}
	}

	@Override
	public Class<Font> handledType() {
		return Font.class;
	}
}