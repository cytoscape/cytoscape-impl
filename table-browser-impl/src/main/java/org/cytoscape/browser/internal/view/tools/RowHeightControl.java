package org.cytoscape.browser.internal.view.tools;

import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isWinLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_ROW_HEIGHT;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.ContinuousRange;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class RowHeightControl extends AbstractToolBarControl {
	
	private static final int MIN_VALUE = 8; // less than this and the table is probably useless
	
	private JSlider heightSlider;
	private JTextField heightText;
	private JButton resetButton;
	
	private int value;
	
	private boolean adjusting;
	
	public RowHeightControl(CyServiceRegistrar serviceRegistrar) {
		super("Row Height", serviceRegistrar);
		
		init();
	}
	
	private void setValue(int value) {
		if (value != this.value) {
			this.value = value;
			apply(TABLE_ROW_HEIGHT, value);
		}
	}

	@Override
	protected void update() {
		adjusting = true;
		
		try {
			var tableView = getTableView();
			
			if (tableView != null) {
				var value = tableView.getVisualProperty(TABLE_ROW_HEIGHT);
				value = clamp(value);
				
				if (this.value != value) {
					this.value = value;
					getHeightSlider().setValue(value);
				}
			}
		} finally {
			adjusting = false;
		}
	}
	
	private void init() {
		var layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(isWinLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getHeightSlider())
				.addComponent(getHeightText())
				.addComponent(getResetButton())
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getHeightSlider())
						.addComponent(getHeightText())
						.addComponent(getResetButton())
				)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		
		makeSmall(getHeightSlider(), getHeightText(), getResetButton());
		setAquaStyle(getHeightSlider(), getHeightText(), getResetButton());
		equalizeSize(getHeightText(), getResetButton());
	}
	
	private JButton getResetButton() {
		if (resetButton == null) {
			resetButton = new JButton(IconManager.ICON_REFRESH);
			resetButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(10.0f));
			resetButton.setToolTipText("Reset");
			resetButton.addActionListener(evt -> {
				getHeightSlider().setValue(TABLE_ROW_HEIGHT.getDefault());
			});
		}
		
		return resetButton;
	}
	
	private JSlider getHeightSlider() {
		if (heightSlider == null) {
			var range = (ContinuousRange<Integer>) TABLE_ROW_HEIGHT.getRange();
			var min = Math.max(MIN_VALUE, range.getMin());
			var max = range.getMax();
			var def = TABLE_ROW_HEIGHT.getDefault();
			
			heightSlider = new JSlider(min, max);
			heightSlider.setValue(value <= 0 ? def : clamp(value));
			heightSlider.setPreferredSize(new Dimension(120, heightSlider.getPreferredSize().height));
			heightSlider.addChangeListener(evt -> {
				if (adjusting)
					return;
				
				var text = "" + heightSlider.getValue();
				getHeightText().setText(text);
				setValue(getCurrentValue());
			});
		}
		
		return heightSlider;
	}
	
	private JTextField getHeightText() {
		if (heightText == null) {
			var min = getHeightSlider().getMinimum();
			var max = getHeightSlider().getMaximum();
			
			heightText = new JTextField();
			heightText.setToolTipText("Enter a number between " + min + " and " + max);
			heightText.setHorizontalAlignment(JTextField.RIGHT);
			
			var d = new Dimension(32, heightText.getPreferredSize().height);
			heightText.setPreferredSize(d);
			heightText.setMaximumSize(d);
			
			((AbstractDocument) heightText.getDocument()).setDocumentFilter(new IntDocFilter());
			heightText.setText("" + getHeightSlider().getValue());
			heightText.addActionListener(evt -> onTextChanged());
			heightText.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent evt) {
					onTextChanged();
				}
			});
		}
		
		return heightText;
	}
	
	private void onTextChanged() {
		int val = getCurrentValue();
		getHeightText().setText("" + val);
		getHeightSlider().setValue(clamp(val));
	}
	
	private int getCurrentValue() {
		int value = this.value;
		
		try {
			value = Integer.parseInt(getHeightText().getText());
			value = clamp(value); // clamp value
		} catch (NumberFormatException e) {
			// Ignore
		}
		
		return value;
	}
	
	private int clamp(int value) {
		var min = getHeightSlider().getMinimum();
		var max = getHeightSlider().getMaximum();
		
		return Math.max(min, Math.min(max, value));
	}
	
	private class IntDocFilter extends DocumentFilter {

		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
				throws BadLocationException {
			var doc = fb.getDocument();
			var sb = new StringBuilder();
			sb.append(doc.getText(0, doc.getLength()));
			sb.insert(offset, string);

			if (test(sb.toString())) {
				super.insertString(fb, offset, string, attr);
			} else {
				// warn the user and don't allow the insert
			}
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
				throws BadLocationException {
			var doc = fb.getDocument();
			var sb = new StringBuilder();
			sb.append(doc.getText(0, doc.getLength()));
			sb.replace(offset, offset + length, text);

			if (test(sb.toString())) {
				super.replace(fb, offset, length, text, attrs);
			} else {
				// Maybe warn the user?
			}
		}

		@Override
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
			var doc = fb.getDocument();
			var sb = new StringBuilder();
			sb.append(doc.getText(0, doc.getLength()));
			sb.delete(offset, offset + length);

			if (test(sb.toString())) {
				super.remove(fb, offset, length);
			} else {
				// Maybe warn the user?
			}
		}
		
		private boolean test(String text) {
			try {
				Integer.parseInt(text);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}

}
