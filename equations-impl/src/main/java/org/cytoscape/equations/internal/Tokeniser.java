/*
  File: Tokeniser.java

  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.equations.internal;

import java.io.IOException;
import java.io.StringReader;


public class Tokeniser {
	private final String equationAsString;
	private Token previousToken;
	private StringReader reader;
	private int tokenStartPos, previousTokenStartPos, currentPos;
	private long previousIntConstant, currentIntConstant;
	private String previousIdent, currentIdent;
	private double previousFloatConstant, currentFloatConstant;
	private boolean previousBooleanConstant, currentBooleanConstant;
	private String previousStringConstant, currentStringConstant;
	private String errorMsg;
	private int previousChar;
	private boolean putBackChar;
	private boolean openingBraceSeen;

	public Tokeniser(final String equationAsString) {
		this.equationAsString = equationAsString;
		previousToken = null;
		reader = new StringReader(equationAsString);
		currentPos = -1;
		putBackChar = false;
		openingBraceSeen = false;
	}

	public Token getToken() {
		if (previousToken != null) {
			final Token retval = previousToken;
			previousToken = null;

			currentIntConstant = previousIntConstant;
			currentFloatConstant = previousFloatConstant;
			currentBooleanConstant = previousBooleanConstant;
			currentStringConstant = previousStringConstant;
			currentIdent = previousIdent;
			tokenStartPos = previousTokenStartPos;

			return retval;
		}

		int nextCh = getChar();
		while (nextCh != -1 && Character.isWhitespace((char)nextCh))
			nextCh = getChar();

		tokenStartPos = currentPos;

		if (nextCh == -1) {
			tokenStartPos = equationAsString.length();
			return Token.EOS;
		}

		final char ch = (char)nextCh;
		switch (ch) {
		case ':': return Token.COLON;
		case '^': return Token.CARET;
		case '{':
			openingBraceSeen = true;
			return Token.OPEN_BRACE;
		case '}':
			openingBraceSeen = false;
			return Token.CLOSE_BRACE;
		case '(': return Token.OPEN_PAREN;
		case ')': return Token.CLOSE_PAREN;
		case '+': return Token.PLUS;
		case '-': return Token.MINUS;
		case '/': return Token.DIV;
		case '*': return Token.MUL;
		case '=': return Token.EQUAL;
		case '$': return Token.DOLLAR;
		case ',': return Token.COMMA;
		case '&': return Token.AMPERSAND;
		}

		if (ch == '"')
			return parseStringConstant();
		if (Character.isDigit(ch) || ch == '.') {
			ungetChar(nextCh);
			return parseNumericConstant();
		}

		if (ch == '<') {
			nextCh = getChar();
			if (nextCh == -1)
				return Token.LESS_THAN;
			if ((char)nextCh == '>')
				return Token.NOT_EQUAL;
			if ((char)nextCh == '=')
				return Token.LESS_OR_EQUAL;
			ungetChar(nextCh);
			return Token.LESS_THAN;
		}

		if (ch == '>') {
			nextCh = getChar();
			if (nextCh == -1)
				return Token.GREATER_THAN;
			if ((char)nextCh == '=')
				return Token.GREATER_OR_EQUAL;
			ungetChar(nextCh);
			return Token.GREATER_THAN;
		}

		if (Character.isLetter(ch)) {
			ungetChar(nextCh);
			return openingBraceSeen ? parseIdentifier() : parseSimpleIdentifier();
		}

		errorMsg = "unexpected input character '" + Character.toString(ch) + "'";

		return Token.ERROR;
	}

	public void ungetToken(final Token token) throws IllegalStateException {
		if (previousToken != null)
			throw new IllegalStateException("can't unget more than one token.");

		previousToken = token;
		previousIntConstant = currentIntConstant;
		previousFloatConstant = currentFloatConstant;
		previousBooleanConstant = currentBooleanConstant;
		previousStringConstant = currentStringConstant;
		previousIdent = currentIdent;
		previousTokenStartPos = tokenStartPos;
	}

	/**
	 *  @return the position where the current token started.  The position returned applies to the last token
	 *          that was retrieved via the getToken() method.
	 */
	public int getStartPos() {
		return tokenStartPos;
	}

	/**
	 *  Returns a representation of the next token as a string.  Used primarily for testing.
	 *  You should stop calling this after it returned "EOS"!
	 */
	public String getTokenAsString() {
		final Token token = getToken();
		if (token == Token.STRING_CONSTANT)
			return "STRING_CONSTANT: \"" + getStringConstant() + "\"";
		if (token == Token.FLOAT_CONSTANT)
			return "FLOAT_CONSTANT: \"" + getFloatConstant() + "\"";
		if (token == Token.BOOLEAN_CONSTANT)
			return "BOOLEAN_CONSTANT: \"" + getBooleanConstant() + "\"";
		if (token == Token.IDENTIFIER)
			return "IDENTIFIER: \"" + getIdent() + "\"";
		if (token == Token.ERROR)
			return "ERROR: \"" + getErrorMsg();
		return token.toString();
	}

	public String getStringConstant() {
		return currentStringConstant;
	}

	public double getFloatConstant() {
		return currentFloatConstant;
	}

	public boolean getBooleanConstant() {
		return currentBooleanConstant;
	}

	public long getIntConstant() {
		return currentIntConstant;
	}

	public String getIdent() {
		return currentIdent;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	private int getChar() {
		final int retval;
		
		if (putBackChar) {
			++currentPos;
			retval = previousChar;
			putBackChar = false;
			return retval;
		}

		try {
			retval = reader.read();
		} catch (final IOException e) {
			return -1;
		}

		if (retval != -1)
			++currentPos;

		return retval;
	}

	private void ungetChar(final int ch) {
		if (putBackChar)
			throw new IllegalStateException("can't unget two chars in a row.");
		previousChar = ch;
		putBackChar = true;
		--currentPos;
	}
	
	private Token parseStringConstant() {
		final int INITIAL_CAPACITY = 20;
		final StringBuilder builder = new StringBuilder(INITIAL_CAPACITY);

		boolean escaped = false;
		int nextCh;
		while ((nextCh = getChar()) != -1) {
			final char ch = (char)nextCh;
			if (ch == '\\')
				escaped = true;
			else {
				if (escaped) {
					switch (ch) {
					case '\\':
						builder.append('\\');
						break;
					case '"':
						builder.append('"');
						break;
					case 'n':
						builder.append('\n');
						break;
					default:
						errorMsg = "unknown escape character '" + Character.toString(ch) + "'.";
						return Token.ERROR;
					}

					escaped = false;
				}
				else if (ch == '"') {
					currentStringConstant = builder.toString();
					return Token.STRING_CONSTANT;
				}
				else
					builder.append(ch);
			}
		}

		errorMsg = "unterminated String constant.";
		return Token.ERROR;
	}

	private Token parseNumericConstant() {
		final int INITIAL_CAPACITY = 20;
		final StringBuilder builder = new StringBuilder(INITIAL_CAPACITY);

		int ch;
		while ((ch = getChar()) != -1 && Character.isDigit((char)ch))
			builder.append((char)ch);

		if (ch == -1 || ((char)ch != 'e' && (char)ch != 'E' && (char)ch != '.')) {
			try {
				final double d = Double.parseDouble(builder.toString());
				currentFloatConstant = d;
				ungetChar(ch);
				return Token.FLOAT_CONSTANT;
			} catch (final NumberFormatException e2) {
				errorMsg = "invalid numeric constant.";
				return Token.ERROR;
			}
		}

		// Optional decimal point.
		if ((char)ch == '.') {
			builder.append((char)ch);
			while ((ch = getChar()) != -1 && Character.isDigit((char)ch))
				builder.append((char)ch);
		}

		// Optional exponent.
		if ((char)ch == 'e' || (char)ch == 'E') {
			builder.append((char)ch);

			ch = getChar();
			if (ch == -1) {
				errorMsg = "invalid numeric constant.";
				return Token.ERROR;
			}

			// Optional sign.
			if ((char)ch == '+' || (char)ch == '-') {
				builder.append((char)ch);
				ch = getChar();
			}

			// Now we require at least a single digit.
			if (!Character.isDigit((char)ch)) {
				errorMsg = "missing digits in exponent.";
				return Token.ERROR;
			}
			ungetChar(ch);

			while ((ch = getChar()) != -1 && Character.isDigit((char)ch))
				builder.append((char)ch);
		}

		ungetChar(ch);

		try {
			final double d = Double.parseDouble(builder.toString());
			currentFloatConstant = d;
			return Token.FLOAT_CONSTANT;
		} catch (final NumberFormatException e3) {
			errorMsg = "invalid numeric constant.";
			return Token.ERROR;
		}
	}

	/**
	 *  Looks for an attribute name.  Attribute names in formulas must not contain '}', ':', ',' nor
	 *  '(' or ')'.  In order to allow a '}' or a ':' in an attribute name, it has to be esacped with a
	 *  backslash.  Any backslash in an identifier a.k.a. attribute name implies that the next
	 *  input character will be included in the identifier this also allows for embedding
	 *  backslashes by doubling them.
	 */
	private Token parseIdentifier() {
		final int INITIAL_CAPACITY = 20;
		final StringBuilder builder = new StringBuilder(INITIAL_CAPACITY);

		boolean escaped = false;
		int ch;
		while ((ch = getChar()) != -1 &&
		       (((char)ch != '}' && (char)ch != ':' && (char)ch != ',' && (char)ch != '(' && (char)ch != ')') || escaped))
		{
			if (escaped) {
				escaped = false;
				builder.append((char)ch);
			}
			else if ((char)ch == '\\')
				escaped = true;
			else
				builder.append((char)ch);
		}
		if (escaped) {
			errorMsg = "invalid attribute name at end of formula.";
			return Token.ERROR;
		}
		ungetChar(ch);

		currentIdent = builder.toString();

		if (currentIdent.equalsIgnoreCase("TRUE")) {
			currentBooleanConstant = true;
			return Token.BOOLEAN_CONSTANT;
		}
		if (currentIdent.equalsIgnoreCase("FALSE")) {
			currentBooleanConstant = false;
			return Token.BOOLEAN_CONSTANT;
		}

		return Token.IDENTIFIER;
	}

	private Token parseSimpleIdentifier() {
		final int INITIAL_CAPACITY = 20;
		final StringBuilder builder = new StringBuilder(INITIAL_CAPACITY);

		int ch;
		while ((ch = getChar()) != -1 && (Character.isLetter((char)ch) || Character.isDigit((char)ch) || (char)ch == '_'))
			builder.append((char)ch);
		ungetChar(ch);

		currentIdent = builder.toString();

		if (currentIdent.equalsIgnoreCase("TRUE")) {
			currentBooleanConstant = true;
			return Token.BOOLEAN_CONSTANT;
		}
		if (currentIdent.equalsIgnoreCase("FALSE")) {
			currentBooleanConstant = false;
			return Token.BOOLEAN_CONSTANT;
		}

		return Token.IDENTIFIER;
	}
	
	static public void main(final String[] args) {
		for (final String arg : args) {
			final Tokeniser tokeniser = new Tokeniser(arg);
			String tokenAsString;
			do {
				tokenAsString = tokeniser.getTokenAsString();
				System.out.println(tokenAsString);
			}
			while (tokenAsString != "EOS");
		}
	}
}
