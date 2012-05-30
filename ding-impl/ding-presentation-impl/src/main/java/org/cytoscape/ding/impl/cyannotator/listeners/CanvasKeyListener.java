package org.cytoscape.ding.impl.cyannotator.listeners;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.Set;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;

public class CanvasKeyListener implements KeyListener {
	private final CyAnnotator cyAnnotator;
	private final InnerCanvas networkCanvas;
	private final DGraphView view;

	public CanvasKeyListener(CyAnnotator c, DGraphView view) {
		this.cyAnnotator = c;
		this.view = view;
		this.networkCanvas = view.getCanvas();
	}

	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		Set<Annotation> selectedAnnotations = cyAnnotator.getSelectedAnnotations();

		if ((selectedAnnotations != null && selectedAnnotations.size() > 0) &&
		    ((code == KeyEvent.VK_UP) || 
		    (code == KeyEvent.VK_DOWN) || 
		    (code == KeyEvent.VK_LEFT)|| 
		    (code == KeyEvent.VK_RIGHT)))
		{
			//Some annotations have been double clicked and selected
			int move=2;
			for (Annotation annotation: selectedAnnotations) {
				Component c = annotation.getComponent();
				int x=c.getX(), y=c.getY();
				if (code == KeyEvent.VK_UP)
					y-=move;
				else if (code == KeyEvent.VK_DOWN)
					y+=move;
				else if (code == KeyEvent.VK_LEFT)
					x-=move;
				else if (code == KeyEvent.VK_RIGHT)
					x+=move;

				//Adjust the locations of the selected annotations
				annotation.getComponent().setLocation(x,y);
			}
			view.updateView();	
		} else {
			networkCanvas.keyPressed(e);
		}
	}

	public void keyReleased(KeyEvent e) { }

	public void keyTyped(KeyEvent e) { }
}
