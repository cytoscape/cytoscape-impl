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
package org.cytoscape.internal.command;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class JResultsPane extends JTextPane implements MessageHandler {
	private final static Logger logger = LoggerFactory.getLogger(JResultsPane.class);
	
	private String commandAttributes;
	private String messageAttributes;
	private String resultAttributes;
	private String errorAttributes;
	private String warningAttributes;
	private HTMLDocument currentDocument;
	private Element rootElement;
	private final JDialog parentDialog;
	private final JPanel dataPanel;
	private JScrollPane scrollPane = null;
	private static String BLUE = "color:blue";
	private static String RED = "color:red";
	private static String GREEN = "color:green";
	private static String ORANGE = "color:orange";
	private static String BLACK = "color:black";
	private static String BOLD = "font-weight:bold";
	private static String ITALICS = "font-style:italic";
	private static String DEFAULT_STYLE = "margin-top:0px;margin-bottom:0px";

	public JResultsPane(JDialog parentDialog, JPanel dataPanel) {
		this.parentDialog = parentDialog;
		this.dataPanel = dataPanel;

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
	}

	public void appendCommand(String s) {
		updateString(commandAttributes, s);
	}

	public void appendError(String s) {
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
		updateString(warningAttributes, s);
	}

	public void appendMessage(String s) {
		updateString(messageAttributes, s);
	}

	public void setScrollPane(JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}

	/**
	 * Recreate new Document, set it to the current JResultsPane and reset the rootElement
	 * to be used while inserting HTML Element
	 */
	public void clear() {
		currentDocument = (HTMLDocument) new HTMLEditorKit().createDefaultDocument();
		setStyledDocument(currentDocument);
		rootElement = currentDocument.getDefaultRootElement();
	}

	private void updateString(final String style, final String s) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateStringImmediate(style, s);
				}
			});
		} else {
			updateStringImmediate(style, s);
		}
	}

	private void updateStringImmediate(final String style, final String s) {
		try {
			String p = "<div style='"+DEFAULT_STYLE+";"+style+"'>"+s+"</div>\n";
			currentDocument.insertBeforeEnd(rootElement, p);
			setCaretPosition(currentDocument.getLength());

			// Force (I mean, *really* force) the dialog to repaint.
			paintImmediately(getBounds());
			if (parentDialog != null)
				parentDialog.revalidate();
			dataPanel.paintImmediately(dataPanel.getBounds());
		} catch (Exception e) {
			logger.error("Unable to update command result in the dialog.", e);
		}
		
		JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
		verticalScrollBar.setValue(verticalScrollBar.getMaximum());
	}
}
