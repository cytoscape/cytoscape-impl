package org.cytoscape.log.internal;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public class LogViewer
{
	final Map config;
	final JEditorPane editorPane;
	final JScrollPane scrollPane;
	HTMLDocument document;
	Element root;
	boolean colorParity = true;

	public LogViewer(Map config)
	{
		this.config = config;
		editorPane = new JEditorPane();
		editorPane.setEditable(false);
		clear();
		scrollPane = new JScrollPane(editorPane);
	}

	public void append(String level, String message, String secondaryMessage)
	{
		String icon = config.get(level).toString();
		String bgColor = (colorParity ? config.get("colorParityTrue").toString() : config.get("colorParityFalse").toString());
		try
		{
			document.insertBeforeEnd(root,
				String.format(config.get("entryTemplate").toString(),
						bgColor, icon,
						message, secondaryMessage));
		}
		catch (BadLocationException e) {}
		catch (IOException e) {}
		colorParity = !colorParity;
	}

	public void scrollToBottom()
	{
		// If we scroll the bottom immediately after
		// we call document.insertBeforeEnd(), the scroll bar won't go to
		// end because the scroll bar by then does not recognize the latest
		// update to document. If we wrap the scrolling code in an
		// invokeLater() call, this will ensure the scroll bar will move
		// to the bottom.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
				if (scrollBar != null)
					scrollBar.setValue(scrollBar.getMaximum());
			}
		});
	}

	public void clear()
	{
		editorPane.setText("");
		editorPane.setContentType("text/html");
		try
		{
			editorPane.setPage(getClass().getResource(config.get("baseHTMLPath").toString()));
		}
		catch (IOException e) {}
		document = (HTMLDocument) editorPane.getDocument();
		root = document.getRootElements()[0];
		colorParity = true;
	}

	public JComponent getComponent()
	{
		return scrollPane;
	}
}
