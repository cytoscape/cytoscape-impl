package org.cytoscape.filter.internal.view;

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;

import org.cytoscape.filter.internal.prefuse.NumberRangeModel;

public abstract class RangeChooserController {
	private NumberRangeModel sliderModel;
	private Number low;
	private Number high;
	private Number minimum;
	private Number maximum;
	private boolean disableListeners;
	
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
	
	Number clampByFormat(AbstractFormatter format, Number value) {
		if (format == null) {
			return value;
		}
		if (value == null) {
			return null;
		}
		try {
			return (Number) format.stringToValue(format.valueToString(value));
		} catch (ParseException e) {
			return null;
		}
	}
	
	public void sliderChanged(RangeChooser chooser) {
		JFormattedTextField minimumField = chooser.getMinimumField();
		JFormattedTextField maximumField = chooser.getMaximumField();
		
		// Ensure that what we display is what actually makes it into the model
		Number newLow = clampByFormat(minimumField.getFormatterFactory().getFormatter(minimumField), (Number) sliderModel.getLowValue());
		Number newHigh = clampByFormat(maximumField.getFormatterFactory().getFormatter(maximumField), (Number) sliderModel.getHighValue());
		
		if (newLow != null && newLow.equals(low) && newHigh != null && newHigh.equals(high)) {
			return;
		}
		
		low = newLow ;
		high = newHigh;
		
		disableListeners = true;
		try {
			minimumField.setValue(low);
			maximumField.setValue(high);
		} finally {
			disableListeners = false;
		}
		handleRangeChanged(low, high);
	}
	
	public void minimumChanged(RangeChooser chooser) {
		low = (Number) chooser.getMinimumField().getValue();
		if (low == null) {
			low = minimum;
		}
		sliderModel.setValueRange(low, high, minimum, maximum);
		if (disableListeners) {
			return;
		}
		handleRangeChanged(low, high);
	}

	public void maximumChanged(RangeChooser chooser) {
		high = (Number) chooser.getMaximumField().getValue();
		if (high == null) {
			high = maximum;
		}
		sliderModel.setValueRange(low, high, minimum, maximum);
		if (disableListeners) {
			return;
		}
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
	
	public Number getMinimum() {
		return minimum;
	}

	public Number getMaximum() {
		return maximum;
	}
	
	protected abstract void handleRangeChanged(Number low, Number high);
}
