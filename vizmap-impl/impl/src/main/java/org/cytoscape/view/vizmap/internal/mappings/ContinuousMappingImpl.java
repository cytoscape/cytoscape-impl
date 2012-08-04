/*
  File: ContinuousMappingImpl.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.view.vizmap.internal.mappings;


import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangeRecord;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedEvent;
import org.cytoscape.view.vizmap.internal.mappings.interpolators.FlatInterpolator;
import org.cytoscape.view.vizmap.internal.mappings.interpolators.Interpolator;
import org.cytoscape.view.vizmap.internal.mappings.interpolators.LinearNumberToColorInterpolator;
import org.cytoscape.view.vizmap.internal.mappings.interpolators.LinearNumberToNumberInterpolator;
import org.cytoscape.view.vizmap.mappings.AbstractVisualMappingFunction;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;


/**
 * Implements an interpolation table mapping data to values of a particular
 * class. The data value is extracted from a bundle of attributes by using a
 * specified data attribute name.
 * 
 * @param <V>
 *            Type of object Visual Property holds
 * 
 *            For refactoring changes in this class, please refer to:
 *            cytoscape.visual.mappings.continuous.README.txt.
 * 
 */
public class ContinuousMappingImpl<K, V> extends AbstractVisualMappingFunction<K, V> implements ContinuousMapping<K, V> {
	
	// used to interpolate between boundaries
	private Interpolator<K, V> interpolator;

	// Contains List of Data Points
	private List<ContinuousMappingPoint<K, V>> points;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ContinuousMappingImpl(final String attrName, final Class<K> attrType, final VisualProperty<V> vp, final CyEventHelper eventHelper) {
		super(attrName, attrType, vp, eventHelper);
		
		// Validate type.  K is always a number.
		if (Number.class.isAssignableFrom(attrType) == false)
			throw new IllegalArgumentException("Attribute type should be Number.");
		
		this.points = new ArrayList<ContinuousMappingPoint<K, V>>();

		// TODO FIXME use factory here.
		// Create Interpolator
		if (vp.getRange().getType() == Color.class || vp.getRange().getType() == Paint.class)
			interpolator = (Interpolator<K, V>) new LinearNumberToColorInterpolator();
		else if (Number.class.isAssignableFrom(vp.getRange().getType()))
			interpolator = (Interpolator<K, V>) new LinearNumberToNumberInterpolator();
		else
			interpolator = (Interpolator<K, V>) new FlatInterpolator();
	}

	@Override
	public String toString() {
		return ContinuousMapping.CONTINUOUS;
	}

	@Override
	public List<ContinuousMappingPoint<K, V>> getAllPoints() {
		return Collections.unmodifiableList(points);
	}

	@Override
	public void addPoint(K value, BoundaryRangeValues<V> brv) {
		points.add(new ContinuousMappingPoint<K, V>(value, brv, this, eventHelper));
		eventHelper.addEventPayload((VisualMappingFunction) this, new VisualMappingFunctionChangeRecord(),
				VisualMappingFunctionChangedEvent.class);
	}

	@Override
	public void removePoint(int index) {
		points.remove(index);
		eventHelper.addEventPayload((VisualMappingFunction) this, new VisualMappingFunctionChangeRecord(),
				VisualMappingFunctionChangedEvent.class);
	}

	@Override
	public int getPointCount() {
		return points.size();
	}

	@Override
	public ContinuousMappingPoint<K, V> getPoint(int index) {
		if(points.isEmpty())
			return null;
		else if(points.size()>index)
			return points.get(index);
		else {
			throw new IllegalArgumentException("Invalid Index: " + index + ".  There are " + points.size() + " points.");
		}
	}

	@Override
	public V getMappedValue(final CyRow row) {
		V value = null;
		
		if (row != null && row.isSet(columnName)) {
			// Skip if source attribute is not defined.
			// ViewColumn will automatically substitute the per-VS or global default, as appropriate

			// In all cases, attribute value should be a number for continuous mapping.
			final K attrValue = row.get(columnName, columnType);
			value = getRangeValue(attrValue);
		}
		
		return value;
	}

	private V getRangeValue(K domainValue) {
		if(points.isEmpty())
			return null;
		
		ContinuousMappingPoint<K, V> firstPoint = points.get(0);
		K minDomain = firstPoint.getValue();

		// if given domain value is smaller than any in our list,
		// return the range value for the smallest domain value we have.
		int firstCmp = compareValues(domainValue, minDomain);

		if (firstCmp <= 0) {
			BoundaryRangeValues<V> bv = firstPoint.getRange();

			if (firstCmp < 0)
				return bv.lesserValue;
			else
				return bv.equalValue;
		}

		// if given domain value is larger than any in our Vector,
		// return the range value for the largest domain value we have.
		ContinuousMappingPoint<K, V> lastPoint = points.get(points.size() - 1);
		K maxDomain = lastPoint.getValue();

		if (compareValues(domainValue, maxDomain) > 0) {
			BoundaryRangeValues<V> bv = lastPoint.getRange();

			return bv.greaterValue;
		}

		// OK, it's somewhere in the middle, so find the boundaries and
		// pass to our interpolator function. First check for a null
		// interpolator function
		if (this.interpolator == null)
			return null;

		// Note that the list of Points is sorted.
		// Also, the case of the inValue equalling the smallest key was
		// checked above.
		ContinuousMappingPoint<K, V> currentPoint;
		int index = 0;

		for (index = 0; index < points.size(); index++) {
			currentPoint = points.get(index);

			K currentValue = currentPoint.getValue();
			int cmpValue = compareValues(domainValue, currentValue);

			if (cmpValue == 0) {
				BoundaryRangeValues<V> bv = currentPoint.getRange();

				return bv.equalValue;
			} else if (cmpValue < 0)
				break;
		}

		return getRangeValue(index, domainValue);
	}

	/**
	 * This is tricky. The desired domain value is greater than lowerDomain and
	 * less than upperDomain. Therefore, we want the "greater" field of the
	 * lower boundary value (because the desired domain value is greater) and
	 * the "lesser" field of the upper boundary value (semantic difficulties).
	 */
	private V getRangeValue(int index, K domainValue) {
		// Get Lower Domain and Range
		ContinuousMappingPoint<K, V> lowerBound = points.get(index - 1);
		K lowerDomain = lowerBound.getValue();
		BoundaryRangeValues<V> lv = lowerBound.getRange();
		V lowerRange = lv.greaterValue;

		// Get Upper Domain and Range
		ContinuousMappingPoint<K, V> upperBound = points.get(index);
		K upperDomain = upperBound.getValue();
		BoundaryRangeValues<V> gv = upperBound.getRange();
		V upperRange = gv.lesserValue;
		
		V value = interpolator.getRangeValue(lowerDomain, lowerRange, upperDomain, upperRange, domainValue);
		
		return value;
	}

	/**
	 * Helper function to compare Number objects. This is needed because Java
	 * doesn't allow comparing, for example, Integer objects to Double objects.
	 */
	private int compareValues(K probe, K target) {
		final Number n1 = (Number) probe;
		final Number n2 = (Number) target;
		double d1 = n1.doubleValue();
		double d2 = n2.doubleValue();

		if (d1 < d2)
			return -1;
		else if (d1 > d2)
			return 1;
		else
			return 0;
	}
}
