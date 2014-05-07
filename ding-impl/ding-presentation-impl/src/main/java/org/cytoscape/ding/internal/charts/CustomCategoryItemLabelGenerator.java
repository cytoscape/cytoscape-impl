package org.cytoscape.ding.internal.charts;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.List;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

public class CustomCategoryItemLabelGenerator extends StandardCategoryItemLabelGenerator {

	private static final long serialVersionUID = -7481427668729582873L;

	private final List<String> itemLabels;
	
	public CustomCategoryItemLabelGenerator(final List<String> itemLabels) {
		super();
		this.itemLabels = itemLabels;
	}

	public CustomCategoryItemLabelGenerator(final List<String> itemLabels, final String labelFormat,
			final DateFormat formatter) {
		super(labelFormat, formatter);
		this.itemLabels = itemLabels;
	}

	public CustomCategoryItemLabelGenerator(final List<String> itemLabels, final String labelFormat,
			final NumberFormat formatter, final NumberFormat percentFormatter) {
		super(labelFormat, formatter, percentFormatter);
		this.itemLabels = itemLabels;
	}

	public CustomCategoryItemLabelGenerator(final List<String> itemLabels, final String labelFormat,
			final NumberFormat formatter) {
		super(labelFormat, formatter);
		this.itemLabels = itemLabels;
	}

	@Override
	public String generateLabel(CategoryDataset dataset, int row, int column) {
		if (itemLabels != null && itemLabels.size() > row)
			return itemLabels.get(row);
		
		return super.generateLabel(dataset, row, column);
	}

	@Override
	public String generateColumnLabel(CategoryDataset dataset, int column) {
		// TODO Auto-generated method stub
		return super.generateColumnLabel(dataset, column);
	}

	@Override
	protected String generateLabelString(CategoryDataset dataset, int row,
			int column) {
		// TODO Auto-generated method stub
		return super.generateLabelString(dataset, row, column);
	}

	@Override
	public String generateRowLabel(CategoryDataset dataset, int row) {
		// TODO Auto-generated method stub
		return super.generateRowLabel(dataset, row);
	}
	
	
}
