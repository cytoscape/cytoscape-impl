package org.cytoscape.filter.internal.range;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

class IntegerSliderModel implements SliderModel<Integer> {

	private final BoundedRangeModel model;
	
	public IntegerSliderModel() {
		this.model = new DefaultBoundedRangeModel();
	}

	@Override
	public BoundedRangeModel getBoundedRangeModel() {
		return model;
	}

	@Override
	public void setValues(Integer low, Integer high, Integer min, Integer max) {
		model.setRangeProperties(low, high-low, min, max, false);
		
	}

	@Override
	public Integer getLow() {
		return model.getValue();
	}

	@Override
	public Integer getHigh() {
		return model.getValue() + model.getExtent();
	}

	@Override
	public Integer getMin() {
		return model.getMinimum();
	}

	@Override
	public Integer getMax() {
		return model.getMaximum();
	}
	
	
}
