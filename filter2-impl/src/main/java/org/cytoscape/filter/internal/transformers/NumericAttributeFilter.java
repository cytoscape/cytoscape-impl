package org.cytoscape.filter.internal.transformers;

import java.util.List;

import org.cytoscape.filter.internal.predicates.NumericPredicateDelegate;
import org.cytoscape.filter.internal.predicates.NumericPredicateDelegates;
import org.cytoscape.filter.predicates.NumericPredicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.Tunable;

public class NumericAttributeFilter extends AttributeFilter {
	private NumericPredicateDelegate delegate;
	private NumericPredicate predicate;
	
	@Tunable
	public Number criterion;
	
	@Tunable
	public NumericPredicate getPredicate() {
		return predicate;
	}
	
	@Tunable
	public void setPredicate(NumericPredicate predicate) {
		this.predicate = predicate;
		delegate = NumericPredicateDelegates.get(predicate);
	}
	
	@Override
	public String getId() {
		return Transformers.NUMERIC_ATTRIBUTE_FILTER;
	}
	
	@Override
	public String getName() {
		return "Numeric Attribute";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean accepts(CyNetwork context, CyIdentifiable element) {
		if (elementType != null && !elementType.isAssignableFrom(element.getClass())) {
			return false;
		}
		
		CyRow row = context.getRow(element);
		CyColumn column = row.getTable().getColumn(attributeName);
		Class<?> columnType = column.getType();
		Class<?> listElementType = column.getListElementType();
		if (columnType.equals(List.class) && Number.class.isAssignableFrom(listElementType)) {
			List<Number> list = (List<Number>) row.getList(attributeName, listElementType);
			for (Number number : list) {
				if (delegate.accepts(criterion, number)) {
					return true;
				}
			}
		}
		if (Number.class.isAssignableFrom(columnType)) {
			Number value = row.get(attributeName, (Class<Number>) columnType);
			return delegate.accepts(criterion, value);
		}
		return false;
	}
}
