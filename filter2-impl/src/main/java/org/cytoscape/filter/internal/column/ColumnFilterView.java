package org.cytoscape.filter.internal.column;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.cytoscape.filter.internal.view.BooleanComboBox;
import org.cytoscape.filter.internal.view.RangeChooser;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;


public interface ColumnFilterView {
	static class ColumnComboBoxElement implements Comparable<ColumnComboBoxElement> {
		public final Class<?> columnType;
		public final String name;

		final String description;
		
		public ColumnComboBoxElement(Class<?> columnType, String name) {
			this.columnType = columnType;
			this.name = name;
			
			if (CyNode.class.equals(columnType)) {
				description = "Node: " + name;
			} else if (CyEdge.class.equals(columnType)) {
				description = "Edge: " + name;
			} else {
				description = name;
			}
		}
		
		@Override
		public String toString() {
			return description;
		}
		
		@Override
		public int compareTo(ColumnComboBoxElement other) {
			if (columnType == null && other.columnType == null) {
				return String.CASE_INSENSITIVE_ORDER.compare(name, other.name);
			}
			
			if (columnType == null) {
				return -1;
			}
			
			if (other.columnType == null) {
				return 1;
			}
			
			if (columnType.equals(other.columnType)) {
				return String.CASE_INSENSITIVE_ORDER.compare(name, other.name);
			}
			
			if (columnType.equals(CyNode.class)) {
				return -1;
			}
			
			return 1;
		}
	}
	
	static class PredicateElement {
		public final Predicate predicate;
		public final String description;
		
		public PredicateElement(Predicate predicate, String description) {
			this.predicate = predicate;
			this.description = description;
		}
		
		@Override
		public String toString() {
			return description;
		}
	}

	JTextField getField();

	JCheckBox getCaseSensitiveCheckBox();

	JComboBox getNameComboBox();

	JComboBox getPredicateComboBox();

	BooleanComboBox getBooleanComboBox();
	
	RangeChooser getRangeChooser();
}
