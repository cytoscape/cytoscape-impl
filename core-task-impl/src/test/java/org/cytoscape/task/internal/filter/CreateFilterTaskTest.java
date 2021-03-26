package org.cytoscape.task.internal.filter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cytoscape.filter.TransformerContainer;
import org.cytoscape.filter.internal.filters.column.ColumnFilter;
import org.cytoscape.filter.internal.filters.column.ColumnFilterFactory;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterFactory;
import org.cytoscape.filter.internal.filters.degree.DegreeFilter;
import org.cytoscape.filter.internal.filters.degree.DegreeFilterFactory;
import org.cytoscape.filter.internal.filters.topology.TopologyFilter;
import org.cytoscape.filter.internal.filters.topology.TopologyFilterFactory;
import org.cytoscape.filter.internal.work.TransformerManagerImpl;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.io.internal.read.transformer.CyTransformerReaderImpl;
import org.cytoscape.io.internal.write.transformer.CyTransformerWriterImpl;
import org.cytoscape.io.read.CyTransformerReader;
import org.cytoscape.io.write.CyTransformerWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.Gson;

public class CreateFilterTaskTest {
	
	@Mock CyServiceRegistrar serviceRegistrar;
	private TestTransformerContainer transformerContainer;
	
	private class TestTransformerContainer implements TransformerContainer<CyNetwork,CyIdentifiable> {
		private Map<String,NamedTransformer<CyNetwork,CyIdentifiable>> map = new HashMap<>();
		
		@Override
		public void addNamedTransformer(NamedTransformer<CyNetwork, CyIdentifiable> transformer) {
			map.put(transformer.getName(), transformer);
		}
		@Override
		public boolean removeNamedTransformer(String name) {
			return map.remove(name) != null;
		}
		@Override
		public NamedTransformer<CyNetwork, CyIdentifiable> getNamedTransformer(String name) {
			return map.get(name);
		}
		@Override
		public List<NamedTransformer<CyNetwork, CyIdentifiable>> getNamedTransformers() {
			return new ArrayList<>(map.values());
		}
	}
	
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		transformerContainer = new TestTransformerContainer();
		
		TransformerManagerImpl transformerManager = new TransformerManagerImpl();
		transformerManager.registerFilterFactory(new ColumnFilterFactory(), Collections.emptyMap());
		transformerManager.registerFilterFactory(new DegreeFilterFactory(), Collections.emptyMap());
		transformerManager.registerFilterFactory(new TopologyFilterFactory(), Collections.emptyMap());
		transformerManager.registerFilterFactory(new CompositeFilterFactory<>(CyNetwork.class, CyIdentifiable.class), Collections.emptyMap());
		
		CyTransformerReaderImpl transformerReader = new CyTransformerReaderImpl();
		transformerReader.registerTransformerManager(transformerManager, Collections.emptyMap());
		when(serviceRegistrar.getService(CyTransformerReader.class)).thenReturn(transformerReader);

		CyTransformerWriterImpl transformerWriter = new CyTransformerWriterImpl();
		when(serviceRegistrar.getService(CyTransformerWriter.class)).thenReturn(transformerWriter);
		
		when(serviceRegistrar.getService(TransformerContainer.class, "(container.type=filter)")).thenReturn(transformerContainer);
	}
	
	
	private static void runTask(Task task) {
		TaskMonitor tm = mock(TaskMonitor.class);
		try {
			task.run(tm);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		verify(tm, times(0)).showMessage(any(), any());
	}
	
	
	private static class NamedTransformerImpl implements NamedTransformer<CyNetwork, CyIdentifiable> {
		String name;
		private List<Transformer<CyNetwork, CyIdentifiable>> transformers;
		
		@SuppressWarnings("unchecked")
		public NamedTransformerImpl(String name, Transformer<?, ?>... transformers) {
			this.name = name;
			
			this.transformers = new CopyOnWriteArrayList<Transformer<CyNetwork, CyIdentifiable>>();
			for (Transformer<?, ?> transformer : transformers) {
				this.transformers.add((Transformer<CyNetwork, CyIdentifiable>) transformer);
			}
		}
		
		@Override
		public String getName() {
			return name;
		}

		@Override
		public List<Transformer<CyNetwork, CyIdentifiable>> getTransformers() {
			return transformers;
		}
	}
	
	
	@Test
	public void testListFilters() {
		transformerContainer.addNamedTransformer(new NamedTransformerImpl("name 1"));
		transformerContainer.addNamedTransformer(new NamedTransformerImpl("name 2"));
		transformerContainer.addNamedTransformer(new NamedTransformerImpl("name 3"));
		
		ListFiltersTask task = new ListFiltersTask(serviceRegistrar);
		runTask(task);
		
		@SuppressWarnings("unchecked")
		List<String> names = task.getResults(List.class);
		
		assertEquals(3, names.size());
		assertTrue(names.contains("name 1"));
		assertTrue(names.contains("name 2"));
		assertTrue(names.contains("name 3"));
	}
	
	@Test
	public void testGetFilter() {
		transformerContainer.addNamedTransformer(new NamedTransformerImpl("name 1"));
		
		GetFilterTask task = new GetFilterTask(serviceRegistrar);
		task.name = "name 1";
		runTask(task);
		
		String json = task.getResults(String.class);
		
		@SuppressWarnings("rawtypes")
		Map fields = (Map)new Gson().fromJson(json, List.class).get(0);
		assertEquals("name 1", fields.get("name"));
	}
	
	@Test
	public void testCreateColumnFilterString() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter", false);
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : \"1\", \"columnName\" : \"name\", \"predicate\" : \"CONTAINS\"} }";
		runTask(task);
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = transformerContainer.getNamedTransformer(task.name);
		assertNotNull(transformer);
		
		ColumnFilter columnFilter = (ColumnFilter)transformer.getTransformers().get(0);
		assertEquals(Predicate.CONTAINS, columnFilter.getPredicate());
		assertEquals("1", columnFilter.getCriterion());
		assertEquals("name", columnFilter.getColumnName());
	}
	
	@Test
	public void testCreateColumnFilterNumber() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter", false);
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"predicate\" : \"BETWEEN\", \"criterion\" : [1,1], \"columnName\" : \"name\"} }";
		runTask(task);
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = transformerContainer.getNamedTransformer(task.name);
		assertNotNull(transformer);
		
		ColumnFilter columnFilter = (ColumnFilter)transformer.getTransformers().get(0);
		assertEquals(Predicate.BETWEEN, columnFilter.getPredicate());
		assertArrayEquals(new Number[] { 1l, 1l }, (Number[])columnFilter.getCriterion());
		assertEquals("name", columnFilter.getColumnName());
	}
	
	@Test
	public void testCreateColumnFilterBoolean() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter", false);
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : false, \"columnName\" : \"name\", \"predicate\" : \"IS\"} }";
		runTask(task);
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = transformerContainer.getNamedTransformer(task.name);
		assertNotNull(transformer);
		
		ColumnFilter columnFilter = (ColumnFilter)transformer.getTransformers().get(0);
		assertEquals(Predicate.IS, columnFilter.getPredicate());
		assertEquals(false, columnFilter.getCriterion());
		assertEquals("name", columnFilter.getColumnName());
	}
	
	
	@Test
	public void testCreateColumnFilterCommandBad1() throws Exception {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter", false);
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : \"1\", \"columnName\" : \"name\", \"predicate\" : \"BLARF\"} }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, atLeastOnce()).showMessage(eq(Level.ERROR), any());
	}
	
	@Test
	public void testCreateColumnFilterCommandBad2() throws Exception {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter", false);
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : \"1\", \"columnName\" : \"name\", \"predicate\" : \"BETWEEN\"} }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, atLeastOnce()).showMessage(eq(Level.ERROR), any());
	}
	
	@Test
	public void testCreateColumnFilterCommandBad3() throws Exception {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter", false);
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : [1,1], \"columnName\" : \"name\", \"predicate\" : \"IS\"} }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, atLeastOnce()).showMessage(eq(Level.ERROR), any());
	}
	
	@Test
	public void testCreateColumnFilterCommandBad4() throws Exception {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter", false);
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : false, \"columnName\" : \"name\", \"predicate\" : \"BETWEEN\"} }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, atLeastOnce()).showMessage(eq(Level.ERROR), any());
	}

	
	@Test
	public void testCreateDegreeFilter() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter", false);
		task.jsonTunable.json = "{ \"id\" : \"DegreeFilter\", \"parameters\" : { \"predicate\" : \"BETWEEN\", \"criterion\" : [ 0, 1 ], \"edgeType\" : \"ANY\" } }";
		runTask(task);
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = transformerContainer.getNamedTransformer(task.name);
		assertNotNull(transformer);
		
		DegreeFilter filter = (DegreeFilter)transformer.getTransformers().get(0);
		assertEquals(Predicate.BETWEEN, filter.getPredicate());
		assertArrayEquals(new Number[] { 0l, 1l }, (Number[])filter.getCriterion());
		assertEquals(CyEdge.Type.ANY, filter.getEdgeType());
	}
	
	@Test
	public void testCreateTopologyFilter() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter", false);
		String subFilterJson  = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : \"1\", \"columnName\" : \"name\", \"predicate\" : \"CONTAINS\"} }";
		task.jsonTunable.json = "{ \"id\" : \"TopologyFilter\", \"parameters\" : { \"predicate\" : \"GREATER_THAN_OR_EQUAL\", \"distance\" : 3, \"threshold\" : 2, \"type\" : \"ALL\" }, \"transformers\" : [ " + subFilterJson + "] }";
		runTask(task);
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = transformerContainer.getNamedTransformer(task.name);
		assertNotNull(transformer);
		
		TopologyFilter topoFilter = (TopologyFilter)transformer.getTransformers().get(0);
		assertEquals(Predicate.GREATER_THAN_OR_EQUAL, topoFilter.getPredicate());
		assertEquals(new Integer(3), topoFilter.getDistance());
		assertEquals(new Integer(2), topoFilter.getThreshold());
		assertEquals(CompositeFilter.Type.ALL, topoFilter.getType());
		assertEquals(1, topoFilter.getLength());
		
		ColumnFilter columnFilter = (ColumnFilter)topoFilter.get(0);
		assertEquals(Predicate.CONTAINS, columnFilter.getPredicate());
		assertEquals("1", columnFilter.getCriterion());
		assertEquals("name", columnFilter.getColumnName());
	}
	
	@Test
	public void testCreateCompositeFilter() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter", false);
		String subFilterJson  = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : \"1\", \"columnName\" : \"name\", \"predicate\" : \"CONTAINS\"} }";
		task.jsonTunable.json = "{ \"id\" : \"org.cytoscape.CompositeFilter\", \"parameters\" : { \"type\" : \"ALL\" }, \"transformers\" : [ " + subFilterJson + " ] }";
		runTask(task);
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = transformerContainer.getNamedTransformer(task.name);
		assertNotNull(transformer);
		
		@SuppressWarnings("rawtypes")
		CompositeFilter compositeFilter = (CompositeFilter) transformer.getTransformers().get(0);
		assertEquals(CompositeFilter.Type.ALL, compositeFilter.getType());
		assertEquals(1, compositeFilter.getLength());
		
		ColumnFilter columnFilter = (ColumnFilter)compositeFilter.get(0);
		assertEquals(Predicate.CONTAINS, columnFilter.getPredicate());
		assertEquals("1", columnFilter.getCriterion());
		assertEquals("name", columnFilter.getColumnName());
	}
	
}
