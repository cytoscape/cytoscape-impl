package csapps.layout.algorithms.circularLayout;

import org.cytoscape.view.layout.AbstractLayoutContext;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class CircularLayoutContext extends AbstractLayoutContext implements TunableValidator {
	
	//TODO: these are not used in current implementations.
	
	@Tunable(description="Horizontal spacing between nodes")
	public int nodeHorizontalSpacing = 64;
	@Tunable(description="Vertical spacing between nodes")
	public int nodeVerticalSpacing = 32;
	@Tunable(description="Left edge margin")
	public int leftEdge = 32;
	@Tunable(description="Top edge margin")
	public int topEdge = 32;
	@Tunable(description="Right edge margin")
	public int rightMargin = 1000;
    @Tunable(description="Don't partition graph before layout", groups="Standard settings")
	public boolean singlePartition;

	@Override //TODO how to validate these values?
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}
}
