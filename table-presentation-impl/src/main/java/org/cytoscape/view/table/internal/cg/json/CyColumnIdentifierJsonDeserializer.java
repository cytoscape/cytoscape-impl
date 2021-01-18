package org.cytoscape.view.table.internal.cg.json;

import java.io.IOException;

import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class CyColumnIdentifierJsonDeserializer extends JsonDeserializer<CyColumnIdentifier> {

	private final CyColumnIdentifierFactory colIdFactory;

	public CyColumnIdentifierJsonDeserializer(CyColumnIdentifierFactory colIdFactory) {
		this.colIdFactory = colIdFactory;
	}

	@Override
	public CyColumnIdentifier deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        
        return node != null && node.isTextual() ? colIdFactory.createColumnIdentifier(node.textValue()) : null;
	}
}
