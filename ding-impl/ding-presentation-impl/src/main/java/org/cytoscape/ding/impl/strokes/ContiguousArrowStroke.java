

package org.cytoscape.ding.impl.strokes;

import java.awt.Shape;
import java.awt.geom.GeneralPath;


public class ContiguousArrowStroke extends ShapeStroke {

	public ContiguousArrowStroke(float width) {
		super( new Shape[] { getArrowStroke(width) }, 3f*width, width );
	}

	public WidthStroke newInstanceForWidth(float w) {
		return new ContiguousArrowStroke(w);
	}

	private static Shape getArrowStroke(final float width) {
		GeneralPath shape = new GeneralPath();

		// change these to change the arrow proportions 

		// length of shape
		float length = 3f*width;

		// width of center line
		float lineWidth = width;

		// width of arrow, from one arm to its opposite 
		float arrowWidth = 4f*width;

		// thickness of the arrow arm relative to the
		// center line width
		float arrowArmBreadthFactor = 0.5f;

		// ====================================================================
		// don't change these - they should always stay
		// the same for this shape!
		float begin = 0f;
		float halfLineWidth = 0.5f * lineWidth;
		float halfArrowWidth = 0.5f * arrowWidth;
		float arrowArmBreadth = arrowArmBreadthFactor*lineWidth;
		float arrowArmStart = (length/2f) - (arrowArmBreadth/2f);
		float arrowArmEnd = (length/2f) + (arrowArmBreadth/2f);

		// make the actual shape
		//             X                  Y
		shape.moveTo( begin,            halfLineWidth);
		shape.lineTo( arrowArmStart,    halfLineWidth);
		shape.lineTo( begin,            halfArrowWidth);
		shape.lineTo( arrowArmBreadth,  halfArrowWidth);
		shape.lineTo( arrowArmEnd,      halfLineWidth);
		shape.lineTo( length,           halfLineWidth);
		shape.lineTo( length,          -halfLineWidth);
		shape.lineTo( arrowArmEnd,     -halfLineWidth);
		shape.lineTo( arrowArmBreadth, -halfArrowWidth);
		shape.lineTo( begin,           -halfArrowWidth);
		shape.lineTo( arrowArmStart,   -halfLineWidth);
		shape.lineTo( begin,           -halfLineWidth);
		shape.lineTo( begin,            halfLineWidth);

		return shape;
	}
}


