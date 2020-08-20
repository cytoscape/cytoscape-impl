package org.cytoscape.browser.internal.equation;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.equations.Function;
import org.cytoscape.model.CyColumn;

public class TutorialItems {

	public static final String FUNCTIONS = "functions";
	public static final String ATTRIBUTES = "attributes";
	public static final String OPERATORS = "operators";
	public static final String LITERALS = "literal values";
	
	public static List<String> getTutorialItems() {
		return Arrays.asList(FUNCTIONS, ATTRIBUTES, OPERATORS, LITERALS);
	}
	
	
	public static String getFunctionDocs(Function f) {
		return f.getFunctionSummary() + "\n\n" + f.getUsageDescription();
	}
	
	
	public static String getTutorialDocs(String item) {
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
	
	
	public static String getColumnDocs(CyColumn f) {
		StringBuilder sb = new StringBuilder();
		sb.append("Full Name: ").append(f.getName()).append("\n");
		sb.append("Namespace: ").append(f.getNamespace() == null ? "-none-" : f.getNamespace()).append("\n");
		sb.append("Type: ");
		
		var t = f.getType();
		if(List.class.equals(t)) {
			sb.append("List of ");
			t = f.getListElementType();
		}
		
		if(String.class.equals(t))
			sb.append("String");
		else if(Long.class.equals(t))
			sb.append("Long Integer");
		else if(Integer.class.equals(t))
			sb.append("Integer");
		else if(Double.class.equals(t))
			sb.append("Floating Point");
		else if(Boolean.class.equals(t))
			sb.append("Boolean");
		
		return sb.toString();
	}
}
