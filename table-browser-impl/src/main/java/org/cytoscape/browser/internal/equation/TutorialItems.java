package org.cytoscape.browser.internal.equation;

import java.util.Arrays;
import java.util.List;

public class TutorialItems {

	public static final String FUNCTIONS = "functions";
	public static final String ATTRIBUTES = "attributes";
	public static final String OPERATORS = "operators";
	public static final String LITERALS = "literal values";
	
	public static List<String> getItems() {
		return Arrays.asList(FUNCTIONS, ATTRIBUTES, OPERATORS, LITERALS);
	}
	
	public static String getDocs(String item) {
		switch(item) {
		case FUNCTIONS:
			return 
				"Cytoscape provides a set of functions that perform specific tasks and calculations. \n" +
				"Example:  MAX(10, 20) \n";
		case ATTRIBUTES:
			return
				"Attributes are named references to columns in the same table as the equation. \n" +
				"Example: $columnName  OR  ${column name with spaces}";
		case OPERATORS:
			return
				"Numeric operators: \n" +
			    "  +  addition," +
				"  -  subtraction," + 
				"  *  multiplication," +
				"  /  division," +
				"  ^  exponentiation \n " +
				"Text operators: \n" +
				"  &  string concatenation \n" +
				"Logical operators (operate on boolean values true/false): \n" +
				"  <  less than, " +
				"  >  greater than, " +
				"  >= greater than or equal, " +
				"  <= less than or equal, " +
				"  =  equal, " +
				"  <> not equal \n\n" +
				"Example: $x + 1";
		case LITERALS:
			return
				"String (text) literals are between double quotes, example: \"abc\" \n" +
				"Numeric literals, example: 123 \n" +
				"Floating point literals, example:  123.45 \n" +
				"Boolean (logical) literals: true, false \n";
		}
		return null;
	}
}
