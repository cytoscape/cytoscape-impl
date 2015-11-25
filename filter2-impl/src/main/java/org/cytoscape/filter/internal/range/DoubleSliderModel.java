package org.cytoscape.filter.internal.range;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

class DoubleSliderModel implements SliderModel<Double> {

	private int NUM_TICKS = 500;
	
	
	private final BoundedRangeModel model;
	
	private double minVal;
	private double maxVal;
	
	public DoubleSliderModel() {
		this.model = new DefaultBoundedRangeModel();
	}

	@Override
	public BoundedRangeModel getBoundedRangeModel() {
		return model;
	}

	@Override
	public void setValues(Double low, Double high, Double min, Double max) {
		minVal = min;
		maxVal = max;
		double value  = map(low, min, max, 0, NUM_TICKS);
		double extent = map(high, min, max, 0, NUM_TICKS) - value;
		model.setRangeProperties(Double.valueOf(value).intValue(), Double.valueOf(extent).intValue(), 0, NUM_TICKS, false);
	}

	private static double map(double in, double inStart, double inEnd, double outStart, double outEnd) {
		double slope = (outEnd - outStart) / (inEnd - inStart);
		return outStart + slope * (in - inStart);
	}
	
	@Override
	public Double getLow() {
		double low = model.getValue();
		return map(low, 0, NUM_TICKS, minVal, maxVal);
	}

	@Override
	public Double getHigh() {
		double high = model.getValue() + model.getExtent();
		return map(high, 0, NUM_TICKS, minVal, maxVal);
	}

	@Override
	public Double getMin() {
		return minVal;
	}

	@Override
	public Double getMax() {
		return maxVal;
	}
	
}
