
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.filter.internal.widgets.autocomplete.view;


import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.cytoscape.filter.internal.widgets.autocomplete.index.Hit;


/**
 * Custom Renderer for the TextIndexComboBox.
 *
 * @author Ethan Cerami
 */
public class TextBoxRenderer implements ListCellRenderer {
	private JComboBox box;
	private double popupSizeMultiple;
	private static final int HGAP = 10;
	private static final boolean DEBUG = false;

	/**
	 * Constructor.
	 *
	 * @param box               JComboBox.
	 * @param popupSizeMultiple Popup size multiple.
	 */
	public TextBoxRenderer(JComboBox box, double popupSizeMultiple) {
		this.box = box;
		this.popupSizeMultiple = popupSizeMultiple;
	}

	/**
	 * This method finds the image and text corresponding
	 * to the selected value and returns the label, set up
	 * to display the text and image.
	 *
	 * @param list         JList Object.
	 * @param value        Object value.
	 * @param index        Index value.
	 * @param isSelected   is item selected flag.
	 * @param cellHasFocus call has focus flag.
	 * @return Component Object.
	 */
	public Component getListCellRendererComponent(JList list, Object value, int index,
	                                              boolean isSelected, boolean cellHasFocus) {
		//  Create a JPanel Object
		JPanel panel = new JPanel();
		panel.setOpaque(true);

		//  Set different colors, depending on state
		if (isSelected) {
			panel.setBackground(list.getSelectionBackground());
			panel.setForeground(list.getSelectionForeground());
		} else {
			panel.setBackground(list.getBackground());
			panel.setForeground(list.getForeground());
		}

		//  Use Box Layout, X-AXIS
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(new EmptyBorder(2, 2, 2, 2));

		//  Create text match label, but do not (yet) set final text,
		//  as we may need to truncate it.
		JLabel textMatchLabel = new JLabel("TEMP", JLabel.LEFT);
		panel.setToolTipText(value.toString());

		String numResults = getNumResults(value);
		JLabel numResultsLabel = new JLabel(numResults, JLabel.RIGHT);
		numResultsLabel.setFont(new Font("Monospaced", Font.PLAIN,
		                                 numResultsLabel.getFont().getSize() - 1));

		//  Set color to green (matches exist) or red (no matches exist);
		Color color = new Color(51, 102, 51);

		if (value instanceof Hit) {
			Hit hit = (Hit) value;

			if (hit.getAssociatedObjects() != null) {
				int numHits = hit.getAssociatedObjects().length;

				if (numHits == 0) {
					color = new Color(150, 0, 0);
				}
			}
		}

		numResultsLabel.setForeground(color);

		//  Resize Labels
		resizeLabels(numResultsLabel, textMatchLabel, value.toString());

		//  Add Label 1, then glue, then Label 2
		//  The glue forces Label 1 to be left aligned, and Label 2 to be
		//  right aligned
		panel.add(textMatchLabel);
		panel.add(Box.createHorizontalGlue());
		panel.add(numResultsLabel);

		return panel;
	}

	/**
	 * Resizes the Labels, as needed.
	 *
	 * @param numResultsLabel Number of Results Label.
	 * @param textMatchLabel  Text Match Label.
	 */
	private void resizeLabels(JLabel numResultsLabel, JLabel textMatchLabel, String textString) {
		if (box.isPopupVisible()) {
			//  How wide is the popup window?
			int widthOfPopUpWindow = (int) (box.getSize().width * this.popupSizeMultiple);

			//  How wide is numResultsLabel?
			FontMetrics fontMetrics = numResultsLabel.getFontMetrics(numResultsLabel.getFont());
			int widthOfNumResultsText = fontMetrics.stringWidth(numResultsLabel.getText());

			//  How wide is the vertical scrollbar?
			int widthOfVerticalScrollBar = 0;
			Object comp = box.getUI().getAccessibleChild(box, 0);

			if (comp instanceof JPopupMenu) {
				JScrollPane scrollPane = (JScrollPane) ((JPopupMenu) comp).getComponent(0);
				JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();

				if ((verticalScrollBar != null) && verticalScrollBar.isVisible()) {
					widthOfVerticalScrollBar = verticalScrollBar.getPreferredSize().width;
				}
			}

			//  Truncate label text, as needed
			int maxWidthOfTextValue = widthOfPopUpWindow
			                          - (HGAP + widthOfNumResultsText + widthOfVerticalScrollBar);
			fontMetrics = textMatchLabel.getFontMetrics(textMatchLabel.getFont());

			int currentWidth = fontMetrics.stringWidth(textString);

			if ((currentWidth + HGAP) > maxWidthOfTextValue) {
				textMatchLabel.setText(textMatchLabel.getText() + "...");

				//  while loop:  truncate string one letter at a time
				//  until we have the right size string.
				while ((currentWidth + HGAP) > maxWidthOfTextValue) {
					textString = textString.substring(0, textString.length() - 4) + "...";
					currentWidth = fontMetrics.stringWidth(textString);
				}
			}

			textMatchLabel.setText(textString);

			if (DEBUG) {
				System.out.println("Width of window:  " + widthOfPopUpWindow);
				System.out.println("Width of num results text:  " + widthOfNumResultsText);
				System.out.println("Bar:  " + widthOfVerticalScrollBar);
				System.out.println("Width of text:  " + maxWidthOfTextValue);
			}
		}
	}

	/**
	 * Gets Number of Matching Results.
	 *
	 * @param value Object Value.
	 * @return Number of Results String.
	 */
	private String getNumResults(Object value) {
		String numResults = null;

		if (value instanceof Hit) {
			Hit hit = (Hit) value;
			Object[] objects = hit.getAssociatedObjects();

			if (objects != null) {
				if (objects.length == 1) {
					numResults = "1 hit ";
				} else {
					numResults = objects.length + " hits ";
				}
			}
		}

		if (numResults == null) {
			numResults = " -- ";
		}

		return numResults;
	}
}
