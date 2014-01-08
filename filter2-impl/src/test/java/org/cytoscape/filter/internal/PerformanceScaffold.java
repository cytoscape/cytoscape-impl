package org.cytoscape.filter.internal;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.column.ColumnFilter;
import org.cytoscape.filter.internal.degree.DegreeFilter;
import org.cytoscape.filter.internal.filters.model.AtomicFilter;
import org.cytoscape.filter.internal.filters.model.CompositeFilter;
import org.cytoscape.filter.internal.filters.model.NumericFilter;
import org.cytoscape.filter.internal.filters.model.StringFilter;
import org.cytoscape.filter.internal.filters.model.TopologyFilter;
import org.cytoscape.filter.internal.filters.util.FilterUtil;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.filter.internal.quickfind.util.QuickFindImpl;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.TransformerSink;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;

public class PerformanceScaffold {
	static final String STRING_COLUMN = "string";
	static final String INTEGER_COLUMN = "integer";
	static final String LIST_STRING_COLUMN = "list_of_string";
	
	static final int ITERATIONS = 10;
	static final int TOTAL_NODES = 100000;
	static final int AVERAGE_EDGES_PER_NODE = 4;
	
	public static void main(String[] args) {
		NetworkTestSupport testSupport = new NetworkTestSupport();
		CyNetwork network = testSupport.getNetwork();
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(STRING_COLUMN, String.class, false);
		nodeTable.createColumn(INTEGER_COLUMN, Integer.class, false);
		nodeTable.createListColumn(LIST_STRING_COLUMN, String.class, false);
		
		// Use a fixed set of string attributes.
		// Worst case performance for trie expected for first entry
		// Best case performance expected for last entry
		Random random = new Random(1);
		String[] values = {
			"AAAAAAAAAAAAAAA",
			"AAAAAAAAAAAAAAB",
			"AAAAAAAAAAAAACA",
			"AAAAAAAAAAAADAA",
			"AAAAAAAAAAAEAAA",
			"AAAAAAAAAAFAAAA",
			"AAAAAAAAAGAAAAA",
			"AAAAAAAAHAAAAAA",
			"AAAAAAAIAAAAAAA",
			"AAAAAAJAAAAAAAA",
			"AAAAAKAAAAAAAAA",
			"AAAALAAAAAAAAAA",
			"AAAMAAAAAAAAAAA",
			"AANAAAAAAAAAAAA",
			"AOAAAAAAAAAAAAA",
			"PAAAAAAAAAAAAAA",
		};
		
		int totalNodes = TOTAL_NODES;
		int totalEdges = totalNodes * AVERAGE_EDGES_PER_NODE;
		
		long start;
		
		start = System.currentTimeMillis();
		List<CyNode> nodes = new ArrayList<CyNode>();
		for (int i = 0; i < totalNodes; i++) {
			CyNode node = network.addNode();
			int valueIndex = random.nextInt(values.length);
			network.getRow(node).set(STRING_COLUMN, values[valueIndex]);
			network.getRow(node).set(INTEGER_COLUMN, valueIndex);
			network.getRow(node).set(LIST_STRING_COLUMN, Collections.singletonList(values[valueIndex]));
			nodes.add(node);
		}
		
		// Construct random graph
		for (int i = 0; i < totalEdges; i++) {
			CyNode source = nodes.get(random.nextInt(totalNodes));
			CyNode target = nodes.get(random.nextInt(totalNodes));
			network.addEdge(source, target, true);
		}
		System.out.printf("Construct\t%d\n", System.currentTimeMillis() - start);

		TransformerManagerImpl transformerManager = new TransformerManagerImpl();
		Map<String, String> properties = Collections.emptyMap();
		transformerManager.registerTransformerSource(new CyNetworkSource(), properties);
		
		List<UseCase> useCases = new ArrayList<UseCase>();
		QuickFind quickFind = new QuickFindImpl();
		useCases.add(new NumberAttributeUseCase(quickFind, 0, transformerManager));
		useCases.add(new StringAttributeUseCase(quickFind, values[values.length - 1], transformerManager));
		useCases.add(new DegreeUseCase(3, transformerManager));
		
		for (UseCase useCase : useCases) {
			useCase.execute(network, ITERATIONS);
		}
	}
	
	static abstract class UseCase {
		protected Object baselineFilter;
		protected List<Transformer<CyNetwork, CyIdentifiable>> subjectTransformers;
		protected TransformerManager transformerManager;
		
		public UseCase(TransformerManager transformerManager) {
			this.transformerManager = transformerManager;
		}

		void execute(CyNetwork network, int iterations) {
			long start;
			int hits = 0;
			
			setUp(network);
			
			start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				hits += doBaseline(network);
			}
			System.out.printf("Baseline\t%s\t%.2f\t%d\n", getName(), (System.currentTimeMillis() - start) / (double) iterations, hits);
			
			hits = 0;
			start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				hits += doSubject(network);
			}
			System.out.printf("Subject\t%s\t%.2f\t%d\n", getName(), (System.currentTimeMillis() - start) / (double) iterations, hits);
		}
		
		int doBaseline(CyNetwork network) {
			if (baselineFilter instanceof AtomicFilter) {
				AtomicFilter filter = (AtomicFilter) baselineFilter;
				filter.childChanged();
				int hits = 0;
				filter.apply();
				BitSet nodeBits = filter.getNodeBits();
				for (int index = nodeBits.nextSetBit(0); index >= 0; index = nodeBits.nextSetBit(index + 1)) {
					hits++;
				}
				return hits;
			} else if (baselineFilter instanceof CompositeFilter) {
				CompositeFilter filter = (CompositeFilter) baselineFilter;
				filter.childChanged();
				int hits = 0;
				filter.apply();
				BitSet nodeBits = filter.getNodeBits();
				for (int index = nodeBits.nextSetBit(0); index >= 0; index = nodeBits.nextSetBit(index + 1)) {
					hits++;
				}
				return hits;
			}
			throw new RuntimeException();
		}

		int doSubject(CyNetwork network) {
			final AtomicInteger hits = new AtomicInteger();
			TransformerSink<CyIdentifiable> sink = new TransformerSink<CyIdentifiable>() {
				@Override
				public void collect(CyIdentifiable element) {
					hits.incrementAndGet();
				}
			};
			transformerManager.execute(network, subjectTransformers, sink);
			return hits.intValue();
		}
		
		abstract void setUp(CyNetwork network);
		abstract String getName();
	}
	
	static class StringAttributeUseCase extends UseCase {
		private QuickFind quickFind;
		private String searchTerm;

		public StringAttributeUseCase(QuickFind quickFind, String searchTerm, TransformerManager transformerManager) {
			super(transformerManager);
			this.quickFind = quickFind;
			this.searchTerm = searchTerm;
		}
		
		@Override
		String getName() {
			return "String attribute";
		}
		
		@Override
		void setUp(CyNetwork network) {
			StringFilter filter = new StringFilter(quickFind);
			filter.setControllingAttribute(LIST_STRING_COLUMN);
			filter.setSearchStr(searchTerm);
			filter.setNetwork(network);
			baselineFilter = filter;
			
			long start = System.currentTimeMillis();
			FilterUtil.getQuickFindIndex(quickFind, filter.getControllingAttribute(), network, filter.getIndexType());
			System.out.printf("Index\t%s\t%d\n", filter.getControllingAttribute(), System.currentTimeMillis() - start);

			subjectTransformers = new ArrayList<Transformer<CyNetwork,CyIdentifiable>>();
			ColumnFilter filter2 = new ColumnFilter();
			filter2.setColumnName(LIST_STRING_COLUMN);
			filter2.setCriterion(searchTerm);
			filter2.setPredicate(Predicate.IS);
			filter2.setCaseSensitive(true);
			filter2.type.setSelectedValue("nodes");
			subjectTransformers.add(filter2);
		}
	}

	static class DegreeUseCase extends UseCase {
		private int minDegree;

		public DegreeUseCase(int minDegree, TransformerManager transformerManager) {
			super(transformerManager);
			this.minDegree = minDegree;
		}
		
		@Override
		String getName() {
			return "Degree";
		}
		
		@Override
		void setUp(CyNetwork network) {
			CyApplicationManager applicationManager = null;
			
			TopologyFilter filter = new TopologyFilter(applicationManager);
			filter.setMinNeighbors(minDegree);
			filter.setNetwork(network);
			baselineFilter = filter;
			
			subjectTransformers = new ArrayList<Transformer<CyNetwork,CyIdentifiable>>();
			DegreeFilter filter2 = new DegreeFilter();
			filter2.setCriterion(minDegree);
			filter2.setEdgeType(CyEdge.Type.ANY);
			filter2.setPredicate(Predicate.GREATER_THAN_OR_EQUAL);
			subjectTransformers.add(filter2);
		}
	}

	static class NumberAttributeUseCase extends UseCase {
		private QuickFind quickFind;
		private int searchTerm;

		public NumberAttributeUseCase(QuickFind quickFind, int searchTerm, TransformerManager transformerManager) {
			super(transformerManager);
			this.quickFind = quickFind;
			this.searchTerm = searchTerm;
		}
		
		@Override
		String getName() {
			return "Integer attribute";
		}
		
		@Override
		void setUp(CyNetwork network) {
			NumericFilter<Integer> filter = new NumericFilter<Integer>(quickFind);
			filter.setControllingAttribute(INTEGER_COLUMN);
			filter.setRange(searchTerm, searchTerm);
			filter.setNetwork(network);
			baselineFilter = filter;
			
			long start = System.currentTimeMillis();
			FilterUtil.getQuickFindIndex(quickFind, filter.getControllingAttribute(), network, filter.getIndexType());
			System.out.printf("Index\t%s\t%d\n", filter.getControllingAttribute(), System.currentTimeMillis() - start);

			subjectTransformers = new ArrayList<Transformer<CyNetwork,CyIdentifiable>>();
			ColumnFilter filter2 = new ColumnFilter();
			filter2.setColumnName(INTEGER_COLUMN);
			filter2.setCriterion(searchTerm);
			filter2.setPredicate(Predicate.IS);
			filter2.type.setSelectedValue("nodes");
			subjectTransformers.add(filter2);
		}
	}
}
