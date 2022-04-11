package org.cytoscape.editor.internal;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
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

public class ClipboardImpl {
	
	private final CyNetworkView sourceView;
	private final Set<CyNode> nodes;
	private final Set<CyEdge> edges;
	private final Set<Annotation> annotations;
	private final Map<CyNode, Map<VisualProperty<?>, Object>> nodeBypass;
	private final Map<CyEdge, Map<VisualProperty<?>, Object>> edgeBypassMap;
	private boolean cutOperation;
	
	private final Map<CyNode, double[]> nodePositions;
	private final Map<Long, Integer> multiPasteOffset; // CyNetworkView SUID is key, value is number of times pasted to that network

	// Row maps
	private Map<CyIdentifiable, CyRow> oldSharedRowMap;
	private Map<CyIdentifiable, CyRow> oldLocalRowMap;
	private Map<CyIdentifiable, CyRow> oldHiddenRowMap;
	private Map<CyRow, Map<String, Object>> oldValueMap;

	private double xTopLeft, yTopLeft;
	
	private final List<AnnotationFactory<? extends Annotation>> annotationFactories;
	private final CyServiceRegistrar serviceRegistrar;

	public ClipboardImpl(
			CyNetworkView sourceView,
			Set<CyNode> nodes,
			Set<CyEdge> edges,
			boolean cut,
			VisualLexicon lexicon,
			List<AnnotationFactory<? extends Annotation>> annotationFactories,
			CyServiceRegistrar serviceRegistrar
	) {
		this(sourceView, nodes, edges, null, cut, lexicon, annotationFactories, serviceRegistrar);
	}
	
	public ClipboardImpl(
			CyNetworkView sourceView,
			Collection<CyNode> nodes,
			Collection<CyEdge> edges,
			Collection<Annotation> annotations,
			boolean cut,
			VisualLexicon lexicon,
			List<AnnotationFactory<? extends Annotation>> annotationFactories,
			CyServiceRegistrar serviceRegistrar
	) {
		this.sourceView = sourceView;
		this.nodes = nodes != null ? new HashSet<>(nodes) : Collections.emptySet();
		this.edges = edges != null ? new HashSet<>(edges) : Collections.emptySet();
		this.annotations = annotations != null ? new HashSet<>(annotations) : Collections.emptySet();
		this.cutOperation = cut;
		this.serviceRegistrar = serviceRegistrar;
		this.annotationFactories = annotationFactories;
		
		oldSharedRowMap = new WeakHashMap<>();
		oldLocalRowMap = new WeakHashMap<>();
		oldHiddenRowMap = new WeakHashMap<>();
		
		nodeBypass = new HashMap<>();
		edgeBypassMap = new HashMap<>();
		
		nodePositions = new HashMap<>();
		multiPasteOffset = new HashMap<>();

		// For local and hidden rows, we also need to keep track of
		// the values since they will be removed when the row gets removed
		// This is only really necessary for cut operations
		oldValueMap = new WeakHashMap<>();
		
		init(lexicon);
	}
	
	private void init(VisualLexicon lexicon) {
		var sourceNetwork = sourceView.getModel();

		// We need the root network to get the shared attributes
		var sourceRootNetwork = ((CySubNetwork) sourceNetwork).getRootNetwork();

		var nodeProps = lexicon.getAllDescendants(BasicVisualLexicon.NODE);
		var edgeProps = lexicon.getAllDescendants(BasicVisualLexicon.EDGE);
		
		
		// calculate top-left of nodes
		double[] topLeft = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
		Consumer<View<CyNode>> updateTopLeft = nodeView -> {
			double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			double w = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
			double h = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
			topLeft[0] = Math.min(topLeft[0], x - (w * 0.5));
			topLeft[1] = Math.min(topLeft[1], y - (h * 0.5));
		};
		
		for (var node : nodes) {
			addRows(node, sourceRootNetwork, sourceNetwork);

			var nodeView = sourceView.getNodeView(node);
			if (nodeView == null)
				continue;

			saveNodePosition(nodeView);
			saveLockedValues(nodeView, nodeProps, nodeBypass);
			
			updateTopLeft.accept(nodeView);
		}
		
		for (var edge : edges) {
			addRows(edge, sourceRootNetwork, sourceNetwork);

			var edgeView = sourceView.getEdgeView(edge);
			if (edgeView == null)
				continue;

			saveLockedValues(edgeView, edgeProps, edgeBypassMap);

			// If the source/target nodes of this edge were not copied, we still have
			// to save any bypass values set to their views, because the source/target nodes
			// may have to be pasted later (e.g. target network does not contain the source or target of this edge)
			var src = edgeView.getModel().getSource();
			var tgt = edgeView.getModel().getTarget();
			
			if (!nodes.contains(src)) {
				var nodeView = sourceView.getNodeView(src);
				if (nodeView != null) {
					saveNodePosition(nodeView);
					saveLockedValues(nodeView, nodeProps, nodeBypass);
				}
				updateTopLeft.accept(nodeView);
			}

			if (!nodes.contains(tgt)) {
				var nodeView = sourceView.getNodeView(tgt);
				if (nodeView != null) {
					saveNodePosition(nodeView);
					saveLockedValues(nodeView, nodeProps, nodeBypass);
				}
				updateTopLeft.accept(nodeView);
			}
		}
		
		for (var annotation : annotations) {
			var bounds = annotation.getRotatedBounds();
			var x = bounds.getMinX();
			var y = bounds.getMinY();
			
			topLeft[0] = Math.min(topLeft[0], x);
			topLeft[1] = Math.min(topLeft[1], y);
		}
		
		if(!Double.isFinite(topLeft[0]) || !Double.isFinite(topLeft[1])) {
			topLeft[0] = 0.0;
			topLeft[1] = 0.0;
		}
		this.xTopLeft = topLeft[0];
		this.yTopLeft = topLeft[1];
	}

	public Set<CyNode> getNodes() {
		return nodes;
	}

	public Set<CyEdge> getEdges() {
		return edges;
	}
	
	public Set<Annotation> getAnnotations() {
		return annotations;
	}

	public boolean clipboardHasData() {
		return !nodes.isEmpty() || !edges.isEmpty() || !annotations.isEmpty();
	}

	public Collection<Object> paste(CyNetworkView targetView, double x, double y) {
		var pastedObjects = new LinkedHashSet<Object>();
		var rowMap = new HashMap<CyRow, CyRow>();

		// We need to do this in 4 passes.
		// In pass 1, we'll add all of the nodes and store their (possibly new) SUID.
		// In pass 2, we'll add the edges.
		// In pass 3, we'll reposition the nodes and paste any locked visual properties node views.
		// In pass 4, we'll paste any locked visual properties to edge views
		// Note that if we add any nodes, we'll only add edges to nodes that exist.  

		// Pass 1: add the nodes 
		var newNodeMap = new HashMap<CyNode, CyNode>();
		
		for (var node : nodes) {
			var newNode = pasteNode(sourceView, targetView, node, rowMap);
			newNodeMap.put(node, newNode);
			pastedObjects.add(newNode);
		}

		// Pass 2: add the edges
		var newEdgeMap = new HashMap<CyEdge, CyEdge>();
		
		for (var edge : edges) {
			var newEdge = pasteEdge(sourceView, targetView, edge, rowMap, newNodeMap, pastedObjects);
			
			if (newEdge != null) {
				newEdgeMap.put(edge, newEdge);
				pastedObjects.add(newEdge);
			}
		}

		copyRows(rowMap);
		
		var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.flushPayloadEvents(); // Make sure node/edge views were created
		targetView.updateView();

		// Calculate new position
		// adding moves down and to the right
		final int downwardShift = 10;
		final int upwardShift = 50;
		
		int shiftTimes = multiPasteOffset.merge(targetView.getSUID(), 1, (a,b) -> a + 1);
		double xOffset = shiftTimes * downwardShift;
		double yOffset = shiftTimes * downwardShift;
	
		double centerx = targetView.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION);
		double centery = targetView.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION);
		double scale   = targetView.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
		double width   = targetView.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH);
		double height  = targetView.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT);
		
		// Find the bounds of the visible area in node coordinates
		double xMin = centerx - ((0.5 * width)  / scale);
		double yMin = centery - ((0.5 * height) / scale);
		double xMax = centerx + ((0.5 * width)  / scale); 
		double yMax = centery + ((0.5 * height) / scale);
		
		// if the paste location is outside the visible area then adjust so that it is inside
		if(yTopLeft + yOffset < yMin) { // above the visible area
			yOffset = yOffset + (yMin - yTopLeft);
		}
		if(xTopLeft + xOffset < xMin) { // left of the visible area
			xOffset = xOffset + (xMin - xTopLeft);
		}
		if(yTopLeft > yMax) { // below the visible area
			yOffset = yMax - yTopLeft - upwardShift;
		}
		if(xTopLeft > xMax) { // right of the visible area
			xOffset = xMax - xTopLeft - upwardShift;
		}
		
		// Pass 3: paste locked visual properties and reposition the new node views
		for (var node : nodePositions.keySet()) {
			var newNode = newNodeMap.get(node);
			if (newNode == null || !pastedObjects.contains(newNode))
				continue;
			
			var position = nodePositions.get(node);
			double nodeX = (position == null ? 0 : position[0]) + xOffset;
			double nodeY = (position == null ? 0 : position[1]) + yOffset;

			// Now, get the new node view
			var newNodeView = targetView.getNodeView(newNode);
			
			if (newNodeView != null) {
				newNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, nodeX);
				newNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, nodeY);
				setLockedValues(newNodeView, node, nodeBypass);
			}
		}
		
		// Pass 4: paste locked visual properties to new edge views
		if (!edgeBypassMap.isEmpty()) {
			for (var edge : edgeBypassMap.keySet()) {
				// Now, get the new edge view
				var newEdge = newEdgeMap.get(edge);
				var newEdgeView = newEdge != null ? targetView.getEdgeView(newEdge) : null;
				
				if (newEdgeView != null)
					setLockedValues(newEdgeView, edge, edgeBypassMap);
			}
		}

		// Pass 5: Fix up selection.  For some reason, even though the selected column is set
		// the nodes and edges don't show as selected.  We need to fix that now
		for (var object : pastedObjects) {
			// Special-case selected!!!
			if (object instanceof CyIdentifiable ele) {
				if (isSelected(targetView, ele)) {
					reselect(targetView, ele);
				}
			}
		}
		
		// Pass 6: Paste annotations, except ArrowAnnotations
		var annotationMgr = serviceRegistrar.getService(AnnotationManager.class);
		var newAnnotationMap = new HashMap<Annotation, Annotation>();
		var arrowAnnotations = new LinkedHashSet<ArrowAnnotation>();
		
		for (var a : annotations) {
			if (a instanceof ArrowAnnotation) {
				arrowAnnotations.add((ArrowAnnotation) a);
			} else {
				var na = pasteAnnotation(targetView, annotationMgr, a, xOffset, yOffset);
				if (na != null) {
					newAnnotationMap.put(a, na);
					pastedObjects.add(na);
				}
			}
		}
		
		// Pass 7: Paste ArrowAnnotations
		for (var a : arrowAnnotations) {
			var na = pasteArrowAnnotation(targetView, annotationMgr, a, newAnnotationMap, newNodeMap, xOffset, yOffset);
			if(na != null) {
				pastedObjects.add(na);
			}
		}

		return pastedObjects;
	}

	private CyEdge pasteEdge(
			CyNetworkView sourceView,
			CyNetworkView targetView, 
			CyEdge edge,
			Map<CyRow, CyRow> rowMap,
			Map<CyNode, CyNode> newNodeMap,
			Collection<Object> pastedObjects
	) {
		var sourceNetwork = sourceView.getModel();
		var sourceRoot = ((CySubNetwork)sourceNetwork).getRootNetwork();

		var targetNetwork = (CySubNetwork)targetView.getModel();
		var targetRoot = targetNetwork.getRootNetwork();

		CyEdge newEdge = null;
		var sourceNode = edge.getSource();
		var targetNode = edge.getTarget();

		// Same three cases as pasteNode, but we need to be careful to add missing nodes.  If
		// we paste an edge, but there's no corresponding node, we need to copy the
		// node in if we're copying a bare edge.
		//
		// Three cases:
		// 1) We're copying edges to a new network in a different network tree
		// 2) We're copying edges to a new network in the same network tree
		// 3) We're copying edges to a new location in the same network
		if (sourceRoot != targetRoot) {
			// Case 1: different root

			if (!newNodeMap.containsKey(sourceNode)) {
				addRows(sourceNode, sourceRoot, sourceNetwork);
				var newNode = pasteNode(sourceView, targetView, sourceNode, rowMap);
				newNodeMap.put(sourceNode, newNode);
				pastedObjects.add(newNode);
			}

			if (!newNodeMap.containsKey(targetNode)) {
				addRows(targetNode, sourceRoot, sourceNetwork);
				var newNode = pasteNode(sourceView, targetView, targetNode, rowMap);
				newNodeMap.put(targetNode, newNode);
				pastedObjects.add(newNode);
			}

			// Create the edge
			newEdge = targetNetwork.addEdge(newNodeMap.get(sourceNode), newNodeMap.get(targetNode), edge.isDirected());

			// Copy the attributes over
			rowMap.put(oldSharedRowMap.get(edge), targetNetwork.getRow(newEdge, CyNetwork.DEFAULT_ATTRS));
			rowMap.put(oldLocalRowMap.get(edge), targetNetwork.getRow(newEdge, CyNetwork.LOCAL_ATTRS));
			rowMap.put(oldHiddenRowMap.get(edge), targetNetwork.getRow(newEdge, CyNetwork.HIDDEN_ATTRS));
		} else {
			// Case 2: different network, same root and
			// Case 3: same network

			// First, see if we already have the nodes
			if (!newNodeMap.containsKey(sourceNode)) {
				if (targetNetwork.containsNode(sourceNode)) {
					newNodeMap.put(sourceNode, sourceNode);
				} else {
					addRows(sourceNode, sourceRoot, sourceNetwork);
					var newNode = pasteNode(sourceView, targetView, sourceNode, rowMap);
					newNodeMap.put(sourceNode, newNode);
					pastedObjects.add(newNode);
				}
			}

			if (!newNodeMap.containsKey(targetNode)) {
				if (targetNetwork.containsNode(targetNode)) {
					newNodeMap.put(targetNode, targetNode);
				} else {
					addRows(targetNode, sourceRoot, sourceNetwork);
					var newNode = pasteNode(sourceView, targetView, targetNode, rowMap);
					newNodeMap.put(targetNode, newNode);
					pastedObjects.add(newNode);
				}
			}

			if (targetNetwork.containsEdge(edge)) {
				// We want to create another copy of the edge
				newEdge = targetNetwork.addEdge(newNodeMap.get(sourceNode), 
				                                newNodeMap.get(targetNode), edge.isDirected());
			} else {
				// We just want to add the existing edge to this subnetwork
				targetNetwork.addEdge(edge);
				newEdge = edge;
			}
			
			// Copy the attributes over
			rowMap.put(oldLocalRowMap.get(edge), targetNetwork.getRow(newEdge, CyNetwork.LOCAL_ATTRS));
			rowMap.put(oldHiddenRowMap.get(edge), targetNetwork.getRow(newEdge, CyNetwork.HIDDEN_ATTRS));
		}
		
		return newEdge;
	}

	// TODO: Need to figure out how to copy LOCAL_ATTRS, SHARED_ATTRS, and HIDDEN_ATTRS
	// The latter is easy.  The second two are both part of the DEFAULT_ATTRS, but it's
	// not clear how to create a local attribute specifically....
	private CyNode pasteNode(
			CyNetworkView sourceView,
			CyNetworkView targetView,
			CyNode node,
			Map<CyRow, CyRow> rowMap
	) {
		var sourceNetwork = sourceView.getModel();
		var sourceRoot = ((CySubNetwork) sourceNetwork).getRootNetwork();

		var targetNetwork = (CySubNetwork) targetView.getModel();
		var targetRoot = targetNetwork.getRootNetwork();

		CyNode newNode = null;

		// Three cases:
		// 1) We're copying nodes to a new network in a different network tree
		// 2) We're copying nodes to a new network in the same network tree
		// 3) We're copying nodes to a new location in the same network
		if (sourceRoot != targetRoot) {
			// Case 1: Different roots
			newNode = targetNetwork.addNode();
			// Copy the attributes over
			rowMap.put(oldSharedRowMap.get(node), targetNetwork.getRow(newNode, CyNetwork.DEFAULT_ATTRS));
			rowMap.put(oldLocalRowMap.get(node), targetNetwork.getRow(newNode, CyNetwork.LOCAL_ATTRS));
			rowMap.put(oldHiddenRowMap.get(node), targetNetwork.getRow(newNode, CyNetwork.HIDDEN_ATTRS));
		} else if (!targetNetwork.containsNode(node)) {
			// Case 2: different subnetwork, same root
			targetNetwork.addNode(node);
			newNode = node;
			// rowMap.put(oldSharedRowMap.get(node), targetNetwork.getRow(newNode, CyNetwork.DEFAULT_ATTRS));
			rowMap.put(oldLocalRowMap.get(node), targetNetwork.getRow(newNode, CyNetwork.LOCAL_ATTRS));
			rowMap.put(oldHiddenRowMap.get(node), targetNetwork.getRow(newNode, CyNetwork.HIDDEN_ATTRS));
		} else {
			// Case 3: Copying the node to the same network
			// newNode = targetNetwork.addNode();
			// Copy in the hidden attributes
			newNode = targetNetwork.addNode();
			rowMap.put(oldHiddenRowMap.get(node), targetNetwork.getRow(newNode, CyNetwork.HIDDEN_ATTRS));
			// Copy in the local attributes
			rowMap.put(oldLocalRowMap.get(node), targetNetwork.getRow(newNode, CyNetwork.LOCAL_ATTRS));
			// Copy in the default attributes
			// rowMap.put(oldSharedRowMap.get(node), targetNetwork.getRow(newNode, CyNetwork.DEFAULT_ATTRS));
			// targetNetwork.addNode(node);
		}
		
		return newNode;
	}
	
	private Annotation pasteAnnotation(
			CyNetworkView targetView,
			AnnotationManager annotationMgr,
			Annotation a,
			double xOffset, 
			double yOffset
	) {
		var na = cloneAnnotation(targetView, a, xOffset, yOffset);
		if (na != null)
			annotationMgr.addAnnotation(na);
		return na;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrowAnnotation pasteArrowAnnotation(
			CyNetworkView targetView,
			AnnotationManager annotationMgr,
			ArrowAnnotation a,
			HashMap<Annotation, Annotation> newAnnotationMap,
			HashMap<CyNode, CyNode> newNodeMap,
			double xOffset,
			double yOffset
	) {
		var na = cloneAnnotation(targetView, a, xOffset, yOffset);
		
		if (na instanceof ArrowAnnotation) {
			var newArrow = (ArrowAnnotation) na;
			
			// Reassign source
			var src = ((ArrowAnnotation) a).getSource();
			
			if (src != null) {
				if (newAnnotationMap.containsKey(src))
					src = newAnnotationMap.get(src); // The source is a pasted annotation
				else if (!targetView.equals(sourceView)) 
					src = null; // Pasting to a different view, but the source has not been copied
			}
			
			if (src == null)
				return null;
			
			newArrow.setSource(src);
			
			// Reassign target
			var tgt = ((ArrowAnnotation) a).getTarget();

			if (tgt instanceof Annotation) {
				if (newAnnotationMap.containsKey(tgt))
					tgt = newAnnotationMap.get(tgt); // The source is a pasted node
				else if (!targetView.equals(sourceView))
					tgt = null; // Pasting to a different view, but the target annotation has not been copied
				
				newArrow.setTarget((Annotation) tgt);
			} else if (tgt instanceof View) {
				if (newNodeMap.containsKey(((View<?>) tgt).getModel())) {
					// The source is a pasted node
					var node = newNodeMap.get(((View<?>) tgt).getModel());
					tgt = targetView.getNodeView(node);
				} else if (!targetView.equals(sourceView)) {
					var sourceNetwork = sourceView.getModel();
					var sourceRoot = ((CySubNetwork) sourceNetwork).getRootNetwork();

					var targetNetwork = (CySubNetwork) targetView.getModel();
					var targetRoot = targetNetwork.getRootNetwork();
					
					if (sourceRoot.equals(targetRoot)) {
						// Different view that belongs to the same root network
						if (((View<?>) tgt).getModel() instanceof CyNode) {
							var node = ((View<CyNode>) tgt).getModel();
							tgt = targetView.getNodeView(node);
						} else {
							tgt = null; // View<CyEdge> not supported!
						}
					} else {
						// Different view of another root network, but the target node has not been copied
						tgt = null;
					}
				}
				
				newArrow.setTarget((View) tgt);
			} else if (tgt instanceof Point2D) {
				newArrow.setTarget((Point2D) tgt);
			}
			
			if (newArrow.getTarget() != null) {
				annotationMgr.addAnnotation(newArrow);
			
				return newArrow;
			}
		}
		
		return null;
	}

	private void copyRows(Map<CyRow, CyRow> rowMap) {
		if (rowMap == null || rowMap.size() == 0)
			return;

		for (var sourceRow: rowMap.keySet()) {
			var targetRow = rowMap.get(sourceRow);
			var destTable = targetRow.getTable();
			var sourceTable = sourceRow.getTable();

			final Map<String, Object> oldDataMap;
			
			if (oldValueMap.containsKey(sourceRow))
				oldDataMap = oldValueMap.get(sourceRow);
			else
				oldDataMap = sourceRow.getAllValues();

			for (var colName : oldDataMap.keySet()) {
				var column = destTable.getColumn(colName);

				if (column == null) {
					var sourceColumn = sourceTable.getColumn(colName);
					
					if (sourceColumn.getType() == List.class) {
						destTable.createListColumn(colName, sourceColumn.getListElementType(),
								sourceColumn.isImmutable());
					} else {
						destTable.createColumn(colName, sourceColumn.getType(), sourceColumn.isImmutable());
					}
				} else if (column.isPrimaryKey()) {
					continue;
				} else {
					// Column already exists.  We need to check for virtual columns that don't join
					// on SUID (since that's the only thing we're changing).  If they don't, we need to skip them.
					var virtualInfo = column.getVirtualColumnInfo();
					
					if (virtualInfo.isVirtual() && !virtualInfo.getTargetJoinKey().equals(CyNetwork.SUID))
						continue;
				}

				// We need to be careful of the facade table.  If the sourceTable and
				// the targetTable are the same, and this column is virtual, we don't
				// want to copy it.
				try {
					targetRow.set(colName, oldDataMap.get(colName));
				} catch (IllegalArgumentException e) {
					// Log a warning
					System.out.println("Set failed!  " + e);
				} 
			}
		}
	}

	public void addRows(CyIdentifiable object, CyRootNetwork sourceRootNetwork, CyNetwork sourceNetwork) {
		oldSharedRowMap.put(object, sourceRootNetwork.getRow(object, CyRootNetwork.SHARED_ATTRS));

		var localRow = sourceNetwork.getRow(object, CyNetwork.LOCAL_ATTRS); // Careful, this is actually a facade
		oldLocalRowMap.put(object, localRow);

		// If we cut this object, then the row data was removed.  We need to copy the local data for it
		if (cutOperation)
			oldValueMap.put(localRow, copyRowValues(localRow));

		var hiddenRow = sourceNetwork.getRow(object, CyNetwork.HIDDEN_ATTRS);
		oldHiddenRowMap.put(object, hiddenRow);

		if (cutOperation)
			oldValueMap.put(hiddenRow, copyRowValues(hiddenRow));
	}

	private Map<String, Object> copyRowValues(CyRow row) {
		// Make a copy of the row values
		return new HashMap<>(row.getAllValues());
	}

	private double[] saveNodePosition(View<CyNode> view) {
		if (view == null)
			return null;
		
		double x = view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		double y = view.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		var position = new double[]{x, y};
		nodePositions.put(view.getModel(), position);
		
		return position;
	}
	
	private Annotation cloneAnnotation(CyNetworkView targetView, Annotation a, double xOffset, double yOffset) {
		var argMap = new HashMap<>(a.getArgMap());
		argMap.remove("uuid");
		
		argMap.put(Annotation.X, Double.toString(a.getX() + xOffset));
		argMap.put(Annotation.Y, Double.toString(a.getY() + yOffset));
		
		var type = argMap.get("type");
		if (type == null)
			return null;
		
		switch(type) {
			case "ARROW", "org.cytoscape.view.presentation.annotations.ArrowAnnotation":
				return createAnnotation(ArrowAnnotation.class, targetView, argMap);
				
			case "SHAPE", "org.cytoscape.view.presentation.annotations.ShapeAnnotation":
				return createAnnotation(ShapeAnnotation.class, targetView, argMap);
				
			case "TEXT", "org.cytoscape.view.presentation.annotations.TextAnnotation":
				return createAnnotation(TextAnnotation.class, targetView, argMap);
				
			case "BOUNDEDTEXT", "org.cytoscape.view.presentation.annotations.BoundedTextAnnotation":
				return createAnnotation(BoundedTextAnnotation.class, targetView, argMap);
				
			case "IMAGE", "org.cytoscape.view.presentation.annotations.ImageAnnotation":
				return createAnnotation(ImageAnnotation.class, targetView, argMap);
				
			case "GROUP", "org.cytoscape.view.presentation.annotations.GroupAnnotation":
				argMap.remove(GroupAnnotation.MEMBERS);
				var newGroup = (GroupAnnotation) createAnnotation(GroupAnnotation.class, targetView, argMap);
				for(var child : ((GroupAnnotation)a).getMembers()) {
					Annotation newChild = cloneAnnotation(targetView, child, xOffset, yOffset);
					newGroup.addMember(newChild);
				}
				return newGroup;
				
			default:
				return null;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Annotation createAnnotation(Class type, CyNetworkView netView, Map<String, String> argMap) {
		for (var factory : annotationFactories) {
			var a = factory.createAnnotation(type, netView, argMap);
			if (a != null)
				return a;
		}
		return null;
	}

	private boolean isSelected(CyNetworkView networkView, CyIdentifiable object) {
		var network = networkView.getModel();
		return Boolean.TRUE.equals(network.getRow(object).get(CyNetwork.SELECTED, Boolean.class));
	}

	private void reselect(CyNetworkView networkView, CyIdentifiable object) {
		var network = networkView.getModel();
		network.getRow(object).set(CyNetwork.SELECTED, false);
		network.getRow(object).set(CyNetwork.SELECTED, true);
	}

	static <T extends CyIdentifiable> void saveLockedValues(
			View<T> view,
			Collection<VisualProperty<?>> visualProps,
			Map<T, Map<VisualProperty<?>, Object>> bypassMap
	) {
		if (view == null)
			return;
		
		for (var vp : visualProps) {
			if (view.isValueLocked(vp)) {
				var vpMap = bypassMap.get(view.getModel());
				if (vpMap == null)
					bypassMap.put(view.getModel(), vpMap = new HashMap<>());
				
				vpMap.put(vp, view.getVisualProperty(vp));
			}
		}
	}
	
	static <T extends CyIdentifiable> void setLockedValues(
			View<T> target,
			T orginalModel,
			Map<T, Map<VisualProperty<?>, Object>> bypassMap
	) {
		var vpMap = bypassMap.get(orginalModel);
		if (vpMap != null) {
			for (var entry : vpMap.entrySet())
				target.setLockedValue(entry.getKey(), entry.getValue());
		}
	}
}
