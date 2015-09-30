package org.cytoscape.filter.internal.range;

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;

import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.internal.view.look.FilterPanelStyle;

public class RangeChooserController<N extends Number> {
	
	private final SliderModel<N> sliderModel;
	private final AbstractFormatterFactory formatterFactory;
	private final RangeListener<N> listener;
	
	private RangeChooser<N> rangeChooser;
	
	
	public static RangeChooserController<Integer> forInteger(FilterPanelStyle style, RangeListener<Integer> listener) {
		SliderModel<Integer> sliderModel = new IntegerSliderModel();
		AbstractFormatterFactory formatterFactory = ViewUtil.createIntegerFormatterFactory();
		RangeChooserController<Integer> controller = new RangeChooserController<>(sliderModel, formatterFactory, style, listener);
		return controller;
	}
	
	public static RangeChooserController<Double> forDouble(FilterPanelStyle style, RangeListener<Double> listener) {
		SliderModel<Double> sliderModel = new DoubleSliderModel();
		AbstractFormatterFactory formatterFactory = ViewUtil.createNumberFormatterFactory();
		RangeChooserController<Double> controller = new RangeChooserController<>(sliderModel, formatterFactory, style, listener);
		return controller;
	}
	
	
	private RangeChooserController(SliderModel<N> sliderModel, AbstractFormatterFactory formatterFactory, FilterPanelStyle style, RangeListener<N> listener) {
		this.sliderModel = sliderModel;
		this.formatterFactory = formatterFactory;
		this.listener = listener;
		this.rangeChooser = new RangeChooser<>(style, this);
	}
	
	public SliderModel<N> getSliderModel() {
		return sliderModel;
	}
	
	public AbstractFormatterFactory getFormatterFactory() {
		return formatterFactory;
	}
	
	public RangeChooser<N> getRangeChooser() {
		return rangeChooser;
	}
	
	public void setInteractive(boolean isInteractive)  {
		if(rangeChooser != null) {
			rangeChooser.setInteractive(isInteractive);
		}
	}
	
	
	public void reset(N low, N high, N minimum, N maximum) {
		rangeChooser.removeListeners();
		
		sliderModel.setValues(low, high, minimum, maximum);
		rangeChooser.getLowField().setValue(low);
		rangeChooser.getHighField().setValue(high);
		
		rangeChooser.addListeners(this);
	}
	
	
	private N clampByFormat(AbstractFormatter format, N value) {
		if (format == null) {
			return value;
		}
		if (value == null) {
			return null;
		}
		try {
			return (N) format.stringToValue(format.valueToString(value));
		} catch (ParseException e) {
			return null;
		}
	}
	
	void sliderChanged() {
		rangeChooser.removeListeners();
		
		JFormattedTextField minimumField = rangeChooser.getLowField();
		JFormattedTextField maximumField = rangeChooser.getHighField();
		
		// Ensure that what we display is what actually makes it into the model
		N low  = clampByFormat(minimumField.getFormatterFactory().getFormatter(minimumField), sliderModel.getLow());
		N high = clampByFormat(maximumField.getFormatterFactory().getFormatter(maximumField), sliderModel.getHigh());
		
		minimumField.setValue(low);
		maximumField.setValue(high);
		
		handleRangeChanged(low, high);
		
		rangeChooser.addListeners(this);
	}
	
	void textFieldChanged() {
		rangeChooser.removeListeners();
		
		N low  = getLow();
		N high = getHigh();
		
		// If the user set a value outside the range then the slider model will clamp it to the range.
		// We still use what's in the text fields as the definitive value.
		sliderModel.setValues(low, high, sliderModel.getMin(), sliderModel.getMax());
		
		handleRangeChanged(low, high);
		
		rangeChooser.addListeners(this);
	}


	private void handleRangeChanged(N low, N high) {
		if(listener != null) {
			listener.rangeChanged(low, high);
		}
	}
	
	public N getLow() {
		return (N) rangeChooser.getLowField().getValue();
	}
	
	public N getHigh() {
		return (N) rangeChooser.getHighField().getValue();
	}
}
