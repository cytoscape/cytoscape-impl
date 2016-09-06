package org.cytoscape.ding.internal.charts.line;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class LineChartEditor extends AbstractChartEditor<LineChart> {

	private static final long serialVersionUID = 2428987302044041051L;
	
	private JLabel lineWidthLbl;
	private JTextField lineWidthTxt;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LineChartEditor(final LineChart chart, final CyServiceRegistrar serviceRegistrar) {
		super(chart, Number.class, true, true, false, true, true, false, true, true, serviceRegistrar);
		
		getBorderPnl().setVisible(false);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		super.createLabels();
		lineWidthLbl = new JLabel("Line Width:");
	}
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(lineWidthLbl)
						.addComponent(getLineWidthTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE))
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(lineWidthLbl)
						.addComponent(getLineWidthTxt()))
				);
		
		return p;
	}
	
	private JTextField getLineWidthTxt() {
		if (lineWidthTxt == null) {
			lineWidthTxt = new JTextField("" + chart.get(LineChart.LINE_WIDTH, Float.class, 1.0f));
			lineWidthTxt.setInputVerifier(new DoubleInputVerifier());
			lineWidthTxt.setPreferredSize(new Dimension(40, lineWidthTxt.getMinimumSize().height));
			lineWidthTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			lineWidthTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					try {
			            float width = Double.valueOf(lineWidthTxt.getText().trim()).floatValue();
			            chart.set(LineChart.LINE_WIDTH, width);
			        } catch (NumberFormatException ex) {
			        }
				}
			});
		}
		
		return lineWidthTxt;
	}
}
