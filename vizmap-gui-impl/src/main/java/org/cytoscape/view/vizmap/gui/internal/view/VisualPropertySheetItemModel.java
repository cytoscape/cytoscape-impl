package org.cytoscape.view.vizmap.gui.internal.view;

import javax.swing.Icon;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.model.LockedValueState;
import org.jdesktop.swingx.icon.EmptyIcon;

public class VisualPropertySheetItemModel<T> extends AbstractVizMapperModel {

	private final VisualProperty<T> visualProperty;
	private VisualPropertyDependency<T> dependency;
	private final String title;
	private final VisualStyle style;
	private final RenderingEngine<?> engine;
	private final VisualLexicon lexicon;
	private T lockedValue;
	private LockedValueState lockedValueState = LockedValueState.DISABLED;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VisualPropertySheetItemModel(final VisualProperty<T> visualProperty,
										final VisualStyle style,
										final RenderingEngine<?> engine,
										final VisualLexicon lexicon) {
		if (visualProperty == null)
			throw new IllegalArgumentException("'visualProperty' must not be null");
		if (style == null)
			throw new IllegalArgumentException("'style' must not be null");
		if (lexicon == null)
			throw new IllegalArgumentException("'lexicon' must not be null");
		
		this.visualProperty = visualProperty;
		this.style = style;
		this.engine = engine;
		this.lexicon = lexicon;
		
		title = createTitle(visualProperty);
	}

	public VisualPropertySheetItemModel(final VisualPropertyDependency<T> dependency,
										final VisualStyle style,
										final RenderingEngine<?> engine,
										final VisualLexicon lexicon) {
		if (dependency == null)
			throw new IllegalArgumentException("'dependency' must not be null");
		if (style == null)
			throw new IllegalArgumentException("'style' must not be null");
		if (lexicon == null)
			throw new IllegalArgumentException("'lexicon' must not be null");

		this.dependency = dependency;
		this.visualProperty = dependency.getParentVisualProperty();
		this.style = style;
		this.engine = engine;
		this.lexicon = lexicon;
		
		title = dependency.getDisplayName();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public VisualProperty<T> getVisualProperty() {
		return visualProperty;
	}
	
	public VisualPropertyDependency<T> getVisualPropertyDependency() {
		return dependency;
	}
	
	public String getTitle() {
		return title;
	}
	
	public T getDefaultValue() {
		return style.getDefaultValue(visualProperty);
	}
	
	public T getLockedValue() {
		return lockedValue;
	}
	
	public void setLockedValue(final T value) {
		if (value != lockedValue) {
			T oldValue = lockedValue;
			lockedValue = value;
			propChangeSupport.firePropertyChange("lockedValue", oldValue, value);
		}
	}
	
	public LockedValueState getLockedValueState() {
		return lockedValueState;
	}
	
	public void setLockedValueState(final LockedValueState state) {
		if (state != lockedValueState) {
			LockedValueState oldValue = lockedValueState;
			lockedValueState = state;
			propChangeSupport.firePropertyChange("lockedValueState", oldValue, state);
		}
	}

	public VisualStyle getVisualStyle() {
		return style;
	}

	public RenderingEngine<?> getRenderingEngine() {
		return engine;
	}

	public VisualLexicon getVisualLexicon() {
		return lexicon;
	}
	
	public boolean isDependencyEnabled() {
		return getVisualPropertyDependency() != null && getVisualPropertyDependency().isDependencyEnabled();
	}
	
	public VisualMappingFunction<?, T> getVisualMappingFunction() {
		return getVisualStyle().getVisualMappingFunction(getVisualProperty());
	}

	public Class<? extends CyIdentifiable> getTargetDataType() {
		return getVisualProperty().getTargetDataType();
	}
	
	public Icon getDefaultValueIcon(final int width, final int height) {
		return getIcon(getDefaultValue(), width, height);
	}
	
	public Icon getLockedValueIcon(final int width, final int height) {
		return getIcon(getLockedValue(), width, height);
	}
	
	public boolean isVisualMappingAllowed() {
		return getTargetDataType() != CyNetwork.class && getVisualPropertyDependency() == null;
	}
	
	public boolean isLockedValueAllowed() {
		return getVisualPropertyDependency() == null;
	}

	public static String createTitle(final VisualProperty<?> vp) {
		return vp.getDisplayName().replaceFirst(vp.getTargetDataType().getSimpleName().replace("Cy", ""), "").trim();
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Icon getIcon(final T value, final int width, final int height) {
		Icon icon = null;
		
		if (getRenderingEngine() != null && value != null)
			icon = getRenderingEngine().createIcon((VisualProperty) getVisualProperty(), value, width, height);
		
		if (icon == null)
			icon = new EmptyIcon(width, height);
		
		return icon;
	}
}
