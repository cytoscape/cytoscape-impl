package org.cytoscape.ding.customgraphics.json;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class PropertiesJsonDeserializer extends JsonDeserializer<Map<String, Object>> {

	private final AbstractCustomGraphics2<?> cg2;
	
	private static final Logger logger = LoggerFactory.getLogger(PropertiesJsonDeserializer.class);

	public PropertiesJsonDeserializer(final AbstractCustomGraphics2<?> cg2) {
		this.cg2 = cg2;
	}

	@Override
	public Map<String, Object> deserialize(final JsonParser jp, final DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		final Map<String, Object> props = new LinkedHashMap<>();
		
		final ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        final JsonNode rootNode = mapper.readTree(jp);
        
        if (rootNode.isObject()) {
	        final Iterator<String> fieldNames = rootNode.fieldNames();
	        
	        while (fieldNames.hasNext()) {
	        	final String key = fieldNames.next();
	        	final JsonNode jn = rootNode.get(key);
	        	final Object value = readValue(key, jn.toString(), mapper, cg2);
		        props.put(key, value);
	        }
        }
        
		return props;
	}
	
	// TODO remove circular dependency
	public static <T, E> T readValue(final String key,
									 final String input,
									 final ObjectMapper mapper,
									 final AbstractCustomGraphics2<?> cg2) {
		T value = null;
		
		final Class<?> type = cg2.getSettingType(key);
		
    	if (type != null) {
			final TypeFactory typeFactory = mapper.getTypeFactory();
			
			try {
				if (List.class.isAssignableFrom(type)) {
		    		final Class<?> elementType = cg2.getSettingListType(key);
		    		
		    		if (elementType != null) {
		        		final CollectionType collType = typeFactory.constructCollectionType(List.class, elementType);
		        		
		        		if (mapper.canDeserialize(collType))
		        			value = mapper.readValue(input, collType);
		    		}
		    	} else {
		    		final JavaType simpleType = typeFactory.constructSimpleType(type, new JavaType[]{});
		    		
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
