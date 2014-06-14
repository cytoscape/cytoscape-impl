/* vim: set ts=2: */
/**
 * Copyright (c) 2010 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.cytoscape.commandDialog.internal.ui;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.Element;


import org.cytoscape.commandDialog.internal.handlers.MessageHandler;

public class JResultsPane extends JTextPane implements MessageHandler {
	// private SimpleAttributeSet commandAttributes;
	// private SimpleAttributeSet messageAttributes;
	// private SimpleAttributeSet resultAttributes;
	// private SimpleAttributeSet errorAttributes;
	// private SimpleAttributeSet warningAttributes;
	private String commandAttributes;
	private String messageAttributes;
	private String resultAttributes;
	private String errorAttributes;
	private String warningAttributes;
	private HTMLDocument currentDocument;
	private Element rootElement;
	private static String BLUE = "color:blue";
	private static String RED = "color:red";
	private static String GREEN = "color:green";
	private static String ORANGE = "color:orange";
	private static String BLACK = "color:black";
	private static String BOLD = "font-weight:bold";
	private static String ITALICS = "font-style:italic";
	private static String DEFAULT_STYLE = "margin-top:0px;margin-bottom:0px";

	public JResultsPane() {
		super();

		DefaultCaret caret = (DefaultCaret)getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		setContentType("text/html");
		currentDocument = (HTMLDocument) getDocument();
		rootElement = currentDocument.getDefaultRootElement();

		commandAttributes = BLUE+";"+BOLD+";"+ITALICS;
		messageAttributes = BLUE+";margin-left:10px";
		errorAttributes = RED;
		warningAttributes = ORANGE;
		resultAttributes = GREEN+";"+BOLD+";"+ITALICS;

		/*
		commandAttributes = new SimpleAttributeSet();
		commandAttributes.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLUE);
		commandAttributes.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);
		commandAttributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);

		messageAttributes = new SimpleAttributeSet();
		messageAttributes.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLUE);
		messageAttributes.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.FALSE);
		messageAttributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);

		errorAttributes = new SimpleAttributeSet();
		errorAttributes.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.RED);
		errorAttributes.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.FALSE);
		errorAttributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);

		warningAttributes = new SimpleAttributeSet();
		warningAttributes.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.ORANGE);
		warningAttributes.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.FALSE);
		warningAttributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);

		resultAttributes = new SimpleAttributeSet();
		resultAttributes.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.GREEN);
		resultAttributes.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);
		resultAttributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
		*/
	}

	public void appendCommand(String s) {
		// updateString(commandAttributes, s+"\n");
		updateString(commandAttributes, s);
	}

	public void appendError(String s) {
		// updateString(errorAttributes, s+"\n");
		updateString(errorAttributes, s);
	}

	public void appendResult(String s) {
		// Be a little careful.  We want to space newlines to they all
		// appear in column order
		String[] splitString = s.split("\n");
		if (splitString.length > 1) {
			for (String splitS: splitString)
				updateString(resultAttributes, "\u2192  "+splitS+"\n");
		} else 
			updateString(resultAttributes, "\u2192  "+s+"\n");
	}

	public void appendWarning(String s) {
		// updateString(warningAttributes, s+"\n");
		updateString(warningAttributes, s);
	}

	public void appendMessage(String s) {
		// updateString(messageAttributes, "    "+s+"\n");
		updateString(messageAttributes, s);
	}

	public void clear() {
		// setStyledDocument(new DefaultStyledDocument());
		currentDocument = new HTMLDocument();
		setStyledDocument(currentDocument);
		rootElement = currentDocument.getDefaultRootElement();
	}

	private void updateString(final String style, final String s) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				/* StyledDocument doc = getStyledDocument();
				try {
					doc.insertString(doc.getLength(), s, set);
				} catch (BadLocationException badLocationException) {
				}
				*/
				try {
					String p = "<p style='"+DEFAULT_STYLE+";"+style+"'>"+s+"</p>\n";
					currentDocument.insertBeforeEnd(rootElement, p);
					setCaretPosition(currentDocument.getLength());
				} catch (Exception badLocationException) {
				}
			}
		});
	}
}
