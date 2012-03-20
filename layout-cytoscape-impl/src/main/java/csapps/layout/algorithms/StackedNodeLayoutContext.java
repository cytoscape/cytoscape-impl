package csapps.layout.algorithms;

import java.util.Set;

import org.cytoscape.view.layout.AbstractLayoutAlgorithmContext;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;

public class StackedNodeLayoutContext extends AbstractLayoutAlgorithmContext implements TunableValidator {

	public StackedNodeLayoutContext(boolean supportsSelectedOnly, Set<Class<?>> supportedNodeAttributes, Set<Class<?>> supportedEdgeAttributes) {
		super(supportsSelectedOnly, supportedNodeAttributes, supportedEdgeAttributes);
	}

	@Tunable(description="x_position")
	public double x_position = 10.0;

	@Tunable(description="y_start_position")
	public double y_start_position = 10.0;

	//@Tunable(description="nodes")
	//public Collection nodes;


	/**
	 * Puts a collection of nodes into a "stack" layout. This means the nodes are
	 * arranged in a line vertically, with each node overlapping with the previous.
	 *
	 * @param nodes the nodes whose position will be modified
	 * @param x_position the x position for the nodes
	 * @param y_start_position the y starting position for the stack
	 */

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}
}
