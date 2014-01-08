package org.cytoscape.filter.internal.column;

import java.util.List;

import org.cytoscape.filter.internal.predicates.NumericPredicateDelegate;
import org.cytoscape.filter.internal.predicates.PredicateDelegates;
import org.cytoscape.filter.internal.predicates.StringPredicateDelegate;
import org.cytoscape.filter.model.AbstractTransformer;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListChangeListener;
import org.cytoscape.work.util.ListSelection;
import org.cytoscape.work.util.ListSingleSelection;

public class ColumnFilter extends AbstractTransformer<CyNetwork, CyIdentifiable> implements Filter<CyNetwork, CyIdentifiable> {
	public static final String NODES = "nodes";
	public static final String EDGES = "edges";
	public static final String NODES_AND_EDGES = "nodes+edges";
	
	@Tunable
	public final ListSingleSelection<String> type;
	
	Class<? extends CyIdentifiable> elementType;
	
	private Predicate predicate;
	private NumericPredicateDelegate numericDelegate;
	private StringPredicateDelegate stringDelegate;
	
	private boolean caseSensitive;
	private String columnName;

	private Object rawCriterion;
	private Number lowerBound;
	private Number upperBound;
	private String stringCriterion;
	private String lowerCaseCriterion;
	
	public ColumnFilter() {
		type = new ListSingleSelection<String>(NODES, EDGES, NODES_AND_EDGES);
		type.addListener(new ListChangeListener<String>() {
			@Override
			public void selectionChanged(ListSelection<String> source) {
				String value = ((ListSingleSelection<String>) source).getSelectedValue();
				if (NODES.equals(value)) {
					elementType = CyNode.class;
				} else if (EDGES.equals(value)) {
					elementType = CyEdge.class;
				} else {
					elementType = null;
				}
				notifyListeners();
			}
			
			@Override
			public void listChanged(ListSelection<String> source) {
			}
		});
	}
	
	@Tunable
	public boolean getCaseSensitive() {
		return caseSensitive;
	}
	
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		notifyListeners();
	}

	@Tunable
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnName(String name) {
		columnName = name;
		notifyListeners();
	}

	@Tunable
	public Object getCriterion() {
		return rawCriterion;
	}
	
	@SuppressWarnings("unchecked")
	public void setCriterion(Object criterion) {
		try {
			if (criterion == null) {
				rawCriterion = null;
				lowerBound = null;
				upperBound = null;
				stringCriterion = null;
				lowerCaseCriterion = null;
				return;
			}
			
			rawCriterion = criterion;
			
			if (criterion instanceof List) {
				List<Number> list = (List<Number>) criterion;
				lowerBound = list.get(0);
				upperBound = list.get(1);
				rawCriterion = new Number[] { lowerBound, upperBound };
			} else if (criterion instanceof Number[]) {
				Number[] range = (Number[]) criterion;
				lowerBound = range[0];
				upperBound = range[1];
			} else if (criterion instanceof Number) {
				lowerBound = (Number) criterion;
				upperBound = lowerBound;
			}
			
			stringCriterion = criterion.toString();
			lowerCaseCriterion = stringCriterion.toLowerCase();
		} finally {
			notifyListeners();
		}
	}
	
	@Tunable
	public Predicate getPredicate() {
		return predicate;
	}
	
	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
		numericDelegate = PredicateDelegates.getNumericDelegate(predicate);
		stringDelegate = PredicateDelegates.getStringDelegate(predicate);
		notifyListeners();
	}

	public Class<? extends CyIdentifiable> getColumnType() {
		return elementType;
	}
	
	@Override
	public Class<CyNetwork> getContextType() {
		return CyNetwork.class;
	}
	
	@Override
	public Class<CyIdentifiable> getElementType() {
		return CyIdentifiable.class;
	}
	
	@Override
	public String getId() {
		return Transformers.COLUMN_FILTER;
	}
	
	@Override
	public String getName() {
		return "Column Filter";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean accepts(CyNetwork context, CyIdentifiable element) {
		if (columnName == null || rawCriterion == null || predicate == null) {
			// This filter is incomplete. 
			return false;
		}
		
		if (elementType != null && !elementType.isAssignableFrom(element.getClass())) {
			return false;
		}
		
		CyRow row = context.getRow(element);
		CyTable table = row.getTable();
		CyColumn column = table.getColumn(columnName);
		
		if (column == null) {
			return false;
		}
		
		Class<?> columnType = column.getType();
		if (columnType.equals(List.class)) {
			Class<?> listElementType = column.getListElementType();
			if (String.class.equals(listElementType)) {
				List<String> list = row.getList(columnName, String.class);
				for (String item : list) {
					if (stringDelegate.accepts(stringCriterion, lowerCaseCriterion, item, caseSensitive)) {
						return true;
					}
				}
			} else if (Number.class.isAssignableFrom(listElementType)) {
				List<Number> list = (List<Number>) row.getList(columnName, listElementType);
				for (Number number : list) {
					if (numericDelegate.accepts(lowerBound, upperBound, number)) {
						return true;
					}
				}
			}
		} else if (columnType.equals(String.class)) {
			String value = row.get(columnName, String.class);
			return stringDelegate.accepts(stringCriterion, lowerCaseCriterion, value, caseSensitive);
		} else if (Number.class.isAssignableFrom(columnType)) {
			Number value = row.get(columnName, (Class<Number>) columnType);
			return numericDelegate.accepts(lowerBound, upperBound, value);
		}
		return false;
	}
}
