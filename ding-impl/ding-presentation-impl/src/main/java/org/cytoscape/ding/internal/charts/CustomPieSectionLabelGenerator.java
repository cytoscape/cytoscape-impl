package org.cytoscape.ding.internal.charts;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.data.general.PieDataset;


public class CustomPieSectionLabelGenerator extends StandardPieSectionLabelGenerator {

	private static final long serialVersionUID = -1278987792442187738L;

	private final List<String> itemLabels;
	
	public CustomPieSectionLabelGenerator(final List<String> itemLabels) {
		this("{1} ({2})", new DecimalFormat("0.0"), new DecimalFormat("0%"), itemLabels);
	}

	public CustomPieSectionLabelGenerator(Locale locale, final List<String> itemLabels) {
		super(locale);
		this.itemLabels = itemLabels;
	}

	public CustomPieSectionLabelGenerator(String labelFormat, Locale locale, final List<String> itemLabels) {
		super(labelFormat, locale);
		this.itemLabels = itemLabels;
	}

	public CustomPieSectionLabelGenerator(String labelFormat, NumberFormat numberFormat, NumberFormat percentFormat,
			final List<String> itemLabels) {
		super(labelFormat, numberFormat, percentFormat);
		this.itemLabels = itemLabels;
	}

	public CustomPieSectionLabelGenerator(String labelFormat, final List<String> itemLabels) {
		super(labelFormat);
		this.itemLabels = itemLabels;
	}

	@Override
	@SuppressWarnings("rawtypes")
    public String generateSectionLabel(PieDataset dataset, Comparable key) {
        if (dataset.getValue(key).doubleValue() == 0.0)
        	return null;
        if (itemLabels != null && itemLabels.contains(key.toString()))
        	return key.toString();
        
        return super.generateSectionLabel(dataset, key);
    }
}
