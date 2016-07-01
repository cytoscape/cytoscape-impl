package org.cytoscape.filter.internal.filters.column;

import static org.cytoscape.filter.model.ValidationWarning.warn;

import java.util.Collections;
import java.util.List;

import org.cytoscape.filter.internal.predicates.NumericPredicateDelegate;
import org.cytoscape.filter.internal.predicates.PredicateDelegates;
import org.cytoscape.filter.internal.predicates.StringPredicateDelegate;
import org.cytoscape.filter.model.AbstractTransformer;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.ValidatableTransformer;
import org.cytoscape.filter.model.ValidationWarning;
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

public class ColumnFilter extends AbstractTransformer<CyNetwork, CyIdentifiable> 
	implements 	Filter<CyNetwork, CyIdentifiable>, 
				ValidatableTransformer<CyNetwork, CyIdentifiable> {
	
	public static final String NODES = "nodes";
	public static final String EDGES = "edges";
	public static final String NODES_AND_EDGES = "nodes+edges";
	
	@Tunable
	public final ListSingleSelection<String> type;
	
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
	private Boolean booleanCriterion;
	
	public ColumnFilter() {
		type = new ListSingleSelection<String>(NODES, EDGES, NODES_AND_EDGES);
		type.addListener(new ListChangeListener<String>() {
			@Override
			public void selectionChanged(ListSelection<String> source) {
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
	
	public void setCriterion(Object criterion) {
		try {
			setCriterionImpl(criterion);
		} finally {
			notifyListeners();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setCriterionImpl(Object criterion) {
		rawCriterion = null;
		lowerBound = null;
		upperBound = null;
		stringCriterion = null;
		lowerCaseCriterion = null;
		
		if (isListOfNumber(criterion)) {
			List<Number> list = (List<Number>) criterion;
			lowerBound = list.get(0);
			upperBound = list.get(1);
			rawCriterion = new Number[] { lowerBound, upperBound };
		} else if (criterion instanceof Number[]) {
			Number[] range = (Number[]) criterion;
			lowerBound = range[0];
			upperBound = range[1];
			rawCriterion = criterion;
		} else if (criterion instanceof Number) {
			lowerBound = (Number) criterion;
			upperBound = lowerBound;
			rawCriterion = criterion;
		} else if (criterion instanceof String) {
			stringCriterion = criterion.toString();
			lowerCaseCriterion = stringCriterion.toLowerCase();
			rawCriterion = criterion;
		} else if (criterion instanceof Boolean) {
			booleanCriterion = (Boolean)criterion;
			rawCriterion = criterion;
		}
	}
	
	private static boolean isListOfNumber(Object criterion) {
		if(criterion instanceof List) {
			List<?> list = (List<?>) criterion;
			return list.size() >= 2 && list.get(0) instanceof Number && list.get(1) instanceof Number;
		}
		return false;
	}
	
	@Tunable
	public Predicate getPredicate() {
		return predicate;
	}
	
	public void setPredicate(Predicate predicate) {
		setPredicateImpl(predicate);
		notifyListeners();
	}
	
	private void setPredicateImpl(Predicate predicate) {
		this.predicate = predicate;
		numericDelegate = PredicateDelegates.getNumericDelegate(predicate);
		stringDelegate = PredicateDelegates.getStringDelegate(predicate);
	}
	
	/**
	 * Sets both predicate and criterion, updates listeners only once.
	 */
	public void setPredicateAndCriterion(Predicate predicate, Object criterion) {
		try {
			setPredicateImpl(predicate);
			setCriterionImpl(criterion);
		} finally {
			notifyListeners();
		}
	}

	public Class<? extends CyIdentifiable> getTableType() {
		String typeString = type.getSelectedValue();
		if(NODES.equals(typeString))
			return CyNode.class;
		if(EDGES.equals(typeString))
			return CyEdge.class;
		else
			return null;
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
	
	@Override
	public List<ValidationWarning> validate(CyNetwork context) {
		CyTable table;
		String tableType;
		Class<?> type = getTableType();
		if(CyNode.class.equals(type)) {
			table = context.getDefaultNodeTable();
			tableType = "Node";
		} else if(CyEdge.class.equals(type)) {
			table = context.getDefaultEdgeTable();
			tableType = "Edge";
		} else {
			return Collections.emptyList();
		}
		
		if(columnName != null) {
			CyColumn column = table.getColumn(columnName);
			if(column == null) {
				return warn(tableType + " table does not have column <b>\"" + columnName + "\"</b>");
			}
		}
		
		if (rawCriterion == null || predicate == null) {
			// This filter is incomplete. 
			return Collections.emptyList();
		}
		
		CyColumn column = table.getColumn(columnName);
		Class<?> columnType = column.getType();
		if (columnType.equals(List.class)) {
			columnType = column.getListElementType();
		}
		
		if(String.class.equals(columnType) && stringCriterion == null) {
			return warn("Column <b>\"" + columnName + "\"</b> in " + tableType + " table is not a String column");
		}
		else if(Number.class.isAssignableFrom(columnType) && (lowerBound == null || upperBound == null)) {
			return warn("Column <b>\"" + columnName + "\"</b> in " + tableType + " table is not a Numeric column");
		}
		else if(Boolean.class.equals(columnType) && booleanCriterion == null) {
			return warn("Column <b>\"" + columnName + "\"</b> in " + tableType + " table is not a Boolean column");
		}
		
		return Collections.emptyList();
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean accepts(CyNetwork context, CyIdentifiable element) {
		if (columnName == null || rawCriterion == null || predicate == null) {
			// This filter is incomplete. 
			return false;
		}
		
		Class<?> tableType = getTableType();
		if (tableType != null && !tableType.isAssignableFrom(element.getClass())) {
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
				if (list != null) {
					for (String item : list) {
						if (stringDelegate.accepts(stringCriterion, lowerCaseCriterion, item, caseSensitive)) {
							return true;
						}
					}
				}
			} else if (Number.class.isAssignableFrom(listElementType)) {
				List<Number> list = (List<Number>) row.getList(columnName, listElementType);
				if (list != null) {
					for (Number number : list) {
						if (numericDelegate.accepts(lowerBound, upperBound, number)) {
							return true;
						}
					}
				}
			} else if (Boolean.class.equals(listElementType)) {
				List<Boolean> list = (List<Boolean>) row.getList(columnName, listElementType);
				if (list != null) {
					for (Boolean value : list) {
						if (value != null && booleanCriterion.equals(value)) {
							return true;
						}
					}
				}
			}
		} else if (columnType.equals(String.class)) {
			String value = row.get(columnName, String.class);
			return stringDelegate.accepts(stringCriterion, lowerCaseCriterion, value, caseSensitive);
		} else if (Number.class.isAssignableFrom(columnType)) {
			Number value = row.get(columnName, (Class<Number>) columnType);
			return numericDelegate.accepts(lowerBound, upperBound, value);
		} else if (Boolean.class.equals(columnType)) {
			Boolean value = row.get(columnName, Boolean.class);
			return booleanCriterion.equals(value);
		}
		return false;
	}

	@Override
	public String toString() {
		Class<?> tableType = getTableType();
		return "ColumnFilter [type=" + (type == null ? null : type.getSelectedValue())
				+ ", elementType=" + (tableType == null ? null : tableType.getSimpleName()) 
				+ ", predicate=" + predicate
				+ ", caseSensitive="
				+ caseSensitive + ", columnName=" + columnName + ", rawCriterion=" + rawCriterion + ", lowerBound="
				+ lowerBound + ", upperBound=" + upperBound + ", stringCriterion=" + stringCriterion
				+ ", lowerCaseCriterion=" + lowerCaseCriterion + ", booleanCriterion=" + booleanCriterion + "]";
	}

	
	
}
