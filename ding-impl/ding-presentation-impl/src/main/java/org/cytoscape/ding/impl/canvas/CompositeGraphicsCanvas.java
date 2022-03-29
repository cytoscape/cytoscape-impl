package org.cytoscape.ding.impl.canvas;

import static org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID.BACKGROUND;
import static org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID.FOREGROUND;
import static org.cytoscape.graph.render.stateful.RenderDetailFlags.OPT_PDF_FONT_HACK;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.work.NoOutputProgressMonitor;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.presentation.annotations.Annotation;

/**
 * A single use graphics canvas meant for drawing vector graphics for image and PDF export.
 */
public class CompositeGraphicsCanvas {

	public static void paint(
			Graphics2D graphics, 
			Color bgPaint, 
			GraphLOD lod, 
			NetworkTransform transform, 
			DRenderingEngine re, 
			boolean pdfFontHack
	) {
		var g = new SimpleGraphicsProvider(transform, graphics);
		var snapshot = re.getViewModelSnapshot();
		var pm = new NoOutputProgressMonitor();
		
		var flags = RenderDetailFlags.create(snapshot, transform, lod, null);
		if(pdfFontHack) {
			flags = flags.add(OPT_PDF_FONT_HACK);
		}
		
		var canvasList = Arrays.asList(
			new AnnotationCanvas<>(g, re, FOREGROUND, false),
			new NodeCanvas<>(g, re),
			new EdgeCanvas<>(g, re),
			new AnnotationCanvas<>(g, re, BACKGROUND, false)
		);
		Collections.reverse(canvasList);
		
		g.fill(bgPaint);
		
		for(var canvas : canvasList)
			canvas.paint(pm, flags);
	}
	
	
	public static void paintThumbnail(
			Graphics2D graphics, 
			Color bgPaint, 
			GraphLOD lod, 
			NetworkTransform transform, 
			CyNetworkViewSnapshot snapshot, 
			Collection<Annotation> annotations
	) {
		var g = new SimpleGraphicsProvider(transform, graphics);
		var flags = RenderDetailFlags.create(snapshot, transform, lod, null);
		var pm = new NoOutputProgressMonitor();
		
		List<Annotation> foreground = new ArrayList<>();
		List<Annotation> background = new ArrayList<>();

		if(annotations != null) {
			for(var a : annotations) {
				if(Annotation.FOREGROUND.equals(a.getCanvasName()))
					foreground.add(a);
				if(Annotation.BACKGROUND.equals(a.getCanvasName()))
					background.add(a);
			}
		}
		
		var canvasList = Arrays.asList(
			new AnnotationThumbnailCanvas<>(g, foreground),
			new NodeThumbnailCanvas<>(g, snapshot),
			new EdgeThumbnailCanvas<>(g, snapshot),
			new AnnotationThumbnailCanvas<>(g, background)
		);
		Collections.reverse(canvasList);
		
		g.fill(bgPaint);
		
		for(var canvas : canvasList)
			canvas.paint(pm, flags);
	}
	
}
