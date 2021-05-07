package org.cytoscape.ding.impl.strokes;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import java.awt.Shape;
import java.awt.geom.GeneralPath;

public class SineWaveStroke extends ShapeStroke {
	
	private static final float wavelength = 20f;
	private static final float amplitude = 10f;

	public SineWaveStroke(float width) {
		// second arg here is the advance - advance must equal wavelength below
		super( new Shape[] { getSineWave(width) }, 
		       wavelength, width );
	}

	public WidthStroke newInstanceForWidth(float w) {
		return new SineWaveStroke(w);
	}

	private static Shape getSineWave(final float width) {
		GeneralPath shape = new GeneralPath();

		// wavelength must equal advance specified in constructor or 
		// else the waves won't line up!
		//FIXME
//		final float wavelength = PropUtil.getFloat( CytoscapeInit.getProperties(), 
//		                                            "SineWaveStroke.wavelength", 10f ); 
//		final float amplitude = PropUtil.getFloat( CytoscapeInit.getProperties(), 
//		                                           "SineWaveStroke.amplitude",5f ); 

		shape.moveTo(0f,0f);
		shape.lineTo(0f,width);
		shape.quadTo(0.25f*wavelength,amplitude+width,   0.5f*wavelength,width);
		shape.quadTo(0.75f*wavelength,-amplitude-width,      wavelength,width);
		shape.lineTo(wavelength,0f);
		shape.quadTo(0.75f*wavelength,-amplitude-width,   0.5f*wavelength,0f);
		shape.quadTo(0.25f*wavelength,amplitude+width,      0f,0f);

		return shape;
	}
}


