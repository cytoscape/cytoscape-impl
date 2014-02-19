package org.cytoscape.io.internal.write.json.serializer;

public interface ValueSerializer<T> {
	
	String serialize(final T value);

}
