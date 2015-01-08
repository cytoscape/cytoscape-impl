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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;

/**
 * Dialog for applying computed attributes to visual properties of a network.
 * 
 * @author Nadezhda Doncheva
 * @author Yassen Assenov
 */
public class MapParameterDialog extends VisualizeParameterDialog implements ActionListener {
	
	private final CyNetworkViewManager viewManager;
	private final VisualMappingManager vmm;
	private final VisualStyleBuilder vsBuilder;
	
	/**
	 * Initializes a new instance of <code>MapParameterDialog</code>.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aNetwork
	 *            Network to which a visual style is to be applied.
	 * @param aNodeAttr
	 *            Two-dimensional array with computed parameters stored as node attributes. The first column
	 *            contains all networkAnalyzer attributes, and the second one other attributes.
	 * @param aEdgeAttr
	 *            Two-dimensional array with computed parameters stored as edge attributes. The first column
	 *            contains all networkAnalyzer attributes, and the second one other attributes.
	 * @throws IllegalArgumentException
	 *             If no attributes computed by NetworkAnalyzer are found in <code>aNetwork</code>.
	 * @throws NullPointerException
	 *             If <code>aNetwork</code> is <code>null</code>.
	 */
	public MapParameterDialog(Window aOwner, CyNetwork aNetwork, final CyNetworkViewManager viewManager, final VisualStyleBuilder vsBuilder,
			final VisualMappingManager vmm, String[][] aNodeAttr, String[][] aEdgeAttr) {
		super(aOwner, Messages.DT_MAPPARAM, true, aNetwork, aNodeAttr, aEdgeAttr);

		this.viewManager = viewManager;
		this.vmm = vmm;
		this.vsBuilder = vsBuilder;
		
		getMinMaxMeanValues();
		init();
		setResizable(false);
		setLocationRelativeTo(aOwner);

		attrNodeColor = "";
		attrNodeSize = "";
		attrEdgeColor = "";
		attrEdgeSize = "";

		mapNodeSize = Messages.DI_LOWTOSMALL;
		mapNodeColor = Messages.DI_LOWTOBRIGHT;
		mapEdgeSize = Messages.DI_LOWTOSMALL;
		mapEdgeColor = Messages.DI_LOWTOBRIGHT;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		if (src == btnApply) {
			apply();
			setVisible(false);
			dispose();
		} else if (src == btnClose) {
			setVisible(false);
			dispose();
		} else if (src == cbxEdgeColor) {
			btnApply.setEnabled(isMappingSelected());
		} else if (src == cbxEdgeSize) {
			btnApply.setEnabled(isMappingSelected());
		} else if (src == cbxNodeColor) {
			btnApply.setEnabled(isMappingSelected());
		} else if (src == cbxNodeSize) {
			btnApply.setEnabled(isMappingSelected());
		} else if (src == btnNodeSize1) {
			mapNodeSize = btnNodeSize1.getText();
		} else if (src == btnNodeSize2) {
			mapNodeSize = btnNodeSize2.getText();
		} else if (src == btnNodeColor1) {
			mapNodeColor = btnNodeColor1.getText();
		} else if (src == btnNodeColor2) {
			mapNodeColor = btnNodeColor2.getText();
		} else if (src == btnEdgeSize1) {
			mapEdgeSize = btnEdgeSize1.getText();
		} else if (src == btnEdgeSize2) {
			mapEdgeSize = btnEdgeSize2.getText();
		} else if (src == btnEdgeColor1) {
			mapEdgeColor = btnEdgeColor1.getText();
		} else if (src == btnEdgeColor2) {
			mapEdgeColor = btnEdgeColor2.getText();
		}
	}
	
	private final void apply() {
		// Check view exists or not
		final Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		if(views.isEmpty())
			return;
		
		// Pick the first view
		final CyNetworkView networkView = views.iterator().next();
		
		if (cbxNodeSize != null && cbxNodeSize.getSelectedItem() != null
				&& cbxNodeSize.getSelectedItem() != Utils.SEPARATOR) {
			attrNodeSize = (String) cbxNodeSize.getSelectedItem();
		}
		if (cbxNodeColor != null && cbxNodeColor.getSelectedItem() != null
				&& cbxNodeColor.getSelectedItem() != Utils.SEPARATOR) {
			attrNodeColor = (String) cbxNodeColor.getSelectedItem();
		}
		if (cbxEdgeSize != null && cbxEdgeSize.getSelectedItem() != null
				&& cbxEdgeSize.getSelectedItem() != Utils.SEPARATOR) {
			attrEdgeSize = (String) cbxEdgeSize.getSelectedItem();
		}
		if (cbxEdgeColor != null && cbxEdgeColor.getSelectedItem() != null
				&& cbxEdgeColor.getSelectedItem() != Utils.SEPARATOR) {
			attrEdgeColor = (String) cbxEdgeColor.getSelectedItem();
		}
		
		// Create a new style and register it
		final VisualStyle newStyle = vsBuilder.createVisualStyle(networkView, this);
		vmm.addVisualStyle(newStyle);
		
		vmm.setVisualStyle(newStyle, networkView);
		vmm.setCurrentVisualStyle(newStyle);
		
		// Apply and update the current view
		newStyle.apply(networkView);
		networkView.updateView();
	}

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 * 
	 * @throws IllegalArgumentException
	 *             If no attributes computed by NetworkAnalyzer are found in {@link #network}.
	 * @throws NullPointerException
	 *             If {@link #network} is <code>null</code>.
	 */
	private void init() {
		final int BS = Utils.BORDER_SIZE;
		final JPanel contentPane = new JPanel(new BorderLayout(BS, BS));
		Utils.setStandardBorder(contentPane);

		// Add a title label
		String tt = "<html>" + Messages.DI_APPLYVS + "<b>" + network.getRow(network).get("name", String.class) + "</b>";
		contentPane.add(new JLabel(tt, SwingConstants.CENTER), BorderLayout.PAGE_START);

		boolean attrCalculated = false;

		// Get colors from plugin settings
		final Color c1 = SettingsSerializer.getPluginSettings().getBrightColor();
		final Color c2 = SettingsSerializer.getPluginSettings().getMiddleColor();
		final Color c3 = SettingsSerializer.getPluginSettings().getDarkColor();

		// Add controls for mapping attributes to node size and node color
		final JPanel panMapping = new JPanel(new BorderLayout(BS, BS));
		final JPanel panNode = new JPanel(new GridLayout(1, 2, BS, BS));
		final JPanel panEdge = new JPanel(new GridLayout(1, 2, BS, BS));

		if (nodeAttr[0].length > 0 || nodeAttr[1].length > 0) {
			attrCalculated = true;
			// Panel for node size mapping
			cbxNodeSize = new JComboBox(combineAttrArray(nodeAttr, true));
			cbxNodeSize.setLightWeightPopupEnabled(false);
			cbxNodeSize.addActionListener(this);
			cbxNodeSize.setRenderer(new ComboBoxRenderer());
			
			btnNodeSize1 = new JRadioButton(Messages.DI_LOWTOSMALL, true);
			btnNodeSize1.addActionListener(this);
			btnNodeSize2 = new JRadioButton(Messages.DI_LOWTOLARGE, false);
			btnNodeSize2.addActionListener(this);
			final ButtonGroup btngNodeSize = new ButtonGroup();
			btngNodeSize.add(btnNodeSize1);
			btngNodeSize.add(btnNodeSize2);
			
			final JPanel panNodeSize = createVizmapOptionPanel(
					Messages.DI_MAPNODESIZE,
					cbxNodeSize,
					btnNodeSize1,
					btnNodeSize2,
					new NodeEdgeCanvas(true, true),
					new NodeEdgeCanvas(false, true));
			
			panNode.add(panNodeSize);

			// Panel for node color mapping
			cbxNodeColor = new JComboBox(combineAttrArray(nodeAttr, true));
			cbxNodeColor.setLightWeightPopupEnabled(false);
			cbxNodeColor.addActionListener(this);
			cbxNodeColor.setRenderer(new ComboBoxRenderer());
			
			btnNodeColor1 = new JRadioButton(Messages.DI_LOWTOBRIGHT, true);
			btnNodeColor1.addActionListener(this);
			btnNodeColor2 = new JRadioButton(Messages.DI_LOWTODARK, false);
			btnNodeColor2.addActionListener(this);
			final ButtonGroup btngNodeColor = new ButtonGroup();
			btngNodeColor.add(btnNodeColor1);
			btngNodeColor.add(btnNodeColor2);
			
			final JPanel panNodeColor = createVizmapOptionPanel(
					Messages.DI_MAPNODECOLOR,
					cbxNodeColor,
					btnNodeColor1,
					btnNodeColor2,
					new RainbowCanvas(new Color[] { c1, c2, c3 }),
					new RainbowCanvas(new Color[] { c3, c2, c1 }));

			panNode.add(panNodeColor);
		}
		
		if (edgeAttr[0].length > 0 || edgeAttr[1].length > 0) {
			attrCalculated = true;
			
			// Panel for edge size mapping
			cbxEdgeSize = new JComboBox(combineAttrArray(edgeAttr, true));
			cbxEdgeSize.setLightWeightPopupEnabled(false);
			cbxEdgeSize.addActionListener(this);
			cbxEdgeSize.setRenderer(new ComboBoxRenderer());
			
			btnEdgeSize1 = new JRadioButton(Messages.DI_LOWTOSMALL, true);
			btnEdgeSize1.addActionListener(this);
			btnEdgeSize2 = new JRadioButton(Messages.DI_LOWTOLARGE, false);
			btnEdgeSize2.addActionListener(this);
			final ButtonGroup btngEdgeSize = new ButtonGroup();
			btngEdgeSize.add(btnEdgeSize1);
			btngEdgeSize.add(btnEdgeSize2);
			
			final JPanel panEdgeSize = createVizmapOptionPanel(
					Messages.DI_MAPEDGESIZE,
					cbxEdgeSize,
					btnEdgeSize1,
					btnEdgeSize2,
					new NodeEdgeCanvas(true, false),
					new NodeEdgeCanvas(false, false));

			panEdge.add(panEdgeSize);

			// Panel for edge color mapping
			cbxEdgeColor = new JComboBox(combineAttrArray(edgeAttr, true));
			cbxEdgeColor.setLightWeightPopupEnabled(false);
			cbxEdgeColor.addActionListener(this);
			cbxEdgeColor.setRenderer(new ComboBoxRenderer());
			
			btnEdgeColor1 = new JRadioButton(Messages.DI_LOWTOBRIGHT, true);
			btnEdgeColor1.addActionListener(this);
			btnEdgeColor2 = new JRadioButton(Messages.DI_LOWTODARK, false);
			btnEdgeColor2.addActionListener(this);
			final ButtonGroup btngEdgeColor = new ButtonGroup();
			btngEdgeColor.add(btnEdgeColor1);
			btngEdgeColor.add(btnEdgeColor2);
			
			final JPanel panEdgeColor = createVizmapOptionPanel(
					Messages.DI_MAPEDGECOLOR,
					cbxEdgeColor,
					btnEdgeColor1,
					btnEdgeColor2,
					new RainbowCanvas(new Color[] { c1, c2, c3 }),
					new RainbowCanvas(new Color[] { c3, c2, c1 }));

			panEdge.add(panEdgeColor);
		}
		
		if (attrCalculated == false) {
			throw new IllegalArgumentException();
		}
		
		panMapping.add(panNode, BorderLayout.PAGE_START);
		panMapping.add(panEdge, BorderLayout.PAGE_END);
		contentPane.add(panMapping, BorderLayout.CENTER);

		// Add Apply and Cancel buttons
		btnApply = Utils.createButton(Messages.DI_APPLY, null, this);
		btnApply.setEnabled(false);
		btnClose = Utils.createButton(Messages.DI_CANCEL, null, this);
		Utils.equalizeSize(btnApply, btnClose);
		
		final JPanel panButtons = LookAndFeelUtil.createOkCancelPanel(btnApply, btnClose);
		contentPane.add(panButtons, BorderLayout.PAGE_END);

		setContentPane(contentPane);
		pack();
	}
	
	private JPanel createVizmapOptionPanel(final String title, final JComboBox<?> cmb,
			final JRadioButton radio1, final JRadioButton radio2, final Component canvas1, final Component canvas2) {
		final JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder(title));
		
		canvas1.setPreferredSize(new Dimension(180, radio1.getPreferredSize().height));
		canvas2.setPreferredSize(new Dimension(180, radio2.getPreferredSize().height));
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(cmb, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(Alignment.CENTER, layout.createSequentialGroup()
						.addComponent(radio1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(canvas1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(Alignment.CENTER, layout.createSequentialGroup()
						.addComponent(radio2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(canvas2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(cmb, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addGroup(Alignment.LEADING, layout.createSequentialGroup()
								.addComponent(radio1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(radio2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
						.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
								.addComponent(canvas1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(canvas2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
				)
		);
		
		return panel;
	}

	/**
	 * Checks if the user has selected to apply a mapping from an attribute to a visual style.
	 * 
	 * @return <code>true</code> if the user has selected to map at least one attribute to a visual style;
	 *         <code>false</code> otherwise.
	 */
	private boolean isMappingSelected() {
		if (cbxEdgeColor != null && cbxEdgeColor.getSelectedIndex() != 0) {
			return true;
		}
		if (cbxEdgeSize != null && cbxEdgeSize.getSelectedIndex() != 0) {
			return true;
		}
		if (cbxNodeColor != null && cbxNodeColor.getSelectedIndex() != 0) {
			return true;
		}
		if (cbxNodeSize != null && cbxNodeSize.getSelectedIndex() != 0) {
			return true;
		}
		return false;
	}



	/**
	 * Computes the minimal, maximal and mean value for each computed attribute by consequently processing
	 * each node.
	 * 
	 * @param attrMap
	 *            Map containing for each node and attribute the computed value of this attribute.
	 * @param attr
	 *            Array with attributes' names, for which the minimal, maximal and mean value have to be
	 *            retrieved.
	 * @param id
	 *            A node id, whose computed attribute values are retrieved.
	 */
	private void getBoundaryValues(CyNetwork network, String[][] attr, CyIdentifiable entry) {
		for (int i = 0; i < attr.length; i++) {
			for (int j = 0; j < attr[i].length; j++) {
				final Class<?> attrType =
					network.getRow(entry).getTable().getColumn(attr[i][j]).getType();

				Double attrValue = new Double(0.0);
				if (attrType == Integer.class)
				{
					Integer x = network.getRow(entry).get(attr[i][j], Integer.class);
					attrValue = x == null ? null : x.doubleValue();
				}
				else if (attrType == Double.class)
				{
					attrValue = network.getRow(entry).get(attr[i][j], Double.class);
				}

				if (attrValue != null && !attrValue.isNaN()) {
					final Double oldMinValue = minAttrValue.get(attr[i][j]);
					final double value = attrValue.doubleValue();
					if (oldMinValue == null || oldMinValue.doubleValue() > value) {
						minAttrValue.put(attr[i][j], attrValue);
					}
					final Double oldMaxValue = maxAttrValue.get(attr[i][j]);
					if (oldMaxValue == null || oldMaxValue.doubleValue() < value) {
						maxAttrValue.put(attr[i][j], attrValue);
					}
					final Double meanValue = meanAttrValue.get(attr[i][j]);
					if (meanValue == null) {
						meanAttrValue.put(attr[i][j], attrValue);
					} else {
						meanAttrValue.put(attr[i][j], new Double(value + meanValue.doubleValue()));
					}
				}
			}
		}
	}

	/**
	 * Computes the minimum, maximum and mean values for each available attribute and saves them in a min, max
	 * and mean hash maps, respectively.
	 */
	private void getMinMaxMeanValues() {
		minAttrValue = new HashMap<String, Double>(32);
		maxAttrValue = new HashMap<String, Double>(32);
		meanAttrValue = new HashMap<String, Double>(32);

		// Find min, max and mean for each node attribute
		// TODO: [Cytoscape 2.8] Check if the returned iterator is parameterized
		for ( CyNode n : network.getNodeList()) {
			getBoundaryValues( network, nodeAttr, n);
		}
		calculateMean(network.getNodeCount(), nodeAttr);
		// Find min, max and mean for each node attribute
		for ( CyEdge e : network.getEdgeList()) {
			getBoundaryValues( network, edgeAttr, e);
		}
		calculateMean(network.getEdgeCount(), edgeAttr);
	}

	/**
	 * Calculates the mean by dividing the sum of all values by the number of values.
	 * 
	 * @param values
	 *            Number of values for a computed attribute.
	 * @param attr
	 *            Array of attributes, whose means are calculated.
	 */
	private void calculateMean(int values, String[][] attr) {
		for (int i = 0; i < attr.length; i++) {
			for (int j = 0; j < attr[i].length; j++) {
				final double meanValue = meanAttrValue.get(attr[i][j]).doubleValue();
				meanAttrValue.put(attr[i][j], new Double(meanValue / values));
			}
		}
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 1514650388927387666L;

	/**
	 * Drop-down list for selecting an edge attribute to be mapped to edge color.
	 */
	private JComboBox cbxEdgeColor;

	/**
	 * JRadioButton specifying a <code>Small values to bright colors</code> attribute mapping of the node
	 * color.
	 */
	private JRadioButton btnEdgeColor1;

	/**
	 * JRadioButton specifying a <code>Small values to bright colors</code> attribute mapping of the node
	 * color.
	 */
	private JRadioButton btnEdgeColor2;

	/**
	 * Drop-down list for selecting an edge attribute to be mapped to edge size.
	 */
	private JComboBox cbxEdgeSize;

	/**
	 * JRadioButton specifying a <code>Small values to small sizes</code> attribute mapping of the edge size.
	 */
	private JRadioButton btnEdgeSize1;

	/**
	 * JRadioButton specifying a <code>Small values to big sizes</code> attribute mapping of the edge size.
	 */
	private JRadioButton btnEdgeSize2;

	/**
	 * Drop-down list for selecting a node attribute to be mapped to node color.
	 */
	private JComboBox cbxNodeColor;

	/**
	 * JRadioButton specifying a <code>Small values to bright colors</code> attribute mapping of the node
	 * color.
	 */
	private JRadioButton btnNodeColor1;

	/**
	 * JRadioButton specifying a <code>Small values to dark colors</code> attribute mapping of the node color.
	 */
	private JRadioButton btnNodeColor2;

	/**
	 * Drop-down list for selecting a node attribute to be mapped to node size.
	 */
	private JComboBox cbxNodeSize;

	/**
	 * JRadioButton specifying a <code>Small values to small sizes</code> attribute mapping of the node size.
	 */
	private JRadioButton btnNodeSize1;

	/**
	 * JRadioButton specifying a <code>Small values to big sizes</code> attribute mapping of the node size.
	 */
	private JRadioButton btnNodeSize2;

	/**
	 * Button for applying the selected attributes to node and/or edge size and/or color.
	 */
	private JButton btnApply;

	/**
	 * Button for closing this dialog.
	 */
	private JButton btnClose;

	/**
	 * Attribute that is mapped to node size.
	 */
	protected String attrNodeSize;

	/**
	 * Type of mapping the node attributes, i.e. low values to small sizes or otherwise.
	 */
	protected String mapNodeSize;

	/**
	 * Attribute that is mapped to node color.
	 */
	protected String attrNodeColor;

	/**
	 * Type of mapping the node attributes, i.e. low values to bright colors or otherwise.
	 */
	protected String mapNodeColor;

	/**
	 * Attribute that is mapped to edge size.
	 */
	protected String attrEdgeSize;

	/**
	 * Type of mapping the edge attributes, i.e. low values to small sizes or otherwise.
	 */
	protected String mapEdgeSize;

	/**
	 * Attribute that is mapped to edge color.
	 */
	protected String attrEdgeColor;

	/**
	 * Type of mapping the edge attributes, i.e. low values to bright colors or otherwise.
	 */
	protected String mapEdgeColor;

	/**
	 * Map with the minimal computed value for each attribute.
	 */
	protected HashMap<String, Double> minAttrValue;

	/**
	 * Map with the maximal computed value for each attribute.
	 */
	protected HashMap<String, Double> maxAttrValue;

	/**
	 * Map with the mean computed value for each attribute.
	 */
	protected HashMap<String, Double> meanAttrValue;
}
