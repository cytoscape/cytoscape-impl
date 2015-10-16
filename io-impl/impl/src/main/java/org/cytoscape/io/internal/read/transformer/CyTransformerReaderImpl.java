package org.cytoscape.io.internal.read.transformer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.SubFilterTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.io.internal.util.FilterIO;
import org.cytoscape.io.read.CyTransformerReader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class CyTransformerReaderImpl implements CyTransformerReader {
	private JsonFactory factory;
	private TransformerManager transformerManager;
	
	public CyTransformerReaderImpl() {
		factory = new JsonFactory();
		factory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
	}

	public void registerTransformerManager(TransformerManager transformerManager, Map<String, String> properties) {
		this.transformerManager = transformerManager;
	}
	
	public void unregisterTransformerManager(TransformerManager transformerManager, Map<String, String> properties) {
		this.transformerManager = null;
	}
	
	@Override
	public NamedTransformer<?, ?>[] read(InputStream stream) throws IOException {
		JsonParser parser = factory.createParser(stream);
		try {
			assertNextToken(parser, JsonToken.START_ARRAY);
			
			List<NamedTransformer<?, ?>> namedTransformers = new ArrayList<NamedTransformer<?,?>>();
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
				assertField(parser, FilterIO.NAME_FIELD);
				String name = parser.nextTextValue();
				assertField(parser, FilterIO.TRANSFORMERS_FIELD);
				assertNextToken(parser, JsonToken.START_ARRAY);
				
				List<Transformer<?, ?>> transformers = new ArrayList<Transformer<?,?>>();
				while (true) {
					Transformer<?, ?> transformer = readTransformer(parser);
					if (transformer == null) {
						break;
					}
					transformers.add(transformer);
				}
				
				assertNextToken(parser, JsonToken.END_OBJECT);
				namedTransformers.add(FilterIO.createNamedTransformer(name, transformers));
			}
			
			return namedTransformers.toArray(new NamedTransformer[namedTransformers.size()]);
		} finally {
			parser.close();
		}
	}
	
	private Transformer<?, ?> readTransformer(JsonParser parser) throws IOException {
		JsonToken firstToken = parser.nextToken();
		if (firstToken == JsonToken.END_ARRAY) {
			return null;
		}
		
		if (firstToken != JsonToken.START_OBJECT) {
			throw new IOException("Expected: " + JsonToken.START_OBJECT + ". Got: " + firstToken);
		}
		assertField(parser, FilterIO.ID_FIELD);
		String id = parser.nextTextValue();
		assertField(parser, FilterIO.PARAMETERS_FIELD);
		assertNextToken(parser, JsonToken.START_OBJECT);
		
		Transformer<?, ?> transformer = transformerManager.createTransformer(id);
		Map<String, Object> parameters = readParameters(parser, transformer);
		assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
		
		FilterIO.applyParameters(parameters, transformer);
		
		if (transformer instanceof CompositeFilter) {
			readCompositeFilter(parser, (CompositeFilter<?, ?>) transformer);
		}
		else if (transformer instanceof SubFilterTransformer) {
			SubFilterTransformer<?,?> sft = (SubFilterTransformer<?,?>) transformer;
			readCompositeFilter(parser, sft.getCompositeFilter());
		}
		else {
			assertNextToken(parser, JsonToken.END_OBJECT);
		}
		return transformer;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void readCompositeFilter(JsonParser parser, CompositeFilter composite) throws IOException {
		JsonToken firstToken = parser.nextToken();
		if(firstToken == JsonToken.END_OBJECT) {
			return;
		}
		if(firstToken != JsonToken.FIELD_NAME) {
			throw new IOException("Expected: " + JsonToken.FIELD_NAME + ". Got: " + firstToken);
		}
		assertEquals(FilterIO.TRANSFORMERS_FIELD, parser.getCurrentName());
		assertEquals(JsonToken.START_ARRAY, parser.nextToken());
		while (true) {
			Filter filter = (Filter) readTransformer(parser);
			if (filter == null) {
				break;
			}
			composite.append(filter);
		}
		assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());
		assertNextToken(parser, JsonToken.END_OBJECT);
	}

	private Map<String, Object> readParameters(JsonParser parser, Transformer<?, ?> transformer) throws IOException {
		Map<String, Object> parameters = new HashMap<String, Object>();
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
			String name = parser.getCurrentName();
			parser.nextToken();
			Object value = readCurrentValue(parser);
			parameters.put(name, value);
		}
		return parameters;
	}

	private Object readCurrentValue(JsonParser parser) throws IOException {
		switch (parser.getCurrentToken()) {
		case VALUE_FALSE:
		case VALUE_TRUE:
			return parser.getBooleanValue();
		case VALUE_NUMBER_FLOAT:
			return parser.getDoubleValue();
		case VALUE_NUMBER_INT:
			return parser.getLongValue();
		case VALUE_STRING:
			return parser.getText();
		case START_ARRAY:
			return readArray(parser);
		default:
			return null;
		}
	}
	
	private List<Object> readArray(JsonParser parser) throws IOException {
		List<Object> list = new ArrayList<Object>();
		while (parser.nextToken() != JsonToken.END_ARRAY) {
			list.add(readCurrentValue(parser));
		}
		return list;
	}

	private void assertField(JsonParser parser, String name) throws IOException {
		assertNextToken(parser, JsonToken.FIELD_NAME);
		assertEquals(name, parser.getCurrentName());
	}

	private void assertEquals(Object expected, Object actual) throws IOException {
		if (expected == null) {
			if (actual == null) {
				return;
			} else {
				throw new IOException("Expected null");
			}
		}
		
		if (!expected.equals(actual)) {
			throw new IOException("Expected: " + expected + ". Got: " + actual);
		}
	}

	void assertNextToken(JsonParser parser, JsonToken token) throws IOException {
		if (parser.nextToken() != token) {
			throw new IOException("Expected: " + token +". Got: " + parser.getCurrentToken());
		}
	}
}
