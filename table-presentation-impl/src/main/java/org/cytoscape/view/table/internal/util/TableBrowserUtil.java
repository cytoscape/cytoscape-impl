package org.cytoscape.view.table.internal.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;

import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.table.internal.impl.BrowserTable;
import org.cytoscape.view.table.internal.impl.BrowserTableModel;

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
public final class TableBrowserUtil {

	public static final String LINE_BREAK = "\n";
	public static final String CELL_BREAK = "\t";
	
	private static final int EOF = -1;

	public static CyTableView getTableView(JTable table) {
		var browserTable = (BrowserTable) table;
		var model = (BrowserTableModel) browserTable.getModel();

		return model.getTableView();
	}
	
	public static String createCopyString(ValidatedObjectAndEditString cellValue) {
		// Encode cell data in Excel format so we can copy/paste list attributes as multi-line cells.
		var sb = new StringBuffer();
		var validatedObj = cellValue.getValidatedObject();

		if (validatedObj instanceof Collection) {
			sb.append("\"");
			boolean firstRow = true;

			for (var member : (Collection<?>) validatedObj) {
				if (!firstRow)
					sb.append("\r");
				else
					firstRow = false;

				sb.append(member.toString().replaceAll("\"", "\"\""));
			}

			sb.append("\"");
		} else {
			var text = validatedObj != null ? validatedObj.toString() : null;
			sb.append(text != null ? escape(text) : "");
		}
		
		return sb.toString();
	}

	/**
	 *  Creates a Map with the CyColumn names and their types as mapped to the types used by attribute equations.
	 *  Types (and associated names) not used by attribute equations are omitted.
	 *
	 *  @param table the attributes to map
	 *  @param ignore if not null, skip the attribute with this name
	 */
	public static Map<String, Class<?>> getAttNameToTypeMap(CyTable table, String ignore) {
		var map = new HashMap<String, Class<?>>();

		for (var column : table.getColumns())
			map.put(column.getName(), column.getType());

		if (ignore != null)
			map.remove(ignore);

		return map;
	}
	
	public static Object parseLong(String text, StringBuilder errorMessage) {
		try {
			return Long.valueOf(text);
		} catch (Exception e) {
			errorMessage.append("Can't convert text to a whole number.");
			return null;
		}
	}

	public static Object parseInteger(String text, StringBuilder errorMessage) {
		try {
			return Integer.valueOf(text);
		} catch (Exception e) {
			errorMessage.append("Can't convert text to a whole number.");
			return null;
		}
	}

	public static Object parseDouble(String text, StringBuilder errorMessage) {
		try {
			return Double.valueOf(text);
		} catch (Exception e) {
			errorMessage.append("Can't convert text to a floating point number.");
			return null;
		}
	}

	public static Object parseBoolean(String text, StringBuilder errorMessage) {
		if (text.compareToIgnoreCase("true") == 0)
			return Boolean.valueOf(true);

		if (text.compareToIgnoreCase("false") == 0)
			return Boolean.valueOf(false);

		errorMessage.append("Can't convert text to a truth value.");
		return null;
	}

	public static List<Object> parseCellInput(CyTable dataTable, String columnName, Object value){
		var text = (String)value;
		var columnType = dataTable.getColumn(columnName).getType();

		Object parsedValue;
		var errorMessage = new StringBuilder();

		if (columnType == String.class)
			parsedValue = text;
		else if (columnType == Long.class)
			parsedValue = TableBrowserUtil.parseLong(text, errorMessage);
		else if (columnType == Integer.class)
			parsedValue = TableBrowserUtil.parseInteger(text, errorMessage);
		else if (columnType == Double.class)
			parsedValue = TableBrowserUtil.parseDouble(text, errorMessage);
		else if (columnType == Boolean.class)
			parsedValue = TableBrowserUtil.parseBoolean(text, errorMessage);
		else if (columnType == List.class)
			parsedValue =
				parseList(text, dataTable.getColumn(columnName).getListElementType(),
						errorMessage);
		else
			throw new IllegalStateException("unknown column type: "
					+ columnType.getName() + ".");

		var retValue = new ArrayList<Object>();
		retValue.add(parsedValue);;
		retValue.add(errorMessage);

		return retValue;
	}

	static enum ListParserState {
		OPENING_BRACE_EXPECTED, COMMA_OR_CLOSING_BRACE_EXPECTED,
		ITEM_OR_CLOSING_BRACE_EXPECTED, ITEM_EXPECTED, END_OF_INPUT_EXPECTED
	};

	@SuppressWarnings("fallthrough")
	static List<Object> parseList(String text, Class<?> listElementType, StringBuilder errorMessage) {
		var newList = new ArrayList<Object>();
		var reader = new StringReader(text);
		var state = ListParserState.OPENING_BRACE_EXPECTED;
		
		for (;;) {
			int ch = EOF;
			
			try {
				reader.mark(0);
				ch = reader.read();
			} catch (IOException e) {
				throw new IllegalStateException("We should *never* get here.");
			}

			if (ch == '\n' || ch == '\t' || ch == ' ')
				continue;

			switch (state) {
			case OPENING_BRACE_EXPECTED:
				if (ch == '[') {
					state = ListParserState.ITEM_OR_CLOSING_BRACE_EXPECTED;
					break;
				} else {
					errorMessage.append("List must start with '['.");
					return null;
				}
			case ITEM_OR_CLOSING_BRACE_EXPECTED:
				if (ch == ']') {
					state = ListParserState.END_OF_INPUT_EXPECTED;
					break;
				}
			case ITEM_EXPECTED:
				if (ch == EOF) {
					errorMessage.append("Premature end of list.");
					return null;
				}
				try {
					reader.reset();
				} catch (IOException e) {
					throw new IllegalStateException("We should *never* get here.");
				}

				var item = getListItem(reader, listElementType, errorMessage);
				
				if (item == null)
					return null;
				
				newList.add(item);
				state = ListParserState.COMMA_OR_CLOSING_BRACE_EXPECTED;
				break;
			case COMMA_OR_CLOSING_BRACE_EXPECTED:
				if (ch == ']') {
					state = ListParserState.END_OF_INPUT_EXPECTED;
					break;
				} else if (ch == ',') {
					state = ListParserState.ITEM_EXPECTED;
					break;
				} else {
					errorMessage.append("Unexpected character(s) in list detected.");
					return null;
				}
			case END_OF_INPUT_EXPECTED:
				if (ch != EOF) {
					errorMessage.append("Unexpected garbage after end of list.");
					return null;
				}

				return newList;
			}
		}
	}

	private static Object getListItem(StringReader reader, Class<?> listElementType, StringBuilder errorMessage) {
		if (listElementType == Double.class) {
			return getDouble(reader, errorMessage);
		} else if (listElementType == String.class) {
			return getString(reader, errorMessage);
		} else if (listElementType == Integer.class || listElementType == Long.class) {
			// Process optional leading sign:
			int ch = EOF;
			
			try {
				reader.mark(0);
				ch = reader.read();
			} catch (IOException e) {
				throw new IllegalStateException("We should *never* get here.");
			}
			
			var builder = new StringBuilder();
			
			if (ch == '-') {
				builder.append((char)ch);
			} else if (ch == '+') {
				/* Intentionally empty. */;
			} else {
				try {
					reader.reset();
				} catch (IOException e) {
					throw new IllegalStateException("We should *never* get here.");
				}
			}

			grabAsciiDigits(reader, builder);
			
			try {
				if (listElementType == Integer.class)
					return Integer.valueOf(builder.toString());
				else
					return Long.valueOf(builder.toString());
			} catch (NumberFormatException e) {
				errorMessage.append("Found invalid integer or long integer list item.");
				return null;
			}
		} else if (listElementType == Boolean.class) {
			var builder = new StringBuilder();
			grabAsciiLetters(reader, builder);
			var boolValueCandidate = builder.toString();
			
			if (boolValueCandidate.equalsIgnoreCase("true")) {
				return Boolean.valueOf(true);
			} else if (boolValueCandidate.equalsIgnoreCase("false")) {
				return Boolean.valueOf(false);
			} else {
				errorMessage.append("\"" + boolValueCandidate
						+ "\" is not a valid boolean list item.");
				return null;
			}
		} else
			throw new IllegalStateException("unknown list element type: "
					+ listElementType.getName() + ".");
	}


	private static Double getDouble(StringReader reader, StringBuilder errorMessage) {
		try {
			reader.mark(0);
			int ch = reader.read();
			if (ch == EOF) {
				errorMessage.append("Unexpected end of input while trying to read a floating point number.");
				return null;
			}

			var builder = new StringBuilder();

			// Process optional leading sign:
			if (ch == '-' || ch == '+')
				builder.append((char)ch);
			else
				reader.reset();

			int savedLength = builder.length();
			grabAsciiDigits(reader, builder);
			boolean needAfterDecimalPointDigits = builder.length() == savedLength;

			// Process optional decimal point followed by zero or more digits:
			reader.mark(0);
			ch = reader.read();
			if (ch != '.') {
				if (needAfterDecimalPointDigits) {
					errorMessage.append("Bad or missing floating point list item.");
					return null;
				}
				reader.reset();
			} else {
				builder.append('.');
				savedLength = builder.length();
				grabAsciiDigits(reader, builder);
				if (needAfterDecimalPointDigits && savedLength == builder.length()) {
					errorMessage.append("Bad or missing floating point list item.");
					return null;
				}
			}

			// Process optional exponent:
			reader.mark(0);
			ch = reader.read();
			if (ch != 'e' && ch != 'E')
				reader.reset();
			else {
				builder.append('e');

				// Process optional sign:
				reader.mark(0);
				ch = reader.read();
				if (ch != '+' && ch != '-')
					reader.reset();
				else
					builder.append((char)ch);

				savedLength = builder.length();
				grabAsciiDigits(reader, builder);
				if (builder.length() == savedLength) {
					errorMessage.append("Invalid exponent.");
					return null;
				}
			}

			try {
				return Double.valueOf(builder.toString());
			} catch (Exception e) {
				errorMessage.append("Malformed number.");
				return null;
			}
		} catch (IOException e) {
			throw new IllegalStateException("This should *never* happen.");
		}
	}

	private static void grabAsciiDigits(StringReader reader, StringBuilder builder) {
		try {
			for (;;) {
				reader.mark(0);
				int ch = reader.read();
				if (ch == EOF || (char)ch < '0' || (char)ch > '9') {
					reader.reset();
					return;
				}

				builder.append((char)ch);
			}
		} catch (IOException e) {
			throw new IllegalStateException("This should *never* happen.");
		}
	}

	private static String getString(StringReader reader, StringBuilder errorMessage) {
		try {
			if (reader.read() != '"') {
				errorMessage.append("Strings must start with a double quote symbol.");
				return null;
			}

			var builder = new StringBuilder();
			int ch = reader.read();
			boolean escaped = false;
			
			while (escaped || ch != '"') {
				if (ch == EOF) {
					errorMessage.append("Unterminated string list item.");
					return null;
				}

				if (escaped) {
					switch (ch) {
					case 'n':
						builder.append('\n');
						break;
					case 't':
						builder.append('\t');
						break;
					case 'r':
						builder.append('\r');
						break;
					case 'f':
						builder.append('\f');
						break;
					case 'b':
						builder.append('\b');
						break;
					case '"':
						builder.append('"');
						break;
					case '\\':
						builder.append('\\');
						break;
					default:
						builder.append((char)ch);
					}
					escaped = false;
				} else if (ch == '\\')
					escaped = true;
				else
					builder.append((char)ch);

				ch = reader.read();
			}

			return builder.toString();
		} catch (IOException e) {
			throw new IllegalStateException("This should *never* happen.");
		}
	}

	private static void grabAsciiLetters(StringReader reader, StringBuilder builder) {
		try {
			for (;;) {
				reader.mark(0);
				int ch = reader.read();
				if (ch == EOF
				    || (((char)ch < 'a' || (char)ch > 'z')
					&& ((char)ch < 'A' || (char)ch > 'Z'))) {
					reader.reset();
					return;
				}

				builder.append((char)ch);
			}
		} catch (IOException e) {
			throw new IllegalStateException("This should *never* happen.");
		}
	}
	
	private static String escape(String cellValue) {
		return cellValue.replace(LINE_BREAK, " ").replace(CELL_BREAK, " ");
	}
}
