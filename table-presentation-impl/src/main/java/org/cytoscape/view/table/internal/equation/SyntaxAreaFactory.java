package org.cytoscape.view.table.internal.equation;

import java.awt.Color;
import java.awt.Font;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;

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
