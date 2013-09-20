package org.cytoscape.filter.internal.attribute;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.cytoscape.filter.internal.prefuse.JRangeSliderExtended;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;


public interface AttributeFilterView {
	static class AttributeComboBoxElement implements Comparable<AttributeComboBoxElement> {
		public final Class<?> attributeType;
		public final String name;

		final String description;
		
		public AttributeComboBoxElement(Class<?> attributeType, String name) {
			this.attributeType = attributeType;
			this.name = name;
			
			if (CyNode.class.equals(attributeType)) {
				description = "Node: " + name;
			} else if (CyEdge.class.equals(attributeType)) {
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
		public int compareTo(AttributeComboBoxElement other) {
			if (attributeType == null && other.attributeType == null) {
				return String.CASE_INSENSITIVE_ORDER.compare(name, other.name);
			}
			
			if (attributeType == null) {
				return -1;
			}
			
			if (attributeType.equals(other.attributeType)) {
				return String.CASE_INSENSITIVE_ORDER.compare(name, other.name);
			}
			
			if (attributeType.equals(CyNode.class)) {
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

	JRangeSliderExtended getSlider();

	JCheckBox getCaseSensitiveCheckBox();

	JComboBox getNameComboBox();

	JComboBox getPredicateComboBox();
}
