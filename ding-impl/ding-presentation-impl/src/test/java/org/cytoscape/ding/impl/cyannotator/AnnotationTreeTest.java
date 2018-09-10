package org.cytoscape.ding.impl.cyannotator;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.junit.Test;

public class AnnotationTreeTest extends AnnotationTest {

	
	private static Set<DingAnnotation> asSet(Annotation...annotations) {
		Set<DingAnnotation> set = new HashSet<>();
		for(Annotation a : annotations) {
			set.add((DingAnnotation)a);
		}
		return set;
	}
	
	private static void assertCycle(boolean expected, Set<DingAnnotation> annotations) {
		boolean actual = AnnotationTree.containsCycle(annotations);
		assertEquals(expected, actual);
	}
	
	private GroupAnnotation createBalancedBinaryTree(int depth, Set<DingAnnotation> all) {
		if(depth == 1) {
			ShapeAnnotation shape1 = createShapeAnnotation();
			ShapeAnnotation shape2 = createShapeAnnotation();
			GroupAnnotation group1 = createGroupAnnotation();
			group1.addMember(shape1);
			group1.addMember(shape2);
			all.add((DingAnnotation)shape1);
			all.add((DingAnnotation)shape2);
			all.add((DingAnnotation)group1);
			return group1;
		} else {
			GroupAnnotation group1 = createBalancedBinaryTree(depth-1, all);
			GroupAnnotation group2 = createBalancedBinaryTree(depth-1, all);
			GroupAnnotation parent = createGroupAnnotation();
			parent.addMember(group1);
			parent.addMember(group2);
			all.add((DingAnnotation)parent);
			return parent;
		}
	}
	
	
	@Test
	public void testDetectNoCycle() {
		ShapeAnnotation shape1 = createShapeAnnotation();
		ShapeAnnotation shape2 = createShapeAnnotation();
		ShapeAnnotation shape3 = createShapeAnnotation();
		GroupAnnotation group1 = createGroupAnnotation();
		GroupAnnotation group2 = createGroupAnnotation();
		group1.addMember(shape1);
		group1.addMember(shape2);
		group2.addMember(group1);
		group2.addMember(shape3);
		
		assertCycle(false, asSet(shape1, shape2, shape3, group1, group2));
	}
	
	@Test
	public void testDetectNoCycleSimple() {
		ShapeAnnotation shape1 = createShapeAnnotation();
		GroupAnnotation group1 = createGroupAnnotation();
		
		assertCycle(false, asSet(shape1));
		assertCycle(false, asSet(group1));
	}
	
	@Test
	public void testDetectNoCycleMultipleComponents() {
		// create two separate trees
		ShapeAnnotation shape1a = createShapeAnnotation();
		ShapeAnnotation shape2a = createShapeAnnotation();
		ShapeAnnotation shape3a = createShapeAnnotation();
		GroupAnnotation group1a = createGroupAnnotation();
		GroupAnnotation group2a = createGroupAnnotation();
		group1a.addMember(shape1a);
		group1a.addMember(shape2a);
		group2a.addMember(group1a);
		group2a.addMember(shape3a);
		
		ShapeAnnotation shape1b = createShapeAnnotation();
		ShapeAnnotation shape2b = createShapeAnnotation();
		ShapeAnnotation shape3b = createShapeAnnotation();
		GroupAnnotation group1b = createGroupAnnotation();
		GroupAnnotation group2b = createGroupAnnotation();
		group1b.addMember(shape1b);
		group1b.addMember(shape2b);
		group2b.addMember(group1b);
		group2b.addMember(shape3b);
		
		Set<DingAnnotation> annotations = new HashSet<>();
		annotations.addAll(asSet(shape1a,  shape2a,  shape3a,  group1a,  group2a));
		annotations.addAll(asSet(shape1b,  shape2b,  shape3b,  group1b,  group2b));
		
		assertCycle(false, annotations);
	}
	
	@Test
	public void testDetectCycleSimple() {
		GroupAnnotation group1 = createGroupAnnotation();
		GroupAnnotation group2 = createGroupAnnotation();
		group1.addMember(group2);
		group2.addMember(group1);
		
		assertCycle(true, asSet(group1, group2));
	}
	
	@Test
	public void testDetectCycleSelfReference() {
		GroupAnnotation group1 = createGroupAnnotation();
		group1.addMember(group1);
		
		assertCycle(true, asSet(group1));
	}
	
	
	@Test
	public void testDetectCycleBig() {
		Set<DingAnnotation> all = new HashSet<>();
		GroupAnnotation g1 = createBalancedBinaryTree(5, all);
		GroupAnnotation g2 = createBalancedBinaryTree(5, all);
		GroupAnnotation g3 = createBalancedBinaryTree(5, all);
		GroupAnnotation g4 = createBalancedBinaryTree(5, all);
		GroupAnnotation g5 = createBalancedBinaryTree(5, all);
		GroupAnnotation g6 = createBalancedBinaryTree(5, all);
		g1.addMember(g2);
		g2.addMember(g3);
		g3.addMember(g4);
		g4.addMember(g5);
		g5.addMember(g6);
		g6.addMember(g1);
		
		assertEquals(378, all.size()); // sanity check
		assertCycle(true, all);
	}
	

	@Test
	public void testDetectNoCycleBig() {
		Set<DingAnnotation> all = new HashSet<>();
		GroupAnnotation g1 = createBalancedBinaryTree(5, all);
		GroupAnnotation g2 = createBalancedBinaryTree(5, all);
		GroupAnnotation g3 = createBalancedBinaryTree(5, all);
		GroupAnnotation g4 = createBalancedBinaryTree(5, all);
		GroupAnnotation g5 = createBalancedBinaryTree(5, all);
		GroupAnnotation g6 = createBalancedBinaryTree(5, all);
		g1.addMember(g2);
		g2.addMember(g3);
		g3.addMember(g4);
		g4.addMember(g5);
		g5.addMember(g6);
		// g6.addMember(g1);  // NO CYCLE
		
		assertEquals(378, all.size()); // sanity check
		assertCycle(false, all);
	}
	
	
	public void testConvertToTree() {
		
		
		
	}
	
	
	public void testDetectDuplicateMembership() {
		
	}
	

}
