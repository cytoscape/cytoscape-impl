package org.cytoscape.filter.internal;

/**
 * An internal interface for transformers that need to know when the process of 
 * running the transformer begins and ends.
 * <br><br>
 * 
 * Note: 
 * This is just an optimization and there is no guarantee that these
 * methods will be called. The transformer must be able to function properly
 * without these methods being called. The only guarantee is if 
 * startOptimization() is called then endOptimization() will be called.
 * 
 * @see AbstractMemoizedTransformer
 */
public interface LifecycleTransformer {

	void setUp();
	
	void tearDown();
	
}
