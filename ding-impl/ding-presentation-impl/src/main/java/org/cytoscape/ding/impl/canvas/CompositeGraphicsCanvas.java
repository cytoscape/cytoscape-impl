package org.cytoscape.ding.impl.canvas;

import static org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID.BACKGROUND;
import static org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID.FOREGROUND;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.work.NoOutputProgressMonitor;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

/**
 * A single use graphics canvas meant for drawing vector graphics for image and PDF export.
 */
public class CompositeGraphicsCanvas {

	public static void paint(Graphics2D graphics, DRenderingEngine re, Color bgPaint, GraphLOD lod, NetworkTransform t) {
		var transform = new NetworkGraphicsTransform(graphics, t);
		var snapshot = re.getViewModelSnapshot();
		var flags = RenderDetailFlags.create(snapshot, transform, lod);
		var pm = new NoOutputProgressMonitor();
		
		var canvasList = Arrays.asList(
			new AnnotationCanvas<>(transform, FOREGROUND, re),
			new NodeCanvas<>(transform, re),
			new EdgeCanvas<>(transform, re),
			new AnnotationCanvas<>(transform, BACKGROUND, re),
			new ColorCanvas<>(transform, bgPaint)
		);
		Collections.reverse(canvasList);
		
		canvasList.forEach(c -> c.paint(pm, flags));
	}
}
