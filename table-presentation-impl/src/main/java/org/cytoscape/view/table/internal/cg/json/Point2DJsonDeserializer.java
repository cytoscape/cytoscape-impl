package org.cytoscape.view.table.internal.cg.json;

import java.awt.geom.Point2D;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class Point2DJsonDeserializer extends JsonDeserializer<Point2D> {

	@Override
	public Point2D deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        
        JsonNode xNode = node.get("x");
        JsonNode yNode = node.get("y");
        
        double x = xNode != null && xNode.isNumber() ? xNode.asDouble() : 0;
        double y = yNode != null && yNode.isNumber() ? yNode.asDouble() : 0;
        
		return new Point2D.Double(x, y);
	}
}
