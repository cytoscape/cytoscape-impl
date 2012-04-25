package org.cytoscape.browser.internal.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;

public final class TableBrowserUtil {

	private static final Class<?>[] OBJECT_TYPES = {CyNode.class, CyEdge.class, CyNetwork.class};
	
	private static final int EOF = -1;

	public static Object parseLong(final String text, final StringBuilder errorMessage) {
		try {
			return Long.valueOf(text);
		} catch (final Exception e) {
			errorMessage.append("Can't convert text to a whole number!");
			return null;
		}
	}

	public static Object parseInteger(final String text, final StringBuilder errorMessage) {
		try {
			return Integer.valueOf(text);
		} catch (final Exception e) {
			errorMessage.append("Can't convert text to a whole number!");
			return null;
		}
	}

	public static Object parseDouble(final String text, final StringBuilder errorMessage) {
		try {
			return Double.valueOf(text);
		} catch (final Exception e) {
			errorMessage.append("Can't convert text to a floating point number!");
			return null;
		}
	}

	public static Object parseBoolean(final String text, final StringBuilder errorMessage) {
		if (text.compareToIgnoreCase("true") == 0)
			return Boolean.valueOf(true);

		if (text.compareToIgnoreCase("false") == 0)
			return Boolean.valueOf(false);

		errorMessage.append("Can't convert text to a truth value!");
		return null;
	}


	///
	public static ArrayList parseCellInput(CyTable dataTable, String columnName, Object value){
		final String text = (String)value;

		final Class<?> columnType = dataTable.getColumn(columnName).getType();

		Object parsedValue;
		final StringBuilder errorMessage = new StringBuilder();

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
					+ columnType.getName() + "!");

		ArrayList retValue = new ArrayList();
		retValue.add(parsedValue);;
		retValue.add(errorMessage);

		return retValue;
	}

	static enum ListParserState {
		OPENING_BRACE_EXPECTED, COMMA_OR_CLOSING_BRACE_EXPECTED,
		ITEM_OR_CLOSING_BRACE_EXPECTED, ITEM_EXPECTED, END_OF_INPUT_EXPECTED
	};

	@SuppressWarnings (value={"unchecked", "fallthrough"})
	static List parseList(final String text, final Class<?> listElementType, final StringBuilder errorMessage) {
		final List newList = new ArrayList();
		final StringReader reader = new StringReader(text);

		ListParserState state = ListParserState.OPENING_BRACE_EXPECTED;
		for (;;) {
			int ch = EOF;
			try {
				reader.mark(0);
				ch = reader.read();
			} catch (final IOException e) {
				throw new IllegalStateException("We should *never* get here!");
			}

			if (ch == '\n' || ch == '\t' || ch == ' ')
				continue;

			switch (state) {
			case OPENING_BRACE_EXPECTED:
				if (ch == '[') {
					state = ListParserState.ITEM_OR_CLOSING_BRACE_EXPECTED;
					break;
				} else {
					errorMessage.append("List must start with '['!");
					return null;
				}
			case ITEM_OR_CLOSING_BRACE_EXPECTED:
				if (ch == ']') {
					state = ListParserState.END_OF_INPUT_EXPECTED;
					break;
				}
			case ITEM_EXPECTED:
				if (ch == EOF) {
					errorMessage.append("Premature end of list!");
					return null;
				}
				try {
					reader.reset();
				} catch (final IOException e) {
					throw new IllegalStateException("We should *never* get here!");
				}

				final Object item = getListItem(reader, listElementType, errorMessage);
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
					errorMessage.append("Unexpected character(s) in list detected!");
					return null;
				}
			case END_OF_INPUT_EXPECTED:
				if (ch != EOF) {
					errorMessage.append("Unexpected garbage after end of list!");
					return null;
				}

				return newList;
			}
		}
	}

	private static Object getListItem(final StringReader reader, final Class<?> listElementType,
			final StringBuilder errorMessage)
	{
		if (listElementType == Double.class)
			return getDouble(reader, errorMessage);
		else if (listElementType == String.class)
			return getString(reader, errorMessage);
		else if (listElementType == Integer.class || listElementType == Long.class) {
			// Process optional leading sign:
			int ch = EOF;
			try {
				reader.mark(0);
				ch = reader.read();
			} catch (final IOException e) {
				throw new IllegalStateException("We should *never* get here!");
			}
			final StringBuilder builder = new StringBuilder();
			if (ch == '-')
				builder.append((char)ch);
			else if (ch == '+')
				/* Intentionally empty! */;
			else {
				try {
					reader.reset();
				} catch (final IOException e) {
					throw new IllegalStateException("We should *never* get here!");
				}
			}

			grabAsciiDigits(reader, builder);
			try {
				if (listElementType == Integer.class)
					return Integer.valueOf(builder.toString());
				else
					return Long.valueOf(builder.toString());
			} catch (final NumberFormatException e) {
				errorMessage.append("Found invalid integer or long integer list item!");
				return null;
			}
		} else if (listElementType == Boolean.class) {
			final StringBuilder builder = new StringBuilder();
			grabAsciiLetters(reader, builder);
			final String boolValueCandidate = builder.toString();
			if (boolValueCandidate.equalsIgnoreCase("true"))
				return Boolean.valueOf(true);
			else if (boolValueCandidate.equalsIgnoreCase("false"))
				return Boolean.valueOf(false);
			else {
				errorMessage.append("\"" + boolValueCandidate
						+ "\" is not a valid boolean list item!");
				return null;
			}
		} else
			throw new IllegalStateException("unknown list element type: "
					+ listElementType.getName() + "!");
	}


	private static Double getDouble(final StringReader reader, final StringBuilder errorMessage) {
		try {
			reader.mark(0);
			int ch = reader.read();
			if (ch == EOF) {
				errorMessage.append("Unexpected end of input while trying to read a floating point number!");
				return null;
			}

			final StringBuilder builder = new StringBuilder();

			// Process optional leading sign:
			if (ch == '-' || ch == '+')
				builder.append((char)ch);
			else
				reader.reset();

			int savedLength = builder.length();
			grabAsciiDigits(reader, builder);
			final boolean needAfterDecimalPointDigits = builder.length() == savedLength;

			// Process optional decimal point followed by zero or more digits:
			reader.mark(0);
			ch = reader.read();
			if (ch != '.') {
				if (needAfterDecimalPointDigits) {
					errorMessage.append("Bad or missing floating point list item!");
					return null;
				}
				reader.reset();
			} else {
				builder.append('.');
				savedLength = builder.length();
				grabAsciiDigits(reader, builder);
				if (needAfterDecimalPointDigits && savedLength == builder.length()) {
					errorMessage.append("Bad or missing floating point list item!");
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
					errorMessage.append("Invalid exponent!");
					return null;
				}
			}

			try {
				return Double.valueOf(builder.toString());
			} catch (Exception e) {
				errorMessage.append("Malformed number!");
				return null;
			}
		} catch (final IOException e) {
			throw new IllegalStateException("This should *never* happen!");
		}
	}

	private static void grabAsciiDigits(final StringReader reader, final StringBuilder builder) {
		try {
			for (;;) {
				reader.mark(0);
				final int ch = reader.read();
				if (ch == EOF || (char)ch < '0' || (char)ch > '9') {
					reader.reset();
					return;
				}

				builder.append((char)ch);
			}
		} catch (final IOException e) {
			throw new IllegalStateException("This should *never* happen!");
		}
	}

	private static String getString(final StringReader reader, final StringBuilder errorMessage) {
		try {
			if (reader.read() != '"') {
				errorMessage.append("Strings must start with a double quote symbol!");
				return null;
			}

			final StringBuilder builder = new StringBuilder();

			int ch = reader.read();
			boolean escaped = false;
			while (escaped || ch != '"') {
				if (ch == EOF) {
					errorMessage.append("Unterminated string list item!");
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
		} catch (final IOException e) {
			throw new IllegalStateException("This should *never* happen!");
		}
	}

	private static void grabAsciiLetters(final StringReader reader, final StringBuilder builder)
	{
		try {
			for (;;) {
				reader.mark(0);
				final int ch = reader.read();
				if (ch == EOF
				    || (((char)ch < 'a' || (char)ch > 'z')
					&& ((char)ch < 'A' || (char)ch > 'Z')))
				{
					reader.reset();
					return;
				}

				builder.append((char)ch);
			}
		} catch (final IOException e) {
			throw new IllegalStateException("This should *never* happen!");
		}
	}
	
}
