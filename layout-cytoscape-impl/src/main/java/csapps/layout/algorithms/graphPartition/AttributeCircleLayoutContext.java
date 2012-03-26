package csapps.layout.algorithms.graphPartition;

import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.layout.AbstractLayoutAlgorithmContext;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.model.CyNetwork;

public class AttributeCircleLayoutContext extends AbstractLayoutAlgorithmContext implements TunableValidator {
	@Tunable(description = "The attribute to use for the layout")
	public String attribute = CyNetwork.NAME;
	
//	@Tunable(description="Node attribute to be use")
//	public ListSingleSelection<Integer> attrName;
	
	@Tunable(description = "Circle size")
	public double spacing = 100.0;
	@Tunable(description = "Don't partition graph before layout", groups = "Standard settings")
	public boolean singlePartition;


	public AttributeCircleLayoutContext(boolean supportsSelectedOnly, Set<Class<?>> supportedNodeAttributes, Set<Class<?>> supportedEdgeAttributes) {
		super(supportsSelectedOnly, supportedNodeAttributes, supportedEdgeAttributes);
	}

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
