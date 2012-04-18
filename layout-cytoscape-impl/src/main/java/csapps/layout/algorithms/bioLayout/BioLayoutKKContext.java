package csapps.layout.algorithms.bioLayout;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class BioLayoutKKContext extends BioLayoutContext implements TunableValidator {
	/**
	 * The average number of iterations per Node
	 */
	@Tunable(description="Average number of iteratations for each node")
	public double m_averageIterationsPerNode = 40;
	@Tunable(description="Spring strength")
	public double m_nodeDistanceStrengthConstant=15.0;
	@Tunable(description="Spring rest length")
	public double m_nodeDistanceRestLengthConstant=45.0;
	@Tunable(description="Strength of a 'disconnected' spring")
	public double m_disconnectedNodeDistanceSpringStrength=0.05;
	@Tunable(description="Rest length of a 'disconnected' spring")
	public double m_disconnectedNodeDistanceSpringRestLength=2000.0;
	@Tunable(description="Strength to apply to avoid collisions")
	public double m_anticollisionSpringStrength;
	@Tunable(description="Number of layout passes")
	public int m_layoutPass = 2;
	@Tunable(description="Don't partition graph before layout", groups="Standard settings")
	public boolean singlePartition;

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}

}
