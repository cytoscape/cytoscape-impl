package org.cytoscape.ding.impl.cyannotator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.JComponent;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;

public class AnnotationTree {

	// The root of the tree will be null, all other nodes must not be null
	private Annotation annotation;
	
	private List<AnnotationTree> children = new ArrayList<>();
	private AnnotationTree parent;
	
	private Map<Annotation,AnnotationTree> quickLookup;
	
	private AnnotationTree(Annotation annotation) {
		this.annotation = Objects.requireNonNull(annotation);
	}
	
	private AnnotationTree() {
	}
	
	
	public void add(AnnotationTree child) {
		child.parent = this;
		children.add(child);
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public int childCount() {
		return children.size();
	}
	
	public List<AnnotationTree> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	public Annotation getAnnotation() {
		return annotation;
	}
	
	public int indexOf(AnnotationTree child) {
		return children.indexOf(child);
	}
	
	public AnnotationTree parent() {
		return parent;
	}
	
	
	public AnnotationTree get(Annotation a) {
		if(annotation == null)
			return quickLookup.get(a);
		
		if(annotation.equals(a))
			return this;
		
		for(AnnotationTree child : children) {
			AnnotationTree result = child.get(a);
			if(result != null) {
				return result;
			}
		}
		return null;
	}
	
	
	public List<Annotation> depthFirstOrder() {
		List<Annotation> annotations = new ArrayList<>();
		depthFirstOrder(annotations);
		return annotations;
	}
	
	private void depthFirstOrder(List<Annotation> annotations) {
		if(annotation != null)
			annotations.add(annotation);
		
		for(AnnotationTree child : children) {
			child.depthFirstOrder(annotations);
		}
	}
	
	
	/**
	 * This method will not detect cycles in the given Set of annotations. The reason is that however unlikely
	 * its possible the old session files might have annotations that contain cycles. So when loading we
	 * need to silently convert into a tree.
	 */
	public static AnnotationTree buildTree(Collection<? extends Annotation> annotations) {
		// We sort the annotations so that if an app created a "wrong" z-order we can make a best
		// attempt at ordering the annotations in a way that is similar to the original.
		List<Annotation> sortedAnnotations = new ArrayList<>(annotations);
		sortAnnotations(sortedAnnotations);
		
		// Build the annotation tree bottom-up, cycles and duplicate membership is ignored.
		AnnotationTree root = new AnnotationTree();
		Map<Annotation,AnnotationTree> all = new HashMap<>();
		for(Annotation a : sortedAnnotations) {
			addNode(a, root, all);
		}
		root.quickLookup = all;
		return root;
	}
	

	private static void addNode(Annotation a, AnnotationTree root, Map<Annotation,AnnotationTree> all) {
		AnnotationTree n = all.computeIfAbsent(a, AnnotationTree::new);
		
		if(a instanceof DingAnnotation && ((DingAnnotation)a).getGroupParent() != null) {
			DingAnnotation ga = (DingAnnotation)((DingAnnotation)a).getGroupParent();
			AnnotationTree pn = all.get(ga);
			if(pn == null) {
				// Now we can create the Nodes for each GroupAnnotation we find,
				// because a group node can be added to both background and foreground trees,
				// since it may contain child annotations from different canvases
				all.put(ga, pn = new AnnotationTree(ga));
				addNode(ga, root, all);
			}
			
			if(pn.indexOf(n) < 0)
				pn.add(n);
			
		} else if (root.indexOf(n) < 0) {
			root.add(n);
		}
	}
	
	
	/**
	 * This method will detect cycles in the given set of annotations.
	 * 2) GroupAnnotationImpl.addAnnotation() will enforce that an annotation cannot be in two groups at the same time.
	 * These two assertions together enforce that the set of annotations forms a proper tree.
	 */
	public static boolean containsCycle(Set<DingAnnotation> annotations) {
		Set<DingAnnotation> annotationsRemaining = new HashSet<>(annotations);
		while(!annotationsRemaining.isEmpty()) {
			DingAnnotation start = annotationsRemaining.iterator().next();
			if(containsCycle(start, annotationsRemaining, new HashSet<>())) {
				return true;
			}
		}
		return false;
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

	
	/**
	 * Returns a subset of the given set that contains just the annotations that are at the top level
	 * (i.e. not contained in any group).
	 */
	public static List<DingAnnotation> getRootSet(Set<DingAnnotation> allAnnotations) {
		List<DingAnnotation> rootSet = new ArrayList<>();
		for(DingAnnotation a : allAnnotations) {
			if(a.getGroupParent() == null) {
				rootSet.add(a);
			}
		}
		return rootSet;
	}
	
	
	private static void sortAnnotations(List<Annotation> annotations) {
		// Sort the annotations by existing z-order.
		// This will give us a decent heuristic for how to order the annotations if their z-order is screwed up.
		// Once the z-order is "fixed" this will maintain the established order.
		Comparator<Annotation> comparator = (a1, a2) -> {
			if (a1 instanceof DingAnnotation && a2 instanceof DingAnnotation) {
				DingAnnotation da1 = (DingAnnotation) a1;
				DingAnnotation da2 = (DingAnnotation) a2;
				JComponent canvas1 = da1.getCanvas();
				JComponent canvas2 = da2.getCanvas();
				int z1 = canvas1.getComponentZOrder(da1.getComponent());
				int z2 = canvas2.getComponentZOrder(da2.getComponent());
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
