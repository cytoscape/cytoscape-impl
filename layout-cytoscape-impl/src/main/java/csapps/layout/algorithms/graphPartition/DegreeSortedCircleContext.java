package csapps.layout.algorithms.graphPartition;

import java.util.Set;

import org.cytoscape.view.layout.AbstractLayoutAlgorithmContext;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class DegreeSortedCircleContext extends AbstractLayoutAlgorithmContext implements TunableValidator {
	@Tunable(description="Don't partition graph before layout", groups="Standard settings")
	public boolean singlePartition;


	public DegreeSortedCircleContext(boolean supportsSelectedOnly, Set<Class<?>> supportedNodeAttributes, Set<Class<?>> supportedEdgeAttributes) {
		super(supportsSelectedOnly, supportedNodeAttributes, supportedEdgeAttributes);
	}

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}

}
