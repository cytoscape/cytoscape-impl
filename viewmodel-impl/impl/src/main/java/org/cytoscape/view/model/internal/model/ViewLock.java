package org.cytoscape.view.model.internal.model;

/**
 * A lock object with some extra fields.
 * Find lock objects in a profiler like YourKIT by searching for the name "ViewLock".
 */
public class ViewLock {

	private boolean updateDirty = true;

	public boolean isUpdateDirty() {
		return updateDirty;
	}

	public void setUpdateDirty(boolean updateDirty) {
		this.updateDirty = updateDirty;
	}
	
}
