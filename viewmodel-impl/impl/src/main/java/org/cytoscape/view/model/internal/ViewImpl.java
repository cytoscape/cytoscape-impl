package org.cytoscape.view.model.internal;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;


/**
 * This is an implementation of row-oriented ViewModel.
 * 
 * @author kono
 *
 * @param <M>
 */
public abstract class ViewImpl<M> implements View<M> {
	
	protected final M model;
	protected final long suid;
	
	protected final CyEventHelper cyEventHelper;
	
	//TODO: Thread safety?
	protected final Map<VisualProperty<?>, Object> visualProperties;
	protected final Map<VisualProperty<?>, Object> visualPropertyLocks;
	
	
	/**
	 * Create an instance of view model, but not firing event to upper layer.
	 * 
	 * @param model
	 * @param cyEventHelper
	 */
	public ViewImpl(final M model, final CyEventHelper cyEventHelper) {
		if(model == null)
			throw new IllegalArgumentException("Data model cannot be null.");
		if(cyEventHelper == null)
			throw new IllegalArgumentException("CyEventHelper is null.");
		
		this.suid = SUIDFactory.getNextSUID();
		this.model = model;
		this.cyEventHelper = cyEventHelper;
		
		this.visualProperties = new HashMap<VisualProperty<?>, Object>();
		this.visualPropertyLocks = new HashMap<VisualProperty<?>, Object>();
	}

	
	@Override
	public M getModel() {
		return model;
	}

	
	@Override
	public long getSUID() {
		return suid;
	}
	

	abstract public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, V value);

	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getVisualProperty(VisualProperty<T> vp) {
		
		if(visualPropertyLocks.get(vp) == null) {
			if(visualProperties.get(vp) == null)
				return vp.getDefault();
			else
				return (T) visualProperties.get(vp);
			
		} else
			return (T) this.visualPropertyLocks.get(vp);
	}

	
	// TODO: should I fire event?
	@Override
	public <T, V extends T> void setLockedValue(VisualProperty<? extends T> vp, V value) {
		this.visualPropertyLocks.put(vp, value);
	}
	

	@Override
	public boolean isValueLocked(VisualProperty<?> vp) {
		if(visualPropertyLocks.get(vp) == null)
			return false;
		else 
			return true;
	}

	@Override
	public void clearValueLock(VisualProperty<?> vp) {
		this.visualPropertyLocks.remove(vp);
	}

}
