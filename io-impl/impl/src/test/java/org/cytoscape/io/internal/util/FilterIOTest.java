package org.cytoscape.io.internal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cytoscape.filter.internal.TransformerManagerImpl;
import org.cytoscape.filter.internal.filters.column.ColumnFilter;
import org.cytoscape.filter.internal.filters.column.ColumnFilterFactory;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterFactory;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterImpl;
import org.cytoscape.filter.internal.filters.degree.DegreeFilter;
import org.cytoscape.filter.internal.filters.degree.DegreeFilterFactory;
import org.cytoscape.filter.internal.filters.topology.TopologyFilter;
import org.cytoscape.filter.internal.filters.topology.TopologyFilterFactory;
import org.cytoscape.filter.internal.transformers.adjacency.AdjacencyTransformer;
import org.cytoscape.filter.internal.transformers.adjacency.AdjacencyTransformer.Action;
import org.cytoscape.filter.internal.transformers.adjacency.AdjacencyTransformer.EdgesAre;
import org.cytoscape.filter.internal.transformers.adjacency.AdjacencyTransformer.What;
import org.cytoscape.filter.internal.transformers.adjacency.AdjacencyTransformerFactory;
import org.cytoscape.filter.model.AbstractTransformer;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.CompositeFilter.Type;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.FilterFactory;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.io.internal.read.transformer.CyTransformerReaderImpl;
import org.cytoscape.io.internal.write.transformer.CyTransformerWriterImpl;
import org.cytoscape.io.write.CyTransformerWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.junit.Assert;
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
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testNestedFilterRoundTrip() throws Exception {
		DegreeFilter degreeFilter = new DegreeFilter();
		degreeFilter.setCriterion(new Double[] { 0.2, 5.5 });
		degreeFilter.setEdgeType(CyEdge.Type.ANY);
		
		CompositeFilter<CyNetwork, CyIdentifiable> composite = new CompositeFilterImpl<CyNetwork, CyIdentifiable>(CyNetwork.class, CyIdentifiable.class);
		composite.setType(Type.ANY);
		composite.append(degreeFilter);
		
		CyTransformerWriter writer = new CyTransformerWriterImpl();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		writer.write(stream, FilterIO.createNamedTransformer("filter1", composite));
		String serialized = stream.toString("utf-8");
		
		TransformerManagerImpl transformerManager = new TransformerManagerImpl();
		Map<String, String> properties = Collections.emptyMap();
		transformerManager.registerFilterFactory(new DegreeFilterFactory(), properties);
		transformerManager.registerFilterFactory(new CompositeFilterFactory<CyNetwork, CyIdentifiable>(CyNetwork.class, CyIdentifiable.class), properties);
		
		CyTransformerReaderImpl reader = new CyTransformerReaderImpl();
		reader.registerTransformerManager(transformerManager, null);
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(stream.toByteArray());
		NamedTransformer<?, ?>[] transformers = reader.read(inputStream);
		
		Assert.assertNotNull(transformers);
		Assert.assertEquals(1, transformers.length);
		
		NamedTransformer<?, ?> namedTransformer = transformers[0];
		Assert.assertNotNull(namedTransformer);
		Assert.assertEquals("filter1", namedTransformer.getName());
		
		List children = namedTransformer.getTransformers();
		Assert.assertEquals(1, children.size());
		
		Transformer child = (Transformer) children.get(0);
		Assert.assertNotNull(child);
		Assert.assertTrue(child instanceof CompositeFilter);
		
		CompositeFilter composite2 = (CompositeFilter) child;
		Assert.assertEquals(composite.getType(), composite2.getType());
		Assert.assertEquals(composite.getLength(), composite2.getLength());
		
		Filter filter = composite2.get(0);
		Assert.assertTrue(filter instanceof DegreeFilter);
		
		DegreeFilter degreeFilter2 = (DegreeFilter) filter;
		Assert.assertArrayEquals((Number[]) degreeFilter.getCriterion(), (Number[]) degreeFilter2.getCriterion());
		Assert.assertEquals(degreeFilter.getEdgeType(), degreeFilter2.getEdgeType());
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testSubFilterTransformerRoundTrip() throws Exception {
		ColumnFilter columnFilter = new ColumnFilter();
		columnFilter.setColumnName("MyColName");
		columnFilter.setPredicateAndCriterion(Predicate.CONTAINS, "blah");
		
		AdjacencyTransformer adjacency = new AdjacencyTransformer();
		adjacency.setAction(Action.REPLACE);
		adjacency.setEdgesAre(EdgesAre.INCOMING);
		adjacency.setFilterTarget(What.EDGES);
		adjacency.setOutput(What.EDGES);
		adjacency.getCompositeFilter().append(columnFilter);
		
		CyTransformerWriter writer = new CyTransformerWriterImpl();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		writer.write(stream, FilterIO.createNamedTransformer("transformer1", adjacency));
		//String serialized = stream.toString("utf-8");
		
		TransformerManagerImpl transformerManager = new TransformerManagerImpl();
		Map<String, String> properties = Collections.emptyMap();
		transformerManager.registerFilterFactory(new ColumnFilterFactory(), properties);
		transformerManager.registerElementTransformerFactory(new AdjacencyTransformerFactory(), properties);
		
		CyTransformerReaderImpl reader = new CyTransformerReaderImpl();
		reader.registerTransformerManager(transformerManager, null);
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(stream.toByteArray());
		NamedTransformer<?, ?>[] transformers = reader.read(inputStream);
		
		Assert.assertNotNull(transformers);
		Assert.assertEquals(1, transformers.length);
		
		NamedTransformer<?, ?> namedTransformer = transformers[0];
		Assert.assertNotNull(namedTransformer);
		Assert.assertEquals("transformer1", namedTransformer.getName());
		
		List children = namedTransformer.getTransformers();
		Assert.assertEquals(1, children.size());
		
		Transformer child = (Transformer) children.get(0);
		Assert.assertNotNull(child);
		Assert.assertTrue(child instanceof AdjacencyTransformer);
		
		AdjacencyTransformer adjacency2 = (AdjacencyTransformer) child;
		Assert.assertEquals(adjacency.getAction(), adjacency2.getAction());
		Assert.assertEquals(adjacency.getEdgesAre(), adjacency2.getEdgesAre());
		Assert.assertEquals(adjacency.getFilterTarget(), adjacency2.getFilterTarget());
		Assert.assertEquals(adjacency.getOutput(), adjacency2.getOutput());
		
		CompositeFilter<?,?> composite = adjacency2.getCompositeFilter();
		Assert.assertEquals(1, composite.getLength()); 
		Filter filter = composite.get(0);
		Assert.assertTrue(filter instanceof ColumnFilter);
		
		ColumnFilter columnFilter2 = (ColumnFilter) filter;
		Assert.assertEquals(columnFilter.getColumnName(), columnFilter2.getColumnName());
		Assert.assertEquals(columnFilter.getPredicate(), columnFilter2.getPredicate());
		Assert.assertEquals(columnFilter.getCriterion(), columnFilter2.getCriterion());
	}
	
	/**
	 * The topology filter was made a composite filter in 3.3. Older versions of cytoscape
	 * will export a topology filter without the "transformers" field that is part of 
	 * composite filters. The filter json parser was enhanced to support this.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void testLoadOldTopologyFilter() throws IOException {
		TransformerManagerImpl transformerManager = new TransformerManagerImpl();
		Map<String, String> properties = Collections.emptyMap();
		transformerManager.registerFilterFactory(new CompositeFilterFactory<CyNetwork, CyIdentifiable>(CyNetwork.class, CyIdentifiable.class), properties);
		transformerManager.registerFilterFactory(new TopologyFilterFactory(), properties);
		transformerManager.registerFilterFactory(new ColumnFilterFactory(), properties);
		
		String path = "./src/test/resources/testData/filter/topology-filter-3.2.json";
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		//String contents = new String(encoded);
		//System.out.println(contents);
		
		CyTransformerReaderImpl reader = new CyTransformerReaderImpl();
		reader.registerTransformerManager(transformerManager, null);
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(encoded);
		NamedTransformer<?, ?>[] transformers = reader.read(inputStream);
		
		Assert.assertNotNull(transformers);
		Assert.assertEquals(1, transformers.length);
		
		NamedTransformer<?, ?> namedTransformer = transformers[0];
		Assert.assertNotNull(namedTransformer);
		Assert.assertEquals("Default filter", namedTransformer.getName());
		
		List children = namedTransformer.getTransformers();
		Assert.assertEquals(1, children.size());
		
		Transformer child = (Transformer) children.get(0);
		Assert.assertNotNull(child);
		Assert.assertTrue(child instanceof CompositeFilter);
		
		CompositeFilter composite = (CompositeFilter) child;
		Assert.assertEquals(2, composite.getLength());
		Assert.assertTrue(composite.get(0) instanceof TopologyFilter);
		Assert.assertTrue(composite.get(1) instanceof ColumnFilter);
		
		TopologyFilter topology = (TopologyFilter) composite.get(0);
		Assert.assertEquals(0, topology.getLength());
		Assert.assertEquals(Integer.valueOf(4), topology.getDistance());
		Assert.assertEquals(Integer.valueOf(5), topology.getThreshold());
		
		ColumnFilter column = (ColumnFilter) composite.get(1);
		Assert.assertEquals("name", column.getColumnName());
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
