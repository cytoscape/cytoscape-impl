package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;
import java.util.Set;

import org.cytoscape.view.vizmap.VisualStyle;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsVsiaulStyleSetSerializer extends JsonSerializer<Set<VisualStyle>> {

	@Override
	public void serialize(final Set<VisualStyle> styleSet, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.useDefaultPrettyPrinter();
		
		jgen.writeStartArray();

		for (VisualStyle style : styleSet) {
			jgen.writeObject(style);
		}

		jgen.writeEndArray();
	}

	@Override
	public Class<Set<VisualStyle>> handledType() {
		return (Class<Set<VisualStyle>>) (Class) Set.class;
	}
}
