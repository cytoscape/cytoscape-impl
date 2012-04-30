package csapps.layout.algorithms.bioLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.undo.UndoSupport;

/**
 * Superclass for the two bioLayout algorithms (KK and FR).
 *
 * @author <a href="mailto:scooter@cgl.ucsf.edu">Scooter Morris</a>
 * @version 0.9
 */
public abstract class BioLayoutAlgorithm extends AbstractLayoutAlgorithm {

	/**
	 * Value to set for doing unweighted layouts
	 */
	public static final String UNWEIGHTEDATTRIBUTE = "(unweighted)";

	final boolean supportWeights; 

	public BioLayoutAlgorithm(String computerName, String humanName, boolean supportWeights, UndoSupport undo) {
		super(computerName, humanName, undo);
		this.supportWeights = supportWeights;
	}

	
	@Override
	public Set<Class<?>> getSupportedEdgeAttributeTypes() {
		Set<Class<?>> ret = new HashSet<Class<?>>();
		if (!supportWeights)
			return ret;

		ret.add( Integer.class );
		ret.add( Double.class );

		return ret;
	}

	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
