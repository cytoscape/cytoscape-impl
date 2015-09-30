package org.cytoscape.filter.internal.filters.column;

public interface ColumnFilterController {
	ColumnFilter getFilter();
	void columnsChanged(ColumnFilterView view);
	void setSliderBounds(int min, int max);
	void setSliderBounds(double min, double max);
}
