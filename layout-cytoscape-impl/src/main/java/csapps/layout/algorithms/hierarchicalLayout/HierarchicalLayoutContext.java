package csapps.layout.algorithms.hierarchicalLayout;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class HierarchicalLayoutContext implements TunableValidator {
	@Tunable(description="Horizontal spacing between nodes")
	public int nodeHorizontalSpacing = 64;
	@Tunable(description="Vertical spacing between nodes")
	public int nodeVerticalSpacing = 32;
	@Tunable(description="Component spacing")
	public int componentSpacing = 64;
	@Tunable(description="Band gap")
	public int bandGap = 64;
	@Tunable(description="Left edge margin")
	public int leftEdge = 32;
	@Tunable(description="Top edge margin")
	public int topEdge = 32;
	@Tunable(description="Right edge margin")
	public int rightMargin = 7000;

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}
	
}
