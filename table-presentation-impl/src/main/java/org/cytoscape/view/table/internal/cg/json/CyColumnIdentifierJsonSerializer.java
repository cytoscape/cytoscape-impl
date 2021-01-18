package org.cytoscape.view.table.internal.cg.json;

import java.io.IOException;

import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CyColumnIdentifierJsonSerializer extends JsonSerializer<CyColumnIdentifier> {

	@Override
	public void serialize(CyColumnIdentifier value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		if (value != null)
			jgen.writeString(((CyColumnIdentifier)value).toString());
		else
			jgen.writeNull();
	}
	
	@Override
	public Class<CyColumnIdentifier> handledType() {
		return CyColumnIdentifier.class;
	}
}
