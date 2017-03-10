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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.model.CyTable;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.NumberConverter;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.TrackRenderer;

/**
 * Continuous-Continuous mapping editor.<br>
 * 
 * This editor will be used for Number-to-Number mapping.
 * 
 * <p>
 * This is a editor for continuous values, i.e., numbers.
 * </p>
 * 
 */
public class C2CMappingEditorPanel<K extends Number, V extends Number> extends ContinuousMappingEditorPanel<K, V>
		implements PropertyChangeListener {

	private final static long serialVersionUID = 1213748836613718L;

	// Default value for below and above.
	private static final Number DEF_BELOW_AND_ABOVE = 1d;
	private static final Number FIRST_LOCATION = 10d;
	private static final Number SECOND_LOCATION = 30d;


	public C2CMappingEditorPanel(final VisualStyle style, final ContinuousMapping<K, V> mapping, final CyTable attr,
			final ServicesUtil servicesUtil) {
		super(style, mapping, attr, servicesUtil);

		getAbovePanel().setVisible(false);
		getBelowPanel().setVisible(false);

		initSlider();
		
		if (mapping.getAllPoints().isEmpty()) {
			addSlider(0d, FIRST_LOCATION);
			addSlider(100d, SECOND_LOCATION);
		}

		setPropertySpinner();
	}

	private V convertToValue(final Number value) {
		return NumberConverter.convert(vpValueType, value);
	}

	private K convertToColumnValue(final Number value) {
		return NumberConverter.convert(columnType, value);
	}

	public ImageIcon getIcon(final int iconWidth, final int iconHeight) {
		final TrackRenderer rend = getSlider().getTrackRenderer();

		if (rend instanceof ContinuousTrackRenderer) {
			rend.getRendererComponent(getSlider());

			return ((ContinuousTrackRenderer) rend).getTrackGraphicIcon(iconWidth, iconHeight);
		} else {
			return null;
		}
	}

	public ImageIcon getLegend(final int width, final int height) {
		final ContinuousTrackRenderer<K, V> rend = (ContinuousTrackRenderer) getSlider().getTrackRenderer();
		rend.getRendererComponent(getSlider());

		return rend.getLegend(width, height);
	}

	/**
	 * Add New Slider to editor
	 * 
	 * @param position
	 * @param value
	 */
	private void addSlider(final Number position, final Number value) {
		final K maxValue = convertToColumnValue(tracer.getMax(type));
		BoundaryRangeValues<V> newRange;

		getSlider().getModel().addThumb(position.floatValue(), convertToValue(value));

		// There is no handle at this point.  Add new one.
		if (mapping.getPointCount() == 0) {
			newRange = new BoundaryRangeValues<V>(convertToValue(below), convertToValue(5d), convertToValue(above));
			final K newKey = convertToColumnValue((maxValue.doubleValue() / 2));

			mapping.addPoint(newKey, newRange);
			updateMap();
			return;
		}

		// There are one or more handles exists.  Need sorting.
		final SortedMap<Double, ContinuousMappingPoint<K, V>> sortedPoints = new TreeMap<Double, ContinuousMappingPoint<K, V>>();

		for (final ContinuousMappingPoint<K, V> point : mapping.getAllPoints()) {
			final Number val = point.getValue();
			sortedPoints.put(val.doubleValue(), point);
		}

		final ContinuousMappingPoint<K, V> previousPoint = sortedPoints.get(sortedPoints.lastKey());
		final BoundaryRangeValues<V> previousRange = previousPoint.getRange();

		V lesserVal = previousPoint.getRange().greaterValue;
		V equalVal = previousPoint.getRange().greaterValue;
		V greaterVal = previousRange.greaterValue;

		newRange = new BoundaryRangeValues<V>(convertToValue(lesserVal), convertToValue(equalVal),
				convertToValue(greaterVal));
		mapping.addPoint(maxValue, newRange);

		updateMap();
	}

	@Override
	protected void addButtonActionPerformed(ActionEvent evt) {
		addSlider(50d, convertToValue(5d));
	}

	@Override
	protected void deleteButtonActionPerformed(ActionEvent evt) {
		final int selectedIndex = getSlider().getSelectedIndex();

		// TODO: Is this correct?
		if (0 <= selectedIndex) {
			getSlider().getModel().removeThumb(selectedIndex);
			mapping.removePoint(selectedIndex);
			initSlider();
		}
	}

	private void initSlider() {
		getSlider().updateUI();

		final Number minValue = tracer.getMin(type);
		Number actualRange = tracer.getRange(type);

		BoundaryRangeValues<V> bound;
		Number fraction;

		final List<Thumb<V>> sorted = getSlider().getModel().getSortedThumbs();
		for (Thumb<V> t : sorted)
			getSlider().getModel().removeThumb(getSlider().getModel().getThumbIndex(t));

		final SortedMap<Double, ContinuousMappingPoint<K, V>> sortedPoints = new TreeMap<Double, ContinuousMappingPoint<K, V>>();
		for (final ContinuousMappingPoint<K, V> point : mapping.getAllPoints()) {
			final Number val = point.getValue();
			sortedPoints.put(val.doubleValue(), point);
		}

		for (Double key : sortedPoints.keySet()) {
			ContinuousMappingPoint<K, V> point = sortedPoints.get(key);
			bound = point.getRange();
			fraction = ((Number) ((point.getValue().doubleValue() - minValue.doubleValue()) / actualRange.doubleValue()))
					.floatValue() * 100d;
			getSlider().getModel().addThumb(fraction.floatValue(), bound.equalValue);
		}

		if (!sortedPoints.isEmpty()) {
			below = sortedPoints.get(sortedPoints.firstKey()).getRange().lesserValue;
			above = sortedPoints.get(sortedPoints.lastKey()).getRange().greaterValue;
		} else {
			below = convertToValue(DEF_BELOW_AND_ABOVE);
			above = convertToValue(DEF_BELOW_AND_ABOVE);
		}

		/*
		 * get min and max for the value object
		 */
		TriangleThumbRenderer thumbRend = new TriangleThumbRenderer();

		ContinuousTrackRenderer<K, V> cRend = new ContinuousTrackRenderer<K, V>(style, mapping, below, above, tracer,
				servicesUtil);
		cRend.addPropertyChangeListener(this);

		getSlider().setThumbRenderer(thumbRend);
		getSlider().setTrackRenderer(cRend);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ContinuousMappingEditorPanel.BELOW_VALUE_CHANGED)) {
			below = convertToValue((Number) evt.getNewValue());
		} else if (evt.getPropertyName().equals(ContinuousMappingEditorPanel.ABOVE_VALUE_CHANGED)) {
			above = convertToValue((Number) evt.getNewValue());
		}
	}

	@Override
	public ImageIcon drawIcon(int iconWidth, int iconHeight, boolean detail) {
		final TrackRenderer rend = getSlider().getTrackRenderer();

		if (rend instanceof ContinuousTrackRenderer) {
			rend.getRendererComponent(getSlider());

			return ((ContinuousTrackRenderer) rend).getTrackGraphicIcon(iconWidth, iconHeight);
		} else {
			return null;
		}
	}

	private void setPropertySpinner() {
		SpinnerNumberModel propertySpinnerModel = new SpinnerNumberModel(0.0d, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, 0.01d);
		propertySpinnerModel.addChangeListener(new PropertyValueSpinnerChangeListener(propertySpinnerModel));
		getPropertySpinner().setModel(propertySpinnerModel);
	}

	private final class PropertyValueSpinnerChangeListener implements ChangeListener {

		private final SpinnerNumberModel spinnerModel;

		public PropertyValueSpinnerChangeListener(SpinnerNumberModel model) {
			this.spinnerModel = model;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			final Number newVal = spinnerModel.getNumber().doubleValue();
			final int selectedIndex = getSlider().getSelectedIndex();
			V currentValue = getSlider().getModel().getThumbAt(selectedIndex).getObject();
			
			if (currentValue.equals(newVal))
				return;
			
			getSlider().getModel().getThumbAt(selectedIndex).setObject(convertToValue(newVal));
			getSlider().repaint();
			
			updateMap();
		}
	}

	@Override
	protected void cancelChanges() {
		initSlider();
	}
}
