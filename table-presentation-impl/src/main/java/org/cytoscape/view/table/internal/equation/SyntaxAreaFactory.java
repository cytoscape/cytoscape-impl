package org.cytoscape.view.table.internal.equation;

import java.awt.Color;
import java.awt.Font;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;

public class SyntaxAreaFactory {
	
	public static RSyntaxTextArea createEquationTextArea(CyServiceRegistrar registrar) {
		RSyntaxTextArea textArea = new RSyntaxTextArea();
		installTokeniser(textArea, registrar);
		installSyntaxScheme(textArea);
		return textArea;
	}
	

	private static void installTokeniser(RSyntaxTextArea textArea, CyServiceRegistrar registrar) {
		EquationTokenMaker tokenMaker = new EquationTokenMaker(registrar);
		((RSyntaxDocument)textArea.getDocument()).setSyntaxStyle(tokenMaker);
	}
	
	
	private static void installSyntaxScheme(RSyntaxTextArea textArea) {
		textArea.setCodeFoldingEnabled(false);
		textArea.setHighlightCurrentLine(false);
		textArea.setAutoIndentEnabled(false); // important, auto indent doesn't work with our tokeniser
		
		Font bold = RSyntaxTextArea.getDefaultFont().deriveFont(Font.BOLD);
		
		Color function = Style.DEFAULT_FOREGROUND;
		Color identifier = Style.DEFAULT_FOREGROUND;
		Color literal = new Color(50, 109, 168);
		
		SyntaxScheme scheme = new SyntaxScheme(false);
		scheme.setStyle(Token.FUNCTION,                    new Style(function, null, bold));
		scheme.setStyle(Token.IDENTIFIER,                  new Style(identifier));
		scheme.setStyle(Token.LITERAL_STRING_DOUBLE_QUOTE, new Style(literal));
		scheme.setStyle(Token.LITERAL_BOOLEAN,             new Style(literal));
		scheme.setStyle(Token.LITERAL_NUMBER_FLOAT,        new Style(literal));
		
		// All token types must have a Style object to avoid NPEs
		for(int i = 0; i < scheme.getStyleCount(); i++) {
			if(scheme.getStyle(i) == null) {
				scheme.setStyle(i, new Style());
			}
		}
		
		textArea.setSyntaxScheme(scheme);
		textArea.setText(""); // important, avoids a bug
	}

}
