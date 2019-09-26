package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.SpinnerNumberModel;

import org.cytoscape.model.CyTable;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.util.NumberConverter;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.TrackRenderer;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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
 * Continuous-Continuous mapping editor.<br>
 * 
 * This editor will be used for Number-to-Number mapping.
 * 
 * <p>
 * This is a editor for continuous values, i.e., numbers.
 * </p>
 */
public class C2CMappingEditorPanel<K extends Number, V extends Number> extends ContinuousMappingEditorPanel<K, V>
		implements PropertyChangeListener {

	private final static long serialVersionUID = 1213748836613718L;

	// Default value for below and above.
	private static final Number DEF_BELOW_AND_ABOVE = 1d;
	private static final Number FIRST_LOCATION = 10d;
	private static final Number SECOND_LOCATION = 30d;

	public C2CMappingEditorPanel(
			VisualStyle style,
			ContinuousMapping<K, V> mapping,
			CyTable attr,
			EditorManager editorManager,
			ServicesUtil servicesUtil
	) {
		super(style, mapping, attr, editorManager, servicesUtil);

		getAbovePanel().setVisible(false);
		getBelowPanel().setVisible(false);
		getPalettesPanel().setVisible(false);

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

	@SuppressWarnings("rawtypes")
	public ImageIcon getIcon(final int iconWidth, final int iconHeight) {
		final TrackRenderer rend = getSlider().getTrackRenderer();

		if (rend instanceof ContinuousTrackRenderer) {
			rend.getRendererComponent(getSlider());

			return ((ContinuousTrackRenderer) rend).getTrackGraphicIcon(iconWidth, iconHeight);
		} else {
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
			newRange = new BoundaryRangeValues<>(convertToValue(below), convertToValue(5d), convertToValue(above));
			final K newKey = convertToColumnValue((maxValue.doubleValue() / 2));

			mapping.addPoint(newKey, newRange);
			updateMap();
			return;
		}

		// There are one or more handles exists.  Need sorting.
		final SortedMap<Double, ContinuousMappingPoint<K, V>> sortedPoints = new TreeMap<>();

		for (final ContinuousMappingPoint<K, V> point : mapping.getAllPoints()) {
			final Number val = point.getValue();
			sortedPoints.put(val.doubleValue(), point);
		}

		final ContinuousMappingPoint<K, V> previousPoint = sortedPoints.get(sortedPoints.lastKey());
		final BoundaryRangeValues<V> previousRange = previousPoint.getRange();

		V lesserVal = previousPoint.getRange().greaterValue;
		V equalVal = previousPoint.getRange().greaterValue;
		V greaterVal = previousRange.greaterValue;

		newRange = new BoundaryRangeValues<>(convertToValue(lesserVal), convertToValue(equalVal),
				convertToValue(greaterVal));
		mapping.addPoint(maxValue, newRange);

		updateMap();
	}

	@Override
	protected void addButtonActionPerformed(ActionEvent evt) {
		addSlider(50d, convertToValue(5d));
		update();
	}

	@Override
	protected void deleteButtonActionPerformed(ActionEvent evt) {
		final int selectedIndex = getSlider().getSelectedIndex();

		// TODO: Is this correct?
		if (selectedIndex >= 0) {
			if (getSlider().getModel().getThumbCount() > selectedIndex)
				getSlider().getModel().removeThumb(selectedIndex);
			
			if (mapping.getPointCount() > selectedIndex)
				mapping.removePoint(selectedIndex);
			
			initSlider();
		}
		
		update();
	}

	private void initSlider() {
		getSlider().updateUI();

		Number minValue = tracer.getMin(type);
		Number actualRange = tracer.getRange(type);

		BoundaryRangeValues<V> bound;
		Number fraction;

		final List<Thumb<V>> sorted = getSlider().getModel().getSortedThumbs();

		for (Thumb<V> t : sorted)
			getSlider().getModel().removeThumb(getSlider().getModel().getThumbIndex(t));

		final SortedMap<Double, ContinuousMappingPoint<K, V>> sortedPoints = new TreeMap<>();

		for (final ContinuousMappingPoint<K, V> point : mapping.getAllPoints()) {
			final Number val = point.getValue();
			sortedPoints.put(val.doubleValue(), point);
		}

		for (Double key : sortedPoints.keySet()) {
			ContinuousMappingPoint<K, V> point = sortedPoints.get(key);
			bound = point.getRange();
			fraction = ((Number) ((point.getValue().doubleValue() - minValue.doubleValue())
					/ actualRange.doubleValue())).floatValue() * 100d;
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

		ContinuousTrackRenderer<K, V> cRend = new ContinuousTrackRenderer<>(style, mapping, below, above, tracer,
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
	@SuppressWarnings("rawtypes")
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
		final SpinnerNumberModel model = new SpinnerNumberModel(0.0d, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				0.01d);
		model.addChangeListener(evt -> {
			if (!getPropertySpinner().isEnabled())
				return;
			
			final Number newVal = model.getNumber().doubleValue();
			final int selectedIndex = getSlider().getSelectedIndex();
			V currentValue = getSlider().getModel().getThumbAt(selectedIndex).getObject();
			
			if (currentValue.equals(newVal))
				return;
			
			getSlider().getModel().getThumbAt(selectedIndex).setObject(convertToValue(newVal));
			getSlider().repaint();
			
			updateMap();
		});
		getPropertySpinner().setModel(model);
	}

	@Override
	protected void cancelChanges() {
		initSlider();
	}
}
