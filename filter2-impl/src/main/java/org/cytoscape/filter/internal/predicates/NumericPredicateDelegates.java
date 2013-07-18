package org.cytoscape.filter.internal.predicates;

import org.cytoscape.filter.predicates.NumericPredicate;


public class NumericPredicateDelegates {
	public static NumericPredicateDelegate get(NumericPredicate predicate) {
		switch (predicate) {
		case EQUALS:
			return EqualsDelegate.instance;
		case DOES_NOT_EQUAL:
			return DoesNotEqualDelegate.instance;
		case GREATER_THAN:
			return GreaterThanDelegate.instance;
		case GREATER_THAN_OR_EQUAL:
			return GreaterThanOrEqualDelegate.instance;
		case LESS_THAN:
			return LessThanDelegate.instance;
		case LESS_THAN_OR_EQUAL:
			return LessThanOrEqualDelegate.instance;
		}
		throw new UnsupportedOperationException();
	}
	
	static class EqualsDelegate implements NumericPredicateDelegate {
		static NumericPredicateDelegate instance = new EqualsDelegate();
		
		@Override
		public boolean accepts(Number criterion, Number value) {
			if (value == null) {
				return false;
			}
			return criterion.doubleValue() == value.doubleValue(); 
		}
	}
	
	static class DoesNotEqualDelegate implements NumericPredicateDelegate {
		static NumericPredicateDelegate instance = new DoesNotEqualDelegate();
		
		@Override
		public boolean accepts(Number criterion, Number value) {
			if (value == null) {
				return false;
			}
			return criterion.doubleValue() != value.doubleValue(); 
		}
	}
	
	static class LessThanDelegate implements NumericPredicateDelegate {
		static NumericPredicateDelegate instance = new LessThanDelegate();
		
		@Override
		public boolean accepts(Number criterion, Number value) {
			if (value == null) {
				return false;
			}
			return Double.compare(value.doubleValue(), criterion.doubleValue()) < 0; 
		}
	}
	
	static class LessThanOrEqualDelegate implements NumericPredicateDelegate {
		static NumericPredicateDelegate instance = new LessThanOrEqualDelegate();
		
		@Override
		public boolean accepts(Number criterion, Number value) {
			if (value == null) {
				return false;
			}
			return Double.compare(value.doubleValue(), criterion.doubleValue()) <= 0; 
		}
	}
	
	static class GreaterThanDelegate implements NumericPredicateDelegate {
		static NumericPredicateDelegate instance = new GreaterThanDelegate();
		
		@Override
		public boolean accepts(Number criterion, Number value) {
			if (value == null) {
				return false;
			}
			return Double.compare(value.doubleValue(), criterion.doubleValue()) > 0; 
		}
	}
	
	static class GreaterThanOrEqualDelegate implements NumericPredicateDelegate {
		static NumericPredicateDelegate instance = new GreaterThanOrEqualDelegate();
		
		@Override
		public boolean accepts(Number criterion, Number value) {
			if (value == null) {
				return false;
			}
			return Double.compare(value.doubleValue(), criterion.doubleValue()) >= 0; 
		}
	}
}
