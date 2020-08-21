package org.cytoscape.view.table.internal.equation;

import java.util.Set;

import javax.swing.text.Segment;

import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.EquationTokeniser;
import org.cytoscape.equations.Function;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

public class EquationTokenMaker extends AbstractTokenMaker {
	
	private final CyServiceRegistrar registrar;
	
	public EquationTokenMaker(CyServiceRegistrar registrar) {
		super();
		this.registrar = registrar;
		// Need to call this explicitly because getWordsToHighlight() requires the registrar.
		super.wordsToHighlight = getWordsToHighlight();
	}

	@Override
	public Token getTokenList(Segment text, int initialTokenType, final int startOffset) {
		System.out.println("EquationTokenMaker.getTokenList: text, initialTokenType, startOffset: " + java.util.Arrays.asList(text.toString(), initialTokenType, startOffset));
		
		resetTokenList();
		
		EquationTokeniser tokeniser = registrar.getService(EquationTokeniser.class);
		String equation = text.toString();
		var tokenIterator = tokeniser.getTokenIterator(equation);

		final int offset = text.offset;
		final int endOffset = equation.length() + offset - 1;
		
		int prevEnd = 0;
		
		while(tokenIterator.hasNext()) {
			var t = tokenIterator.next();
			var tokenType = getTokenType(t.getType());
			if(tokenType > 0) {
				int start = t.getStart() + offset;
				int end = start + (t.getEnd() - t.getStart());
				
				if(start - prevEnd > 1) { // whitespace!!!
					int wsStart = prevEnd + 1;
					addToken(text, wsStart, start - 1, TokenTypes.WHITESPACE, startOffset + wsStart - offset);
				}
				
				addToken(text, start, end, tokenType, startOffset + t.getStart());
				prevEnd = end;
			}
		}
		
		
		if(prevEnd < endOffset) {
			int wsStart = prevEnd + 1;
			addToken(text, wsStart, endOffset, TokenTypes.WHITESPACE, startOffset + wsStart - offset);
		}
		
		addNullToken();
		return firstToken;
	}
	
	

	@Override
	public void addToken(Segment segment, int start, int end, int tokenType, int startOffset) {
		System.out.println("addToken: '" + segment + "' (" + start + "," + end + ") " + tokenType + " '" + segment.toString().substring(start, end+1) + "'");
		// This assumes all keywords, etc. were parsed as "identifiers."
		if (tokenType == Token.IDENTIFIER) {
			int value = wordsToHighlight.get(segment, start, end);
			if (value != -1) {
				tokenType = value;
			}
		}
		super.addToken(segment, start, end, tokenType, startOffset);
	}

	
	@SuppressWarnings("incomplete-switch")
	private static int getTokenType(org.cytoscape.equations.Token.Type tokenType) {
		switch(tokenType) {
			case ERROR:
				return TokenTypes.ERROR_IDENTIFIER; // ???
			case CLOSE_BRACE:
			case CLOSE_PAREN:
			case COMMA:
			case OPEN_BRACE:
			case OPEN_PAREN:
			case COLON:
				return TokenTypes.SEPARATOR;
			case AMPERSAND:
			case PLUS: 
			case MINUS:
			case DIV:
			case MUL:
			case LESS_OR_EQUAL:
			case LESS_THAN:
			case GREATER_OR_EQUAL:
			case GREATER_THAN:
			case NOT_EQUAL:
			case EQUAL:
			case CARET:
				return TokenTypes.OPERATOR;
			case DOLLAR:
			case IDENTIFIER:
				return TokenTypes.IDENTIFIER;
			case BOOLEAN_CONSTANT:
				return TokenTypes.LITERAL_BOOLEAN;
			case FLOAT_CONSTANT:
				return TokenTypes.LITERAL_NUMBER_FLOAT;
			case STRING_CONSTANT: 
				return TokenTypes.LITERAL_STRING_DOUBLE_QUOTE;
		}
		return -1;
	}
	

	@Override
	public TokenMap getWordsToHighlight() {
		if(registrar == null)
			return null;
		
		EquationParser parser = registrar.getService(EquationParser.class);
		Set<Function> functions = parser.getRegisteredFunctions();
		
		TokenMap tokenMap = new TokenMap();
		for(var f : functions) {
			tokenMap.put(f.getName(), Token.FUNCTION);
		}
		return tokenMap;
	}

}
