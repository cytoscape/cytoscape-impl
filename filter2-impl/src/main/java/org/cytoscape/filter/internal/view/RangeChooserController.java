package org.cytoscape.filter.internal.view;

import org.cytoscape.filter.internal.prefuse.NumberRangeModel;

public abstract class RangeChooserController {
	private NumberRangeModel sliderModel;
	private Number low;
	private Number high;
	private Number minimum;
	private Number maximum;
	
	public RangeChooserController() {
		sliderModel = new NumberRangeModel(0, 0, 0, 0);
		low = 0;
		high = 0;
		minimum = 0;
		maximum = 0;
	}
	
	public NumberRangeModel getSliderModel() {
		return sliderModel;
	}
	
	public void sliderChanged(RangeChooser chooser) {
		Number newLow = (Number) sliderModel.getLowValue();
		Number newHigh = (Number) sliderModel.getHighValue();
		
		if (newLow != null && newLow.equals(low) && newHigh != null && newHigh.equals(high)) {
			return;
		}
		
		low = newLow ;
		high = newHigh;
		
		chooser.getMinimumField().setValue(low);
		chooser.getMaximumField().setValue(high);
		
		handleRangeChanged(low, high);
	}
	
	public void minimumChanged(RangeChooser chooser) {
		low = (Number) chooser.getMinimumField().getValue();
		handleRangeChanged(low, high);
	}

	public void maximumChanged(RangeChooser chooser) {
		high = (Number) chooser.getMaximumField().getValue();
		handleRangeChanged(low, high);
	}

	public void setRange(Number low, Number high, Number minimum, Number maximum) {
		this.low = low;
		this.high = high;
		this.minimum = minimum;
		this.maximum = maximum;
		
		sliderModel.setValueRange(low, high, minimum, maximum);
	}
	
	public void setSelection(Number low, Number high) {
		setRange(low, high, minimum, maximum);
	}
	
	public void setBounds(Number minimum, Number maximum) {
		setRange(low, high, minimum, maximum);
	}
	
	public void setInteractive(boolean isActive, RangeChooser chooser) {
		chooser.setInteractive(isActive);
		
		if (low == null) {
			low = minimum;
		}
		if (high == null) {
			high = maximum;
		}
		setRange(low, high, minimum, maximum);
	}
	
	protected abstract void handleRangeChanged(Number low, Number high);
}
