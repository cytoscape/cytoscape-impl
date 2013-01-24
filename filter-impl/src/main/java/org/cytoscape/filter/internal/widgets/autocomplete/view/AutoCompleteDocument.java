package org.cytoscape.filter.internal.widgets.autocomplete.view;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import java.awt.*;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import org.cytoscape.filter.internal.widgets.autocomplete.index.Hit;


/**
 * PlainDocument used by the TextIndexComboBox.
 * <p/>
 * Most of this code is based on:
 * <A href="http://www.orbital-computer.de/JComboBox/">
 * http://www.orbital-computer.de/JComboBox/</A>, which is in the public
 * domain.
 *
 * @author Thomas Bierhance, Ethan Cerami.
 */
public class AutoCompleteDocument extends PlainDocument {
	// flag to indicate if setSelectedItem has been called
	// subsequent calls to remove/insertString should be ignored
	private boolean selecting = false;

	//  The container component
	private TextIndexComboBox comboBox;

	//  The editor component
	private JTextComponent editor;

	//  User has pressed the backspace key
	private boolean hitBackspace;

	//  Cursor key pressed
	private boolean cursorKeyPressed = false;

	//  User has pressed the backspace key, while a piece of text was selected
	private boolean hitBackspaceOnSelection;

	//  Debug flag
	private static final boolean DEBUG_MODE = false;

	/**
	 * Constructor.
	 *
	 * @param comboBox TextIndexComboBox.
	 */
	public AutoCompleteDocument(TextIndexComboBox comboBox) {
		this.comboBox = comboBox;
		this.editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
	}

	/**
	 * Indicates that text input has triggered a JComboBox setSelectedItem()
	 * call.
	 *
	 * @return true or false.
	 */
	public boolean getSelecting() {
		return this.selecting;
	}

	/**
	 * Indicates if user has just pressed the backspace key.
	 *
	 * @param hitBackspace true or false.
	 */
	public void setHitBackspace(boolean hitBackspace) {
		this.hitBackspace = hitBackspace;
	}

	/**
	 * Sets that the user has pressed a cursor key.
	 *
	 * @param flag cursor key pressed.
	 */
	public void setCursorKeyPressed(boolean flag) {
		this.cursorKeyPressed = flag;
	}

	/**
	 * Indicates that user has pressed a cursor key.
	 *
	 * @return flag cursor key pressed.
	 */
	public boolean getCursorKeyPressed() {
		return cursorKeyPressed;
	}

	/**
	 * Indicates if the user has just pressed the backspace key while a
	 * specific section of text was selected.
	 *
	 * @param hitBackspaceOnSelection true or false.
	 */
	public void setHitBackspaceOnSelection(boolean hitBackspaceOnSelection) {
		this.hitBackspaceOnSelection = hitBackspaceOnSelection;
	}

	/**
	 * Replaces existing text.
	 *
	 * @param offset offset value.
	 * @param length length value.
	 * @param text   replacement text.
	 * @param attrs  text attributes.
	 * @throws BadLocationException Bad Location Error.
	 */
	public void replace(int offset, int length, String text, AttributeSet attrs)
	    throws BadLocationException {
		text = extractAscciCharsOnly(text);
		debug("replace(), text=" + text + ", cursorKeyPressed=" + cursorKeyPressed);

		int caretPosition = editor.getCaretPosition();
		super.replace(offset, length, text, attrs);

		if (cursorKeyPressed) {
			highlightCompletedText(caretPosition);
		}
	}

	/**
	 * Removes all text from TextBox;  take no other action.
	 *
	 * @throws BadLocationException Bad Location Error.
	 */
	public void removeAllText() throws BadLocationException {
		if (getLength() > 0) {
			super.remove(0, getLength());
		}
	}

	/**
	 * Removes some content from the document.
	 * <p/>
	 * Overrides: remove() in class AbstractDocument
	 *
	 * @param offs offset
	 * @param len  length
	 * @throws javax.swing.text.BadLocationException
	 *          Bad Location Error.
	 */
	public void remove(int offs, int len) throws BadLocationException {
		// return immediately when selecting an item; prevents infinite loop
		if (selecting) {
			debug("remove(), offs=" + offs + ", len=" + len + " --> Exit");

			return;
		}

		debug("remove(), offs=" + offs + ", len=" + len + " --> Process");

		//  Handle Backspace Key
		if (hitBackspace) {
			// user hit backspace => move the selection backwards
			// old item keeps being selected
			if (offs > 0) {
				if (hitBackspaceOnSelection) {
					offs--;
				}
			}
		}

		//  Assume we have the following text:  "butter".
		//  Now, we call remove (2, 1), we want the new text to be:
		//  "bu", not "buter".
		int numCharsToRemove = getLength() - offs;
		super.remove(offs, numCharsToRemove);

		//  Look up new items; calling this method causes the pull-down menu
		//  to be populated with new items.
		lookUpItem(0, getText(0, getLength()));
	}

	/**
	 * Inserts String into document.
	 *
	 * @param offs Offset value.
	 * @param str  String value.
	 * @param a    AttributeSet Object.
	 * @throws BadLocationException Bad Location Error.
	 */
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		// return immediately when selecting an item;  prevents infinite loops
		if (selecting) {
			debug("insertString(), str=" + str + "  --> Exit");

			return;
		}
		str = extractAscciCharsOnly(str);
		debug("insertString(), str=" + str + "  --> Process");

		//  Assume we have the following text:  "butter".
		//  We now call insert (2, "g");
		//  We want the text to now be set to "bug", not "bugtter".
		//  First, insert new string.
		super.insertString(offs, str, a);

		int newEnd = offs + str.length();

		//  Then, remove everything past the newly inserted word
		if ((getLength() - newEnd) > 0) {
			super.remove(newEnd, getLength() - (newEnd));
		}

		//  Perform item lookup
		lookUpItem(offs, str);
	}

	/**
	 * Populates the Pull-Down menu with all words that begin with the
	 * specific pattern.
	 *
	 * @param pattern String pattern.
	 */
	public void populatePullDownMenu(String pattern) {
		debug("Start :  populatePullDownMenu(), pattern=" + pattern);

		Hit[] hits = comboBox.getTextIndex()
		                     .getHits(pattern, TextIndexComboBox.DEFAULT_MAX_HITS_SHOWN);

		//  Populate Pull-Down Menu with matching items derived from TextIndex
		//  Must set selecting flag to true, as modifying the model will
		//  result is multiple calls to setSelectedItem(), which results
		//  in an infinite loop.
		setSelecting(true);

		DefaultComboBoxModel model = (DefaultComboBoxModel) comboBox.getModel();
		debug("Start:  Remove all elements");
		model.removeAllElements();
		debug("End:  Remove all elements");

		if (hits.length > 0) {
			int stopHits = Math.min(hits.length, TextIndexComboBox.DEFAULT_MAX_HITS_SHOWN);

			for (int i = 0; i < stopHits; i++) {
				//  When we call addElement and there is only one item in
				//  the list, the DefaultComboBox will automatically call
				//  setSelected();  this can result in an inifinte loop.
				//  To prevent this, we set the set selecting flag to true.
				model.addElement(hits[i]);
			}

			editor.setBackground(Color.WHITE);
			editor.setForeground(Color.BLACK);
		} else {
			if ((comboBox.getTextIndex() != null) && (comboBox.getTextIndex().getNumKeys() > 0)) {
				model.addElement(new Hit(pattern, new Object[0]));
				editor.setBackground(new Color(212, 153, 159));
				editor.setForeground(Color.WHITE);
			}
		}

		//  Set Max Row Count;  this automatically resizes the pop-up
		//  menu window.
		if (model.getSize() < TextIndexComboBox.DEFAULT_MAX_ROWS_VISIBLE) {
			comboBox.setMaximumRowCount(model.getSize());
		} else {
			comboBox.setMaximumRowCount(TextIndexComboBox.DEFAULT_MAX_ROWS_VISIBLE);
		}

		//  Must set selected item to null;  otherwise, zeroeth
		//  item in model is selected, and we don't want it to be.
		setSelectedItem(null);
		setSelecting(false);
		debug("End:  populatePullDownMenu(), pattern=" + pattern);
	}

	/**
	 * Looks up item from the Pull-Down menu.
	 *
	 * @param offs Offset into document.
	 * @param str  Newly inserted addition to the document.
	 * @throws BadLocationException Bad Location Error.
	 */
	private void lookUpItem(int offs, String str) throws BadLocationException {
		if (cursorKeyPressed) {
			debug("lookUpItem(), str=" + str + ", cursorKeyPressed:  " + cursorKeyPressed
			      + " --> Exit");

			return;
		}

		debug("lookUpItem(), str=" + str + ", cursorKeyPressed:  " + cursorKeyPressed
		      + " --> Process");

		String pattern = getText(0, getLength());

		if (pattern != null) {
			populatePullDownMenu(pattern);

			//  Make pop-up visible;  must be done b/c model has been reset
			if (!comboBox.getUI().isPopupVisible(comboBox)) {
				if (comboBox.isDisplayable()) {
					comboBox.setPopupVisible(true);
				}
			}

			//  Iterate through all items in pull-down menu
			Object item = null;
			ComboBoxModel model = comboBox.getModel();

			for (int i = 0, n = model.getSize(); i < n; i++) {
				Object currentItem = model.getElementAt(i);

				// current item starts with the pattern?
				if ((currentItem != null) && startsWithIgnoreCase(currentItem.toString(), pattern)) {
					item = currentItem;

					break;
				}
			}

			if (item != null) {
				setSelectedItem(item);
				// select the completed part
				highlightCompletedText(offs + str.length());
			}
		}
	}

	/**
	 * Sets Document Text.
	 *
	 * @param text Document Text.
	 */
	private void setText(String text) {
		debug("setText(), text=" + text);

		try {
			// remove all text and insert the completed string
			super.remove(0, getLength());
			super.insertString(0, text, null);
		} catch (BadLocationException e) {
			throw new RuntimeException(e.toString());
		}
	}

	/**
	 * Highlights Completed Text, so that it is clear which text was entered
	 * by the user, and which text was auto-completed.
	 *
	 * @param start start index.
	 */
	private void highlightCompletedText(int start) {
		debug("highlightCompletedText, start= " + start);
		editor.setCaretPosition(getLength());
		editor.moveCaretPosition(start);
	}

	/**
	 * Sets Selected Item.
	 *
	 * @param item Object selected.
	 */
	private void setSelectedItem(Object item) {
		debug("setSelectedItem(), item=" + item);

		if (item != null) {
			setSelecting(true);

			ComboBoxModel model = comboBox.getModel();
			model.setSelectedItem(item);
			setText(item.toString());
			setSelecting(false);
		}
	}

	/**
	 * Indicates whether item is being selected from the pull-down menu.
	 *
	 * @param selecting true or false.
	 */
	public void setSelecting(boolean selecting) {
		this.selecting = selecting;
	}

	// checks if str1 starts with str2 - ignores case
	protected boolean startsWithIgnoreCase(String str1, String str2) {
		return str1.toUpperCase().startsWith(str2.toUpperCase());
	}

	private void debug(String str) {
		if (DEBUG_MODE) {
			System.out.println(str);
		}
	}

	/**
	 * Enables users to only enter ASCII chars <= 255.
	 * This prevents users from entering Unicode values via, e.g. the ALT/Option key
	 * combination.  Part of fix for bug #1310.
	 * @param text
	 * @return text containing only ASCII characters;  all other chars are filered out.
	 */
	private String extractAscciCharsOnly (String text) {
		StringBuffer newText = new StringBuffer();
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (c <= 126) {
				newText.append(c);
			}
		}
		return newText.toString();
	}
}
