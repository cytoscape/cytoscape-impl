package org.cytoscape.view.table.internal.equation;

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
		String usage = f.getUsageDescription();
		if(usage.startsWith("Call with "))
			usage = usage.substring(10);
		if(usage.endsWith("."))
			usage = usage.substring(0, usage.length()-1);
		return "<html><b>" + usage + "</b>&nbsp;&nbsp;&nbsp;<a href=\"\">insert</a><br><br>" + f.getFunctionSummary() + "</html>";
	}
	
	
	public static String getTutorialDocs(String item) {
		switch(item) {
		case FUNCTIONS:
			return 
				"<html>Cytoscape provides a set of functions that perform specific tasks and calculations. <br><br>" +
				"Example:  <b>MAX(10, 20)</b></html>\n";
		case ATTRIBUTES:
			return
				"<html>Attributes are named references to columns in the same table as the equation. <br><br>" +
				"Example: <b>$columnName</b> <br>" +
				"Example: <b>${column name with spaces}</b> </html>";
		case OPERATORS:
			return
				"<html>" +
				"Numeric operators: <br>" +
			    " <b>+</b>  addition," +
				" <b>-</b>  subtraction," + 
				" <b>*</b>  multiplication," +
				" <b>/</b>  division," +
				" <b>^</b>  exponentiation <br><br> " +
				"Text operators: <br>" +
				" <b>&</b>  string concatenation <br><br>" +
				"Logical operators (operate on boolean values true/false): <br>" +
				" <b>&lt;</b>  less than, " +
				" <b>&gt;</b>  greater than, " +
				" <b>&gt;=</b> greater than or equal, " +
				" <b>&lt;=</b> less than or equal, <br>" +
				" <b>=</b>  equal, " +
				" <b>&lt;&gt;</b> not equal <br><br>" +
				"Example: <b>$x + 1</b>" +
			    "</html>";
		case LITERALS:
			return
				"String (text) literals are between double quotes, example: <b>\"abc\"</b> <br>" +
				"Numeric literals, example: <b>123</b> <br>" +
				"Floating point literals, example:  <b>123.45</b> <br>" +
				"Boolean (logical) literals: <b>true</b>, <b>false</b> <br>";
		}
		return null;
	}
	
	
	public static String getColumnDocs(CyColumn f) {
		StringBuilder sb = new StringBuilder("<html>");
		sb.append("<b>${").append(f.getName()).append("}</b>&nbsp;&nbsp;&nbsp;<a href=\"\">insert</a><br><br>");
		sb.append("Full Name: ").append(f.getName()).append("<br>");
		sb.append("Namespace: ").append(f.getNamespace() == null ? "-none-" : f.getNamespace()).append("<br>");
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
		
		sb.append("</html>");
		return sb.toString();
	}
}
