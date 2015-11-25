package org.cytoscape.filter.internal.range;

import javax.swing.BoundedRangeModel;

/**
 * A wrapper for a BoundedRangeModel that converts from numeric types
 * to the ints expected by BoundedRangeModel.
 */
interface SliderModel<N extends Number> {
	
	public BoundedRangeModel getBoundedRangeModel();
	
	public void setValues(N low, N high, N min, N max);
	
	public N getLow();
	
	public N getHigh();
	
	public N getMin();
	
	public N getMax();
	
}
