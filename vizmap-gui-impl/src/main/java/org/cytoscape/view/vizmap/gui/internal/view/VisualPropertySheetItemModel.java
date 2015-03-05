package org.cytoscape.view.vizmap.gui.internal.view;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.model.LockedValueState;

public class VisualPropertySheetItemModel<T> extends AbstractVizMapperModel {

	private final VisualProperty<T> visualProperty;
	private VisualPropertyDependency<T> dependency;
	private final String title;
	private final VisualStyle style;
	private RenderingEngine<?> engine;
	private final VisualLexicon lexicon;
	private String mappingColumnName;
	private T defaultValue;
	private T lockedValue;
	private VisualMappingFunction<?, T> visualMappingFunction;
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

		defaultValue = style.getDefaultValue(visualProperty);
		setVisualMappingFunction(style.getVisualMappingFunction(visualProperty));

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

	public String getId() {
		return dependency != null ? dependency.getIdString() : visualProperty.getIdString();
	}

	public Class<? extends CyIdentifiable> getTargetDataType() {
		return getVisualProperty().getTargetDataType();
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(final T value) {
		if ((value == null && defaultValue != null) || (value != null && !value.equals(defaultValue)))
			propChangeSupport.firePropertyChange("defaultValue", defaultValue, defaultValue = value);
	}

	public T getLockedValue() {
		return lockedValue;
	}
	
	public void resetDefaultValue() {
		setDefaultValue(getVisualProperty().getDefault());
	}

	public void setLockedValue(final T value) {
		if ((value == null && lockedValue != null) || (value != null && (!value.equals(lockedValue)||value instanceof Bend) ))
		{
			//TODO: This seems like a sort of hack. To prevent firePropertyChange from being suppressed in the case of a Bend, pretend like the old value was null.
			if( value instanceof Bend)
				propChangeSupport.firePropertyChange("lockedValue", null, lockedValue = value);
			else
				propChangeSupport.firePropertyChange("lockedValue", lockedValue, lockedValue = value);
		}
	}

	public LockedValueState getLockedValueState() {
		return lockedValueState;
	}

	public void setLockedValueState(final LockedValueState state) {
		if (state != lockedValueState)
			propChangeSupport.firePropertyChange("lockedValueState", lockedValueState, lockedValueState = state);
	}

	public VisualStyle getVisualStyle() {
		return style;
	}
	
	public RenderingEngine<?> getRenderingEngine() {
		return engine;
	}
	
	public void setRenderingEngine(final RenderingEngine<?> engine) {
		if (engine != this.engine)
			propChangeSupport.firePropertyChange("renderingEngine", engine, this.engine = engine);
	}

	public VisualLexicon getVisualLexicon() {
		return lexicon;
	}
	
	public boolean isDependencyEnabled() {
		return getVisualPropertyDependency() != null && getVisualPropertyDependency().isDependencyEnabled();
	}
	
	public VisualMappingFunction<?, T> getVisualMappingFunction() {
		return visualMappingFunction;
	}
	
	public void setVisualMappingFunction(final VisualMappingFunction<?, T> mapping) {
		if (isVisualMappingAllowed() && (mapping == null || mapping.getVisualProperty() == visualProperty)) {
			if (mapping != visualMappingFunction)
				propChangeSupport.firePropertyChange("visualMappingFunction", visualMappingFunction,
						visualMappingFunction = mapping);
		}
		
		setMappingColumnName(mapping == null ? null : mapping.getMappingColumnName(), false);
	}

	public String getMappingColumnName() {
		return mappingColumnName;
	}
	
	public void setMappingColumnName(final String name) {
		setMappingColumnName(name, true);
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

	private void setMappingColumnName(final String name, boolean updateRelatedProperties) {
		if ((name == null && mappingColumnName != null) || (name != null && !name.equals(mappingColumnName))) {
			mappingColumnName = name;
			
			if (updateRelatedProperties)
				setVisualMappingFunction(null);
			
			propChangeSupport.firePropertyChange("mappingColumnName", mappingColumnName, name);
		}
	}

	@SuppressWarnings("unchecked")
	public void update(final RenderingEngine<CyNetwork> engine) {
		setRenderingEngine(engine);
		
		if (dependency == null) {
			setDefaultValue(style.getDefaultValue(visualProperty));
			setVisualMappingFunction(style.getVisualMappingFunction(visualProperty));
		} else {
			for (final VisualPropertyDependency<?> dep : style.getAllVisualPropertyDependencies()) {
				if (dep.getIdString().equals(dependency.getIdString())
						&& dep.getParentVisualProperty().equals(dependency.getParentVisualProperty())) {
					dependency = (VisualPropertyDependency<T>) dep;
					break;
				}
			}
		}
	}
}
