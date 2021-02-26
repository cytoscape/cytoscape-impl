package org.cytoscape.cg.internal.charts;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.data.general.PieDataset;

@SuppressWarnings("serial")
public class CustomPieSectionLabelGenerator extends StandardPieSectionLabelGenerator {

	private final Map<String, String> labels;
	
	public CustomPieSectionLabelGenerator(Map<String, String> labels) {
		this("{1} ({2})", new DecimalFormat("0.0"), new DecimalFormat("0%"), labels);
	}

	public CustomPieSectionLabelGenerator(Locale locale, Map<String, String> labels) {
		super(locale);
		this.labels = labels;
	}

	public CustomPieSectionLabelGenerator(String labelFormat, Locale locale, Map<String, String> labels) {
		super(labelFormat, locale);
		this.labels = labels;
	}

	public CustomPieSectionLabelGenerator(String labelFormat, NumberFormat numberFormat, NumberFormat percentFormat,
			Map<String, String> labels) {
		super(labelFormat, numberFormat, percentFormat);
		this.labels = labels;
	}

	public CustomPieSectionLabelGenerator(String labelFormat, Map<String, String> labels) {
		super(labelFormat);
		this.labels = labels;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public String generateSectionLabel(PieDataset dataset, Comparable key) {
		if (dataset.getValue(key).doubleValue() == 0.0)
			return null;
		if (labels != null && labels.get(key.toString()) != null)
			return labels.get(key.toString());

		return super.generateSectionLabel(dataset, key);
	}
}
