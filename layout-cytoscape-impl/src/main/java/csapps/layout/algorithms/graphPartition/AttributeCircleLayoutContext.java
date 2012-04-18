package csapps.layout.algorithms.graphPartition;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.layout.AbstractLayoutContext;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class AttributeCircleLayoutContext extends AbstractLayoutContext implements TunableValidator {
	@Tunable(description = "The attribute to use for the layout")
	public String attribute = CyNetwork.NAME;
	
//	@Tunable(description="Node attribute to be use")
//	public ListSingleSelection<Integer> attrName;
	
	@Tunable(description = "Circle size")
	public double spacing = 100.0;
	@Tunable(description = "Don't partition graph before layout", groups = "Standard settings")
	public boolean singlePartition;


	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		return attribute.length() > 0 && spacing > 0.0 ? ValidationState.OK : ValidationState.INVALID;
	}

	/**
	 * Sets the attribute to use for the weights
	 *
	 * @param value the name of the attribute
	 */
	public void setLayoutAttribute(String value) {
		if (value.equals("(none)"))
			this.attribute = null;
		else
			this.attribute = value;
	}
}
