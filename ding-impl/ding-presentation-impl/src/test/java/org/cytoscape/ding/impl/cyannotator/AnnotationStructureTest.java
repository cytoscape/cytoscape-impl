package org.cytoscape.ding.impl.cyannotator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.junit.Ignore;
import org.junit.Test;

public class AnnotationStructureTest extends AnnotationTest {

	
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
		
		// MKTODO test the annotation tree
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
		
		// MKTODO test the annotation tree
	}
	
	
	@Test
	public void testAddGroup3() {
		// MKTODO should this work? Should we require that annotations be added to the root first?
		
		// Don't add the annotations to the cyAnnotator
		ShapeAnnotation shape1 = createShapeAnnotation();
		ShapeAnnotation shape2 = createShapeAnnotation();
		
		// Add the group annotation but not to the cyannotator
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
		
		// MKTODO test the annotation tree
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
		
		// MKTODO test the annotation tree
	}
	
	
	// Illegal tests
	// More than one group at the same time
	// Create a cycle
	// Create a self-cycle
	
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
	
	
	@Ignore
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
		
		// Everything should be fine now
		// MKTODO test the annotation tree instead of just checking the size
		List<Annotation> annotations = annotationManager.getAnnotations(graphView);
		assertEquals(4, annotations.size());
		
		try {
			group3.addMember(group1);
			fail();
		} catch(IllegalAnnotationStructureException e) { }
	}
	
	public void testSpanningCanvases() {
		// MKTODO devise a test that involves annotations spanning canvases
	}
	
}
