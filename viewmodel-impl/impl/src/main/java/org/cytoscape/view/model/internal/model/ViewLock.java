package org.cytoscape.view.model.internal.model;

/**
 * A lock object with some extra fields.
 * Find lock objects in a profiler like YourKIT by searching for the name "ViewLock".
 */
public class ViewLock {

	private int updateDirty = 0;
	private final ViewLock parent;
	
	public ViewLock(ViewLock parent) {
		this.parent = parent;
	}

	public boolean isUpdateDirty() {
		return updateDirty == 0 && (parent == null || parent.isUpdateDirty());
	}

	public void enterBatch(Runnable runnable) {
		try {
			++updateDirty;
			runnable.run();
		} finally {
			--updateDirty;
		}
	}
}
