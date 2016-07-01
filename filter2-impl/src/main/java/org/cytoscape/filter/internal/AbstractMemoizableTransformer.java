package org.cytoscape.filter.internal;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.filter.model.AbstractTransformer;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.TransformerListener;
import org.cytoscape.model.CyIdentifiable;

/**
 * Composite transformers that apply a sub-filter to multiple nodes/edges have the problem
 * where the sub-filter might get applied to the same node/edge multiple times.
 * For certain situations, such as nested topology filters, this actually leads to bad
 * complexity. The solution/optimization is to apply the dynamic programming technique
 * of memoization to the sub-filter to cache its results. But for this to work we need 
 * to know when the process of running the transformer begins and ends, so the 
 * {@link MemoizableTransformer} interface is also needed.
 * 
 * @author mkucera
 *
 * @see MemoizableTransformer
 */
public abstract class AbstractMemoizableTransformer<C,E extends CyIdentifiable> extends AbstractTransformer<C, E> implements MemoizableTransformer {
	
	private Filter<C,E> memoizedFilter;
	
	abstract protected CompositeFilter<C,E> getCompositeFilter();
	
	
	protected Filter<C,E> getMemoizedFilter() {
		if(memoizedFilter == null) {
			memoizedFilter = getCompositeFilter();
		}
		return memoizedFilter;
	}
	
	
	@Override
	public void startCaching() {
		CompositeFilter<C,E> subfilter = getCompositeFilter();
		if(subfilter.getLength() > 0) {
			if(subfilter instanceof MemoizableTransformer) {
				((MemoizableTransformer) subfilter).startCaching();
			}
			memoizedFilter = memoize(subfilter);
		}
	}
	
	@Override
	public void clearCache() {
		Filter<C,E> subfilter = getCompositeFilter();
		memoizedFilter = subfilter;
		if(subfilter instanceof MemoizableTransformer) {
			((MemoizableTransformer) subfilter).clearCache();
		}
	}
	
	
	public <A,B extends CyIdentifiable> Filter<A,B> memoize(Filter<A,B> filter) {
		return new Filter<A, B>() {
			
			private Map<Long, Boolean> cache = new HashMap<>();
			
			@Override
			public boolean accepts(A context, B element) {
				Boolean value = cache.get(element.getSUID());
				if(value == null) {
					value = filter.accepts(context, element);
					cache.put(element.getSUID(), value);
				}
				return value;
			}
			
			@Override
			public String getName() {
				return filter.getName();
			}
			@Override
			public String getId() {
				return filter.getId();
			}
			@Override
			public Class<A> getContextType() {
				return filter.getContextType();
			}
			@Override
			public Class<B> getElementType() {
				return filter.getElementType();
			}
			@Override
			public void addListener(TransformerListener listener) {
				filter.addListener(listener);
			}
			@Override
			public void removeListener(TransformerListener listener) {
				filter.removeListener(listener);
			}
		};
	}

}
