package org.cytoscape.equations.internal;

/*
 * #%L
 * Cytoscape Equations Impl (equations-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


import java.util.ArrayList;
import junit.framework.*;


public class TokeniserTest extends TestCase {
	// Stores the expected token's string representation and position.
	static class TokenStringRepAndPosition {
		private final String stringRep;
		private final int position;

		TokenStringRepAndPosition(final String stringRep, final int position) {
			this.stringRep = stringRep;
			this.position  = position;
		}

		String getStringRep() { return this.stringRep; }
		int getPosition() { return position; }
	}

	public void testScanner1() throws Exception {
		final Tokeniser tokeniser = new Tokeniser("AND(1.0 >= $(BOB), OR($(JOE) = $(tiny), $(x) > LOG(1.3e17)))");
		final ArrayList<TokenStringRepAndPosition> tokens = new ArrayList<TokenStringRepAndPosition>();
		String tokenAsString;
		do {
			tokenAsString = tokeniser.getTokenAsString();
			final int position = tokeniser.getStartPos();
			tokens.add(new TokenStringRepAndPosition(tokenAsString, position));
		} while (tokenAsString != "EOS");
		final TokenStringRepAndPosition[] actualTokens = new TokenStringRepAndPosition[tokens.size()];
		tokens.toArray(actualTokens);

		final TokenStringRepAndPosition[] expectedTokens = {
			new TokenStringRepAndPosition("IDENTIFIER: \"AND\"", 0),
			new TokenStringRepAndPosition("OPEN_PAREN", 3),
			new TokenStringRepAndPosition("FLOAT_CONSTANT: \"1.0\"", 4),
			new TokenStringRepAndPosition("GREATER_OR_EQUAL", 8),
			new TokenStringRepAndPosition("DOLLAR", 11),
			new TokenStringRepAndPosition("OPEN_PAREN", 12),
			new TokenStringRepAndPosition("IDENTIFIER: \"BOB\"", 13),
			new TokenStringRepAndPosition("CLOSE_PAREN", 16),
			new TokenStringRepAndPosition("COMMA", 17),
			new TokenStringRepAndPosition("IDENTIFIER: \"OR\"", 19),
			new TokenStringRepAndPosition("OPEN_PAREN", 21),
			new TokenStringRepAndPosition("DOLLAR", 22),
			new TokenStringRepAndPosition("OPEN_PAREN", 23),
			new TokenStringRepAndPosition("IDENTIFIER: \"JOE\"", 24),
			new TokenStringRepAndPosition("CLOSE_PAREN", 27),
			new TokenStringRepAndPosition("EQUAL", 29),
			new TokenStringRepAndPosition("DOLLAR", 31),
			new TokenStringRepAndPosition("OPEN_PAREN", 32),
			new TokenStringRepAndPosition("IDENTIFIER: \"tiny\"", 33),
			new TokenStringRepAndPosition("CLOSE_PAREN", 37),
			new TokenStringRepAndPosition("COMMA", 38),
			new TokenStringRepAndPosition("DOLLAR", 40),
			new TokenStringRepAndPosition("OPEN_PAREN", 41),
			new TokenStringRepAndPosition("IDENTIFIER: \"x\"", 42),
			new TokenStringRepAndPosition("CLOSE_PAREN", 43),
			new TokenStringRepAndPosition("GREATER_THAN", 45),
			new TokenStringRepAndPosition("IDENTIFIER: \"LOG\"", 47),
			new TokenStringRepAndPosition("OPEN_PAREN", 50),
			new TokenStringRepAndPosition("FLOAT_CONSTANT: \"1.3E17\"", 51),
			new TokenStringRepAndPosition("CLOSE_PAREN", 57),
			new TokenStringRepAndPosition("CLOSE_PAREN", 58),
			new TokenStringRepAndPosition("CLOSE_PAREN", 59),
			new TokenStringRepAndPosition("EOS", 60)
		};

		assertEquals(expectedTokens.length, actualTokens.length);
		for (int i = 0; i < expectedTokens.length; ++i) {
			assertEquals(expectedTokens[i].getStringRep(), actualTokens[i].getStringRep());
			assertEquals(expectedTokens[i].getPosition(), actualTokens[i].getPosition());
		}
	}

	/**
	 *  Please note that the scanner input here is the same as for testScanner1() except for the lack of spaces.
	 */
	public void testScanner2() throws Exception {
		final Tokeniser tokeniser = new Tokeniser("AND(1.0>=$(BOB),OR($(JOE)=$(tiny),$(x)>LOG(1.3e17)))");
		final ArrayList<TokenStringRepAndPosition> tokens = new ArrayList<TokenStringRepAndPosition>();
		String tokenAsString;
		do {
			tokenAsString = tokeniser.getTokenAsString();
			final int position = tokeniser.getStartPos();
			tokens.add(new TokenStringRepAndPosition(tokenAsString, position));
		} while (tokenAsString != "EOS");
		final TokenStringRepAndPosition[] actualTokens = new TokenStringRepAndPosition[tokens.size()];
		tokens.toArray(actualTokens);

		final TokenStringRepAndPosition[] expectedTokens = {
			new TokenStringRepAndPosition("IDENTIFIER: \"AND\"", 0),
			new TokenStringRepAndPosition("OPEN_PAREN", 3),
			new TokenStringRepAndPosition("FLOAT_CONSTANT: \"1.0\"", 4),
			new TokenStringRepAndPosition("GREATER_OR_EQUAL", 7),
			new TokenStringRepAndPosition("DOLLAR", 9),
			new TokenStringRepAndPosition("OPEN_PAREN", 10),
			new TokenStringRepAndPosition("IDENTIFIER: \"BOB\"", 11),
			new TokenStringRepAndPosition("CLOSE_PAREN", 14),
			new TokenStringRepAndPosition("COMMA", 15),
			new TokenStringRepAndPosition("IDENTIFIER: \"OR\"", 16),
			new TokenStringRepAndPosition("OPEN_PAREN", 18),
			new TokenStringRepAndPosition("DOLLAR", 19),
			new TokenStringRepAndPosition("OPEN_PAREN", 20),
			new TokenStringRepAndPosition("IDENTIFIER: \"JOE\"", 21),
			new TokenStringRepAndPosition("CLOSE_PAREN", 24),
			new TokenStringRepAndPosition("EQUAL", 25),
			new TokenStringRepAndPosition("DOLLAR", 26),
			new TokenStringRepAndPosition("OPEN_PAREN", 27),
			new TokenStringRepAndPosition("IDENTIFIER: \"tiny\"", 28),
			new TokenStringRepAndPosition("CLOSE_PAREN", 32),
			new TokenStringRepAndPosition("COMMA", 33),
			new TokenStringRepAndPosition("DOLLAR", 34),
			new TokenStringRepAndPosition("OPEN_PAREN", 35),
			new TokenStringRepAndPosition("IDENTIFIER: \"x\"", 36),
			new TokenStringRepAndPosition("CLOSE_PAREN", 37),
			new TokenStringRepAndPosition("GREATER_THAN", 38),
			new TokenStringRepAndPosition("IDENTIFIER: \"LOG\"", 39),
			new TokenStringRepAndPosition("OPEN_PAREN", 42),
			new TokenStringRepAndPosition("FLOAT_CONSTANT: \"1.3E17\"", 43),
			new TokenStringRepAndPosition("CLOSE_PAREN", 49),
			new TokenStringRepAndPosition("CLOSE_PAREN", 50),
			new TokenStringRepAndPosition("CLOSE_PAREN", 51),
			new TokenStringRepAndPosition("EOS", 52)
		};

		assertEquals(expectedTokens.length, actualTokens.length);
		for (int i = 0; i < expectedTokens.length; ++i) {
			assertEquals(expectedTokens[i].getStringRep(), actualTokens[i].getStringRep());
			assertEquals(expectedTokens[i].getPosition(), actualTokens[i].getPosition());
		}
	}

	public void testScanner3() throws Exception {
		final Tokeniser tokeniser = new Tokeniser("1.79^3");
		final ArrayList<TokenStringRepAndPosition> tokens = new ArrayList<TokenStringRepAndPosition>();
		String tokenAsString;
		do {
			tokenAsString = tokeniser.getTokenAsString();
			final int position = tokeniser.getStartPos();
			tokens.add(new TokenStringRepAndPosition(tokenAsString, position));
		} while (tokenAsString != "EOS");
		final TokenStringRepAndPosition[] actualTokens = new TokenStringRepAndPosition[tokens.size()];
		tokens.toArray(actualTokens);

		final TokenStringRepAndPosition[] expectedTokens = {
			new TokenStringRepAndPosition("FLOAT_CONSTANT: \"1.79\"", 0),
			new TokenStringRepAndPosition("CARET", 4),
			new TokenStringRepAndPosition("FLOAT_CONSTANT: \"3.0\"", 5),
			new TokenStringRepAndPosition("EOS", 6)
		};

		assertEquals(expectedTokens.length, actualTokens.length);
		for (int i = 0; i < expectedTokens.length; ++i) {
			assertEquals(expectedTokens[i].getStringRep(), actualTokens[i].getStringRep());
			assertEquals(expectedTokens[i].getPosition(), actualTokens[i].getPosition());
		}
	}

	public void testScanner4() throws Exception {
		final Tokeniser tokeniser = new Tokeniser("true, fred, FALSE, True");
		final ArrayList<TokenStringRepAndPosition> tokens = new ArrayList<TokenStringRepAndPosition>();
		String tokenAsString;
		do {
			tokenAsString = tokeniser.getTokenAsString();
			final int position = tokeniser.getStartPos();
			tokens.add(new TokenStringRepAndPosition(tokenAsString, position));
		} while (tokenAsString != "EOS");
		final TokenStringRepAndPosition[] actualTokens = new TokenStringRepAndPosition[tokens.size()];
		tokens.toArray(actualTokens);

		final TokenStringRepAndPosition[] expectedTokens = {
			new TokenStringRepAndPosition("BOOLEAN_CONSTANT: \"true\"", 0),
			new TokenStringRepAndPosition("COMMA", 4),
			new TokenStringRepAndPosition("IDENTIFIER: \"fred\"", 6),
			new TokenStringRepAndPosition("COMMA", 10),
			new TokenStringRepAndPosition("BOOLEAN_CONSTANT: \"false\"", 12),
			new TokenStringRepAndPosition("COMMA", 17),
			new TokenStringRepAndPosition("BOOLEAN_CONSTANT: \"true\"", 19),
			new TokenStringRepAndPosition("EOS", 23)
		};

		assertEquals(expectedTokens.length, actualTokens.length);
		for (int i = 0; i < expectedTokens.length; ++i) {
			assertEquals(expectedTokens[i].getStringRep(), actualTokens[i].getStringRep());
			assertEquals(expectedTokens[i].getPosition(), actualTokens[i].getPosition());
		}
	}

	public void testScanner5() throws Exception {
		final Tokeniser tokeniser = new Tokeniser(".79e2");
		final ArrayList<TokenStringRepAndPosition> tokens = new ArrayList<TokenStringRepAndPosition>();
		String tokenAsString;
		do {
			tokenAsString = tokeniser.getTokenAsString();
			final int position = tokeniser.getStartPos();
			tokens.add(new TokenStringRepAndPosition(tokenAsString, position));
		} while (tokenAsString != "EOS");
		final TokenStringRepAndPosition[] actualTokens = new TokenStringRepAndPosition[tokens.size()];
		tokens.toArray(actualTokens);

		final TokenStringRepAndPosition[] expectedTokens = {
			new TokenStringRepAndPosition("FLOAT_CONSTANT: \"79.0\"", 0),
			new TokenStringRepAndPosition("EOS", 5)
		};

		assertEquals(expectedTokens.length, actualTokens.length);
		for (int i = 0; i < expectedTokens.length; ++i) {
			assertEquals(expectedTokens[i].getStringRep(), actualTokens[i].getStringRep());
			assertEquals(expectedTokens[i].getPosition(), actualTokens[i].getPosition());
		}
	}
}
