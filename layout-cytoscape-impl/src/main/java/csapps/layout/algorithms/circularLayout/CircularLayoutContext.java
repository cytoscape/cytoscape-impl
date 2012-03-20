package csapps.layout.algorithms.circularLayout;

import java.util.Set;

import org.cytoscape.view.layout.AbstractLayoutAlgorithmContext;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class CircularLayoutContext extends AbstractLayoutAlgorithmContext implements TunableValidator {
	
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

	public CircularLayoutContext(boolean supportsSelectedOnly, Set<Class<?>> supportedNodeAttributes, Set<Class<?>> supportedEdgeAttributes) {
		super(supportsSelectedOnly, supportedNodeAttributes, supportedEdgeAttributes);
	}

	@Override //TODO how to validate these values?
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}
}
