package org.cytoscape.ding.impl.cyannotator;

import static org.cytoscape.view.presentation.annotations.Annotation.BACKGROUND;
import static org.cytoscape.view.presentation.annotations.Annotation.FOREGROUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.ding.impl.cyannotator.AnnotationTree.Shift;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.junit.Before;
import org.junit.Test;

public class AnnotationTreeTest extends AbstractAnnotationTest {

	protected CyAnnotator cyAnnotator;
	
	@Before
	public void setUpCyAnnotator() {
		cyAnnotator = mock(CyAnnotator.class);
		when(cyAnnotator.contains(any())).thenReturn(true);
	}
	
	
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
	
	
	@Test
	public void testConvertToTree() {
		GroupAnnotation group2 = createGroupAnnotation("group2", 0, FOREGROUND);
		GroupAnnotation group1 = createGroupAnnotation("group1", 1, FOREGROUND);
		ShapeAnnotation shape1 = createShapeAnnotation("shape1", 2, FOREGROUND);
		ShapeAnnotation shape2 = createShapeAnnotation("shape2", 3, FOREGROUND);
		ShapeAnnotation shape3 = createShapeAnnotation("shape3", 4, FOREGROUND);
		group1.addMember(shape1);
		group1.addMember(shape2);
		group2.addMember(group1);
		group2.addMember(shape3);
		
		Set<DingAnnotation> annotations = asSet(shape1, shape2, shape3, group1, group2);
		AnnotationTree head = AnnotationTree.buildTree(annotations, cyAnnotator);
		
		// the root of the tree does not contain an annotation
		assertNull(head.getForegroundRoot().getAnnotation());
		assertEquals(1, head.getForegroundRoot().getChildCount());
		
		AnnotationNode ng2 = head.getForegroundRoot().getChildAt(0);
		assertEquals("group2", ng2.getAnnotation().getName());
		assertEquals(2, ng2.getChildCount());
		assertEquals("shape3", ng2.getChildAt(1).getAnnotation().getName());
		AnnotationNode ng1 = ng2.getChildAt(0);
		assertEquals("group1", ng1.getAnnotation().getName());
		assertEquals(2, ng1.getChildCount());
		assertEquals("shape1", ng1.getChildAt(0).getAnnotation().getName());
		assertEquals("shape2", ng1.getChildAt(1).getAnnotation().getName());
		
		List<Annotation> depthFirst = head.getForegroundRoot().depthFirstOrder();
		assertEquals(5, depthFirst.size());
		assertEquals("group2", depthFirst.get(0).getName());
		assertEquals("group1", depthFirst.get(1).getName());
		assertEquals("shape1", depthFirst.get(2).getName());
		assertEquals("shape2", depthFirst.get(3).getName());
		assertEquals("shape3", depthFirst.get(4).getName());
		
		assertEquals(ng2, head.get(FOREGROUND, group2));
		assertEquals(ng1, head.get(FOREGROUND, group1));
		assertEquals(ng1.getChildAt(0), head.get(FOREGROUND, shape1));
	}
	
	
	@Test
	public void testDetectDuplicateMembership() {
		GroupAnnotation group1 = createGroupAnnotation();
		GroupAnnotation group2 = createGroupAnnotation();
		ShapeAnnotation shape1 = createShapeAnnotation();
		group1.addMember(shape1);
		try {
			group2.addMember(shape1);
			fail();
		} catch(IllegalAnnotationStructureException e) {}
	}
	
	
	@Test
	public void testTreePath() {
		GroupAnnotation group2 = createGroupAnnotation("group2", 0, FOREGROUND);
		GroupAnnotation group1 = createGroupAnnotation("group1", 1, FOREGROUND);
		ShapeAnnotation shape1 = createShapeAnnotation("shape1", 2, FOREGROUND);
		ShapeAnnotation shape2 = createShapeAnnotation("shape2", 3, FOREGROUND);
		ShapeAnnotation shape3 = createShapeAnnotation("shape3", 4, FOREGROUND);
		group1.addMember(shape1);
		group1.addMember(shape2);
		group2.addMember(group1);
		group2.addMember(shape3);
		
		Set<DingAnnotation> annotations = asSet(shape1, shape2, shape3, group1, group2);
		AnnotationTree head = AnnotationTree.buildTree(annotations, cyAnnotator);
		
		AnnotationNode shape1Node = head.get(FOREGROUND, shape1);
		AnnotationNode shape2Node = head.get(FOREGROUND, shape2);
		AnnotationNode group1Node = head.get(FOREGROUND, group1);
		AnnotationNode group2Node = head.get(FOREGROUND, group2);
		
		AnnotationNode[] path = shape1Node.getPath();
		assertEquals(4, path.length);
		assertEquals(head.getForegroundRoot(), path[0]);
		assertEquals(group2Node, path[1]);
		assertEquals(group1Node, path[2]);
		assertEquals(shape1Node, path[3]);
		
		assertFalse(head.getForegroundRoot().isLeaf());
		assertFalse(group2Node.isLeaf());
		assertFalse(group1Node.isLeaf());
		assertTrue(shape1Node.isLeaf());
		
		assertEquals(0, group1Node.getIndex(shape1Node));
		assertEquals(1, group1Node.getIndex(shape2Node));
	}

	
	@Test
	public void testForegroundAndBackgroundTrees() {
		GroupAnnotation group2 = createGroupAnnotation("group2", 0, FOREGROUND); // canvas shouldn't matter for groups
		GroupAnnotation group1 = createGroupAnnotation("group1", 1, BACKGROUND);
		ShapeAnnotation shape1 = createShapeAnnotation("shape1", 2, FOREGROUND);
		ShapeAnnotation shape2 = createShapeAnnotation("shape2", 3, BACKGROUND); // shape 2 on the background!!!
		ShapeAnnotation shape3 = createShapeAnnotation("shape3", 4, FOREGROUND);
		group1.addMember(shape1);
		group1.addMember(shape2);
		group2.addMember(group1);
		group2.addMember(shape3);
		
		Set<DingAnnotation> annotations = asSet(shape1, shape2, shape3, group1, group2);
		AnnotationTree tree = AnnotationTree.buildTree(annotations, cyAnnotator);
		
		{
			AnnotationNode foreground = tree.getForegroundRoot();
			assertEquals(1, foreground.getChildCount());
			assertEquals("group2", foreground.getChildAt(0).getAnnotation().getName());
			assertEquals(2, foreground.getChildAt(0).getChildCount());
			assertEquals("group1", foreground.getChildAt(0).getChildAt(0).getAnnotation().getName());
			assertEquals("shape3", foreground.getChildAt(0).getChildAt(1).getAnnotation().getName());
			assertEquals(1, foreground.getChildAt(0).getChildAt(0).getChildCount());
			assertEquals("shape1", foreground.getChildAt(0).getChildAt(0).getChildAt(0).getAnnotation().getName());
		}
		{
			AnnotationNode background = tree.getBackgroundRoot();
			assertEquals(1, background.getChildCount());
			assertEquals("group2", background.getChildAt(0).getAnnotation().getName());
			assertEquals(1, background.getChildAt(0).getChildCount());
			assertEquals("group1", background.getChildAt(0).getChildAt(0).getAnnotation().getName());
			assertEquals(1, background.getChildAt(0).getChildAt(0).getChildCount());
			assertEquals("shape2", background.getChildAt(0).getChildAt(0).getChildAt(0).getAnnotation().getName());
		}
	}
	
	
	@Test
	public void testShift() {
		GroupAnnotation group1 = createGroupAnnotation("group1", 0, FOREGROUND);
		ShapeAnnotation shape1 = createShapeAnnotation("shape1", 1, FOREGROUND);
		ShapeAnnotation shape2 = createShapeAnnotation("shape2", 2, FOREGROUND);
		ShapeAnnotation shape3 = createShapeAnnotation("shape3", 3, FOREGROUND);
		group1.addMember(shape1);
		group1.addMember(shape2);
		group1.addMember(shape3);
		
		GroupAnnotation group2 = createGroupAnnotation("group2", 4, FOREGROUND);
		ShapeAnnotation shape4 = createShapeAnnotation("shape4", 5, FOREGROUND);
		ShapeAnnotation shape5 = createShapeAnnotation("shape5", 6, FOREGROUND);
		ShapeAnnotation shape6 = createShapeAnnotation("shape6", 7, FOREGROUND);
		group2.addMember(shape4);
		group2.addMember(shape5);
		group2.addMember(shape6);
		
		Set<DingAnnotation> annotations = new HashSet<>();
		annotations.addAll(asSet(group1, shape1, shape2, shape3));
		annotations.addAll(asSet(group2, shape4, shape5, shape6));
		
		AnnotationTree tree = AnnotationTree.buildTree(annotations, cyAnnotator);
		
		// test shiftAllowed
		assertFalse(tree.shiftAllowed(Shift.UP_ONE, FOREGROUND, Arrays.asList(shape1)));
		assertTrue(tree.shiftAllowed(Shift.UP_ONE, FOREGROUND, Arrays.asList(shape2)));
		assertFalse(tree.shiftAllowed(Shift.UP_ONE, FOREGROUND, Arrays.asList(shape2, shape1)));
		assertTrue(tree.shiftAllowed(Shift.UP_ONE, FOREGROUND, Arrays.asList(shape2, shape3)));
		assertFalse(tree.shiftAllowed(Shift.DOWN_ONE, FOREGROUND, Arrays.asList(shape3)));
		assertTrue(tree.shiftAllowed(Shift.DOWN_ONE, FOREGROUND, Arrays.asList(shape2)));
		assertFalse(tree.shiftAllowed(Shift.DOWN_ONE, FOREGROUND, Arrays.asList(shape2, shape3)));
		assertTrue(tree.shiftAllowed(Shift.DOWN_ONE, FOREGROUND, Arrays.asList(shape2, shape1)));
		assertFalse(tree.shiftAllowed(Shift.UP_ONE, FOREGROUND, Arrays.asList(shape5, shape1)));
		assertTrue(tree.shiftAllowed(Shift.DOWN_ONE, FOREGROUND, Arrays.asList(shape2, shape4, shape5)));
		
		AnnotationNode group1node = tree.get(FOREGROUND, group1);
		AnnotationNode group2node = tree.get(FOREGROUND, group2);
		
		// test shift
		tree.shift(Shift.UP_ONE, FOREGROUND, Arrays.asList(shape2, shape3));
		assertEquals(0, group1node.getIndex(tree.get(FOREGROUND, shape2)));
		assertEquals(1, group1node.getIndex(tree.get(FOREGROUND, shape3)));
		assertEquals(2, group1node.getIndex(tree.get(FOREGROUND, shape1)));
		
		// attempting to shift up again should do nothing
		tree.shift(Shift.UP_ONE, FOREGROUND, Arrays.asList(shape2, shape3));
		assertEquals(0, group1node.getIndex(tree.get(FOREGROUND, shape2)));
		assertEquals(1, group1node.getIndex(tree.get(FOREGROUND, shape3)));
		assertEquals(2, group1node.getIndex(tree.get(FOREGROUND, shape1)));
		
		// shift down
		tree.shift(Shift.DOWN_ONE, FOREGROUND, Arrays.asList(shape2, shape3));
		assertEquals(0, group1node.getIndex(tree.get(FOREGROUND, shape1)));
		assertEquals(1, group1node.getIndex(tree.get(FOREGROUND, shape2)));
		assertEquals(2, group1node.getIndex(tree.get(FOREGROUND, shape3)));
		
		// shift in both at the same time
		tree.shift(Shift.UP_ONE, FOREGROUND, Arrays.asList(shape2, shape5));
		assertEquals(0, group1node.getIndex(tree.get(FOREGROUND, shape2)));
		assertEquals(1, group1node.getIndex(tree.get(FOREGROUND, shape1)));
		assertEquals(2, group1node.getIndex(tree.get(FOREGROUND, shape3)));
		assertEquals(0, group2node.getIndex(tree.get(FOREGROUND, shape5)));
		assertEquals(1, group2node.getIndex(tree.get(FOREGROUND, shape4)));
		assertEquals(2, group2node.getIndex(tree.get(FOREGROUND, shape6)));
		
		// TODO test shifting a group
	}
}
