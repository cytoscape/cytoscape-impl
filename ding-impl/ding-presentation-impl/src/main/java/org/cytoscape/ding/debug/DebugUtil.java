package org.cytoscape.ding.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DebugUtil {

	public static <T,S> List<S> map(List<T> xs, Function<T,S> f) {
		List<S> rs = new ArrayList<>(xs.size());
		for(T x : xs)
			rs.add(f.apply(x));
		return rs;
	}
	
	public static <T,S,R> List<R> map2(Collection<T> xs, Collection<S> ys, BiFunction<T,S,R> f) {
		List<R> rs = new ArrayList<>(xs.size());
		Iterator<T> ts = xs.iterator();
		Iterator<S> ss = ys.iterator();
		while(ts.hasNext() && ss.hasNext()) {
			rs.add(f.apply(ts.next(), ss.next()));
		}
		return rs;
	}
	
	public static <T> T reduce(List<T> xs, BiFunction<T,T,T> f) {
		if(xs.isEmpty())
			return null;
		Iterator<T> iter = xs.iterator();
		T t = iter.next();
		while(iter.hasNext()) {
			t = f.apply(t, iter.next());
		}
		return t;
	}
	
	public static <T> int countNodesInTree(T t, Function<T,Collection<T>> children) {
		int num = 1;
		for(T x : children.apply(t)) {
			num += countNodesInTree(x, children);
		}
		return num;
	}
	
}
