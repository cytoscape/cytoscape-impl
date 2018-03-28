package org.cytoscape.task.internal.filter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.io.internal.read.transformer.CyTransformerReaderImpl;
import org.cytoscape.io.read.CyTransformerReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
		when(serviceRegistrar.getService(TransformerContainer.class, "(container.type=filter)")).thenReturn(transformerContainer);
	}
	
	
	@Test
	public void testCreateColumnFilterString() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter");
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : \"1\", \"columnName\" : \"name\", \"predicate\" : \"CONTAINS\"} }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, times(0)).showMessage(any(), any());
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = transformerContainer.getNamedTransformer(task.name);
		assertNotNull(transformer);
		
		ColumnFilter columnFilter = (ColumnFilter)transformer.getTransformers().get(0);
		assertEquals(Predicate.CONTAINS, columnFilter.getPredicate());
		assertEquals("1", columnFilter.getCriterion());
		assertEquals("name", columnFilter.getColumnName());
	}
	
	@Test
	public void testCreateColumnFilterNumber() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter");
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"predicate\" : \"BETWEEN\", \"criterion\" : [1,1], \"columnName\" : \"name\"} }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, times(0)).showMessage(any(), any());
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = transformerContainer.getNamedTransformer(task.name);
		assertNotNull(transformer);
		
		ColumnFilter columnFilter = (ColumnFilter)transformer.getTransformers().get(0);
		assertEquals(Predicate.BETWEEN, columnFilter.getPredicate());
		assertArrayEquals(new Number[] { 1l, 1l }, (Number[])columnFilter.getCriterion());
		assertEquals("name", columnFilter.getColumnName());
	}
	
	@Test
	public void testCreateColumnFilterBoolean() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter");
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : false, \"columnName\" : \"name\", \"predicate\" : \"IS\"} }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, times(0)).showMessage(any(), any());
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = transformerContainer.getNamedTransformer(task.name);
		assertNotNull(transformer);
		
		ColumnFilter columnFilter = (ColumnFilter)transformer.getTransformers().get(0);
		assertEquals(Predicate.IS, columnFilter.getPredicate());
		assertEquals(false, columnFilter.getCriterion());
		assertEquals("name", columnFilter.getColumnName());
	}
	
	
	@Test
	public void testCreateColumnFilterCommandBad1() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter");
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : \"1\", \"columnName\" : \"name\", \"predicate\" : \"BLARF\"} }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, atLeastOnce()).showMessage(eq(Level.ERROR), any());
	}
	
	@Test
	public void testCreateColumnFilterCommandBad2() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter");
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : \"1\", \"columnName\" : \"name\", \"predicate\" : \"BETWEEN\"} }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, atLeastOnce()).showMessage(eq(Level.ERROR), any());
	}
	
	@Test
	public void testCreateColumnFilterCommandBad3() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter");
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : [1,1], \"columnName\" : \"name\", \"predicate\" : \"IS\"} }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, atLeastOnce()).showMessage(eq(Level.ERROR), any());
	}
	
	@Test
	public void testCreateColumnFilterCommandBad4() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter");
		task.jsonTunable.json = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : false, \"columnName\" : \"name\", \"predicate\" : \"BETWEEN\"} }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, atLeastOnce()).showMessage(eq(Level.ERROR), any());
	}

	
	@Test
	public void testCreateDegreeFilter() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter");
		task.jsonTunable.json = "{ \"id\" : \"DegreeFilter\", \"parameters\" : { \"predicate\" : \"BETWEEN\", \"criterion\" : [ 0, 1 ], \"edgeType\" : \"ANY\" } }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, times(0)).showMessage(any(), any());
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = transformerContainer.getNamedTransformer(task.name);
		assertNotNull(transformer);
		
		DegreeFilter filter = (DegreeFilter)transformer.getTransformers().get(0);
		assertEquals(Predicate.BETWEEN, filter.getPredicate());
		assertArrayEquals(new Number[] { 0l, 1l }, (Number[])filter.getCriterion());
		assertEquals(CyEdge.Type.ANY, filter.getEdgeType());
	}
	
	@Test
	public void testCreateTopologyFilter() {
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter");
		String subFilterJson  = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : \"1\", \"columnName\" : \"name\", \"predicate\" : \"CONTAINS\"} }";
		task.jsonTunable.json = "{ \"id\" : \"TopologyFilter\", \"parameters\" : { \"predicate\" : \"GREATER_THAN_OR_EQUAL\", \"distance\" : 3, \"threshold\" : 2, \"type\" : \"ALL\" }, \"transformers\" : [ " + subFilterJson + "] }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, times(0)).showMessage(any(), any());
		
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
		CreateFilterTask task = new CreateFilterTask(serviceRegistrar, "MyFilter");
		String subFilterJson  = "{ \"id\" : \"ColumnFilter\", \"parameters\" : { \"criterion\" : \"1\", \"columnName\" : \"name\", \"predicate\" : \"CONTAINS\"} }";
		task.jsonTunable.json = "{ \"id\" : \"org.cytoscape.CompositeFilter\", \"parameters\" : { \"type\" : \"ALL\" }, \"transformers\" : [ " + subFilterJson + " ] }";
		
		TaskMonitor tm = mock(TaskMonitor.class);
		task.run(tm);
		verify(tm, times(0)).showMessage(any(), any());
		
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
