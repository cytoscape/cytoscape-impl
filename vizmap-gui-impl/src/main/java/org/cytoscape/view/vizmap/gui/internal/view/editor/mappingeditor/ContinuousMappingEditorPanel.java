package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.ref.WeakReference;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.util.color.PaletteType;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
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

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

/**
 * Abstract class for all Continuous Mapping Editors. This is the mapping from
 * Number to visual property value.
 */
@SuppressWarnings("serial")
public abstract class ContinuousMappingEditorPanel<K extends Number, V> extends JPanel {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	private static final Dimension SPINNER_SIZE = new Dimension(100, 25);

	protected static final String BELOW_VALUE_CHANGED = "BELOW_VALUE_CHANGED";
	protected static final String ABOVE_VALUE_CHANGED = "ABOVE_VALUE_CHANGED";

	private JLabel attrNameLabel;
	private JLabel handlePositionLabel;

	private JButton addButton;
	private JButton colorButton;
	private JButton deleteButton;
	private JButton minMaxButton;

	private JButton cancelButton;
	private JButton okButton;

	private JPanel mainPanel;
	private JPanel editorPanel;
	private JPanel formPanel;
	private JPanel iconPanel;
	
	/*
	 * For Gradient panel only.
	 */
	private JPanel palettesPanel;
	private Palette lastPalette;
	protected Palette currentPalette;
	protected PaletteType paletteType;
	private JButton paletteButton;
	private BelowAndAbovePanel abovePanel;
	private BelowAndAbovePanel belowPanel;
	
	private JXMultiThumbSlider<V> slider;
	private JSpinner valueSpinner;

	private JSpinner propertySpinner;
	private JLabel propertyLabel;
	private JComponent valueEditor;

	// Only accepts Continuous Mapping
	protected final ContinuousMapping<K, V> mapping;
	protected final VisualProperty<V> type;
	private final WeakReference<CyTable> dataTable;
	protected final EditorManager editorManager;

	private SpinnerNumberModel spinnerModel;

	protected V below;
	protected V above;

	protected boolean userEdited = false;

	protected Double lastSpinnerNumber;

	// This should be injected.
	protected static EditorValueRangeTracer tracer;
	protected final ServicesUtil servicesUtil;

	protected final VisualStyle style;

	protected final Class<K> columnType;
	protected final Class<V> vpValueType;

	protected PaletteProviderManager paletteProviderMgr;
	
	private final ContinuousMapping<K, V> original;
	boolean commitChanges;

	/**
	 * Creates new form ContinuousMapperEditorPanel Accepts only one visual property type T.
	 * 
	 * @param editorManager may be null.
	 */
	public ContinuousMappingEditorPanel(
			VisualStyle style,
			ContinuousMapping<K, V> mapping,
			CyTable table,
			EditorManager editorManager,
			ServicesUtil servicesUtil
	) {
		if (style == null)
			throw new IllegalArgumentException("'style' should not be null.");
		if (mapping == null)
			throw new IllegalArgumentException("'mapping' should not be null.");
		if (table == null)
			throw new IllegalArgumentException("'table' should not be null.");
		
		this.mapping = mapping;
		this.type = mapping.getVisualProperty();
		this.style = style;
		this.dataTable = new WeakReference<>(table);
		this.editorManager = editorManager;
		this.servicesUtil = servicesUtil;
		this.original = createCopy(mapping);

		columnType = mapping.getMappingColumnType();
		vpValueType = mapping.getVisualProperty().getRange().getType();

		final String controllingAttrName = mapping.getMappingColumnName();

		paletteProviderMgr = servicesUtil.get(PaletteProviderManager.class);

		// TODO more error checking
		final CyColumn col = table.getColumn(controllingAttrName);

		if (col == null)
			logger.info("The column \"" + controllingAttrName + "\" does not exist in the \"" + table.getTitle()
					+ "\" table");

		final Class<?> attrType = mapping.getMappingColumnType();

		if (!Number.class.isAssignableFrom(attrType))
			throw new IllegalArgumentException("Cannot support column data type.  Numerical values only: "
					+ attrType);

		if (tracer == null)
			tracer = new EditorValueRangeTracer(servicesUtil);

		if (mapping.getPointCount() > 0)
			userEdited = true;

		initRangeValues();
		initComponents();
		setSpinner();
		getSlider().addMouseListener(new MouseAdapter() {
			/**
			 * Updates GUI when user moves & releases the handle.
			 */
			@Override
			public void mouseReleased(MouseEvent evt) {
				final int selectedIndex = getSlider().getSelectedIndex();
				final int tCount = getSlider().getModel().getThumbCount();
				
				if (selectedIndex >= 0 && tCount > selectedIndex) {
					final Thumb<V> handle = getSlider().getModel().getThumbAt(selectedIndex);
					final Double handlePosition = ((handle.getPosition() / 100) * tracer.getRange(type)) + tracer.getMin(type);

					updateMap();
					
					// Updates spinner values
					spinnerModel = new SpinnerNumberModel(handlePosition.doubleValue(), Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 0.01d);
					spinnerModel.addChangeListener(new SpinnerChangeListener());
					getValueSpinner().setModel(spinnerModel);
				}
				
				update();
			}
		});
	}

	public static void setTracer(EditorValueRangeTracer t) {
		tracer = t;
	}

	public static void resetTracer(VisualProperty<?> vp) {
		if (tracer == null) return;
		tracer.setMin(vp, 0.0);
		tracer.setMax(vp, 0.0);
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
			BoundaryRangeValues<V> range = new BoundaryRangeValues<>(point.getRange());
			mapping.addPoint(point.getValue(), range);
		}
		
		return mapping;
	}

	private void setSpinner() {
		spinnerModel = new SpinnerNumberModel(0.0d, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 0.01d);
		spinnerModel.addChangeListener(new SpinnerChangeListener());
		getValueSpinner().setModel(spinnerModel);
	}

	protected void reset() {
		initRangeValues();
		updateMap();
		userEdited = false;
		repaint();
	}

	private void initComponents() {
		attrNameLabel = new JLabel("Column Name");
		attrNameLabel.setFont(attrNameLabel.getFont().deriveFont(Font.BOLD, 14));
		
		handlePositionLabel = new JLabel("Handle Position:");
		
		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(getOkButton(), getCancelButton());
		
		// add the main panel to the dialog.
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(getPalettesPanel())
				.addComponent(getMainPanel())
				.addComponent(buttonPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getPalettesPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getMainPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		update();
	}
	
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel() {
				@Override
				public void addNotify() {
					super.addNotify();
					
					final JDialog dialog = (JDialog) getRootPane().getParent();
					dialog.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent event) {
							if (!commitChanges)
								cancelChangesInternal();
						}
					});
				}
			};
			
			final GroupLayout layout = new GroupLayout(mainPanel);
			mainPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);

			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(getEditorPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getFormPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getEditorPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(20)
					.addComponent(getFormPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return mainPanel;
	}

	/**
	 * Create a list of diverging palettes to choose from
	 */
	protected JPanel getPalettesPanel() {
		if (palettesPanel == null) {
			palettesPanel = new JPanel();
			JLabel label = new JLabel("Current Palette:");
			// paletteBox = new JComboBox<>(getPalettes().toArray(new Palette[1]));
			var layout = new GroupLayout(palettesPanel);
			palettesPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(label, DEFAULT_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getPaletteButton(), DEFAULT_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);

			layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(label, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getPaletteButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		return palettesPanel;
	}

	protected JButton getPaletteButton() {
		if (paletteButton == null) {
			paletteButton = new JButton("None");

			if (tracer.getMax(type) > 0.0 && tracer.getMin(type) < 0.0) {
				paletteType = BrewerType.DIVERGING;
			} else {
				paletteType = BrewerType.SEQUENTIAL;
			}

			lastPalette = retrievePalette();
			if (lastPalette != null) {
				setCurrentPalette(lastPalette);
			}
		}
		return paletteButton;
	}

	protected void savePalette(Palette palette) {
		paletteProviderMgr.savePalette(style.getTitle()+" "+type.getIdString(), palette);
	}

	protected Palette retrievePalette() {
		return paletteProviderMgr.retrievePalette(style.getTitle()+" "+type.getIdString());
	}

	protected void setCurrentPalette(Palette palette) {
		paletteButton.setText(palette.toString());
		currentPalette = palette;
		return;
	}
	
	private JPanel getEditorPanel() {
		if (editorPanel == null) {
			editorPanel = new JPanel();
			editorPanel.setMinimumSize(new Dimension(280, 240));
			
			var layout = new GroupLayout(editorPanel);
			editorPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(getIconPanel(), 52, 52, PREFERRED_SIZE)
					.addComponent(getBelowPanel(), 26, 26, PREFERRED_SIZE)
					.addGap(2)
					.addComponent(getSlider(), 120, 280, Short.MAX_VALUE)
					.addGap(2)
					.addComponent(getAbovePanel(), 26, 26, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.TRAILING)
					.addComponent(getIconPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getBelowPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getSlider(), 120, 180, Short.MAX_VALUE)
					.addComponent(getAbovePanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		return editorPanel;
	}
	
	private JPanel getFormPanel() {
		if (formPanel == null) {
			formPanel = new JPanel();
			formPanel.setBorder(LookAndFeelUtil.createTitledBorder("Edit Handle Positions and Values"));
			
			var infoLabel = new JLabel("Double-click on icon to change " + type.getDisplayName());
			infoLabel.setFont(infoLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			
			if (Number.class.isAssignableFrom(vpValueType) || Paint.class.isAssignableFrom(vpValueType))
				infoLabel.setVisible(false);
			
			var layout = new GroupLayout(formPanel);
			formPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
									.addComponent(handlePositionLabel)
									.addComponent(getPropertyLabel())
							)
							.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
									.addGroup(layout.createSequentialGroup()
											.addComponent(getValueSpinner())
											.addGap(10, 20, Short.MAX_VALUE)
											.addComponent(getMinMaxButton())
											.addGap(10, 20, Short.MAX_VALUE)
											.addComponent(getAddButton())
											.addComponent(getDeleteButton())
									)
									.addComponent(getValueEditor())
							)
					)
					.addComponent(infoLabel)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(handlePositionLabel)
							.addComponent(getValueSpinner(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getMinMaxButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getDeleteButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getAddButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(getPropertyLabel())
							.addComponent(getValueEditor(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(infoLabel)
			);
			
			equalizeSize(getAddButton(), getDeleteButton());
		}
		
		return formPanel;
	}
	
	protected JPanel getIconPanel() {
		if (iconPanel == null) {
			iconPanel = new YValueLegendPanel(type);
		}
		
		return iconPanel;
	}
	
	protected JXMultiThumbSlider<V> getSlider() {
		if (slider == null) {
			slider = new JXMultiThumbSlider<>();
			slider.setMaximumValue(100.0F);
		}
		
		return slider;
	}
	
	protected BelowAndAbovePanel getBelowPanel() {
		if (belowPanel == null) {
			belowPanel = new BelowAndAbovePanel(Color.WHITE, true, mapping, this);
			belowPanel.setName("belowPanel");
		}
		
		return belowPanel;
	}
	
	protected BelowAndAbovePanel getAbovePanel() {
		if (abovePanel == null) {
			abovePanel = new BelowAndAbovePanel(Color.YELLOW, false, mapping, this);
			abovePanel.setName("abovePanel");
		}
		
		return abovePanel;
	}
	
	private JLabel getPropertyLabel() {
		if (propertyLabel == null) {
			propertyLabel = new JLabel();
			
			if (Number.class.isAssignableFrom(vpValueType) || Paint.class.isAssignableFrom(vpValueType)) {
				propertyLabel.setText(type.getDisplayName() + ":");
				propertyLabel.setLabelFor(getValueEditor());
			} else {
				propertyLabel.setVisible(false);
			}
		}
		
		return propertyLabel;
	}
	
	private JComponent getValueEditor() {
		if (valueEditor == null) {
			if (Number.class.isAssignableFrom(vpValueType)) {
				valueEditor = getPropertySpinner();
			} else if (Paint.class.isAssignableFrom(vpValueType)) {
				// We use the colorButton for both discrete and color
				valueEditor = getColorButton();
			} else {
				valueEditor = new JLabel();
				valueEditor.setVisible(false);
			}
		}

		return valueEditor;
	}
	
	protected JSpinner getPropertySpinner() {
		if (propertySpinner == null) {
			propertySpinner = new JSpinner();
			propertySpinner.setPreferredSize(SPINNER_SIZE);
			propertySpinner.setMaximumSize(SPINNER_SIZE);
			propertySpinner.setEnabled(false);
		}
		
		return propertySpinner;
	}
	
	protected JButton getColorButton() {
		if (colorButton == null) {
			colorButton = new JButton();
			
			if (LookAndFeelUtil.isAquaLAF())
				colorButton.putClientProperty("JButton.buttonType", "gradient");
			
			colorButton.setHorizontalTextPosition(JButton.CENTER);
			colorButton.setVerticalTextPosition(JButton.CENTER);
			colorButton.setEnabled(false);
			setButtonColor(new Color(0, 0, 0, 0)); // Transparent
		}
		
		return colorButton;
	}
	
	protected JButton getAddButton() {
		if (addButton == null) {
			addButton = new JButton("Add");
			addButton.addActionListener(evt -> addButtonActionPerformed(evt));
		}
		
		return addButton;
	}
	
	private JButton getDeleteButton() {
		if (deleteButton == null) {
			deleteButton = new JButton("Delete");
			deleteButton.addActionListener(evt -> deleteButtonActionPerformed(evt));
		}
		
		return deleteButton;
	}
	
	private JButton getMinMaxButton() {
		if (minMaxButton == null) {
			minMaxButton = new JButton("Set Min and Max...");
			minMaxButton.addActionListener(evt -> minMaxButtonActionPerformed(evt));
		}
		
		return minMaxButton;
	}
	
	protected JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(new AbstractAction("Cancel") {
				@Override
				public void actionPerformed(ActionEvent e) {
					final JDialog dialog = (JDialog) getMainPanel().getRootPane().getParent();
					dialog.dispose();
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
					commitChanges = true;
					final JDialog dialog = (JDialog) getMainPanel().getRootPane().getParent();
					dialog.dispose();
				}
			});
		}
		
		return okButton;
	}
	
	private JSpinner getValueSpinner() {
		if (valueSpinner == null) {
			valueSpinner = new JSpinner();
			valueSpinner.setPreferredSize(SPINNER_SIZE);
			valueSpinner.setMaximumSize(SPINNER_SIZE);
		}
		
		return valueSpinner;
	}

	// ///////////////// Action Listeners //////////////////////

	protected void minMaxButtonActionPerformed(ActionEvent evt) {
		CyTable table = dataTable.get();
		if(table != null) {
			CyColumn col = table.getColumn(mapping.getMappingColumnName());
			
			// Get new column's min/max values
			double minTgtVal = Double.POSITIVE_INFINITY;
			double maxTgtVal = Double.NEGATIVE_INFINITY;
			final List<?> valueList = col.getValues(col.getType());
	
			for (final Object o : valueList) {
				if (o instanceof Number) {
					double val = ((Number) o).doubleValue();
					
					if (!Double.isNaN(val)) {
						maxTgtVal = Math.max(maxTgtVal, val);
						minTgtVal = Math.min(minTgtVal, val);
					}
				}
			}
			
			
			final JDialog dialog = (JDialog) getMainPanel().getRootPane().getParent();
			
			final Double[] newVal = MinMaxDialog.getMinMax(tracer.getMin(type), tracer.getMax(type), minTgtVal, maxTgtVal, dialog);
	
			if (newVal == null)
				return;
	
			tracer.setMin(type, newVal[0]);
			tracer.setMax(type, newVal[1]);
			updateMap();
			this.repaint();
		}
	}

	abstract protected void deleteButtonActionPerformed(ActionEvent evt);

	abstract protected void addButtonActionPerformed(ActionEvent evt);

	// Generate icon from current mapping.
	abstract public ImageIcon drawIcon(int iconWidth, int iconHeight, boolean detail);

	private void initRangeValues() {
		// Set range values
		if (tracer.getRange(type) == 0) {
			CyTable table = dataTable.get();
			if(table != null) {
				final CyColumn col = table.getColumn(mapping.getMappingColumnName());
				if (col != null) {
					// If the current mapping already have points, start with the actual mapping's min/max values
					Double max = VisualPropertyUtil.getMaxValue(mapping);
					Double min = VisualPropertyUtil.getMinValue(mapping);
					if (max == null || min == null) {
						max = max == null ? Double.NEGATIVE_INFINITY : max;
						min = min == null ? Double.POSITIVE_INFINITY : min;
					
						final List<?> valueList = col.getValues(col.getType());
	
						boolean hasValue = false;
						for (Object o : valueList) {
							if (o instanceof Number) {
								hasValue = true;
								Number val = (Number) o;
		
								if (val.doubleValue() > max)
									max = val.doubleValue();
		
								if (val.doubleValue() < min)
									min = val.doubleValue();
							}
	
						}
						
						if(hasValue) {
							tracer.setMax(type, max);
							tracer.setMin(type, min);
						} else {
							tracer.setMin(type, 0.0);
							tracer.setMax(type, 1.0);
						}
					} else {
						tracer.setMax(type, max);
						tracer.setMin(type, min);
					}
				}
			}
		}
		
		if(tracer.getRange(type) == 0) { // if the range is still 0
			tracer.setMax(type, tracer.getMax(type) + 1.0);
		}
	}

	protected void setSidePanelIconColor(Color below, Color above) {
		this.getAbovePanel().setColor(above);
		this.getBelowPanel().setColor(below);
		repaint();
	}

	protected int getSelectedPoint(int selectedIndex) {
		final List<Thumb<V>> thumbs = getSlider().getModel().getSortedThumbs();
		final Thumb<V> selected = getSlider().getModel().getThumbAt(selectedIndex);

		for (int i = 0; i < thumbs.size(); i++) {
			if (thumbs.get(i) == selected)
				return i;
		}

		return -1;
	}
	
	@SuppressWarnings("unchecked")
	protected void updateMap() {
		final List<Thumb<V>> thumbs = getSlider().getModel().getSortedThumbs();
		final double min = tracer.getMin(type);
		final double range = tracer.getRange(type);

		userEdited = true;

		// There is only one point.
		if (thumbs.size() == 1) {
			updateOnePoint(thumbs, min, range);
			return;
		}

		// There are two or more points.
		final int size = thumbs.size();
		final int mappingPointCount = mapping.getPointCount();
		
		// This should not happen!
		if (size != mappingPointCount)
			throw new IllegalStateException(
					"Number of handles (" + size + ") is not equal to mapping points (" + mappingPointCount + ").");

		int i = 0;
		
		for (final Thumb<V> handle:thumbs) {
			final ContinuousMappingPoint<K, V> point = mapping.getPoint(i);
			final Number handlePosition = ((handle.getPosition() / 100) * range) + min;
			
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
				newRange = new BoundaryRangeValues<>(NumberConverter.convert(vpValueType, (Number) lesserVal),
						NumberConverter.convert(vpValueType, (Number) equalVal), NumberConverter.convert(vpValueType,
								(Number) greaterVal));

			} else {
				newRange = new BoundaryRangeValues<>(lesserVal, equalVal, greaterVal);
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
					new BoundaryRangeValues<>(NumberConverter.convert(vpValueType, (Number) lesserVal), NumberConverter
							.convert(vpValueType, (Number) equalVal), NumberConverter.convert(vpValueType,
							(Number) greaterVal)));
		else {
			mapping.getPoint(0).setRange(new BoundaryRangeValues<>(lesserVal, equalVal, greaterVal));
		}
		Number newVal = ((thumbs.get(0).getPosition() / 100) * range) + min;
		mapping.getPoint(0).setValue((K) newVal);
	}

	protected void setButtonColor(final Color newColor) {
		getColorButton().setIcon(new ColorIcon(newColor));
	}

	void cancelChangesInternal() {
		while (mapping.getPointCount() > 0) {
			mapping.removePoint(0);
		}
		
		for (ContinuousMappingPoint<K, V> point : original.getAllPoints()) {
			mapping.addPoint(point.getValue(), point.getRange());
		}

		if (lastPalette != null)
			savePalette(lastPalette);
		
		cancelChanges();
		getSlider().repaint();
	}
	
	protected abstract void cancelChanges();
	
	protected void update() {
		final int selectedIndex = getSlider().getSelectedIndex();
		final int count = getSlider().getModel().getThumbCount();
		
		if (selectedIndex >= 0 && count > selectedIndex) {
			var editorType = editorManager != null
					? editorManager.getVisualPropertyEditor(type).getContinuousEditorType()
					: null;
			
			// C2D requires at least 1 handle, and the other types require at least 2
			getDeleteButton().setEnabled(
					(editorType == ContinuousEditorType.DISCRETE && count > 1) || 
					(editorType != ContinuousEditorType.DISCRETE && count > 2)
			);
			getValueSpinner().setEnabled(true);
			getValueEditor().setEnabled(true);

			final Thumb<V> handle = getSlider().getModel().getThumbAt(selectedIndex);
			V value = handle.getObject();
			
			if (Number.class.isAssignableFrom(vpValueType))
				getPropertySpinner().setValue(value);
			else if (Paint.class.isAssignableFrom(vpValueType))
				setButtonColor((Color) value);
		} else {
			getDeleteButton().setEnabled(false);
			getValueSpinner().setEnabled(false);
			getValueEditor().setEnabled(false);
			
			getValueSpinner().setValue(0);
			
			if (Number.class.isAssignableFrom(vpValueType))
				getPropertySpinner().setValue(0);
			else if (Paint.class.isAssignableFrom(vpValueType))
				setButtonColor(null);
		}
		
		getSlider().repaint();
		repaint();
	}

	private final class SpinnerChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			final Number newVal = spinnerModel.getNumber();
			
			if (lastSpinnerNumber == null)
				lastSpinnerNumber = newVal.doubleValue();
			else if (newVal.equals(lastSpinnerNumber))
				return;
		
			int selectedIndex = getSlider().getSelectedIndex();

			if ((0 <= selectedIndex) && (getSlider().getModel().getThumbCount() >= 1)) {
				if ((newVal.doubleValue() < tracer.getMin(type)) || (newVal.doubleValue() > tracer.getMax(type))) {
					if ((lastSpinnerNumber > tracer.getMin(type)) && (lastSpinnerNumber < tracer.getMax(type)))
						spinnerModel.setValue(lastSpinnerNumber);
					else
						spinnerModel.setValue(0);
					
					return;
				}

				final Double newPosition = ((newVal.floatValue() - tracer.getMin(type)) / tracer.getRange(type));

				getSlider().getModel().getThumbAt(selectedIndex).setPosition(newPosition.floatValue() * 100);
				getSlider().getSelectedThumb().setLocation((int) ((getSlider().getSize().width - 12) * newPosition), 0);

				updateMap();
				getSlider().getSelectedThumb().repaint();
				getSlider().getParent().repaint();
				getSlider().repaint();
				lastSpinnerNumber = newVal.doubleValue();
			}
		}
	}

	private class ColorIcon implements Icon {

		private final Color color;
		private final Color borderColor;
		
		ColorIcon(final Color c) {
			this.color = c != null ? c : new Color(0, 0, 0, 0); // transparent
			this.borderColor = VisualPropertyUtil.getContrastingColor(this.color);
		}
		
		@Override
		public int getIconHeight() {
			return 16;
		}

		@Override
		public int getIconWidth() {
			return 44;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			int w = getIconWidth();
			int h = getIconHeight();
			
			g.setColor(color);
			g.fillRect(x, y, w, h);
			g.setColor(borderColor);
			g.drawRect(x, y, w, h);
		}
	}
}
