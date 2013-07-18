package org.cytoscape.filter.internal.predicates;

import java.util.regex.Pattern;

import org.cytoscape.filter.predicates.StringPredicate;

public class StringPredicateDelegates {
	public static StringPredicateDelegate get(StringPredicate predicate) {
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
		}
		throw new UnsupportedOperationException();
	}
	
	static class IsDelegate implements StringPredicateDelegate {
		static StringPredicateDelegate instance = new IsDelegate();
		
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
	
	static class IsNotDelegate implements StringPredicateDelegate {
		static StringPredicateDelegate instance = new IsNotDelegate();

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
}
