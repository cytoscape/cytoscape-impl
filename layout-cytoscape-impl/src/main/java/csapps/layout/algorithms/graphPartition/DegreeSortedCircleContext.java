package csapps.layout.algorithms.graphPartition;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class DegreeSortedCircleContext implements TunableValidator {
	@Tunable(description="Don't partition graph before layout", groups="Standard settings")
	public boolean singlePartition;

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}

}
