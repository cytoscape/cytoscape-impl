package org.cytoscape.ding.impl.cyannotator;

import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * This class is a wrapper for the annotation tree, the actual root of the tree is stored in the 'root' field.
 * This class contains methods and fields that only make sense to be called on the root of the tree.
 */
public class AnnotationTree {

	// This is the actual root node of each tree.
	private AnnotationNode foregroundTree;
	private AnnotationNode backgroundTree;
	
	private Map<Annotation,AnnotationNode> foregroundLookup;
	private Map<Annotation,AnnotationNode> backgroundLookup;
	
	private CyAnnotator cyAnnotator;
	
	public static enum Shift {
		UP_ONE, 
		DOWN_ONE, 
		TO_FRONT, 
		TO_BACK
	}
	
	/**
	 * Create this object using the static factory method buildTree().
	 */
	private AnnotationTree(AnnotationNode foregroundTree, AnnotationNode backgroundTree) {
		this.foregroundTree = foregroundTree;
		this.backgroundTree = backgroundTree;
	}

	public AnnotationNode getForegroundRoot() {
		return foregroundTree;
	}
	
	public AnnotationNode getBackgroundRoot() {
		return backgroundTree;
	}
	
	public AnnotationNode getRoot(String canvas) {
		switch(canvas) {
			case Annotation.FOREGROUND: return foregroundTree;
			case Annotation.BACKGROUND: return backgroundTree;
			default: return null;
		}
	}
	
	public AnnotationNode get(String canvas, Annotation a) {
		switch(canvas) {
			case Annotation.FOREGROUND: return foregroundLookup.get(a);
			case Annotation.BACKGROUND: return backgroundLookup.get(a);
			default: return null;
		}
	}
	
	public void shift(Shift shift, String canvas, Collection<? extends Annotation> annotations) {
		groupByParent(canvas, annotations).forEach((parent, childrenToShift) -> parent.shift(shift, childrenToShift));
	}
	
	public boolean shiftAllowed(Shift shift, String canvas, Collection<? extends Annotation> annotations) {
		if(annotations.isEmpty())
			return false;
		
		for(Map.Entry<AnnotationNode,List<AnnotationNode>> entry : groupByParent(canvas, annotations).entrySet()) {
			AnnotationNode parent = entry.getKey();
			List<AnnotationNode> childrenToShift = entry.getValue();
			if(!parent.shiftAllowed(shift, childrenToShift)) {
				return false;
			}
		}
		return true;
	}
	
	private Map<AnnotationNode,List<AnnotationNode>> groupByParent(String canvas, Collection<? extends Annotation> annotations) {
		return annotations.stream()
			.map(a -> this.get(canvas, a))
			.filter(a -> a != null)
			.collect(groupingBy(AnnotationNode::getParent)); // doesn't matter which tree they are already separate
	}
	
	/**
	 * This method can only be called on the root.
	 */
	public void resetZOrder() {
		if(cyAnnotator == null)
			return;
		// Need to calculate z-order separately for each canvas
		// Note that group annotations are assumed to be on the foreground canvas 
		// even though their members can be on either canvas.
		
		int[] zf = {0}; // foreground canvas z-order
		foregroundTree.depthFirstTraversal(node -> {
			DingAnnotation da = (DingAnnotation) node.getAnnotation();
			da.setZOrder(zf[0]++);
		});
		
		int[] zb = {0};
		backgroundTree.depthFirstTraversal(node -> {
			DingAnnotation da = (DingAnnotation) node.getAnnotation();
			if(!(da instanceof GroupAnnotation)) {
				da.setZOrder(zb[0]++);
			}
		});
	}
	
	/**
	 * This method will not detect cycles in the given Set of annotations. The reason is that however unlikely
	 * its possible that old session files might have annotations that contain cycles. So when loading we
	 * need to silently convert into a tree.
	 */
	public static AnnotationTree buildTree(Collection<? extends Annotation> annotations, CyAnnotator cyAnnotator) {
		Map<String, List<Annotation>> layers = separateByLayers(annotations, true);
		
		// Build the annotation tree bottom-up, cycles and duplicate membership is ignored.
		AnnotationNode foregroundTree = new AnnotationNode(null);
		AnnotationNode backgroundTree = new AnnotationNode(null);
		
		Map<Annotation,AnnotationNode> foregroundNodes = new HashMap<>();
		Map<Annotation,AnnotationNode> backgroundNodes = new HashMap<>();
		
		for(Annotation a : layers.get(Annotation.FOREGROUND)) {
			addNode(a, foregroundTree, foregroundNodes, cyAnnotator);
		}
		for(Annotation a : layers.get(Annotation.BACKGROUND)) {
			addNode(a, backgroundTree, backgroundNodes, cyAnnotator);
		}
		
		foregroundTree.removeEmptyGroups();
		backgroundTree.removeEmptyGroups();

		AnnotationTree head = new AnnotationTree(foregroundTree, backgroundTree);
		head.foregroundLookup = foregroundNodes;
		head.backgroundLookup = backgroundNodes;
		head.cyAnnotator = cyAnnotator;
		return head;
	}

	private static void addNode(Annotation a, AnnotationNode root, Map<Annotation,AnnotationNode> all, CyAnnotator cyAnnotator) {
		if(!(a instanceof DingAnnotation))
			return;
		
		AnnotationNode n = all.computeIfAbsent(a, AnnotationNode::new);
		
		DingAnnotation groupParent = (DingAnnotation)((DingAnnotation)a).getGroupParent();
		
		if(groupParent != null && cyAnnotator.contains(groupParent)) {
			AnnotationNode pn = all.get(groupParent);
			if(pn == null) {
				// Now we can create the Nodes for each GroupAnnotation we find,
				// because a group node can be added to both background and foreground trees,
				// since it may contain child annotations from different canvases
				all.put(groupParent, pn = new AnnotationNode(groupParent));
				addNode(groupParent, root, all, cyAnnotator);
			}
			
			if(pn.getIndex(n) < 0)
				pn.add(n);
			
		} else if (root.getIndex(n) < 0) {
			root.add(n);
		}
	}
	
	private static Map<String, List<Annotation>> separateByLayers(Collection<? extends Annotation> list, boolean includeGroups) {
		Map<String, List<Annotation>> map = new HashMap<>();
		map.put(Annotation.FOREGROUND, new ArrayList<>());
		map.put(Annotation.BACKGROUND, new ArrayList<>());
		
		if (list != null) {
			for (Annotation a : list) {
				if(a instanceof GroupAnnotation) {
					map.get(Annotation.FOREGROUND).add(a);
					map.get(Annotation.BACKGROUND).add(a);
				} else {
					List<Annotation> set = map.get(a.getCanvasName());
					if (set != null) // Should never be null, unless a new canvas name is created!
						set.add(a);
				}
			};
		}
		
		// We sort the annotations so that if an app created a "wrong" z-order we can make a best
		// attempt at ordering the annotations in a way that is similar to the original.
		sortAnnotations(map.get(Annotation.FOREGROUND));
		sortAnnotations(map.get(Annotation.BACKGROUND));
		return map;
	}
	
	
	/**
	 * This method will detect cycles in the given set of annotations.
	 * 2) GroupAnnotationImpl.addAnnotation() will enforce that an annotation cannot be in two groups at the same time.
	 * These two assertions together enforce that the set of annotations forms a proper tree.
	 * 
	 * @param moreAnnotations We need to make a copy of the annotations parameter anyway, better not to force CyAnnotator
	 * to also make a copy. Just pass in the annotations that are being added as a separate parameter.
	 */
	public static boolean containsCycle(Collection<DingAnnotation> annotations, Collection<DingAnnotation> moreAnnotations) {
		Set<DingAnnotation> annotationsRemaining = new HashSet<>(annotations);
		if(moreAnnotations != null)
			annotationsRemaining.addAll(moreAnnotations);
		
		while(!annotationsRemaining.isEmpty()) {
			DingAnnotation start = annotationsRemaining.iterator().next();
			if(containsCycle(start, annotationsRemaining, new HashSet<>())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean containsCycle(Collection<DingAnnotation> annotations, DingAnnotation extraAnnotation) {
		return containsCycle(annotations, Collections.singleton(extraAnnotation));
	}
	
	public static boolean containsCycle(Collection<DingAnnotation> annotations) {
		return containsCycle(annotations, (Collection<DingAnnotation>)null);
	}

	private static boolean containsCycle(DingAnnotation a, Collection<DingAnnotation> annotations, Set<Annotation> marked) {
		if(!marked.add(a))
			return true;
		if(!annotations.remove(a))
			return false;
		
		if(a instanceof GroupAnnotation) {
			GroupAnnotation ga = (GroupAnnotation) a;
			for(Annotation member : ga.getMembers()) {
				if(containsCycle((DingAnnotation) member, annotations, marked)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean hasSameParent(Collection<? extends Annotation> annotations) {
		if(annotations.isEmpty())
			return false;
		
		Iterator<? extends Annotation> iter = annotations.iterator();
		GroupAnnotation parent = null;
		if(iter.hasNext()) {
			DingAnnotation a = (DingAnnotation)iter.next();
			parent = a.getGroupParent();
		}
		while(iter.hasNext()) {
			DingAnnotation a = (DingAnnotation)iter.next();
			if(parent != a.getGroupParent()) {
				return false;
			}
		}
		
		return true;
	}
	
	public static List<DingAnnotation> getAncestors(DingAnnotation a) {
		List<DingAnnotation> ancestors = new ArrayList<>();
		DingAnnotation ancestor = (DingAnnotation) a.getGroupParent();
		while(ancestor != null) {
			ancestors.add(ancestor);
			ancestor = (DingAnnotation) ancestor.getGroupParent();
		}
		return ancestors;
	}
	
	private static void sortAnnotations(List<Annotation> annotations) {
		// Sort the annotations by existing z-order.
		// This will give us a decent heuristic for how to order the annotations if their z-order is screwed up.
		// Once the z-order is "fixed" this will maintain the established order.
		Comparator<Annotation> comparator = (a1, a2) -> {
			if (a1 instanceof DingAnnotation && a2 instanceof DingAnnotation) {
				DingAnnotation da1 = (DingAnnotation) a1;
				DingAnnotation da2 = (DingAnnotation) a2;
				int z1 = da1.getZOrder();
				int z2 = da2.getZOrder();
				if(z1 >= 0 && z2 >= 0) {
					return Integer.compare(z1, z2);
				} else {
					return a1.getName().compareToIgnoreCase(a2.getName());
				}
			}
			return 0;
		};
		Collections.sort(annotations, comparator);
	}
}
