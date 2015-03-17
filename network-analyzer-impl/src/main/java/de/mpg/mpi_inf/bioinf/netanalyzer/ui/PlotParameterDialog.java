package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.LookAndFeelUtil;

import de.mpg.mpi_inf.bioinf.netanalyzer.InnerException;
import de.mpg.mpi_inf.bioinf.netanalyzer.Plugin;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Decorators;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Points2D;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.Points2DGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.dec.Decorator;

/**
 * Dialog for plotting two computed attributes, i.e. plotting the correlation between two
 * user-defined attributes.
 * 
 * @author Nadezhda Doncheva
 */
public class PlotParameterDialog extends VisualizeParameterDialog implements ActionListener {

	private static final long serialVersionUID = 9024129225256810465L;
	
	/** Name of the ComplexParameter used as wrapper for the plot of two arbitrary parameters. */
	private static final String paramID = "userDefined";

	/** Panel containing the Parameter Plot. */
	private JPanel panPlot;

	/** Button for closing this dialog. */
	private JButton btnClose;

	/** Drop-down list for selecting a node attribute to be plotted on the x-axis. */
	private JComboBox cbxAttr1;

	/** Drop-down list for selecting a node attribute to be plotted on the y-axis. */
	private JComboBox cbxAttr2;

	/**
	 * Cytoscape's node attributes.
	 */
	private CyTable cyAttr;
	
	private final Frame parent;
	
	/**
	 * Initializes a new instance of <code>PlotParameterDialog</code>.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aNetwork
	 *            Network whose attributes are to be plotted.
	 * @param aNodeAttr
	 *            Two-dimensional array with computed parameters stored as node attributes. The
	 *            first column contains all networkAnalyzer attributes, and the second one other
	 *            attributes.
	 */
	public PlotParameterDialog(Frame aOwner, CyNetwork aNetwork, String[][] aNodeAttr) {
		super(aOwner, Messages.DT_PLOTPARAM, true, aNetwork, aNodeAttr, null);
		cyAttr = aNetwork.getDefaultNodeTable();
		parent = aOwner;
		init();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == cbxAttr1 || src == cbxAttr2) {
			update();
		}
	}

	/**
	 * Returns the value of an attribute named "attr" for the node with a node identifier "id" as a
	 * Double.
	 * 
	 * @param id
	 *            Node identifier of the node whose attribute has to be returned.
	 * @param attr
	 *            Name of the attribute whose value has to be returned.
	 * @param attrType
	 *            Type of the attribute. Can only be float or integer.
	 * @return A Double value representation of the attribute value for the node with node
	 *         identifier id.
	 */
	private Double getAttrValue(CyNetwork network, CyNode node, String attr, Class<?> attrType) {
		if (attrType == Double.class) {
			return network.getRow(node).get(attr,Double.class);
		}
		if (attrType == Integer.class) {
			return network.getRow(node).get(attr,Integer.class).doubleValue();
		}
		return null;
	}

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	@SuppressWarnings("serial")
	private void init() {
		setResizable(false);
		
		// Title
		final JLabel title = new JLabel(
				"<html>" + Messages.DI_PLOT1 + 
				"<b>" + network.getRow(network).get("name",String.class) + "</b>" + 
				Messages.DI_PLOT2
		);

		// Drop-down menus with node attributes to plot against each other
		final String[] combined = combineAttrArray(nodeAttr, false);
		
		final JLabel label1 = new JLabel(Messages.DI_ATTRIBUTE1);
		final JLabel label2 = new JLabel(Messages.DI_ATTRIBUTE2);
		
		cbxAttr1 = new JComboBox(combined);
		cbxAttr1.addActionListener(this);
		cbxAttr1.setRenderer(new ComboBoxRenderer());
		
		cbxAttr2 = new JComboBox(combined);
		cbxAttr2.addActionListener(this);
		cbxAttr2.setRenderer(new ComboBoxRenderer());

		panPlot = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panPlot.setBorder(LookAndFeelUtil.createPanelBorder());

		// Close button
		btnClose = new JButton(new AbstractAction(Messages.DI_CLOSE) {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		
		final JPanel panBottom = LookAndFeelUtil.createOkCancelPanel(null, btnClose);
		panBottom.add(btnClose);

		final Random r = new Random();
		cbxAttr1.setSelectedIndex(r.nextInt(combined.length));
		cbxAttr2.setSelectedIndex(r.nextInt(combined.length));
		
		final JPanel contentPane = new JPanel();
		final GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(title, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(label1)
								.addComponent(label2)
						)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addComponent(cbxAttr1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(cbxAttr2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
				)
				.addComponent(panPlot)
				.addComponent(panBottom, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(title)
				.addGap(20)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(label1)
						.addComponent(cbxAttr1)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(label2)
						.addComponent(cbxAttr2)
				)
				.addComponent(panPlot)
				.addComponent(panBottom)
		);
		
		setContentPane(contentPane);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), null, btnClose.getAction());
		getRootPane().setDefaultButton(btnClose);
		
		update();
	}

	/**
	 * Plots the attributes attr1 and attr2 against each other if they aren't null. This method is
	 * called every time a new node attribute is chosen. If only one of the attributes is chosen,
	 * then an empty chart is shown.
	 */
	private void update() {
		final String attrX = cbxAttr1.getSelectedItem().toString();
		final String attrY = cbxAttr2.getSelectedItem().toString();
		
		if (attrX == null || attrY == null || attrX == Utils.SEPARATOR || attrY == Utils.SEPARATOR) {
			return;
		}
		
		final Class<?> attrType1 = cyAttr.getColumn(attrX).getType();
		final Class<?> attrType2 = cyAttr.getColumn(attrY).getType();
		final List<Point2D.Double> plotValues = new ArrayList<Point2D.Double>(network.getNodeCount());
		
		for (CyNode node : network.getNodeList()) {
			Double value1 = getAttrValue(network, node, attrX, attrType1);
			Double value2 = getAttrValue(network, node, attrY, attrType2);
			
			if (value1 != null && value2 != null) {
				plotValues.add(new Point2D.Double(value1.doubleValue(), value2.doubleValue()));
			}
		}
		
		initChartPanel(plotValues, attrX, attrY);
		((JPanel) getContentPane()).updateUI();
		pack();
		
		setLocationRelativeTo(parent);
	}

	private void initChartPanel(List<Point2D.Double> plotValues, String attrX, String attrY) {
		ComplexParam cp = new Points2D(plotValues);
		String typeName = cp.getClass().getSimpleName();
		
		try {
			final Class<?> visClass = Plugin.getVisualizerClass(typeName);
			final Constructor<?> con = visClass.getConstructors()[0];

			final Points2DGroup settings = (Points2DGroup) SettingsSerializer.getDefault(paramID);
			final Class<?> settingsClass = Plugin.getSettingsGroupClass(typeName);
			final Field axesField = settingsClass.getField("axes");
			final Object aSettings = axesField.get(settings);
			final Class<?> axesFieldType = axesField.getType();
			axesFieldType.getMethod("setDomainAxisLabel", new Class[] { String.class }).invoke(aSettings, attrX);
			axesFieldType.getMethod("setRangeAxisLabel", new Class[] { String.class }).invoke(aSettings, attrY);
			
			final Object[] conParams = new Object[] { cp, settings };
			final ComplexParamVisualizer v = (ComplexParamVisualizer) con.newInstance(conParams);
			final Decorator[] decs = Decorators.get(paramID);
			final ChartDisplayPanel cPanel = new ChartDisplayPanel(this, paramID, v, decs);
			
			if (LookAndFeelUtil.isAquaLAF())
				cPanel.setOpaque(false);
			
			panPlot.removeAll();
			panPlot.add(cPanel, BorderLayout.CENTER);
		} catch (Exception ex) {
			throw new InnerException(ex);
		}
	}
}
