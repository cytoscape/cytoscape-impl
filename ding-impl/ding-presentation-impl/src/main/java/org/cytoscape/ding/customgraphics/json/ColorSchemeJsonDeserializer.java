package org.cytoscape.ding.customgraphics.json;

import java.io.IOException;

import org.cytoscape.ding.customgraphics.ColorScheme;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ColorSchemeJsonDeserializer extends JsonDeserializer<ColorScheme> {

	@Override
	public ColorScheme deserialize(final JsonParser jp, final DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		final ObjectCodec oc = jp.getCodec();
        final JsonNode node = oc.readTree(jp);
        
		return ColorScheme.parse(node.isTextual() ? node.asText() : "");
	}
}
