package org.cytoscape.filter.internal.predicates;

import java.util.regex.Pattern;

import org.cytoscape.filter.predicates.Predicate;


public class PredicateDelegates {
	public static NumericPredicateDelegate getNumericDelegate(Predicate predicate) {
		if (predicate == null) {
			return null;
		}
		
		switch (predicate) {
		case IS:
			return IsDelegate.instance;
		case IS_NOT:
			return IsNotDelegate.instance;
		case GREATER_THAN:
			return GreaterThanDelegate.instance;
		case GREATER_THAN_OR_EQUAL:
			return GreaterThanOrEqualDelegate.instance;
		case LESS_THAN:
			return LessThanDelegate.instance;
		case LESS_THAN_OR_EQUAL:
			return LessThanOrEqualDelegate.instance;
		case BETWEEN:
			return BetweenDelegate.instance;
		default:
			return UnsupportedOperationDelegate.instance;
		}
	}
	
	public static StringPredicateDelegate getStringDelegate(Predicate predicate) {
		if (predicate == null) {
			return null;
		}

		switch (predicate) {
		case IS:
			return IsDelegate.instance;
		case IS_NOT:
			return IsNotDelegate.instance;
		case CONTAINS:
			return ContainsDelegate.instance;
		case DOES_NOT_CONTAIN:
			return DoesNotContainDelegate.instance;
		case REGEX:
			return new RegExDelegate();
		default:
			return UnsupportedOperationDelegate.instance;
		}
	}

	static class UnsupportedOperationDelegate implements StringPredicateDelegate, NumericPredicateDelegate {
		static UnsupportedOperationDelegate instance = new UnsupportedOperationDelegate();

		@Override
		public boolean accepts(Number lowerBound, Number upperBound, Number value) {
			return false;
		}

		@Override
		public boolean accepts(String criterion, String lowerCaseCriterion, String value, boolean caseSensitive) {
			return false;
		}
		
	}
	
	static class IsDelegate implements NumericPredicateDelegate, StringPredicateDelegate {
		static IsDelegate instance = new IsDelegate();
		
		@Override
		public boolean accepts(Number lowerBound, Number upperBound, Number value) {
			if (value == null) {
				return false;
			}
			return lowerBound.doubleValue() == value.doubleValue(); 
		}
		
		@Override
		public boolean accepts(String criterion, String lowerCaseCriterion, String value, boolean caseSensitive) {
			if (value == null) {
				return false;
			}
			if (caseSensitive) {
				return value.equals(criterion);
			}
			return value.equalsIgnoreCase(criterion);
		}

	}
	
	static class IsNotDelegate implements NumericPredicateDelegate, StringPredicateDelegate {
		static IsNotDelegate instance = new IsNotDelegate();
		
		@Override
		public boolean accepts(Number lowerBound, Number upperBound, Number value) {
			if (value == null) {
				return false;
			}
			return lowerBound.doubleValue() != value.doubleValue(); 
		}
		
		@Override
		public boolean accepts(String criterion, String lowerCaseCriterion, String value, boolean caseSensitive) {
			if (value == null) {
				return true;
			}
			if (caseSensitive) {
				return !value.equals(criterion);
			}
			return !value.equalsIgnoreCase(criterion);
		}
	}
	
	static class LessThanDelegate implements NumericPredicateDelegate {
		static NumericPredicateDelegate instance = new LessThanDelegate();
		
		@Override
		public boolean accepts(Number lowerBound, Number upperBound, Number value) {
			if (value == null) {
				return false;
			}
			return Double.compare(value.doubleValue(), lowerBound.doubleValue()) < 0; 
		}
	}
	
	static class LessThanOrEqualDelegate implements NumericPredicateDelegate {
		static NumericPredicateDelegate instance = new LessThanOrEqualDelegate();
		
		@Override
		public boolean accepts(Number lowerBound, Number upperBound, Number value) {
			if (value == null) {
				return false;
			}
			return Double.compare(value.doubleValue(), lowerBound.doubleValue()) <= 0; 
		}
	}
	
	static class GreaterThanDelegate implements NumericPredicateDelegate {
		static NumericPredicateDelegate instance = new GreaterThanDelegate();
		
		@Override
		public boolean accepts(Number lowerBound, Number upperBound, Number value) {
			if (value == null) {
				return false;
			}
			return Double.compare(value.doubleValue(), lowerBound.doubleValue()) > 0; 
		}
	}
	
	static class GreaterThanOrEqualDelegate implements NumericPredicateDelegate {
		static NumericPredicateDelegate instance = new GreaterThanOrEqualDelegate();
		
		@Override
		public boolean accepts(Number lowerBound, Number upperBound, Number value) {
			if (value == null) {
				return false;
			}
			return Double.compare(value.doubleValue(), lowerBound.doubleValue()) >= 0; 
		}
	}
	
	static class ContainsDelegate implements StringPredicateDelegate {
		static StringPredicateDelegate instance = new ContainsDelegate();

		@Override
		public boolean accepts(String criterion, String lowerCaseCriterion, String value, boolean caseSensitive) {
			if (value == null) {
				return false;
			}
			if (caseSensitive) {
				return value.contains(criterion);
			}
			return value.toLowerCase().contains(lowerCaseCriterion);
		}
	}
	
	static class DoesNotContainDelegate implements StringPredicateDelegate {
		static StringPredicateDelegate instance = new DoesNotContainDelegate();
		
		@Override
		public boolean accepts(String criterion, String lowerCaseCriterion, String value, boolean caseSensitive) {
			if (value == null) {
				return true;
			}
			if (caseSensitive) {
				return !value.contains(criterion);
			}
			return !value.toLowerCase().contains(lowerCaseCriterion);
		}
	}
	
	static class RegExDelegate implements StringPredicateDelegate {
		String lastCriterion;
		Pattern caseSensitivePattern;
		Pattern caseInsensitivePattern;
		
		@Override
		public boolean accepts(String criterion, String lowerCaseCriterion, String value, boolean caseSensitive) {
			if (value == null) {
				return false;
			}
			if (lastCriterion == null || !criterion.equals(lastCriterion)) {
				lastCriterion = criterion;
				caseSensitivePattern = Pattern.compile(criterion);
				caseInsensitivePattern = Pattern.compile(criterion, Pattern.CASE_INSENSITIVE);
			}
			if (caseSensitive) {
				return caseSensitivePattern.matcher(value).matches();
			}
			return caseInsensitivePattern.matcher(value).matches();
		}
	}
	
	static class BetweenDelegate implements NumericPredicateDelegate {
		static NumericPredicateDelegate instance = new BetweenDelegate();
		
		@Override
		public boolean accepts(Number lowerBound, Number upperBound, Number value) {
			if (lowerBound == null || upperBound == null || value == null) {
				return false;
			}
			double value2 = value.doubleValue();
			return (lowerBound == null || Double.compare(value2, lowerBound.doubleValue()) >= 0)
				&& (upperBound == null || Double.compare(value2, upperBound.doubleValue()) <= 0);
		}
	}
}
