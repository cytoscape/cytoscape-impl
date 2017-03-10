package org.cytoscape.ding.internal.charts.ring;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.charts.pie.PieChart;
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

public class RingChartEditor extends AbstractChartEditor<RingChart> {

	private static final long serialVersionUID = -1867268965571724061L;
	
	private JLabel startAngleLbl;
	private JComboBox<Double> startAngleCmb;
	private JLabel holeLbl;
	private JTextField holeTxt;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RingChartEditor(final RingChart chart, final CyServiceRegistrar serviceRegistrar) {
		super(chart, Number.class, false, false, false, true, false, false, false, false, serviceRegistrar);
		
		domainLabelPositionLbl.setVisible(false);
		getDomainLabelPositionCmb().setVisible(false);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		super.createLabels();
		startAngleLbl = new JLabel("Start Angle (degrees):");
		holeLbl = new JLabel("Hole Size (0.0-1.0):");
	}
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
					.addComponent(startAngleLbl)
					.addComponent(holeLbl))
				.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
					.addComponent(getStartAngleCmb(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE)
					.addComponent(getHoleTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE))
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(startAngleLbl)
						.addComponent(getStartAngleCmb()))
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(holeLbl)
						.addComponent(getHoleTxt()))
				);
		
		return p;
	}
	
	private JComboBox<Double> getStartAngleCmb() {
		if (startAngleCmb == null) {
			startAngleCmb = createAngleComboBox(chart, PieChart.START_ANGLE, ANGLES);
		}
		
		return startAngleCmb;
	}
	
	private JTextField getHoleTxt() {
		if (holeTxt == null) {
			holeTxt = new JTextField("" + chart.get(RingChart.HOLE_SIZE, Double.class, 0.4));
			holeTxt.setToolTipText("Diameter of the ring hole, as a proportion of the entire plot");
			holeTxt.setInputVerifier(new DoubleInputVerifier());
			holeTxt.setPreferredSize(new Dimension(60, holeTxt.getMinimumSize().height));
			holeTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			holeTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					try {
			            double angle = Double.valueOf(holeTxt.getText().trim()).doubleValue();
			            chart.set(RingChart.HOLE_SIZE, angle);
			        } catch (NumberFormatException ex) {
			        }
				}
			});
		}
		
		return holeTxt;
	}
}
