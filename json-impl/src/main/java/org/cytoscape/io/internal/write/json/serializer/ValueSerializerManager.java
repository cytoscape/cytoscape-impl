package org.cytoscape.io.internal.write.json.serializer;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;

public class ValueSerializerManager {
	
	private static final Map<Class, ValueSerializer> SERIALIZERS = new HashMap<Class, ValueSerializer>();

	static {
		SERIALIZERS.put(Paint.class, new ColorValueSerializer());
		SERIALIZERS.put(Color.class, new ColorValueSerializer());
	}
	
	public <T> ValueSerializer<T> getSerializer(Class<T> type) {
		return SERIALIZERS.get(type);
	}

}
