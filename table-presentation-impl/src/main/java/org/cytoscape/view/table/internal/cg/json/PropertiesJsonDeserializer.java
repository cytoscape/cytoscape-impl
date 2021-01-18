package org.cytoscape.view.table.internal.cg.json;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.view.table.internal.cg.AbstractCellCustomGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PropertiesJsonDeserializer extends JsonDeserializer<Map<String, Object>> {

	private final AbstractCellCustomGraphics cg; // TODO remove circular dependency
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public PropertiesJsonDeserializer(AbstractCellCustomGraphics cg) {
		this.cg = cg;
	}

	@Override
	public Map<String, Object> deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		var props = new LinkedHashMap<String, Object>();
		
		var mapper = (ObjectMapper) jp.getCodec();
        JsonNode rootNode = mapper.readTree(jp);
        
        if (rootNode.isObject()) {
	        var fieldNames = rootNode.fieldNames();
	        
	        while (fieldNames.hasNext()) {
	        	var key = fieldNames.next();
	        	var jn = rootNode.get(key);
	        	var value = readValue(key, jn.toString(), mapper, cg);
		        props.put(key, value);
	        }
        }
        
		return props;
	}
	
	public static Object readValue(String key, String input, ObjectMapper mapper, AbstractCellCustomGraphics cg) {
		Object value = null;
		var type = cg.getSettingType(key);
		
    	if (type != null) {
    		var typeFactory = mapper.getTypeFactory();
			
			try {
				if (type == Array.class) {
					var elementType = cg.getSettingElementType(key);
					
					if (elementType != null) {
		        		var arrType = typeFactory.constructArrayType(elementType);
		        		
		        		if (mapper.canDeserialize(arrType))
		        			value = mapper.readValue(input, arrType);
		    		}
				} else if (List.class.isAssignableFrom(type)) {
		    		var elementType = cg.getSettingElementType(key);
		    		
		    		if (elementType != null) {
		        		var collType = typeFactory.constructCollectionType(List.class, elementType);
		        		
		        		if (mapper.canDeserialize(collType))
		        			value = mapper.readValue(input, collType);
		    		}
		    	} else {
		    		var simpleType = typeFactory.constructSimpleType(type, new JavaType[]{});
		    		
		    		if (mapper.canDeserialize(simpleType))
		    			value = mapper.readValue(input, simpleType);
		    	}
			} catch (Exception e) {
        		logger.error("Cannot parse JSON field " + key, e);
        	}
    	}
		
		return value;
	}
}
