/* %% Ignore-License */
/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.cytoscape.ding.impl.strokes;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;


public class ZigzagStroke implements WidthStroke {

	private float amplitude = 10.0f;
	private float wavelength = 10.0f;
	private Stroke stroke;
	private static final float FLATNESS = 1;
	private float width;

	// TODO we can do fancier stuff if we pass in Stroke, amplitude and
	// wavelength
	// as params
	public ZigzagStroke(float width) {
		this.width = width;
		this.stroke = new BasicStroke(width);
	}

	public Shape createStrokedShape(Shape shape) {
		GeneralPath result = new GeneralPath();
		PathIterator it = new FlatteningPathIterator(
				shape.getPathIterator(null), FLATNESS);
		float points[] = new float[6];
		float moveX = 0, moveY = 0;
		float lastX = 0, lastY = 0;
		float thisX = 0, thisY = 0;
		int type = 0;
		boolean first = false;
		float next = 0;
		int phase = 0;

		float factor = 1;

		while (!it.isDone()) {
			type = it.currentSegment(points);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				moveX = lastX = points[0];
				moveY = lastY = points[1];
				result.moveTo(moveX, moveY);
				first = true;
				next = wavelength / 2;
				break;

			case PathIterator.SEG_CLOSE:
				points[0] = moveX;
				points[1] = moveY;
				// Fall into....

			case PathIterator.SEG_LINETO:
				thisX = points[0];
				thisY = points[1];
				float dx = thisX - lastX;
				float dy = thisY - lastY;
				float distance = (float) Math.sqrt(dx * dx + dy * dy);
				if (distance >= next) {
					float r = 1.0f / distance;
					float angle = (float) Math.atan2(dy, dx);
					while (distance >= next) {
						float x = lastX + next * dx * r;
						float y = lastY + next * dy * r;
						float tx = amplitude * dy * r;
						float ty = amplitude * dx * r;
						if ((phase & 1) == 0)
							result.lineTo(x + amplitude * dy * r, y - amplitude
									* dx * r);
						else
							result.lineTo(x - amplitude * dy * r, y + amplitude
									* dx * r);
						next += wavelength;
						phase++;
					}
				}
				next -= distance;
				first = false;
				lastX = thisX;
				lastY = thisY;
				if (type == PathIterator.SEG_CLOSE)
					result.closePath();
				break;
			}
			it.next();
		}

		return stroke.createStrokedShape(result);
	}

	public WidthStroke newInstanceForWidth(float w) {
		return new ZigzagStroke(w);
	}

	@Override public String toString() {
		return this.getClass().getSimpleName() + " " + Float.toString(width);
	}

}
