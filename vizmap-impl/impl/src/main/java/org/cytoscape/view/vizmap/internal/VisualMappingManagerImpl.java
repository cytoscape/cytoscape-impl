package org.cytoscape.view.vizmap.internal;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.events.SetCurrentVisualStyleEvent;
import org.cytoscape.view.vizmap.events.VisualStyleAboutToBeRemovedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleAddedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleSetEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class VisualMappingManagerImpl implements VisualMappingManager, SetCurrentNetworkViewListener {
	
	private static final Logger logger = LoggerFactory.getLogger(VisualMappingManagerImpl.class);
	
	// title for the default visual style.
	public static final String DEFAULT_STYLE_NAME = "default";
	
	// Default Style
	private static final Color NETWORK_COLOR = Color.WHITE;
	private static final Color NODE_COLOR = new Color(0x4F, 0x94, 0xCD);
	private static final Color NODE_LABEL_COLOR = Color.BLACK;
	private static final Color EDGE_COLOR = new Color(50, 50, 50);
	private static final Double EDGE_WIDTH = 2d;
	private static final Double NODE_WIDTH = 35d;
	private static final Double NODE_HEIGHT = 35d;
	private static final Color EDGE_LABEL_COLOR = Color.BLACK;

	private VisualStyle defaultStyle;
	private volatile VisualStyle currentStyle;

	private final Map<CyNetworkView, VisualStyle> network2VisualStyleMap;
	private final Set<VisualStyle> visualStyles;

	private final CyEventHelper cyEventHelper;
	private final VisualLexiconManager lexManager;
	private final CyServiceRegistrar serviceRegistrar;

	public VisualMappingManagerImpl(final CyEventHelper eventHelper, final VisualStyleFactory factory,
			final VisualLexiconManager lexManager, final CyServiceRegistrar serviceRegistrar) {
		if (eventHelper == null)
			throw new NullPointerException("'eventHelper' cannot be null");
		if (serviceRegistrar == null)
			throw new NullPointerException("'serviceRegistrar' cannot be null");

		this.cyEventHelper = eventHelper;
		this.lexManager = lexManager;
		this.serviceRegistrar = serviceRegistrar;

		visualStyles = new HashSet<VisualStyle>();
		network2VisualStyleMap = new WeakHashMap<CyNetworkView, VisualStyle>();

		this.defaultStyle = buildGlobalDefaultStyle(factory);
		this.visualStyles.add(defaultStyle);
		this.currentStyle = defaultStyle;
	}
	
	private VisualStyle buildGlobalDefaultStyle(final VisualStyleFactory factory) {
		final VisualStyle defStyle = factory.createVisualStyle(DEFAULT_STYLE_NAME);
		
		defStyle.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, NETWORK_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, NODE_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, NODE_LABEL_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, NODE_WIDTH);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, NODE_HEIGHT);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, EDGE_WIDTH);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_PAINT, EDGE_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR, EDGE_LABEL_COLOR);
		
		return defStyle;
	}

	/**
	 * Returns an associated Visual Style for the View Model.
	 */
	@Override
	public VisualStyle getVisualStyle(CyNetworkView nv) {
		if (nv == null) {
			logger.warn("Attempting to get the visual style for a null network view; " + 
			            "returning the default visual style.");
			return getDefaultVisualStyle();	
		}

		VisualStyle style = network2VisualStyleMap.get(nv);
		// Not registered yet. Provide default style.
		if (style == null) {
			style = getDefaultVisualStyle();
			network2VisualStyleMap.put(nv, style);
		}
		
		return style;
	}

	@Override
	public void setVisualStyle(final VisualStyle vs, final CyNetworkView nv) {
		if (nv == null)
			throw new NullPointerException("Network view is null.");

		boolean changed = false;

		if (vs == null) {
			changed = network2VisualStyleMap.remove(nv) != null;
		} else {
			final VisualStyle previousStyle = network2VisualStyleMap.put(nv, vs);
			changed = !vs.equals(previousStyle);
		}

		if (this.visualStyles.contains(vs) == false)
			this.visualStyles.add(vs);

		if (changed) {
			cyEventHelper.fireEvent(new VisualStyleSetEvent(this, vs, nv));
			final CyApplicationManager appManager = serviceRegistrar.getService(CyApplicationManager.class);
		
			if (appManager != null && nv.equals(appManager.getCurrentNetworkView()))
				setCurrentVisualStyle(vs);
		}
	}

	/**
	 * Remove a {@linkplain VisualStyle} from this manager. This will be called through OSGi service mechanism.
	 */
	@Override
	public void removeVisualStyle(VisualStyle vs) {
		if (vs == null)
			throw new NullPointerException("Visual Style is null.");
		if (vs == defaultStyle)
			throw new IllegalArgumentException("Cannot remove default visual style.");

		logger.info("Visual Style about to be removed from VMM: " + vs.getTitle());
		cyEventHelper.fireEvent(new VisualStyleAboutToBeRemovedEvent(this, vs));
		visualStyles.remove(vs);
		
		// Change current style, if it is the deleted one
		if (currentStyle == vs)
			setCurrentVisualStyle(getDefaultVisualStyle());
		
		// Use default for all views using this vs.
		if (network2VisualStyleMap.values().contains(vs)) {
			for (final CyNetworkView view : network2VisualStyleMap.keySet()) {
				if (network2VisualStyleMap.get(view).equals(vs))
					setVisualStyle(defaultStyle, view);
			}
		}
		
		logger.info("Total Number of VS in VMM after remove = " + visualStyles.size());
	}

	/**
	 * Add a new VisualStyle to this manager. This will be called through OSGi service mechanism.
	 * 
	 * @param vs new Visual Style to be added.
	 */
	@Override
	public void addVisualStyle(final VisualStyle vs) {
		if (vs == null) {
			logger.warn("Tried to add null to VMM.");
			return;
		}

		if (hasDuplicatedTitle(vs)) {
			String newTitle = getSuggestedTitle(vs.getTitle());
			// Update the title
			vs.setTitle(newTitle);
		}

		this.visualStyles.add(vs);
		logger.info("New visual Style registered to VMM: " + vs.getTitle());
		logger.info("Total Number of VS in VMM = " + visualStyles.size());
		
		if (vs.getTitle() != null && vs.getTitle().equals(DEFAULT_STYLE_NAME))
			defaultStyle = vs;

		cyEventHelper.fireEvent(new VisualStyleAddedEvent(this, vs));
	}

	private String getSuggestedTitle(String title) {
		int i = 0;
		String suggesteTitle = title;

		while (true) {
			suggesteTitle = title + "_" + (new Integer(i).toString());
			boolean duplicated = false;

			Iterator<VisualStyle> it = this.getAllVisualStyles().iterator();

			while (it.hasNext()) {
				VisualStyle exist_vs = it.next();

				if (exist_vs.getTitle().equalsIgnoreCase(suggesteTitle)) {
					duplicated = true;
					break;
				}
			}

			if (duplicated) {
				i++;
				continue;
			}

			break;
		}

		return suggesteTitle;
	}
	
	private boolean hasDuplicatedTitle(VisualStyle vs) {
		if (this.getAllVisualStyles().size() == 0)
			return false;

		Iterator<VisualStyle> it = this.getAllVisualStyles().iterator();

		while (it.hasNext()) {
			VisualStyle exist_vs = it.next();

			if (exist_vs.getTitle() == null || vs.getTitle() == null)
				continue;
			if (exist_vs.getTitle().equalsIgnoreCase(vs.getTitle()))
				return true;
		}

		return false;
	}

	@Override
	public Set<VisualStyle> getAllVisualStyles() {
		return visualStyles;
	}

	@Override
	public VisualStyle getDefaultVisualStyle() {
		if (defaultStyle == null)
			throw new IllegalStateException("No rendering engine is available, and cannot create default style.");
		
		return defaultStyle;
	}

	@Override
	public Set<VisualLexicon> getAllVisualLexicon() {
		return lexManager.getAllVisualLexicon();
	}

	@Override
	public VisualStyle getCurrentVisualStyle() {
		return currentStyle;
	}

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		final CyNetworkView view = e.getNetworkView();

		if (view == null)
			return;

		final VisualStyle newStyle = this.getVisualStyle(view);

		if (newStyle != null)
			setCurrentVisualStyle(newStyle);
	}

	@Override
	public void setCurrentVisualStyle(VisualStyle newStyle) {
		if (newStyle == null)
			newStyle = defaultStyle;
		
		boolean changed = !newStyle.equals(currentStyle);
		this.currentStyle = newStyle;
		
		if (changed)
			cyEventHelper.fireEvent(new SetCurrentVisualStyleEvent(this, currentStyle));
	}
}
