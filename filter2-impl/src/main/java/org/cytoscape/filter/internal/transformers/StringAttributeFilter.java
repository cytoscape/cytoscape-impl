package org.cytoscape.filter.internal.transformers;

import java.util.List;

import org.cytoscape.filter.internal.predicates.StringPredicateDelegate;
import org.cytoscape.filter.internal.predicates.StringPredicateDelegates;
import org.cytoscape.filter.predicates.StringPredicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Tunable;

public final class StringAttributeFilter extends AttributeFilter {
	private String lowerCaseCriterion;
	private StringPredicateDelegate delegate;

	private StringPredicate predicate;
	private String criterion;
	
	@Tunable
	public boolean caseSensitive;
	
	@Tunable
	public void setCriterion(String criterion) {
		this.criterion = criterion;
		this.lowerCaseCriterion = criterion.toLowerCase();
	}
	
	@Tunable
	public StringPredicate getPredicate() {
		return predicate;
	}
	
	@Tunable
	public void setPredicate(StringPredicate predicate) {
		this.predicate = predicate;
		delegate = StringPredicateDelegates.get(predicate);
	}
	
	@Override
	public String getId() {
		return Transformers.STRING_ATTRIBUTE_FILTER;
	}
	
	@Override
	public String getName() {
		return "String Attribute";
	}
	
	@Override
	public boolean accepts(CyNetwork context, CyIdentifiable element) {
		if (elementType != null && !elementType.isAssignableFrom(element.getClass())) {
			return false;
		}
		
		CyRow row = context.getRow(element);
		CyTable table = row.getTable();
		CyColumn column = table.getColumn(attributeName);
		Class<?> columnType = column.getType();
		if (columnType.equals(List.class) && column.getListElementType().equals(String.class)) {
			List<String> list = row.getList(attributeName, String.class);
			for (String item : list) {
				if (delegate.accepts(criterion, lowerCaseCriterion, item, caseSensitive)) {
					return true;
				}
			}
		} else if (columnType.equals(String.class)) {
			String value = row.get(attributeName, String.class);
			return delegate.accepts(criterion, lowerCaseCriterion, value, caseSensitive);
		}
		return false;
	}
}
