package org.cytoscape.cg.internal.charts;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.List;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

@SuppressWarnings("serial")
public class CustomCategoryItemLabelGenerator extends StandardCategoryItemLabelGenerator {

	private final List<String> itemLabels;
	
	public CustomCategoryItemLabelGenerator(List<String> itemLabels) {
		super();
		this.itemLabels = itemLabels;
	}

	public CustomCategoryItemLabelGenerator(List<String> itemLabels, String labelFormat, DateFormat formatter) {
		super(labelFormat, formatter);
		this.itemLabels = itemLabels;
	}

	public CustomCategoryItemLabelGenerator(List<String> itemLabels, String labelFormat, NumberFormat formatter,
			NumberFormat percentFormatter) {
		super(labelFormat, formatter, percentFormatter);
		this.itemLabels = itemLabels;
	}

	public CustomCategoryItemLabelGenerator(List<String> itemLabels, String labelFormat, NumberFormat formatter) {
		super(labelFormat, formatter);
		this.itemLabels = itemLabels;
	}

	@Override
	public String generateLabel(CategoryDataset dataset, int row, int column) {
		if (itemLabels != null && itemLabels.size() > row)
			return itemLabels.get(row);

		return super.generateLabel(dataset, row, column);
	}
}
