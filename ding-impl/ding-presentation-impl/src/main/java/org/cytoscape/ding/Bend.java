package org.cytoscape.ding;

import java.util.List;


/**
 * Definition of Edge Bends.
 * Bend is an ordered {@link List} of {@link Handle}s.
 *
 */
public interface Bend {
	
	/**
	 * 
	 * @return All {@link Handle}s belong to this Bend.
	 */
	List<Handle> getAllHandles();
	
	/**
	 * Insert a Handle to the specified position in the Bend
	 * 
	 * @param index Position of the new Handle
	 * @param handle Handle to be added
	 */
	void insertHandleAt(final int index, final Handle handle);

	/**
	 * Remove a Handle at the given index.
	 * 
	 * @param handleIndex Index of the Handle to be removed
	 */
	void removeHandleAt(final int handleIndex);
	
	/**
	 * Remove all Handles in this Bend
	 */
	void removeAllHandles();

	
	/**
	 * Get index of a Handle
	 * 
	 * @param handle 
	 * 
	 * @return Index of the given Handle
	 */
	int getIndex(final Handle handle);
	
	/**
	 * Create string representation of this object for parsing.
	 * @return
	 */
	String getSerializableString();
}
