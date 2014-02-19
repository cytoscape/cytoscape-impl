package org.cytoscape.io.internal.write.json.serializer;


import java.io.IOException;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsNodeViewSerializer extends JsonSerializer<View<CyNode>> {

	@Override
	public void serialize(View<CyNode> value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		
		final Double x = value.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		final Double y = value.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		
		jgen.writeObjectFieldStart("position");
		jgen.writeNumberField("x", x);
		jgen.writeNumberField("y", y);
		jgen.writeEndObject();
	}
	
	@Override
	public Class<View<CyNode>> handledType() {
		return (Class<View<CyNode>>) (Class) View.class;
	}
}
