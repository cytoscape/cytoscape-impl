package org.cytoscape.io.internal.write.transformer;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.io.internal.util.FilterIO;
import org.cytoscape.io.write.CyTransformerWriter;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class CyTransformerWriterImpl implements CyTransformerWriter {
	private JsonFactory factory;

	public CyTransformerWriterImpl() {
		factory = new JsonFactory();
		factory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
	}
	
	@Override
	public void write(OutputStream stream, NamedTransformer<?, ?>... namedTransformers) throws IOException {
		JsonGenerator generator = factory.createGenerator(stream);
		try {
			generator.setPrettyPrinter(new DefaultPrettyPrinter());
	
			generator.writeStartArray();
			try {
				for (NamedTransformer<?, ?> namedTransformer : namedTransformers) {
					write(generator, namedTransformer);
				}
			} finally {
				generator.writeEndArray();
			}
		} finally {
			generator.close();
		}
	}

	void write(JsonGenerator generator, NamedTransformer<?, ?> namedTransformer) throws IOException {
		generator.writeStartObject();
		try {
			generator.writeStringField(FilterIO.NAME_FIELD, namedTransformer.getName());
			generator.writeArrayFieldStart(FilterIO.TRANSFORMERS_FIELD);
			try {
				for (Transformer<?, ?> transformer : namedTransformer.getTransformers()) {
					write(generator, transformer);
				}
			} finally {
				generator.writeEndArray();
			}
		} finally {
			generator.writeEndObject();
		}
	}
	
	private void write(JsonGenerator generator, Transformer<?, ?> transformer) throws IOException {
		generator.writeStartObject();
		try {
			generator.writeStringField(FilterIO.ID_FIELD, transformer.getId());
			generator.writeObjectFieldStart(FilterIO.PARAMETERS_FIELD);
			try {
				Map<String, Object> parameters = FilterIO.getParameters(transformer);
				for (Entry<String, Object> entry : parameters.entrySet()) {
					String name = entry.getKey();
					Object value = entry.getValue();
					writeField(generator, name, value);
				}
			} catch (IntrospectionException e) {
				throw new IOException(e);
			} finally {
				generator.writeEndObject();
			}
		} finally {
			generator.writeEndObject();
		}
	}

	private void writeField(JsonGenerator generator, String name, Object value) throws IOException {
		if (value instanceof Number) {
			if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
				generator.writeNumberField(name, ((Number) value).longValue());
			} else {
				generator.writeNumberField(name, ((Number) value).doubleValue());
			}
		} else if (value instanceof ListSingleSelection) {
			Object selected = ((ListSingleSelection<?>) value).getSelectedValue();
			writeField(generator, name, selected);
		} else if (value instanceof ListMultipleSelection) {
			List<?> values = ((ListMultipleSelection<?>) value).getSelectedValues();
			writeField(generator, name, values);
		} else if (value instanceof List) {
			generator.writeArrayFieldStart(name);
			try {
				for (Object object : (List<?>) value) {
					writeValue(generator, object);
				}
			} finally {
				generator.writeEndArray();
			}
		} else if (value instanceof Boolean) {
			generator.writeBooleanField(name, ((Boolean) value).booleanValue());
		} else if (value == null) {
			generator.writeNullField(name);
		} else if (value.getClass().isArray()) {
			generator.writeArrayFieldStart(name);
			try {
				for (int i = 0; i < Array.getLength(value); i++) {
					writeValue(generator, Array.get(value, i));
				}
			} finally {
				generator.writeEndArray();
			}
		} else {
			generator.writeStringField(name, value.toString());
		}
	}

	private void writeValue(JsonGenerator generator, Object object) throws IOException {
		if (object instanceof Number) {
			if (object instanceof Long || object instanceof Integer || object instanceof Short || object instanceof Byte) {
				generator.writeNumber(((Number) object).longValue());
			} else {
				generator.writeNumber(((Number) object).doubleValue());
			}
		} else if (object instanceof Boolean) {
			generator.writeBoolean(((Boolean) object).booleanValue());
		} else if (object == null) {
			generator.writeNull();
		} else {
			generator.writeString(object.toString());
		}
	}
}
