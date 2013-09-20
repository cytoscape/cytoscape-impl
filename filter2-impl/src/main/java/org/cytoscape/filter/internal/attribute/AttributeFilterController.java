package org.cytoscape.filter.internal.attribute;

import org.cytoscape.filter.internal.prefuse.NumberRangeModel;

public interface AttributeFilterController {
	AttributeFilter getFilter();
	NumberRangeModel getSliderModel();
	void synchronize(AttributeFilterView view);
}
