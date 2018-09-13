package org.cytoscape.ding.impl.cyannotator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;

public class AnnotationNode implements TreeNode {

	// The root of the tree will be null, all other nodes must not be null
	private Annotation annotation;
	
	private List<AnnotationNode> children = new ArrayList<>();
	private AnnotationNode parent;
	
	
	@FunctionalInterface
	public static interface Visitor {
		public void visit(AnnotationNode node);
	}
	
	
	AnnotationNode(Annotation annotation) {
		this.annotation = annotation;
	}
	
	@Override
	public AnnotationNode getChildAt(int childIndex) {
		return children.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return children.size();
	}
	
	@Override
	public AnnotationNode getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		return children.indexOf((AnnotationNode)node);
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public boolean isLeaf() {
		return children.isEmpty();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration children() {
		return Collections.enumeration(children);
	}
	
	@Override
	public String toString() {
		return "AnnotationNode[" + (annotation == null ? null : annotation.getName()) + "]";
	}
	
	void removeEmptyGroups() {
		for(AnnotationNode n : children) {
			n.removeEmptyGroups();
		}
		Iterator<AnnotationNode> iter = children.iterator();
		while(iter.hasNext()) {
			AnnotationNode n = iter.next();
			if(n.getAnnotation() instanceof GroupAnnotation && n.getChildCount() == 0) {
				iter.remove();
			}
		}
	}
	
	
	/**
	 * @param direction  negative means bring forward (up), positive means send backward (down)
	 * @param children   it is assumed that the given list is all children of this node
	 */
	void shift(int direction, List<AnnotationNode> nodes) {
		if(nodes.isEmpty())
			return;
		
		nodes.sort(Comparator.comparing(this::getIndex));
		
		if(direction < 0) { // move up
			if(getIndex(nodes.get(0)) > 0) {
				for(AnnotationNode n : nodes) {
					int index = getIndex(n);
					swap(index-1, index);
				}
			}
		} else { // move down
			if(getIndex(nodes.get(nodes.size()-1)) < children.size()-1) {
				for(int i = nodes.size()-1; i >= 0; i--) {
					int index = getIndex(nodes.get(i));
					swap(index, index+1);
				}
			}
		}
	}
	
	boolean shiftAllowed(int direction, List<AnnotationNode> nodes) {
		if(nodes.isEmpty())
			return false;
		
		nodes.sort(Comparator.comparing(this::getIndex));
		
		if(direction < 0) // move up
			return getIndex(nodes.get(0)) > 0;
		else
			return getIndex(nodes.get(nodes.size()-1)) < children.size()-1;
	}
	
	
	private void swap(int i1, int i2) {
		if(i1 < 0 || i2 < 0)
			return;
		AnnotationNode n1 = children.get(i1);
		AnnotationNode n2 = children.get(i2);
		children.set(i1, n2);
		children.set(i2, n1);
	}
	
	public void add(AnnotationNode child) {
		child.parent = this;
		children.add(child);
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public Annotation getAnnotation() {
		return annotation;
	}
	
	
	// NOTE: does not visit the root node
	public void depthFirstTraversal(Visitor visitor) {
		if(annotation != null)
			visitor.visit(this);
		
		for(AnnotationNode child : children) {
			child.depthFirstTraversal(visitor);
		}
	}
	
	public List<Annotation> depthFirstOrder() {
		List<Annotation> annotations = new ArrayList<>();
		depthFirstTraversal(n -> annotations.add(n.annotation));
		return annotations;
	}
	
	
	public AnnotationNode[] getPath() {
		LinkedList<AnnotationNode> list = new LinkedList<>();
		AnnotationNode n = this;
		while(n != null) {
			list.addFirst(n);
			n = n.getParent();
		}
		return list.toArray(new AnnotationNode[list.size()]);
	}
	
}
