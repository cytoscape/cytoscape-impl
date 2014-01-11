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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.NumberConverter;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.VisualPropertyUtil;
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
 */
public abstract class ContinuousMappingEditorPanel<K extends Number, V> extends JPanel {

	private static final long serialVersionUID = 2077889066171872186L;
	private static final Logger logger = LoggerFactory.getLogger(ContinuousMappingEditorPanel.class);

	protected static final Color BACKGROUND = Color.WHITE;
	private static final Dimension BUTTON_SIZE = new Dimension(120, 1);
	private static final Dimension SPINNER_SIZE = new Dimension(100, 25);
	private static final Dimension EDITOR_SIZE = new Dimension(850, 350);

	private static final Font SMALL_FONT = new Font("SansSerif", 1, 10);

	protected static final String BELOW_VALUE_CHANGED = "BELOW_VALUE_CHANGED";
	protected static final String ABOVE_VALUE_CHANGED = "ABOVE_VALUE_CHANGED";

	// Only accepts Continuous Mapping
	protected final ContinuousMapping<K, V> mapping;
	protected final VisualProperty<V> type;
	private final CyTable dataTable;

	private SpinnerNumberModel spinnerModel;

	protected V below;
	protected V above;

	protected Double lastSpinnerNumber = null;

	// This should be injected.
	protected final EditorValueRangeTracer tracer;
	protected final ServicesUtil servicesUtil;

	protected final VisualStyle style;

	final JPanel mainPanel;

	protected final Class<K> columnType;
	protected final Class<V> vpValueType;
	
	private final ContinuousMapping<K, V> original;
	boolean commitChanges;

	/**
	 * Creates new form ContinuousMapperEditorPanel Accepts only one visual
	 * property type T.
	 */
	public ContinuousMappingEditorPanel(final VisualStyle style, final ContinuousMapping<K, V> mapping,
			final CyTable table, final ServicesUtil servicesUtil) {
		if (mapping == null)
			throw new NullPointerException("ContinuousMapping should not be null.");
		if (table == null)
			throw new NullPointerException("Data table should not be null.");
		if (style == null)
			throw new NullPointerException("Visual Style should not be null.");

		this.tracer = new EditorValueRangeTracer(servicesUtil);
		this.mapping = mapping;
		this.type = mapping.getVisualProperty();
		this.style = style;
		this.mainPanel = createMainPanel();
		this.dataTable = table;
		this.servicesUtil = servicesUtil;
		this.original = createCopy(mapping);

		columnType = mapping.getMappingColumnType();
		vpValueType = mapping.getVisualProperty().getRange().getType();

		final String controllingAttrName = mapping.getMappingColumnName();

		// TODO more error checking
		final CyColumn col = table.getColumn(controllingAttrName);

		if (col == null) {
			logger.info("The column \"" + controllingAttrName + "\" does not exist in the \"" + table.getTitle()
					+ "\" table");
		}

		final Class<?> attrType = mapping.getMappingColumnType();
		logger.debug("Selected attr type is " + attrType);

		if (!Number.class.isAssignableFrom(attrType))
			throw new IllegalArgumentException("Cannot support column data type.  Numerical values only: "
					+ attrType);

		initComponents();
		initRangeValues();
		setSpinner();
		slider.addMouseListener(new ThumbMouseListener());
	}

	@SuppressWarnings("serial")
	private JPanel createMainPanel() {
		return new JPanel() {
			@Override
			public void addNotify() {
				super.addNotify();
				
				final JDialog dialog = (JDialog) getRootPane().getParent();
				dialog.addWindowListener(new WindowListener() {
					@Override
					public void windowOpened(WindowEvent event) {
					}
					
					@Override
					public void windowIconified(WindowEvent event) {
					}
					
					@Override
					public void windowDeiconified(WindowEvent event) {
					}
					
					@Override
					public void windowDeactivated(WindowEvent event) {
					}
					
					@Override
					public void windowClosing(WindowEvent event) {
					}
					
					@Override
					public void windowClosed(WindowEvent event) {
						if (!commitChanges) {
							cancelChangesInternal();
						}
					}
					
					@Override
					public void windowActivated(WindowEvent arg0) {
					}
				});
			}
		};
	}

	@SuppressWarnings("unchecked")
	private ContinuousMapping<K, V> createCopy(final ContinuousMapping<K, V> source) {
		final String attribute = source.getMappingColumnName();
		final Class<?> attributeType = source.getMappingColumnType();
		final VisualProperty<?> visualProperty = source.getVisualProperty();
		
		final VisualMappingFunctionFactory continuousMappingFactory = 
				servicesUtil.get(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		final ContinuousMapping<K, V> mapping = (ContinuousMapping<K, V>) continuousMappingFactory
				.createVisualMappingFunction(attribute, attributeType, visualProperty);
		
		for (ContinuousMappingPoint<K, V> point : source.getAllPoints()) {
			BoundaryRangeValues<V> range = new BoundaryRangeValues<V>(point.getRange());
			mapping.addPoint(point.getValue(), range);
		}
		
		return mapping;
	}

	private void setSpinner() {
		spinnerModel = new SpinnerNumberModel(0.0d, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 0.01d);
		spinnerModel.addChangeListener(new SpinnerChangeListener());
		valueSpinner.setModel(spinnerModel);
	}

	protected void reset() {
		initRangeValues();
		updateMap();
		repaint();
	}

	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
	private void initComponents() {
		// Add margin
		this.setOpaque(true);
		this.setBackground(BACKGROUND);
		this.setBorder(BorderFactory.createLineBorder(BACKGROUND, 5));

		this.setSize(EDITOR_SIZE);
		this.setMinimumSize(EDITOR_SIZE);
		this.setPreferredSize(EDITOR_SIZE);

		mainPanel.setBackground(BACKGROUND);

		abovePanel = new BelowAndAbovePanel(Color.yellow, false, mapping, this);
		abovePanel.setName("abovePanel");
		belowPanel = new BelowAndAbovePanel(Color.white, true, mapping, this);
		belowPanel.setName("belowPanel");
		abovePanel.setPreferredSize(new Dimension(20, 1));
		belowPanel.setPreferredSize(new Dimension(20, 1));

		rangeSettingPanel = new javax.swing.JPanel();
		handlePositionSpinnerLabel = new JLabel();
		addButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();

		// New in 2.6
		minMaxButton = new javax.swing.JButton();
		colorButton = new javax.swing.JButton();

		okButton = new JButton();
		cancelButton = new JButton();

		slider = new JXMultiThumbSlider<V>();
		attrNameLabel = new javax.swing.JLabel();
		iconPanel = new YValueLegendPanel(type);
		iconPanel.setBackground(BACKGROUND);

		handlePositionSpinnerLabel.setFont(SMALL_FONT);
		handlePositionSpinnerLabel.setText("Selected Handle Position:");
		valueSpinner = new JSpinner();
		valueSpinner.setPreferredSize(SPINNER_SIZE);
		valueSpinner.setMaximumSize(SPINNER_SIZE);
		valueSpinner.setEnabled(false);

		iconPanel.setPreferredSize(new Dimension(25, 1));

		addButton.setText("Add");
		addButton.setPreferredSize(BUTTON_SIZE);
		addButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addButtonActionPerformed(evt);
			}
		});

		deleteButton.setText("Delete");
		deleteButton.setPreferredSize(BUTTON_SIZE);
		deleteButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		deleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteButtonActionPerformed(evt);
			}
		});

		minMaxButton.setText("Min/Max");
		minMaxButton.setPreferredSize(BUTTON_SIZE);
		minMaxButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		minMaxButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				minMaxButtonActionPerformed(evt);
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				final JDialog dialog = (JDialog) mainPanel.getRootPane().getParent();
				dialog.dispose();
			}
		});

		okButton.setText("OK");
		okButton.setMargin(new Insets(2, 2, 2, 2));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				commitChanges = true;
				final JDialog dialog = (JDialog) mainPanel.getRootPane().getParent();
				dialog.dispose();
			}
		});
		
		// Property value editor components
		final JPanel propValuePanel = new JPanel();
		propValuePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		propValuePanel.setPreferredSize(new Dimension(400, 40));
		propValuePanel.setLayout(new BoxLayout(propValuePanel, BoxLayout.X_AXIS));
		propValuePanel.setBackground(BACKGROUND);
		rangeSettingPanel.setBackground(BACKGROUND);

		final Class<V> vpValueType = type.getRange().getType();
		propertyLabel = new JLabel();
		propertyLabel.setFont(SMALL_FONT);

		if (Number.class.isAssignableFrom(vpValueType)) {
			propertySpinner = new JSpinner();
			propertySpinner.setPreferredSize(SPINNER_SIZE);
			propertySpinner.setMaximumSize(SPINNER_SIZE);
			propertySpinner.setEnabled(false);
			propertyComponent = propertySpinner;
			propertyLabel.setText(type.getDisplayName());
			propertyLabel.setLabelFor(propertyComponent);
		} else if (Paint.class.isAssignableFrom(vpValueType)) {
			// We use the colorButton for both discrete and color
			colorButton = new javax.swing.JButton("Change");
			colorButton.setPreferredSize(BUTTON_SIZE);
			colorButton.setMargin(new Insets(2, 2, 2, 2));
			colorButton.setEnabled(false);
			propertyComponent = colorButton;
			propertyLabel.setText(type.getDisplayName());
			propertyLabel.setLabelFor(propertyComponent);
		} else {
			propertyComponent = new JLabel();
			propertyLabel.setText("Double-click on icon to change " + type.getDisplayName());
		}

		propValuePanel.add(propertyLabel);
		propValuePanel.add(propertyComponent);

		final JPanel editorPanel = new JPanel();
		editorPanel.setLayout(new BorderLayout());
		editorPanel.setBorder(BorderFactory.createTitledBorder(null, "Edit Handle Positions and Values",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, SMALL_FONT, new java.awt.Color(0, 0, 0)));
		editorPanel.setBackground(BACKGROUND);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(BACKGROUND);
		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);
		buttonPanelLayout.setHorizontalGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						buttonPanelLayout.createSequentialGroup().addContainerGap(200, Short.MAX_VALUE)
								.addComponent(cancelButton).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(okButton).addContainerGap()));
		buttonPanelLayout.setVerticalGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						GroupLayout.Alignment.TRAILING,
						buttonPanelLayout
								.createSequentialGroup()
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(
										buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
												.addComponent(okButton).addComponent(cancelButton)).addContainerGap()));

		slider.setMaximumValue(100.0F);

		GroupLayout sliderLayout = new GroupLayout(slider);
		slider.setLayout(sliderLayout);
		sliderLayout.setHorizontalGroup(sliderLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 486,
				Short.MAX_VALUE));
		sliderLayout.setVerticalGroup(sliderLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 116,
				Short.MAX_VALUE));

		attrNameLabel.setFont(new java.awt.Font("SansSerif", 1, 14));
		attrNameLabel.setForeground(java.awt.Color.darkGray);
		attrNameLabel.setText("Column Name");

		GroupLayout rangeSettingPanelLayout = new GroupLayout(rangeSettingPanel);
		rangeSettingPanel.setLayout(rangeSettingPanelLayout);
		rangeSettingPanelLayout.setHorizontalGroup(rangeSettingPanelLayout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				rangeSettingPanelLayout.createSequentialGroup().addContainerGap()
						.addComponent(handlePositionSpinnerLabel)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(valueSpinner)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
						.addComponent(minMaxButton).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(addButton).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(deleteButton).addGap(10, 10, 10)));
		rangeSettingPanelLayout.setVerticalGroup(rangeSettingPanelLayout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				rangeSettingPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(handlePositionSpinnerLabel).addComponent(valueSpinner).addComponent(minMaxButton)
						.addComponent(deleteButton).addComponent(addButton)));

		editorPanel.add(rangeSettingPanel, BorderLayout.CENTER);
		editorPanel.add(propValuePanel, BorderLayout.SOUTH);

		GroupLayout layout = new GroupLayout(mainPanel);
		mainPanel.setLayout(layout);

		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(editorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(iconPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(belowPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(slider, GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
								.addComponent(abovePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addGroup(
								layout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(slider, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
												145, Short.MAX_VALUE)
										.addComponent(iconPanel, GroupLayout.Alignment.TRAILING,
												GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(belowPanel, GroupLayout.Alignment.TRAILING,
												GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(abovePanel, GroupLayout.Alignment.TRAILING,
												GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(editorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)));

		// add the main panel to the dialog.
		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
	} // </editor-fold>

	// ///////////////// Action Listeners //////////////////////

	protected void minMaxButtonActionPerformed(ActionEvent evt) {
		final Double[] newVal = MinMaxDialog.getMinMax(tracer.getMin(type), tracer.getMax(type), this);

		if (newVal == null)
			return;

		tracer.setMin(type, newVal[0]);
		tracer.setMax(type, newVal[1]);
		updateMap();
		this.repaint();
	}

	abstract protected void deleteButtonActionPerformed(java.awt.event.ActionEvent evt);

	abstract protected void addButtonActionPerformed(java.awt.event.ActionEvent evt);

	// Generate icon from current mapping.
	abstract public ImageIcon drawIcon(int iconWidth, int iconHeight, boolean detail);

	private void initRangeValues() {
		// Set range values
		if (tracer.getRange(type) == 0) {
			final CyColumn col = dataTable.getColumn(mapping.getMappingColumnName());

			if (col != null) {
				// If the current mapping already have points, start with the actual mapping's min/max values
				Double max = VisualPropertyUtil.getMaxValue(mapping);
				Double min = VisualPropertyUtil.getMinValue(mapping);
				max = max == null ? Double.NEGATIVE_INFINITY : max;
				min = min == null ? Double.POSITIVE_INFINITY : min;
				
				final List<?> valueList = col.getValues(col.getType());

				for (Object o : valueList) {
					if (o instanceof Number) {
						Number val = (Number) o;

						if (val.doubleValue() > max)
							max = val.doubleValue();

						if (val.doubleValue() < min)
							min = val.doubleValue();
					}

				}

				tracer.setMax(type, max);
				tracer.setMin(type, min);
			}
		}
	}

	protected void setSidePanelIconColor(Color below, Color above) {
		this.abovePanel.setColor(above);
		this.belowPanel.setColor(below);
		repaint();
	}

	// Variables declaration - do not modify

	private JLabel attrNameLabel;
	private JLabel handlePositionSpinnerLabel;

	protected JButton addButton;
	protected JButton colorButton;
	protected JButton deleteButton;
	protected JButton minMaxButton;

	protected JButton cancelButton;
	protected JButton okButton;

	protected javax.swing.JPanel iconPanel;
	private javax.swing.JPanel rangeSettingPanel;
	protected JXMultiThumbSlider<V> slider;
	protected JSpinner valueSpinner;

	protected JSpinner propertySpinner;
	private JLabel propertyLabel;
	private JComponent propertyComponent;

	/*
	 * For Gradient panel only.
	 */
	protected BelowAndAbovePanel abovePanel;
	protected BelowAndAbovePanel belowPanel;

	protected int getSelectedPoint(int selectedIndex) {
		final List<Thumb<V>> thumbs = slider.getModel().getSortedThumbs();
		final Thumb<V> selected = slider.getModel().getThumbAt(selectedIndex);

		for (int i = 0; i < thumbs.size(); i++) {
			if (thumbs.get(i) == selected)
				return i;
		}

		return -1;
	}

	
	
	protected void updateMap() {
		final List<Thumb<V>> thumbs = slider.getModel().getSortedThumbs();
		final double min = tracer.getMin(type);
		final double range = tracer.getRange(type);

		// There is only one point.
		if (thumbs.size() == 1) {
			updateOnePoint(thumbs, min, range);
			return;
		}

		// There are two or more points.
		final int size = thumbs.size();
		final int mappingPointCount = mapping.getPointCount();
		
		// This should not happen!
		if(size != mappingPointCount)
			throw new IllegalStateException("Number of handles is not equal to mapping points.");

		int i = 0;
		for (final Thumb<V> handle:thumbs) {
			final ContinuousMappingPoint<K, V> point = mapping.getPoint(i);
			final Number handlePosition = ((handle.getPosition() / 100) * range) + min;

			// Debug
//			System.out.print("@@@@@@@ Index = " + i);
//			System.out.print(", handle position = " + handlePosition);
//			System.out.println(", handle Value = " + handle.getObject());

			V lesserVal;
			V equalVal = handle.getObject();
			V greaterVal;

			if (i == 0) {
				// First handle.  Use Below for lesser.
				lesserVal = below;
				greaterVal = handle.getObject();
			} else if (i == (size - 1)) {
				// Last handle.  Use above.
				greaterVal = above;
				lesserVal = handle.getObject();
			} else {
				lesserVal = handle.getObject();
				greaterVal = handle.getObject();
			}

			final BoundaryRangeValues<V> newRange;
			if (equalVal instanceof Number) {
				newRange = new BoundaryRangeValues<V>(NumberConverter.convert(vpValueType, (Number) lesserVal),
						NumberConverter.convert(vpValueType, (Number) equalVal), NumberConverter.convert(vpValueType,
								(Number) greaterVal));

			} else {
				newRange = new BoundaryRangeValues<V>(lesserVal, equalVal, greaterVal);
			}

			point.setRange(newRange);
			point.setValue((K) handlePosition);
			
			i++;
		}
	}
	
	private void updateOnePoint(final List<Thumb<V>> thumbs, final double min,
			final double range) {
		// Special case: only one handle.
		V equalVal = thumbs.get(0).getObject();
		V lesserVal = below;
		V greaterVal = above;

		if (equalVal instanceof Number)
			mapping.getPoint(0).setRange(
					new BoundaryRangeValues<V>(NumberConverter.convert(vpValueType, (Number) lesserVal), NumberConverter
							.convert(vpValueType, (Number) equalVal), NumberConverter.convert(vpValueType,
							(Number) greaterVal)));
		else {
			mapping.getPoint(0).setRange(new BoundaryRangeValues<V>(lesserVal, equalVal, greaterVal));
		}
		Number newVal = ((thumbs.get(0).getPosition() / 100) * range) + min;
		mapping.getPoint(0).setValue((K) newVal);
	}

	private final void enableValueEditor(final V newObject) {
		if (Number.class.isAssignableFrom(vpValueType)) {
			propertySpinner.setEnabled(true);
			propertySpinner.setValue(newObject);
		} else if (Paint.class.isAssignableFrom(vpValueType)) {
			colorButton.setEnabled(true);
			setButtonColor((Color) newObject);
		}
	}

	protected void setButtonColor(final Color newColor) {
		final int iconWidth = 20;
		final int iconHeight = 10;
		final BufferedImage bi = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2 = bi.createGraphics();

		g2.setColor(newColor);
		g2.fillRect(0, 0, iconWidth, iconHeight);

		Icon colorIcon = new ImageIcon(bi);
		colorButton.setIcon(colorIcon);
		colorButton.setIconTextGap(10);
	}

	void cancelChangesInternal() {
		while (mapping.getPointCount() > 0) {
			mapping.removePoint(0);
		}
		
		for (ContinuousMappingPoint<K, V> point : original.getAllPoints()) {
			mapping.addPoint(point.getValue(), point.getRange());
		}
		cancelChanges();
		slider.repaint();
	}
	
	protected abstract void cancelChanges();

	// End of variables declaration
	protected class ThumbMouseListener extends MouseAdapter {
		
		/**
		 * Updates GUI when user moves & releases the handle.
		 */
		public void mouseReleased(MouseEvent e) {
			final int selectedIndex = slider.getSelectedIndex();
			final int tCount = slider.getModel().getThumbCount();
			
			if ((0 <= selectedIndex) && (tCount> 0)) {
				final Thumb<V> handle = slider.getModel().getThumbAt(selectedIndex);
				final Double handlePosition = ((handle.getPosition() / 100) * tracer.getRange(type)) + tracer.getMin(type);

				updateMap();
				slider.repaint();
				repaint();
				
				// Updates spinner values
				spinnerModel = new SpinnerNumberModel(handlePosition.doubleValue(), Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 0.01d);
				spinnerModel.addChangeListener(new SpinnerChangeListener());
				valueSpinner.setModel(spinnerModel);
				valueSpinner.setEnabled(true);

				V object = handle.getObject();
				enableValueEditor(object);
				
			} else {
				valueSpinner.setEnabled(false);
				valueSpinner.setValue(0);
			}
		}
	}

	private final class SpinnerChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			final Number newVal = spinnerModel.getNumber();
			if( lastSpinnerNumber == null )
			{
				lastSpinnerNumber = newVal.doubleValue();
			}
			else if ( newVal.equals(lastSpinnerNumber) )
			{
				return;
			}
		
			int selectedIndex = slider.getSelectedIndex();

			if ((0 <= selectedIndex) && (slider.getModel().getThumbCount() >= 1)) {
				if ((newVal.doubleValue() < tracer.getMin(type)) || (newVal.doubleValue() > tracer.getMax(type))) {
					if ((lastSpinnerNumber > tracer.getMin(type)) && (lastSpinnerNumber < tracer.getMax(type))) {
						spinnerModel.setValue(lastSpinnerNumber);
					} else {
						spinnerModel.setValue(0);
					}
					return;
				}

				final Double newPosition = ((newVal.floatValue() - tracer.getMin(type)) / tracer.getRange(type));

				slider.getModel().getThumbAt(selectedIndex).setPosition(newPosition.floatValue() * 100);
				slider.getSelectedThumb().setLocation((int) ((slider.getSize().width - 12) * newPosition), 0);

				updateMap();
				slider.getSelectedThumb().repaint();
				slider.getParent().repaint();
				slider.repaint();
				lastSpinnerNumber = newVal.doubleValue();
			}
		}
	}
}