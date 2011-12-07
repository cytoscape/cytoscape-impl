package csapps.layout.algorithms.graphPartition;


import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.undo.UndoSupport;


public class DegreeSortedCircleLayout extends AbstractLayoutAlgorithm implements TunableValidator {
	private static final String DEGREE_ATTR_NAME = "degree";

	@Tunable(description="Don't partition graph before layout", groups="Standard settings")
	public boolean singlePartition;

	/**
	 * Creates a new DegreeSortedCircleLayout object.
	 */
	public DegreeSortedCircleLayout(UndoSupport undoSupport) {
		super(undoSupport, "degree-circle", "Degree Sorted Circle Layout", true);
	}

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new DegreeSortedCircleLayoutTask(networkView, getName(), selectedOnly,
							 staticNodes, DEGREE_ATTR_NAME, singlePartition));
	}
}
