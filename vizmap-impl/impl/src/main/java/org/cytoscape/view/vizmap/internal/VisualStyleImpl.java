/*
 Copyright (c) 2008, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.view.vizmap.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class VisualStyleImpl implements VisualStyle {

	private static final Logger logger = LoggerFactory.getLogger(VisualStyleImpl.class);

	private static final String DEFAULT_TITLE = "?";

	private final Map<VisualProperty<?>, VisualMappingFunction<?, ?>> mappings;
	private final Map<VisualProperty<?>, Object> styleDefaults;

	private final VisualLexiconManager lexManager;
	
	private final Map<Class<? extends CyIdentifiable>, ApplyHandler> applyHandlersMap;

	private String title;

	/**
	 * 
	 * @param title Title of the new Visual Style
	 * @param lexManager
	 */
	public VisualStyleImpl(final String title, final VisualLexiconManager lexManager) {

		if (lexManager == null)
			throw new NullPointerException("Lexicon Manager is missing.");

		this.lexManager = lexManager;

		if (title == null)
			this.title = DEFAULT_TITLE;
		else
			this.title = title;

		mappings = new HashMap<VisualProperty<?>, VisualMappingFunction<?, ?>>();
		styleDefaults = new HashMap<VisualProperty<?>, Object>();
		
		// Init Apply handlers for node, egde and network.
		this.applyHandlersMap = new HashMap<Class<? extends CyIdentifiable>, ApplyHandler>();
		applyHandlersMap.put(CyNetwork.class, new ApplyToNetworkHandler(this, lexManager));
		applyHandlersMap.put(CyNode.class, new ApplyToNodeHandler(this, lexManager));
		applyHandlersMap.put(CyEdge.class, new ApplyToEdgeHandler(this, lexManager));

		logger.info("New Visual Style Created: Style Name = " + this.title);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addVisualMappingFunction(final VisualMappingFunction<?, ?> mapping) {
		mappings.put(mapping.getVisualProperty(), mapping);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <V> VisualMappingFunction<?, V> getVisualMappingFunction(VisualProperty<V> t) {
		return (VisualMappingFunction<?, V>) mappings.get(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeVisualMappingFunction(VisualProperty<?> t) {
		mappings.remove(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <V> V getDefaultValue(final VisualProperty<V> vp) {
		return (V) styleDefaults.get(vp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <V, S extends V> void setDefaultValue(final VisualProperty<V> vp, final S value) {
		styleDefaults.put(vp, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void apply(final View<? extends CyIdentifiable> view) {
		if (view == null) {
			logger.warn("Tried to apply Visual Style to null view");
			return;
		}
	
		final long start = System.currentTimeMillis();
		
		ApplyHandler handler = null;
		for(final Class<?> viewType: applyHandlersMap.keySet()) {
			if(viewType.isAssignableFrom(view.getModel().getClass())) {
				handler = this.applyHandlersMap.get(viewType);
				break;
			}
		}
		
		if(handler==null)
			throw new IllegalArgumentException("This view type is not supported: " + view.getClass());
		
		handler.apply(view);
		
		logger.info(title + ": Visual Style applied in " + (System.currentTimeMillis() - start) + " msec.");
	}
	
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public void apply(final View<? extends CyIdentifiable> view) {
//		if (view == null) {
//			logger.warn("Tried to apply Visual Style to null view");
//			return;
//		}
//		
//		// First, apply default values
//		for ( VisualProperty<?> vp : vps ) {
//			Object defaultValue = getDefaultValue(vp);
//
//			if (defaultValue == null) {
//				this.perVSDefaults.put(vp, vp.getDefault());
//				defaultValue = getDefaultValue(vp);
//			}
//
//			view.setViewDefault(vp,defaultValue);
//		}
//	}
	
	
	private void applyImpl(final CyNetworkView networkView, final Collection<? extends View<?>> views,
			final Collection<VisualProperty<?>> visualProperties) {
		
		for (VisualProperty<?> vp : visualProperties)
					applyToView(networkView, views, vp);
	}

	private void applyToView(final CyNetworkView networkView, final Collection<? extends View<?>> views, final VisualProperty<?> vp) {

		final VisualMappingFunction<?, ?> mapping = getVisualMappingFunction(vp);

		if (mapping != null) {

			// Default of this style
			final Object styleDefaultValue = getDefaultValue(vp);
			// Default of this Visual Property
			final Object vpDefault = vp.getDefault();
			final CyNetwork net = networkView.getModel();

			for (View<?> v : views) {
				View<? extends CyIdentifiable> view = (View<? extends CyIdentifiable>)v;
				mapping.apply( net.getRow( view.getModel() ), view);
				
				if (view.getVisualProperty(vp) == vpDefault)
					view.setVisualProperty(vp, styleDefaultValue);
			}
		} 
	}

	private void applyViewDefaults(final CyNetworkView view, final Collection<VisualProperty<?>> vps) {

		for ( VisualProperty<?> vp : vps ) {
			Object defaultValue = getDefaultValue(vp);

			if (defaultValue == null) {
				this.styleDefaults.put(vp, vp.getDefault());
				defaultValue = getDefaultValue(vp);
			}

			view.setViewDefault(vp,defaultValue);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.title;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<VisualMappingFunction<?, ?>> getAllVisualMappingFunctions() {
		return mappings.values();
	}
	
	
	Map<VisualProperty<?>, Object> getStyleDefaults() {
		return this.styleDefaults;
	}
	
}
