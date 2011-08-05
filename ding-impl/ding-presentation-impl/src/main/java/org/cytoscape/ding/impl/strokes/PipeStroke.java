package org.cytoscape.ding.impl.strokes;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

public class PipeStroke extends ShapeStroke {

	public enum Type {
		VERTICAL(0f), FORWARD(-1f), BACKWARD(1f), ;

		private float adjust;

		private Type(float adjust) {
			this.adjust = adjust;
		}

		public float adjust(float input) {
			return adjust * input;
		}
	}

	private Type offsetType;

	PipeStroke(float width, Type offsetType) {
		super(new Shape[] { getShape(width, offsetType) }, width,
				width);
		this.offsetType = offsetType;
		
		//TODO: is this necessary?
		//this.lineStyle = lineStyle;
	}

	public WidthStroke newInstanceForWidth(float w) {
		return new PipeStroke(w, offsetType);
	}

	private static Shape getShape(final float input, final Type offsetType) {
		GeneralPath shape = new GeneralPath();
		float height = input;
		float width = input / 5f;
		float offset = offsetType.adjust(input);

		shape.moveTo(0f, -height);
		shape.lineTo(width, -height);
		shape.lineTo(width + offset, height);
		shape.lineTo(0f + offset, height);
		shape.lineTo(0f, -height);

		return shape;
	}
}
