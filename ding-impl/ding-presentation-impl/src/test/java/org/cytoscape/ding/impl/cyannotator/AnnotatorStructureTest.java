package org.cytoscape.ding.impl.cyannotator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.junit.Ignore;
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
	
	
	public void testSpanningCanvases() {
		// MKTODO devise a test that involves annotations spanning canvases
	}
	
	
	public void testZOrderReset() {
		
	}
	
	
	public void testGroupAutoAddChildren() {
		// if you add a group with children it should auto add the children
	}
}
