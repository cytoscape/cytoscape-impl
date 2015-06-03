package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class MinMaxDialog extends JDialog {
	
	private static MinMaxDialog dialog;
	
	private JButton cancelButton;
	private JLabel maxLabel;
	private JTextField maxTextField;
	private JLabel minLabel;
	private JTextField minTextField;
	private JButton okButton;
	private JPanel mainPanel;
	
	private Double min;
	private Double max;

	public MinMaxDialog(final Window parent, Double min, Double max) {
		super(parent, ModalityType.APPLICATION_MODAL);
		
		this.setLocationRelativeTo(parent);
		this.min = min;
		this.max = max;
		
		initComponents();

		this.minTextField.setText(min.toString());
		this.maxTextField.setText(max.toString());
	}

	public static Double[] getMinMax(double min, double max, final Window parent) {
		Double[] minMax = new Double[2];
		dialog = new MinMaxDialog(parent, min, max);
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

		if ((dialog.min == null) || (dialog.max == null))
			return null;

		minMax[0] = dialog.min;
		minMax[1] = dialog.max;

		return minMax;
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Set Range");
		setModalityType(DEFAULT_MODALITY_TYPE);
		setResizable(false);

		minLabel = new JLabel("Min:");
		maxLabel = new JLabel("Max:");
		
		minTextField = new JTextField();
		minTextField.setHorizontalAlignment(JTextField.RIGHT);
		
		maxTextField = new JTextField();
		maxTextField.setHorizontalAlignment(JTextField.RIGHT);
		
		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(getOkButton(), getCancelButton());
		
		final JPanel contents = new JPanel();
		final GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getMainPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getMainPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);

		getContentPane().add(contents);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getOkButton().getAction(),
				getCancelButton().getAction());
		getRootPane().setDefaultButton(getOkButton());
		
		pack();
	}
	
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setBorder(LookAndFeelUtil.createTitledBorder("Set Value Range"));
			
			final GroupLayout layout = new GroupLayout(mainPanel);
			mainPanel.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
							.addComponent(minLabel)
							.addComponent(maxLabel)
					)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
							.addComponent(minTextField, 182, 182, 182)
							.addComponent(maxTextField, 182, 182, 182)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(minLabel)
							.addComponent(minTextField)
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(maxLabel)
							.addComponent(maxTextField)
					)
			);
		}
		
		return mainPanel;
	}
	
	protected JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(new AbstractAction("Cancel") {
				@Override
				public void actionPerformed(ActionEvent e) {
					min = null;
					max = null;
					dispose();
				}
			});
		}
		
		return cancelButton;
	}
	
	protected JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(new AbstractAction("OK"){
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						min = Double.valueOf(minTextField.getText());
						max = Double.valueOf(maxTextField.getText());
					} catch (NumberFormatException nfe) {
						min = null;
						max = null;
					}

					dispose();
				}
			});
		}
		
		return okButton;
	}
}
