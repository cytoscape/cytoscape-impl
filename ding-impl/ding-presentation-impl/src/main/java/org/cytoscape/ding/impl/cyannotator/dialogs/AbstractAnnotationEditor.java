package org.cytoscape.ding.impl.cyannotator.dialogs;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.ding.impl.cyannotator.annotations.AbstractAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.utils.EnhancedSlider;
import org.cytoscape.ding.impl.undo.AnnotationEdit;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;

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

@SuppressWarnings("serial")
public abstract class AbstractAnnotationEditor<T extends Annotation> extends JPanel implements PropertyChangeListener {
	
	protected T annotation;
	protected boolean adjusting;
	protected boolean applying;
	
	protected final AnnotationFactory<T> factory;
	protected final CyServiceRegistrar serviceRegistrar;
	
	protected AbstractAnnotationEditor(AnnotationFactory<T> factory, CyServiceRegistrar serviceRegistrar) {
		this.factory = factory;
		this.serviceRegistrar = serviceRegistrar;
		
		setOpaque(!isAquaLAF());
		
		init();
		update();
	}
	
	public AnnotationFactory<T> getAnnotationFactory() {
		return factory;
	}
	
	/**
	 * Sets the annotation to be edited. When a different annotation is set, this method will call {@link #update()}
	 * to refresh the UI.
	 *  
	 * @param annotation The annotation to be edited or null. 
	 */
	public void setAnnotation(T annotation) {
		if (!Objects.equals(this.annotation, annotation)) {
			if (this.annotation instanceof DingAnnotation)
				((DingAnnotation) this.annotation).removePropertyChangeListener(this);
			
			if (annotation instanceof DingAnnotation)
				((DingAnnotation) annotation).addPropertyChangeListener(this);
			
			this.annotation = annotation;
			update();
		}
	}
	
	/**
	 * @return The annotation being edited.
	 */
	public T getAnnotation() {
		return annotation;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!applying)
			update(); // Update the form if the current annotation's property values changed
	}
	
	/**
	 * Calls {@link #doUpdate()} but first set the {@link #adjusting} flag to <code>true</code>.
	 */
	protected void update() {
		adjusting = true;
		
		try {
			doUpdate();
		} finally {
			adjusting = false;
		}
	}
	
	/**
	 * This must update the editor's components. Remember that the {@link #annotation} may be null.
	 */
	protected abstract void doUpdate();
	
	/**
	 * Apply all the UI values to the current annotation, if there is one.<br>
	 * The subclass should call this method every time the user changes a style, such as the border color,
	 * so the change is applied to the current annotation right away.
	 */
	protected void apply(boolean postUndo) {
		// Ignore here...
	}
	
	protected void apply() {
		if (annotation != null && !adjusting) {
			applying = true;
			
			AnnotationEdit edit = null;
			if (annotation instanceof AbstractAnnotation) {
				String label = factory == null ? "Edit Annotation" : "Edit " + factory.getName() + " Annotation";
				var re = ((AbstractAnnotation) annotation).getRenderingEngine();
				edit = new AnnotationEdit(label, re);
			}
			
			try {
				apply(annotation);
				
				if (edit != null) {
					edit.post();
				}
			} finally {
				applying = false;
			}
		}
	}
	
	/**
	 * Apply all the current UI values to the passed annotation.<br>
	 * This does not call {@link #setAnnotation(Annotation)}.
	 */
	public abstract void apply(T annotation);
	
	/**
	 * Initialize the visual components.
	 */
	protected abstract void init();
	
	protected static JLabel createRotationLabel() {
		var label = new JLabel("Rotation Angle:");
		
		return label;
	}
	
	/**
	 * The the slider's ChangeEvent will call {@link #apply()} by default. 
	 */
	protected EnhancedSlider createRotationSlider() {
		var slider = new EnhancedSlider(-180, 180, 0, 90, 45);
		slider.addChangeListener(evt -> apply());
		
		return slider;
	}
}
