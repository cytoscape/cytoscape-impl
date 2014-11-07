package org.cytoscape.ding.customgraphics.json;

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

	public CyColumnIdentifierJsonDeserializer(final CyColumnIdentifierFactory colIdFactory) {
		this.colIdFactory = colIdFactory;
	}

	@Override
	public CyColumnIdentifier deserialize(final JsonParser jp, final DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		final ObjectCodec oc = jp.getCodec();
        final JsonNode node = oc.readTree(jp);
        
        return node != null && node.isTextual() ? colIdFactory.createColumnIdentifier(node.textValue()) : null;
	}
}
