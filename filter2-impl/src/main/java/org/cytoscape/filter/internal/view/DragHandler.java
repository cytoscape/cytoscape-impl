package org.cytoscape.filter.internal.view;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;

import org.cytoscape.filter.internal.composite.CompositeFilterPanel;
import org.cytoscape.filter.internal.composite.CompositeSeparator;
import org.cytoscape.filter.internal.composite.CompositeTransformerPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragHandler<V extends SelectPanelComponent> implements DragGestureListener, DragSourceListener, DropTargetListener {
	private static final Logger logger = LoggerFactory.getLogger(DragHandler.class);
	
	private JComponent view;
	private final AbstractPanelController<?, V> controller;
	private final V parent;
	
	private Transferable transferable;

	public DragHandler(JComponent view, final AbstractPanelController<?, V> controller, final V parent, JComponent handle) {
		this.view = view;
		this.controller = controller;
		this.parent = parent;
		transferable = createTransferable();
		
		if (handle != null) {
			DragSource source = DragSource.getDefaultDragSource();
			source.createDefaultDragGestureRecognizer(handle, DnDConstants.ACTION_MOVE, this);
		}
	}
	
	@Override
	public void dragExit(DragSourceEvent event) {
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent event) {
		JComponent lastComponent = controller.getLastHoveredComponent();
		if (lastComponent != null) {
			lastComponent.setBackground(ViewUtil.UNSELECTED_BACKGROUND_COLOR);
		}
	}

	@Override
	public void dragEnter(DragSourceDragEvent event) {
	}

	@Override
	public void dragOver(DragSourceDragEvent event) {
	}

	@Override
	public void dropActionChanged(DragSourceDragEvent event) {
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent event) {
		int width = view.getWidth();
		int height = view.getHeight();
		BufferedImage image = view.getGraphicsConfiguration().createCompatibleImage(width, height);
		Graphics2D graphics = image.createGraphics();
		view.setBackground(ViewUtil.UNSELECTED_BACKGROUND_COLOR);
		view.paint(graphics);
		
		Point offset = new Point(0, -height);
		event.startDrag(DragSource.DefaultLinkDrop, image, offset, transferable, this);
	}
	
	Transferable createTransferable() {
		return new Transferable() {
			
			@Override
			public boolean isDataFlavorSupported(DataFlavor flavour) {
				return flavour instanceof PathDataFlavor;
			}
			
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { PathDataFlavor.instance };
			}
			
			@Override
			public Object getTransferData(DataFlavor flavour) throws UnsupportedFlavorException, IOException {
				if (flavour instanceof PathDataFlavor) {
					return controller.getPath(parent, view);
				}
				return null;
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void dragEnter(DropTargetDragEvent event) {
		JComponent lastComponent = controller.getLastHoveredComponent();
		if (lastComponent != null) {
			lastComponent.setBackground(ViewUtil.UNSELECTED_BACKGROUND_COLOR);
		}

		try {
			List<Integer> path = (List<Integer>) event.getTransferable().getTransferData(PathDataFlavor.instance);
			JComponent sourceView = controller.getChild(parent, path);
			List<Integer> targetPath = controller.getPath(parent, view);
			if (!controller.supportsDrop(parent, sourceView, view) || isEquivalentLocation(path, targetPath, view)) {
				event.rejectDrag();
				view.setCursor(DragSource.DefaultCopyNoDrop);
				return;
			}
			if (view instanceof CompositeSeparator || view instanceof CompositeFilterPanel || view instanceof CompositeTransformerPanel) {
				// Move
				view.setCursor(DragSource.DefaultMoveDrop);
			} else {
				// Group
				view.setCursor(DragSource.DefaultCopyDrop);
			}
		} catch (UnsupportedFlavorException e) {
			logger.error("Unexpected error", e);
		} catch (IOException e) {
			logger.error("Unexpected error", e);
		}
		
		view.setBackground(ViewUtil.SELECTED_BACKGROUND_COLOR);
		controller.setLastHoveredComponent(view);
	}
	
	private boolean isEquivalentLocation(List<Integer> source, List<Integer> target, JComponent targetComponent) {
		int size = source.size();
		if (size != target.size()) {
			return false;
		}
		for (int i = 0; i < size - 2; i++) {
			if (source.get(i) != target.get(i)) {
				return false;
			}
		}
		int sourceIndex = source.get(size - 1);
		int targetIndex = target.get(size - 1);
		return sourceIndex == targetIndex || (sourceIndex - 1 == targetIndex && targetComponent instanceof CompositeSeparator);
	}

	@Override
	public void dragExit(DropTargetEvent event) {
		view.getParent().setCursor(null);
		view.setCursor(null);
	}
	
	@Override
	public void dragOver(DropTargetDragEvent event) {
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent event) {
		view.setCursor(null);
		try {
			List<Integer> path = (List<Integer>) event.getTransferable().getTransferData(PathDataFlavor.instance);
			JComponent sourceView = controller.getChild(parent, path);
			if (sourceView == view) {
				return;
			}
			List<Integer> targetPath = controller.getPath(parent, view);
			if (targetPath == null) {
				return;
			}
			controller.handleDrop(parent, sourceView, path, view, targetPath);
			event.acceptDrop(DnDConstants.ACTION_LINK);
			event.dropComplete(true);
		} catch (UnsupportedFlavorException e) {
			logger.error("Unexpected error", e);
		} catch (IOException e) {
			logger.error("Unexpected error", e);
		}
	}
	
	@Override
	public void dropActionChanged(DropTargetDragEvent event) {
	}
}