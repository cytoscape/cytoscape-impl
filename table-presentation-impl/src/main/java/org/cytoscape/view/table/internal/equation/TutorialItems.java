package org.cytoscape.view.table.internal.equation;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.Function;
import org.cytoscape.model.CyColumn;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class TutorialItems {

	public static final String FUNCTIONS = "functions";
	public static final String ATTRIBUTES = "attributes";
	public static final String OPERATORS = "operators";
	public static final String LITERALS = "literal values";
	public static final String CONDITIONAL = "conditional IF";
	
	public static List<String> getTutorialItems() {
		return Arrays.asList(FUNCTIONS, ATTRIBUTES, OPERATORS, LITERALS, CONDITIONAL);
	}
	
	public static String getFunctionDocs(Function f) {
		String usage = f.getUsageDescription();
		if(usage.startsWith("Call with "))
			usage = usage.substring(10);
		if(usage.endsWith("."))
			usage = usage.substring(0, usage.length()-1);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<b>").append(usage).append("</b>&nbsp;&nbsp;&nbsp;<a href=\"\">insert</a><br><br>");
		sb.append(f.getFunctionSummary());
		sb.append("<br><br>");
		
		List<ArgDescriptor> args = f.getArgumentDescriptors();
		if(args != null && !args.isEmpty()) {
			sb.append("Arguments:<br>");
			for(ArgDescriptor arg : args) {
				String name = arg.getArgName();
				String type = getArgTypeText(arg.getArgType());
				String desc = arg.getDescription();
				sb.append("&nbsp;&nbsp;"); // indent
				sb.append(name).append(" [").append(type).append("] - ").append(desc).append("<br>");
			}
		}
		
		sb.append("</html>");
		return sb.toString();
	}
	
	private static String getArgTypeText(ArgType argType) {
		switch(argType) {
			case STRICT_STRING:
			case STRING:
			case STRINGS:
			case OPT_STRICT_STRING:
			case OPT_STRING:
			case OPT_STRINGS:
				return "string";
			case BOOL:
			case BOOLS:
			case OPT_BOOL:
			case OPT_BOOLS:
			case OPT_STRICT_BOOL:
			case STRICT_BOOL:
				return "boolean";
			case FLOAT:
			case FLOATS:
			case INT:
			case INTS:
			case OPT_STRICT_FLOAT:
			case OPT_STRICT_INT:
			case OPT_FLOAT:
			case OPT_FLOATS:
			case OPT_INT:
			case OPT_INTS:
			case STRICT_FLOAT:
			case STRICT_INT:
				return "number";
			case ANY:
			case ANY_LIST:
			case OPT_ANY_LIST:
			case STRICT_ANY_LIST:
			default:
				return "any";
		}
	}
	
	public static String getTutorialDocs(String item) {
		switch(item) {
		case FUNCTIONS:
			return 
				"<html>Cytoscape provides a set of functions that perform specific tasks and calculations. <br><br>" +
				"Example:  <b>MAX(10, 20)</b></html>\n";
		case ATTRIBUTES:
			return
				"<html>" +
				"Attributes are named references to columns in the same table as the equation. " +
				"An attribute reference is written by placing the column name after a dollarsign (<b>$</b>). <br><br>" +
				"Example: <b>$columnName</b> <br><br>" +
				"If the column name contains spaces, special characters or a namespace identifier then the name must be placed between curly braces.<br><br> " +
				"Example: <b>${column name with spaces}</b> <br>" +
				"Example: <b>${namespace::columnName}</b> <br><br>" +
				"Special characters such as commas must be escaped with a leading backslash.<br><br>" +
				"Example: <b>${name with \\, comma}</b> <br><br>" +
				"You may provide a default value that will be used if the column value is blank. Place a colon (<b>:</b>) and the value after the column name. <br><br>" +
				"Example: <b>${columnName:0.0}</b> <br>" +
				"</html>";
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
		case CONDITIONAL:
			return
				"A conditional is written as a function named 'IF' with three arguments: <b>IF(condition, a, b)</b><br>" +
				"If the condition evaluates to <b>true</b> then the value of the <b>a</b> argument is returned, " +
				"otherwise the <b>b</b> argument is returned.<br><br>" +
				"Example: <b>IF($x = $y, \"equal\", \"different\")</b> <br>";
		}
		return null;
	}
	
	public static String getColumnDocs(CyColumn col) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		
		String name = EquationEditorMediator.getAttributeReference(col);
		sb.append("<b>").append(name).append("</b>&nbsp;&nbsp;&nbsp;<a href=\"\">insert</a><br><br>");
		
		if(col.isPrimaryKey() && "SUID".equals(col.getName())) {
			sb.append("Every node and edge has an SUID attribute that acts as a unique identifier.<br><br>");
		}
		
		sb.append("Full Name: ").append(col.getName()).append("<br>");
		sb.append("Namespace: ").append(col.getNamespace() == null ? "-none-" : col.getNamespace()).append("<br>");
		sb.append("Type: ");
		
		var t = col.getType();
		if(List.class.equals(t)) {
			sb.append("List of ");
			t = col.getListElementType();
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
