package csplugins.layout.algorithms.circularLayout;


import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.cytoscape.work.undo.UndoSupport;


public class CircularLayoutAlgorithm extends AbstractLayoutAlgorithm implements TunableValidator {
	
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

	/**
	 * Creates a new Layout object.
	 */
	public CircularLayoutAlgorithm(UndoSupport un) {
		super(un, "circular", "Circular Layout", false);
	}

	@Override //TODO how to validate these values?
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}

	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
				new CircularLayoutAlgorithmTask(
						networkView, getName(), selectedOnly, singlePartition, staticNodes));
	}
}
