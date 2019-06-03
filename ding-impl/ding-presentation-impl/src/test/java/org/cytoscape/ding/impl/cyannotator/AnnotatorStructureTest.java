package org.cytoscape.ding.impl.cyannotator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.junit.Test;

public class AnnotatorStructureTest extends AbstractAnnotationTest {

	
	@Test
	public void testAddGroup1() {
		ShapeAnnotation shape1 = createShapeAnnotation();
		ShapeAnnotation shape2 = createShapeAnnotation();
		annotationManager.addAnnotation(shape1);
		annotationManager.addAnnotation(shape2);
		
		// Add the group annotation to cyAnnotator BEFORE adding the annotations to the group
		GroupAnnotation group1 = createGroupAnnotation();
		annotationManager.addAnnotation(group1);
		group1.addMember(shape1);
		group1.addMember(shape2);
		
		// it should all make sense
		List<Annotation> annotations = annotationManager.getAnnotations(graphView);
		assertEquals(3, annotations.size());
		assertTrue(annotations.contains(shape1));
		assertTrue(annotations.contains(shape2));
		assertTrue(annotations.contains(group1));
	}
	
	
	@Test
	public void testAddGroup2() {
		ShapeAnnotation shape1 = createShapeAnnotation();
		ShapeAnnotation shape2 = createShapeAnnotation();
		annotationManager.addAnnotation(shape1);
		annotationManager.addAnnotation(shape2);
		
		// Add the group annotation to the group BEFORE adding the annotations to cyannotator
		GroupAnnotation group1 = createGroupAnnotation();
		group1.addMember(shape1);
		group1.addMember(shape2);
		annotationManager.addAnnotation(group1);
		
		// it should all make sense
		List<Annotation> annotations = annotationManager.getAnnotations(graphView);
		assertEquals(3, annotations.size());
		assertTrue(annotations.contains(shape1));
		assertTrue(annotations.contains(shape2));
		assertTrue(annotations.contains(group1));
	}
	
	
	@Test
	public void testAddGroup3() {
		// Don't add the annotations to the cyAnnotator
		ShapeAnnotation shape1 = createShapeAnnotation();
		ShapeAnnotation shape2 = createShapeAnnotation();
		
		// Add the group annotation but not to the cyannotator
		GroupAnnotation group1 = createGroupAnnotation();
		group1.addMember(shape1);
		group1.addMember(shape2);
		annotationManager.addAnnotations(Arrays.asList(shape1, shape2, group1));
		
		// it should all make sense
		List<Annotation> annotations = annotationManager.getAnnotations(graphView);
		assertEquals(3, annotations.size());
		assertTrue(annotations.contains(shape1));
		assertTrue(annotations.contains(shape2));
		assertTrue(annotations.contains(group1));
	}
	
	
	@Test
	public void testAddGroup4() {
		// test nesting of groups
		ShapeAnnotation shape1 = createShapeAnnotation();
		ShapeAnnotation shape2 = createShapeAnnotation();
		ShapeAnnotation shape3 = createShapeAnnotation();
		annotationManager.addAnnotation(shape1);
		annotationManager.addAnnotation(shape2);
		annotationManager.addAnnotation(shape3);
		
		GroupAnnotation group1 = createGroupAnnotation();
		group1.addMember(shape1);
		group1.addMember(shape2);
		annotationManager.addAnnotation(group1);
		
		GroupAnnotation group2 = createGroupAnnotation();
		group2.addMember(group1);
		group2.addMember(shape3);
		annotationManager.addAnnotation(group2);
		
		List<Annotation> annotations = annotationManager.getAnnotations(graphView);
		assertEquals(5, annotations.size());
	}
	
	
	@Test
	public void testIllegalGroup1() {
		// Annotation can't be in more than one group at the same time (test both leaf and group annotation)
		ShapeAnnotation shape1 = createShapeAnnotation();
		ShapeAnnotation shape2 = createShapeAnnotation();
		GroupAnnotation group1 = createGroupAnnotation();
		group1.addMember(shape1);
		group1.addMember(shape2);
		annotationManager.addAnnotation(group1);
		
		GroupAnnotation group2 = createGroupAnnotation();
		annotationManager.addAnnotation(group2);
		
		try {
			group2.addMember(shape1);
			fail();
		} catch(IllegalAnnotationStructureException e) { }
	}
	
	
	@Test
	public void testIllegalGroup2() {
		// Same idea as previous test but just a little more complicated
		ShapeAnnotation shape1 = createShapeAnnotation();
		ShapeAnnotation shape2 = createShapeAnnotation();
		ShapeAnnotation shape3 = createShapeAnnotation();
		annotationManager.addAnnotation(shape1);
		annotationManager.addAnnotation(shape2);
		annotationManager.addAnnotation(shape3);
		
		GroupAnnotation group1 = createGroupAnnotation();
		group1.addMember(shape1);
		group1.addMember(shape2);
		annotationManager.addAnnotation(group1);
		
		GroupAnnotation group2 = createGroupAnnotation();
		group2.addMember(group1);
		group2.addMember(shape3);
		annotationManager.addAnnotation(group2);
		
		try {
			group2.addMember(shape1);
			fail();
		} catch(IllegalAnnotationStructureException e) { }
	}
	
	
	@Test
	public void testIllegalCycle1() {
		// Annotation can't be in more than one group at the same time (test both leaf and group annotation)
		GroupAnnotation group1 = createGroupAnnotation();
		group1.addMember(group1);
		
		try {
			annotationManager.addAnnotation(group1);
			fail();
		} catch(IllegalAnnotationStructureException e) { }
	}
	
	@Test
	public void testIllegalCycle2() {
		// Annotation can't be in more than one group at the same time (test both leaf and group annotation)
		ShapeAnnotation shape1 = createShapeAnnotation();
		GroupAnnotation group1 = createGroupAnnotation();
		GroupAnnotation group2 = createGroupAnnotation();
		GroupAnnotation group3 = createGroupAnnotation();
		group1.addMember(group2);
		group2.addMember(group3);
		group3.addMember(shape1);
		
		// Everything should be fine at this point
		annotationManager.addAnnotations(Arrays.asList(group1, group2, group3, shape1));
		List<Annotation> annotations = annotationManager.getAnnotations(graphView);
		assertEquals(4, annotations.size());
		
		try {
			group3.addMember(group1);
			fail();
		} catch(IllegalAnnotationStructureException e) { }
		
		// make sure the state didn't change
		assertEquals(1, group3.getMembers().size());
		assertEquals(shape1, group3.getMembers().get(0));
		assertNull(((DingAnnotation)group1).getGroupParent());
	}
	
	
	@Test
	public void testIllegalCycle3() {
		// Annotation can't be in more than one group at the same time (test both leaf and group annotation)
		ShapeAnnotation shape1 = createShapeAnnotation();
		GroupAnnotation group1 = createGroupAnnotation();
		group1.addMember(shape1);
		
		// Everything should be fine at this point
		annotationManager.addAnnotations(Arrays.asList(group1, shape1));
		List<Annotation> annotations = annotationManager.getAnnotations(graphView);
		assertEquals(2, annotations.size());
		
		GroupAnnotation group2 = createGroupAnnotation();
		GroupAnnotation group3 = createGroupAnnotation();
		group1.addMember(group2);
		group2.addMember(group3);
		group3.addMember(group1);
		
		try {
			annotationManager.addAnnotations(Arrays.asList(group2));
			fail();
		} catch(IllegalAnnotationStructureException e) { }
		
	}
	
	@Test
	public void testIllegalCycle4() {
		// Annotation can't be in more than one group at the same time (test both leaf and group annotation)
		ShapeAnnotation shape1 = createShapeAnnotation();
		GroupAnnotation group1 = createGroupAnnotation();
		group1.addMember(shape1);
		
		// Everything should be fine at this point
		annotationManager.addAnnotations(Arrays.asList(group1, shape1));
		List<Annotation> annotations = annotationManager.getAnnotations(graphView);
		assertEquals(2, annotations.size());
		
		GroupAnnotation group2 = createGroupAnnotation();
		GroupAnnotation group3 = createGroupAnnotation();
		group2.addMember(group3);
		
		try {
			group3.addMember(group3);
			fail();
		} catch(IllegalAnnotationStructureException e) { }
		
	}
	
	@Test
	public void testGroupAutoAddChildren1() {
		ShapeAnnotation shape1 = createShapeAnnotation();
		ShapeAnnotation shape2 = createShapeAnnotation();
		GroupAnnotation group1 = createGroupAnnotation();
		GroupAnnotation group2 = createGroupAnnotation();
		GroupAnnotation group3 = createGroupAnnotation();
		group1.addMember(group2);
		group2.addMember(group3);
		group3.addMember(shape1);
		group3.addMember(shape2);
		
		annotationManager.addAnnotation(group1);
		
		List<Annotation> annotations = annotationManager.getAnnotations(graphView);
		assertEquals(5, annotations.size());
		
		assertSame(graphView, shape1.getNetworkView());
		assertSame(graphView, shape2.getNetworkView());
		assertSame(graphView, group1.getNetworkView());
		assertSame(graphView, group2.getNetworkView());
		assertSame(graphView, group3.getNetworkView());
		
		// MKTODO fix this
//		DingCanvas foreground = graphView.getCanvas(Canvas.FOREGROUND_CANVAS);
//		assertSame(foreground, ((AbstractAnnotation)shape1).getCanvas());
//		assertSame(foreground, ((AbstractAnnotation)shape2).getCanvas());
//		assertSame(foreground, ((AbstractAnnotation)group1).getCanvas());
//		assertSame(foreground, ((AbstractAnnotation)group2).getCanvas());
//		assertSame(foreground, ((AbstractAnnotation)group3).getCanvas());
		
		annotationManager.removeAnnotation(group1);
		
		annotations = annotationManager.getAnnotations(graphView);
		assertEquals(0, annotations.size());
	}
	
	@Test
	public void testGroupAutoAddChildren2() {
		GroupAnnotation group1 = createGroupAnnotation("group1", 0, Annotation.FOREGROUND);
		ShapeAnnotation shape1 = createShapeAnnotation("shape1", 0, Annotation.FOREGROUND);
		ShapeAnnotation shape2 = createShapeAnnotation("shape2", 0, Annotation.BACKGROUND);
		ShapeAnnotation shape3 = createShapeAnnotation("shape3", 0, Annotation.FOREGROUND);
		ShapeAnnotation shape4 = createShapeAnnotation("shape4", 0, Annotation.BACKGROUND);
		group1.addMember(shape1);
		group1.addMember(shape2);
		group1.addMember(shape3);
		group1.addMember(shape4);
		
		annotationManager.addAnnotation(group1);
		
		assertEquals(5, annotationManager.getAnnotations(graphView).size());
		
		annotationManager.removeAnnotation(group1);
		
		assertEquals(0, annotationManager.getAnnotations(graphView).size());
	}
	
	@Test
	public void testGroupAutoAddChildren3() {
		GroupAnnotation group1 = createGroupAnnotation("group1", 0, Annotation.BACKGROUND); // group on BACKGROUND
		ShapeAnnotation shape1 = createShapeAnnotation("shape1", 0, Annotation.FOREGROUND);
		ShapeAnnotation shape2 = createShapeAnnotation("shape2", 0, Annotation.BACKGROUND);
		ShapeAnnotation shape3 = createShapeAnnotation("shape3", 0, Annotation.FOREGROUND);
		ShapeAnnotation shape4 = createShapeAnnotation("shape4", 0, Annotation.BACKGROUND);
		group1.addMember(shape1);
		group1.addMember(shape2);
		group1.addMember(shape3);
		group1.addMember(shape4);
		
		annotationManager.addAnnotation(group1);
		
		assertEquals(5, annotationManager.getAnnotations(graphView).size());
		
		annotationManager.removeAnnotation(group1);
		
		assertEquals(0, annotationManager.getAnnotations(graphView).size());
	}
	
	
	@Test
	public void testGroupCanvas() {
		// group annotations MUST be on the foreground canvas
		ShapeAnnotation shape1 = createShapeAnnotation("shape1", 0, Annotation.FOREGROUND);
		ShapeAnnotation shape2 = createShapeAnnotation("shape2", 1, Annotation.FOREGROUND);
		GroupAnnotation group1 = createGroupAnnotation("group1", 2, Annotation.FOREGROUND);
		GroupAnnotation group2 = createGroupAnnotation("group2", 3, Annotation.BACKGROUND); // uh-oh
		group1.addMember(group2);
		group2.addMember(shape1);
		group2.addMember(shape2);
		
		annotationManager.addAnnotation(group1);
		
		assertEquals(4, annotationManager.getAnnotations(graphView).size());
		
		assertSame(graphView, group2.getNetworkView());
		
		// MKTODO fix this
//		DingCanvas foreground = graphView.getCanvas(Canvas.FOREGROUND_CANVAS);
//		assertSame(foreground, ((AbstractAnnotation)shape1).getCanvas());
//		assertSame(foreground, ((AbstractAnnotation)shape2).getCanvas());
//		assertSame(foreground, ((AbstractAnnotation)group1).getCanvas());
//		assertSame(foreground, ((AbstractAnnotation)group2).getCanvas());
	}
}
