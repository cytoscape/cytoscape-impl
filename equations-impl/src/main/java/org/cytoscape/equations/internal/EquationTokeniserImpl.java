package org.cytoscape.equations.internal;

import static org.cytoscape.equations.Token.Type.EOS;
import static org.cytoscape.equations.Token.Type.ERROR;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.cytoscape.equations.EquationTokeniser;
import org.cytoscape.equations.Token;

public class EquationTokeniserImpl implements EquationTokeniser {

	@Override
	public Iterator<Token> getTokenIterator(String equation) {
		Tokeniser tokeniser = new Tokeniser(equation);
		
		return new Iterator<Token>() {
			boolean done;
			
			@Override
			public boolean hasNext() {
				return !done;
			}

			@Override
			public Token next() {
				if(done)
					throw new NoSuchElementException();
				
				Token t = getNextApiToken(tokeniser);
				
				if(t == null || t.getType() == EOS || t.getType() == ERROR)
					done = true;
				
				return t;
			}
		};
	}
	
	
	@SuppressWarnings("incomplete-switch")
	private static Token getNextApiToken(Tokeniser tokeniser) {
		var implToken = tokeniser.getToken();
		
		Token.Type type = tokenToTokenType(implToken);
		int start = tokeniser.getStartPos();
		
		int end = start;
		switch(type) {
			case IDENTIFIER:
				end += tokeniser.getIdent().length() - 1;
				break;
			case STRING_CONSTANT:
				end += tokeniser.getCurrentStringLiteralLength() - 1;
				break;
			case BOOLEAN_CONSTANT:
				end += tokeniser.getBooleanConstant() ? 3 : 4;
				break;
			case FLOAT_CONSTANT:
				end += tokeniser.getFloatString().length() - 1;
				break;
			case LESS_OR_EQUAL:
			case GREATER_OR_EQUAL:
			case NOT_EQUAL:
				end += 1;
				break;
			case ERROR:
				end = tokeniser.getEquation().length() - 1;
				break;
		}
		return new Token(type, start, end);
	}
	
	
	
	private static Token.Type tokenToTokenType(org.cytoscape.equations.internal.Token implToken) {
		switch(implToken) {
			case AMPERSAND: return Token.Type.AMPERSAND;
			case BOOLEAN_CONSTANT: return Token.Type.BOOLEAN_CONSTANT;
			case CARET: return Token.Type.CARET;
			case CLOSE_BRACE: return Token.Type.CLOSE_BRACE;
			case CLOSE_PAREN: return Token.Type.CLOSE_PAREN;
			case COLON: return Token.Type.COLON;
			case COMMA: return Token.Type.COMMA;
			case DIV: return Token.Type.DIV;
			case DOLLAR: return Token.Type.DOLLAR;
			case EOS: return Token.Type.EOS;
			case EQUAL: return Token.Type.EQUAL;
			case ERROR: return Token.Type.ERROR;
			case FLOAT_CONSTANT: return Token.Type.FLOAT_CONSTANT;
			case GREATER_OR_EQUAL: return Token.Type.GREATER_OR_EQUAL;
			case GREATER_THAN: return Token.Type.GREATER_THAN;
			case IDENTIFIER: return Token.Type.IDENTIFIER;
			case LESS_OR_EQUAL: return Token.Type.LESS_OR_EQUAL;
			case LESS_THAN: return Token.Type.LESS_THAN;
			case MINUS: return Token.Type.MINUS;
			case MUL: return Token.Type.MUL;
			case NOT_EQUAL: return Token.Type.NOT_EQUAL;
			case OPEN_BRACE: return Token.Type.OPEN_BRACE;
			case OPEN_PAREN: return Token.Type.OPEN_PAREN;
			case PLUS: return Token.Type.PLUS;
			case STRING_CONSTANT: return Token.Type.STRING_CONSTANT;
		}
		return null;
	}
	
}
