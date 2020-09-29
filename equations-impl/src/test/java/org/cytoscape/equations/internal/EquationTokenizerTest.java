package org.cytoscape.equations.internal;

import static org.junit.Assert.assertEquals;

import org.cytoscape.equations.Token;
import org.cytoscape.equations.Token.Type;
import org.junit.Test;

public class EquationTokenizerTest {
	
	
	@Test
	public void testTokeniser() {
		var tokeniser = new EquationTokeniserImpl();
		var list = tokeniser.getTokenList("=5 + 6");
		assertEquals(5, list.size());
		assertEquals(new Token(Type.EQUAL, 0, 0),          list.get(0));
		assertEquals(new Token(Type.FLOAT_CONSTANT, 1, 1), list.get(1));
		assertEquals(new Token(Type.PLUS, 3, 3),           list.get(2));
		assertEquals(new Token(Type.FLOAT_CONSTANT, 5, 5), list.get(3));
		assertEquals(new Token(Type.EOS, 6, 6),            list.get(4));
	}
	
	@Test
	public void testSimpleStringLiteral() {
		var tokeniser = new EquationTokeniserImpl();
		var list = tokeniser.getTokenList("=\"asdf\""); // ="asdf"
		assertEquals(3, list.size());
		assertEquals(new Token(Type.EQUAL, 0, 0),           list.get(0));
		assertEquals(new Token(Type.STRING_CONSTANT, 1, 6), list.get(1));
		assertEquals(new Token(Type.EOS, 7, 7),             list.get(2));
	}
	
	@Test
	public void testUnterminatedStringLiteral() {
		var tokeniser = new EquationTokeniserImpl();
		var list = tokeniser.getTokenList("=\"asdfasdfsasdf");
		assertEquals(2, list.size());
		assertEquals(new Token(Type.EQUAL, 0, 0),  list.get(0));
		assertEquals(new Token(Type.ERROR, 1, 14), list.get(1));
	}
	
	@Test
	public void testDoubleBackslash() {
		var tokeniser = new EquationTokeniserImpl();
		var list = tokeniser.getTokenList("=\"\\\\\""); //  ="\\"
		assertEquals(3, list.size());
		assertEquals(new Token(Type.EQUAL, 0, 0),           list.get(0));
		assertEquals(new Token(Type.STRING_CONSTANT, 1, 4), list.get(1));
		assertEquals(new Token(Type.EOS, 5, 5),             list.get(2));
	}
	
	

}
