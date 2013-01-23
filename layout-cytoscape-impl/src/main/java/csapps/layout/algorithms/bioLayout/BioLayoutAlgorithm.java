package csapps.layout.algorithms.bioLayout;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
