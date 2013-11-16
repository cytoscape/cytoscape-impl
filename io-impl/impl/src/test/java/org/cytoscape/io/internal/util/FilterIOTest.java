package org.cytoscape.io.internal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;

import junit.framework.Assert;

import org.cytoscape.filter.internal.TransformerManagerImpl;
import org.cytoscape.filter.internal.degree.DegreeFilter;
import org.cytoscape.filter.internal.degree.DegreeFilterFactory;
import org.cytoscape.filter.model.AbstractTransformer;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.FilterFactory;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.io.internal.read.transformer.CyTransformerReaderImpl;
import org.cytoscape.io.internal.write.transformer.CyTransformerWriterImpl;
import org.cytoscape.io.write.CyTransformerWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.junit.Test;

public class FilterIOTest {
	
	@Test
	public void testSerializationRoundTrip() throws Exception {
		DegreeFilter degreeFilter = new DegreeFilter();
		degreeFilter.setCriterion(new Double[] { 0.2, 5.5 });
		degreeFilter.setEdgeType(CyEdge.Type.ANY);

		DummyFilter dummyFilter = new DummyFilter();
		
		CyTransformerWriter writer = new CyTransformerWriterImpl();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		writer.write(stream, FilterIO.createNamedTransformer("filter1", degreeFilter), FilterIO.createNamedTransformer("filter2", dummyFilter));
		String serialized = stream.toString("utf-8");
		
		TransformerManagerImpl transformerManager = new TransformerManagerImpl();
		Map<String, String> properties = Collections.emptyMap();
		transformerManager.registerFilterFactory(new DegreeFilterFactory(), properties);
		transformerManager.registerFilterFactory(new DummyFilterFactory(), properties);
		
		CyTransformerReaderImpl reader = new CyTransformerReaderImpl();
		reader.registerTransformerManager(transformerManager, null);
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(stream.toByteArray());
		NamedTransformer<?, ?>[] transformers = reader.read(inputStream);
		
		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		writer.write(stream2, transformers);
		
		String serialized2 = stream2.toString("utf-8");
		Assert.assertEquals(serialized, serialized2);
	}
	
	public static class DummyFilter extends AbstractTransformer<Object, Object> implements Filter<Object, Object> {
		@Tunable
		public int intField;
		
		@Tunable
		public long longField;
		
		@Tunable
		public short shortField;
		
		@Tunable
		public char charField;
		
		@Tunable
		public float floatField;
		
		@Tunable
		public double doubleField;
		
		@Tunable
		public boolean booleanField;
		
		@Tunable
		public byte byteField;
		
		@Tunable
		public Integer intObjectField = 1;
		
		@Tunable
		public Long longObjectField = 1L;
		
		@Tunable
		public Short shortObjectField = 1;
		
		@Tunable
		public Character charObjectField = '1';
		
		@Tunable
		public Float floatObjectField = 1.0f;
		
		@Tunable
		public Double doubleObjectField = 1.0;
		
		@Tunable
		public Boolean booleanObjectField = false;
		
		@Tunable
		public Byte byteObjectField = 1;

		@Tunable
		public String stringField;
		
		@Tunable
		public int[] intArrayField = new int[] { 1, 2, 3 };
		
		@Tunable
		public Integer[] integerArrayField = new Integer[] { 1, 2, 3 };
		
		@Tunable
		public ListSingleSelection<Object> listSingleSelection = new ListSingleSelection<Object>("Hello", "World");
		
		@Tunable
		public ListMultipleSelection<Object> listMultipleSelection = new ListMultipleSelection<Object>("Hello", "World");

		public DummyFilter() {
			listSingleSelection.setSelectedValue(listSingleSelection.getPossibleValues().get(1));
			listMultipleSelection.setSelectedValues(listMultipleSelection.getPossibleValues());
		}
		
		@Override
		public String getName() {
			return "Dummy";
		}

		@Override
		public String getId() {
			return "org.cytoscape.internal.Dummy";
		}
		
		@Override
		public boolean accepts(Object context, Object element) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Class<Object> getContextType() {
			return Object.class;
		}

		@Override
		public Class<Object> getElementType() {
			return Object.class;
		}
	}
	
	static class DummyFilterFactory implements FilterFactory<Object, Object> {

		@Override
		public String getId() {
			return "org.cytoscape.internal.Dummy";
		}

		@Override
		public Filter<Object, Object> createFilter() {
			return new DummyFilter();
		}
		
	}
}
