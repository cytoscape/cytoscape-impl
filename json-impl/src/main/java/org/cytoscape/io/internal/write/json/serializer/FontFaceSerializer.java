package org.cytoscape.io.internal.write.json.serializer;

import java.awt.Font;
import java.io.IOException;

import javax.jws.soap.SOAPBinding.Style;

import com.fasterxml.jackson.core.JsonGenerationException;
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
			writeFontWeight(value, jgen);
		}
	}
	
	/**
	 * Convert Java's Font's style to weight
	 * @throws IOException 
	 * @throws JsonGenerationException 
	 */
	private final void writeFontWeight(final Font font, final JsonGenerator jgen) throws JsonGenerationException, IOException {
		int style = font.getStyle();

		if(style == Font.BOLD){
			jgen.writeStringField("font-weight", "bold");
		} else {
			jgen.writeStringField("font-weight", "normal");
		}
	}
	

	@Override
	public Class<Font> handledType() {
		return Font.class;
	}
}