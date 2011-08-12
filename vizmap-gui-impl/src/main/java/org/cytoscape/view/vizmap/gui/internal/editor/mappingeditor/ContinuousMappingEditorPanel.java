/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.Thumb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract class for all Continuous Mapping Editors. This is the mapping from
 * Number to visual property value.
 * 
 * @param T
 *            type of the value associated with the thumb.
 * 
 */
public abstract class ContinuousMappingEditorPanel<K extends Number, V> extends JPanel implements
		PropertyChangeListener {
	private static final long serialVersionUID = 2077889066171872186L;
	
	private static final Logger logger = LoggerFactory.getLogger(ContinuousMappingEditorPanel.class);
	
	protected static final Color BACKGROUND = Color.WHITE;

	protected static final String BELOW_VALUE_CHANGED = "BELOW_VALUE_CHANGED";
	protected static final String ABOVE_VALUE_CHANGED = "ABOVE_VALUE_CHANGED";

	// Only accepts Continuous Mapping
	protected final ContinuousMapping<K, V> mapping;
	protected final VisualProperty<V> type;
	private final CyTable dataTable;
	private final Class<K> dataType;
	
	protected List<ContinuousMappingPoint<K, V>> allPoints;
	private SpinnerNumberModel spinnerModel;

	protected V below;
	protected V above;

	protected double lastSpinnerNumber = 0;

	protected CyNetworkManager cyNetworkManager;

	// This should be injected.
	protected final EditorValueRangeTracer tracer;
	protected final CyApplicationManager appManager;
	
	protected final VisualStyle style;
	
	final JPanel mainPanel;
	
	/**
	 * 
	 * Creates new form ContinuousMapperEditorPanel Accepts only one visual
	 * property type T.
	 * 
	 * */
	public ContinuousMappingEditorPanel(final VisualStyle style, final ContinuousMapping<K, V> mapping, final CyTable attr, final CyApplicationManager appManager, final VisualMappingManager vmm) {
		if(mapping == null)
			throw new NullPointerException("ContinuousMapping should not be null.");
		if(attr == null)
			throw new NullPointerException("Data table should not be null.");
		if(appManager == null)
			throw new NullPointerException("Application Manager should not be null.");
		if(style == null)
			throw new NullPointerException("Visual Style should not be null.");
		
		this.tracer = new EditorValueRangeTracer(vmm);
		this.mapping = mapping;
		this.type = mapping.getVisualProperty();
		this.appManager = appManager;
		this.style = style;
		this.mainPanel = new JPanel();
		
		final String controllingAttrName = mapping.getMappingAttributeName();
		final Class<?> attrType = attr.getColumn(controllingAttrName).getType();
		
		logger.debug("Selected attr type is " + attrType);
		if (!Number.class.isAssignableFrom(attrType))
			throw new IllegalArgumentException("Cannot support attribute data type.  Numerical values only: " + attrType);
		
		this.dataTable = attr;
		this.dataType = (Class<K>) attrType;
		
		initComponents();
		setVisualPropLabel();

		initRangeValues();
		setSpinner();

		// TODO: this should be moved to Editor Manager.
		// this.addWindowListener(new WindowAdapter() {
		// public void windowOpened(WindowEvent e) {
		// System.out.println("windowOpened");
		// firePropertyChange(EditorManager.EDITOR_WINDOW_OPENED, null,
		// type);
		// }
		//
		// public void windowClosing(WindowEvent e) {
		// firePropertyChange(EditorManager.EDITOR_WINDOW_CLOSED, this,
		// type);
		// }
		// });
	}

	protected void setSpinner() {
		spinnerModel = new SpinnerNumberModel(0.0d, Float.NEGATIVE_INFINITY,
				Float.POSITIVE_INFINITY, 0.01d);
		spinnerModel.addChangeListener(new SpinnerChangeListener());
		valueSpinner.setModel(spinnerModel);
	}

	protected void setVisualPropLabel() {
		this.visualPropertyLabel.setText("Visual Property: "
				+ type.getDisplayName());
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
	private void initComponents() {

		mainPanel.setSize(650, 800);
		mainPanel.setMinimumSize(new Dimension(650, 800));
		mainPanel.setPreferredSize(new Dimension(650, 800));
		mainPanel.setBackground(BACKGROUND);
		this.setBackground(BACKGROUND);
		
		abovePanel = new BelowAndAbovePanel(Color.yellow, false, mapping);
		abovePanel.setName("abovePanel");
		belowPanel = new BelowAndAbovePanel(Color.white, true, mapping);
		belowPanel.setName("belowPanel");

		abovePanel.setPreferredSize(new Dimension(16, 1));
		belowPanel.setPreferredSize(new Dimension(16, 1));

		rangeSettingPanel = new javax.swing.JPanel();
		pivotLabel = new javax.swing.JLabel();
		addButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();

		// New in 2.6
		minMaxButton = new javax.swing.JButton();

		colorButton = new javax.swing.JButton();
		rangeEditorPanel = new javax.swing.JPanel();
		slider = new JXMultiThumbSlider<V>();
		attrNameLabel = new javax.swing.JLabel();
		iconPanel = new YValueLegendPanel(type);
		visualPropertyLabel = new javax.swing.JLabel();

		valueSpinner = new JSpinner();

		valueSpinner.setEnabled(false);

		rotaryEncoder = new JXMultiThumbSlider<V>();

		iconPanel.setPreferredSize(new Dimension(25, 1));

		mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
				"Continuous Mapping for " + type.getDisplayName(),
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("SansSerif", Font.BOLD, 12),
				new java.awt.Color(0, 0, 0)));

		rangeSettingPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder(null, "Range Setting",
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION,
						new java.awt.Font("SansSerif", 1, 10),
						new java.awt.Color(0, 0, 0)));
		pivotLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
		pivotLabel.setForeground(java.awt.Color.darkGray);
		pivotLabel.setText("Pivot:");

		addButton.setText("Add");
		addButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addButtonActionPerformed(evt);
			}
		});

		deleteButton.setText("Delete");
		deleteButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		deleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteButtonActionPerformed(evt);
			}
		});

		minMaxButton.setText("Min/Max");
		minMaxButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		minMaxButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				minMaxButtonActionPerformed(evt);
			}
		});

		rangeEditorPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder(null, "Range Editor",
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION,
						new java.awt.Font("SansSerif", 1, 10),
						new java.awt.Color(0, 0, 0)));
		slider.setMaximumValue(100.0F);
		rotaryEncoder.setMaximumValue(100.0F);

		GroupLayout sliderLayout = new GroupLayout(slider);
		slider.setLayout(sliderLayout);
		sliderLayout.setHorizontalGroup(sliderLayout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGap(0, 486, Short.MAX_VALUE));
		sliderLayout.setVerticalGroup(sliderLayout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGap(0, 116, Short.MAX_VALUE));

		attrNameLabel.setFont(new java.awt.Font("SansSerif", 1, 14));
		attrNameLabel.setForeground(java.awt.Color.darkGray);
		attrNameLabel.setText("Attribute Name");

		GroupLayout jXMultiThumbSlider1Layout = new GroupLayout(rotaryEncoder);
		rotaryEncoder.setLayout(jXMultiThumbSlider1Layout);
		jXMultiThumbSlider1Layout.setHorizontalGroup(jXMultiThumbSlider1Layout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0,
						84, Short.MAX_VALUE));
		jXMultiThumbSlider1Layout.setVerticalGroup(jXMultiThumbSlider1Layout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0,
						65, Short.MAX_VALUE));

		visualPropertyLabel.setFont(new java.awt.Font("SansSerif", 1, 14));
		visualPropertyLabel.setForeground(java.awt.Color.darkGray);

		GroupLayout rangeSettingPanelLayout = new GroupLayout(rangeSettingPanel);
		rangeSettingPanel.setLayout(rangeSettingPanelLayout);
		rangeSettingPanelLayout
				.setHorizontalGroup(rangeSettingPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								rangeSettingPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(valueSpinner,
												GroupLayout.PREFERRED_SIZE, 67,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED,
												118, Short.MAX_VALUE)
										.addComponent(minMaxButton,
												GroupLayout.PREFERRED_SIZE, 62,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(addButton,
												GroupLayout.PREFERRED_SIZE, 55,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(deleteButton).addGap(10,
												10, 10)));
		rangeSettingPanelLayout.setVerticalGroup(rangeSettingPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						rangeSettingPanelLayout.createParallelGroup(
								GroupLayout.Alignment.BASELINE).addComponent(
								valueSpinner, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE).addComponent(
								minMaxButton).addComponent(deleteButton)
								.addComponent(addButton,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)));

		GroupLayout layout = new GroupLayout(mainPanel);
		mainPanel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addComponent(rangeSettingPanel,
				GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
				Short.MAX_VALUE).addGroup(
				layout.createSequentialGroup().addComponent(iconPanel,
						GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE).addComponent(belowPanel,
						GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE).addComponent(slider,
						GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
						.addComponent(abovePanel, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)));
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING).addComponent(
								slider, GroupLayout.Alignment.TRAILING,
								GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
								.addComponent(iconPanel,
										GroupLayout.Alignment.TRAILING,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE).addComponent(
										belowPanel,
										GroupLayout.Alignment.TRAILING,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE).addComponent(
										abovePanel,
										GroupLayout.Alignment.TRAILING,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)).addPreferredGap(
						LayoutStyle.ComponentPlacement.RELATED).addComponent(
						rangeSettingPanel, GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));

		// add the main panel to the dialog.
		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
	} // </editor-fold>

	// ///////////////// Action Listeners //////////////////////

	protected void minMaxButtonActionPerformed(ActionEvent evt) {
		final Double[] newVal = MinMaxDialog.getMinMax(tracer.getMin(type),
				tracer.getMax(type), this);

		if (newVal == null)
			return;

		tracer.setMin(type, newVal[0]);
		tracer.setMax(type, newVal[1]);
		updateMap();
		this.repaint();
	}

	abstract protected void deleteButtonActionPerformed(
			java.awt.event.ActionEvent evt);

	abstract protected void addButtonActionPerformed(
			java.awt.event.ActionEvent evt);
	
	// Generate icon from current mapping.
	abstract public ImageIcon drawIcon(int iconWidth, int iconHeight, boolean detail);
	

	private void initRangeValues() {

		// Set range values
		if (tracer.getRange(type) == 0) {
			Double maxValue = Double.NEGATIVE_INFINITY;
			Double minValue = Double.POSITIVE_INFINITY;
			final List<K> valueList = dataTable.getColumn(mapping.getMappingAttributeName()).getValues(this.dataType);
			for (K val : valueList) {
				if (val.doubleValue() > maxValue)
					maxValue = val.doubleValue();

				if (val.doubleValue() < minValue)
					minValue = val.doubleValue();
			}

			tracer.setMax(type, maxValue);
			tracer.setMin(type, minValue);
		}

		allPoints = mapping.getAllPoints();
	}

	protected void setSidePanelIconColor(Color below, Color above) {
		this.abovePanel.setColor(above);
		this.belowPanel.setColor(below);
		repaint();
	}

	// Variables declaration - do not modify
	protected javax.swing.JButton addButton;
	private javax.swing.JLabel attrNameLabel;

	// private javax.swing.JComboBox attributeComboBox;
	protected javax.swing.JButton colorButton;
	protected javax.swing.JButton deleteButton;
	protected javax.swing.JPanel iconPanel;
	private javax.swing.JLabel pivotLabel;
	private javax.swing.JPanel rangeEditorPanel;
	private javax.swing.JPanel rangeSettingPanel;
	protected JXMultiThumbSlider<V> slider;
	protected javax.swing.JSpinner valueSpinner;
	private javax.swing.JLabel visualPropertyLabel;
	protected JXMultiThumbSlider<V> rotaryEncoder;
	protected JButton minMaxButton;

	/*
	 * For Gradient panel only.
	 */
	protected BelowAndAbovePanel abovePanel;
	protected BelowAndAbovePanel belowPanel;

	protected int getSelectedPoint(int selectedIndex) {
		final List<Thumb<V>> thumbs = slider.getModel().getSortedThumbs();
		Thumb<?> selected = slider.getModel().getThumbAt(selectedIndex);
		int i;

		for (i = 0; i < thumbs.size(); i++) {
			if (thumbs.get(i) == selected) {
				return i;
			}
		}

		return -1;
	}

	protected void updateMap() {
		final List<Thumb<V>> thumbs = slider.getModel().getSortedThumbs();

		final double min = tracer.getMin(type);
		final double range = tracer.getRange(type);

		Thumb<?> t;
		Number newVal;

		if (thumbs.size() == 1) {
			// Special case: only one handle.
			V equalVal = thumbs.get(0).getObject();
			V lesserVal = below;
			V greaterVal = above;

			mapping.getPoint(0).setRange( new BoundaryRangeValues<V>(equalVal, lesserVal, greaterVal) );

			newVal = ((thumbs.get(0).getPosition() / 100) * range) + min;
			mapping.getPoint(0).setValue((K) newVal);
			
			// Apply it.
			style.apply(appManager.getCurrentNetworkView());
			return;
		}

		int size = thumbs.size();

		for (int i = 0; i < size; i++) {
			t = thumbs.get(i);

			V lesserVal;
			V equalVal = (V)t.getObject();
			V greaterVal;

			if (i == 0) {
				lesserVal = below;
				greaterVal = (V)t.getObject();
			} else if (i == (thumbs.size() - 1)) {
				greaterVal = above;
				lesserVal = (V)t.getObject();
			} else {
				lesserVal = (V)t.getObject();
				greaterVal = (V)t.getObject();
			}

			mapping.getPoint(i).setRange( new BoundaryRangeValues<V>(lesserVal, equalVal, greaterVal));

			newVal = ((t.getPosition() / 100) * range) + min;
			mapping.getPoint(i).setValue((K) newVal);
		}
		
		// Apply it.
		style.apply(appManager.getCurrentNetworkView());
	}

	// End of variables declaration
	protected class ThumbMouseListener extends MouseAdapter {
		
		public void mouseReleased(MouseEvent e) {
			
			logger.debug("Mouse released from thumb: ");
			
			int selectedIndex = slider.getSelectedIndex();

			if ((0 <= selectedIndex) && (slider.getModel().getThumbCount() > 0)) {
				valueSpinner.setEnabled(true);

				Double newVal = ((slider.getModel().getThumbAt(selectedIndex)
						.getPosition() / 100) * tracer.getRange(type))
						+ tracer.getMin(type);
				valueSpinner.setValue(newVal);

				updateMap();

				slider.repaint();
				repaint();

				final CyNetworkView curView = appManager.getCurrentNetworkView();
				style.apply(curView);
				curView.updateView();
			} else {
				valueSpinner.setEnabled(false);
				valueSpinner.setValue(0);
			}
		}
	}

	
	class SpinnerChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			Number newVal = spinnerModel.getNumber();
			int selectedIndex = slider.getSelectedIndex();

			if ((0 <= selectedIndex) && (slider.getModel().getThumbCount() > 1)) {
				if ((newVal.doubleValue() < tracer.getMin(type))
						|| (newVal.doubleValue() > tracer.getMax(type))) {
					if ((lastSpinnerNumber > tracer.getMin(type))
							&& (lastSpinnerNumber < tracer.getMax(type))) {
						spinnerModel.setValue(lastSpinnerNumber);
					} else {
						spinnerModel.setValue(0);
					}

					return;
				}

				Double newPosition = ((newVal.floatValue() - tracer
						.getMin(type)) / tracer.getRange(type));

				slider.getModel().getThumbAt(selectedIndex).setPosition(
						newPosition.floatValue() * 100);
				slider.getSelectedThumb().setLocation(
						(int) ((slider.getSize().width - 12) * newPosition), 0);

				updateMap();
				// Cytoscape.redrawGraph(vmm.getNetworkView());
				slider.getSelectedThumb().repaint();
				slider.getParent().repaint();
				slider.repaint();

				lastSpinnerNumber = newVal.doubleValue();
			}
		}
	}
}
