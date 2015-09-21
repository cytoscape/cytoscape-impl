package org.cytoscape.filter.internal.filters.column;

public interface ColumnFilterController {
	ColumnFilter getFilter();
	void synchronize(ColumnFilterView view);
	void setSliderBounds(long min, long max);
	void setSliderBounds(double min, double max);
}
