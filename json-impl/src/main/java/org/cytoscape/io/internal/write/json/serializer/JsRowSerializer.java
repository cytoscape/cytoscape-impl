package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Serialize row data to cytoscape.js style key-value pair.
 * 
 */
public class JsRowSerializer extends JsonSerializer<CyRow> {

	@Override
	public void serialize(final CyRow row, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {

		final CyTable table = row.getTable();
		final Map<String, Object> values = row.getAllValues();

		for (final String columnName : values.keySet()) {
			final Object value = values.get(columnName);
			if (value == null)
				continue;

			Class<?> type = table.getColumn(columnName).getType();
			if (type == List.class) {
				type = table.getColumn(columnName).getListElementType();
				writeList(type, columnName, (List<?>) value, jgen);
			} else {
				jgen.writeFieldName(columnName);
				writeValue(type, value, jgen);
			}
		}
	}

	private void writeList(final Class<?> type, String columnName, List<?> values, JsonGenerator jgen)
			throws JsonGenerationException, IOException {
		jgen.writeFieldName(columnName);
		jgen.writeStartArray();

		for (Object value : values)
			writeValue(type, value, jgen);
		
		jgen.writeEndArray();
	}

	private final void writeValue(final Class<?> type, Object value, JsonGenerator jgen)
			throws JsonGenerationException, IOException {
		if (type == String.class) {
			jgen.writeString(value.toString());
		} else if (type == Boolean.class) {
			jgen.writeBoolean((Boolean) value);
		} else if (type == Double.class) {
			jgen.writeNumber((Double) value);
		} else if (type == Integer.class) {
			jgen.writeNumber((Integer) value);
		} else if (type == Long.class) {
			jgen.writeNumber((Long) value);
		} else if (type == Float.class) {
			jgen.writeNumber((Double) value);
		}
	}

	
	@Override
	public Class<CyRow> handledType() {
		return CyRow.class;
	}
}