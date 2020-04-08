package org.cytoscape.view.model.internal.base;

import java.util.Objects;
import java.util.function.Consumer;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;

import io.vavr.collection.Set;

public abstract class CyViewBase<M> implements View<M> {
	
	private final Long suid;
	private final M model;

	public CyViewBase(M model) {
		this.model = Objects.requireNonNull(model);
		this.suid = SUIDFactory.getNextSUID();
	}
	
	/**
	 * There could potentially be millions of these objects on the heap.
	 * We want to keep the size of these objects as small as possible.
	 * Look up these values using abstract methods, rather than store them as fields.
	 */
	public abstract View<?> getParentViewModel();
	public abstract VPStore getVPStore();
	public abstract ViewLock getLock();
	public abstract CyEventHelper getEventHelper();
	public abstract VisualLexicon getVisualLexicon();
	public void setDirty() { }
	
	
	@Override
	public Long getSUID() {
		return suid;
	}

	@Override
	public M getModel() {
		return model;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T, V extends T> void fireViewChangedEvent(VisualProperty<? extends T> vp, V value, boolean lockedValue) {
		ViewChangeRecord record = new ViewChangeRecord<>(this, vp, value, lockedValue);
		getEventHelper().addEventPayload(getParentViewModel(), record, ViewChangedEvent.class);
	}
	
	@Override
	public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, V value) {
		ViewLock lock = getLock();
		synchronized (lock) {
			boolean changed = getVPStore().setVisualProperty(suid, vp, value);
			if(changed) {
				if(lock.isUpdateDirty())
					setDirty();
				boolean locked = getVPStore().isValueLocked(suid, vp);
				if(!locked) {
					// If the value is overridden by a lock then the value returned 
					// by getVisualProperty() won't visibly change by setting the VP here.
					fireViewChangedEvent(vp, value, false);
				}
			}
		}
	}
	
	@Override
	public void batch(Consumer<View<M>> viewConsumer, boolean setDirty) {
		ViewLock lock = getLock();
		synchronized (lock) {
			lock.enterBatch(() ->
				viewConsumer.accept(this)
			);
			if(setDirty && lock.isUpdateDirty()) {
				setDirty();
			}
		}
	}
	
	@Override
	public <T> T getVisualProperty(VisualProperty<T> vp) {
		return getVPStore().getVisualProperty(suid, vp);
	}

	@Override
	public boolean isSet(VisualProperty<?> vp) {
		return getVPStore().isSet(suid, vp);
	}

	
	/*
	 * The behavior here is inconsistent with setVisualProperty()
	 * When applying a visual style the ApplyToNetworkHandler.run() method takes care of applying child visual properties.
	 * However when setting a bypass (ie a locked value) that same logic has to be done here.
	 */
	@Override
	public <T, V extends T> void setLockedValue(VisualProperty<? extends T> vp, V value) {
		ViewLock lock = getLock();
		synchronized (lock) {
			boolean changed = getVPStore().setLockedValue(suid, vp, value);
			if(changed) {
				if(lock.isUpdateDirty())
					setDirty();
			
				VisualLexiconNode visualLexiconNode = getVisualLexicon().getVisualLexiconNode(vp);
				if(visualLexiconNode.getChildren().isEmpty()) {
					// much more common case, might as well optimize for it
					fireViewChangedEvent(vp, value, true);
				} else {
					visualLexiconNode.visitDescendants(node -> {
						VisualProperty<?> nodeVP = node.getVisualProperty();
						Object nodeValue = getVPStore().getVisualProperty(suid, vp);
						fireViewChangedEvent(nodeVP, nodeValue, true);
					});
				}
			}
		}
	}

	@Override
	public boolean isValueLocked(VisualProperty<?> vp) {
		return getVPStore().isValueLocked(suid, vp);
	}

	@Override
	public void clearValueLock(VisualProperty<?> vp) {
		setLockedValue(vp, null);
	}

	@Override
	public boolean isDirectlyLocked(VisualProperty<?> vp) {
		return getVPStore().isDirectlyLocked(suid, vp);
	}

	@Override
	public void clearVisualProperties() {
		Set<VisualProperty<?>> clearableVPs = getVPStore().getClearableVisualProperties(suid);
		synchronized (getLock()) {
			for(VisualProperty<?> vp : clearableVPs) {
				setVisualProperty(vp, null);
			}
		}
	}
	
}
