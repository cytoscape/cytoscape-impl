package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.border.TitledBorder;

import java.awt.Component;

import org.cytoscape.ding.impl.cyannotator.api.Annotation;

public class PreviewPanel extends javax.swing.JPanel {
	Annotation mAnnotation;

	public PreviewPanel(Annotation annotation, int width, int height) {
		this.mAnnotation=annotation;
		Component c = mAnnotation.getComponent();
		mAnnotation.setUsedForPreviews(true);
		
		// System.out.println("Panel size = "+width+"x"+height);
		// System.out.println("Annotation size = "+c.getWidth()+"x"+c.getHeight());
		c.setLocation((width-c.getWidth())/2+5,(height-c.getHeight())/2+5);

		// Border it
		TitledBorder title = BorderFactory.createTitledBorder("Preview");
		setBorder(title);
		setLayout(null);

		// Create the preview panel
		add(c);
		setBounds(10,10,width,height);
		repaint();
	}

	public Annotation getPreviewAnnotation() {
		return mAnnotation;
	}
}
