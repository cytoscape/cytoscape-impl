package org.cytoscape.filter.internal.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragHandler<V extends SelectPanelComponent> implements DragGestureListener, DragSourceListener, DropTargetListener {
	private static final Logger logger = LoggerFactory.getLogger(DragHandler.class);
	
	private JComponent view;
	private final AbstractPanelController<?, V> controller;
	private final V parent;
	
	private Transferable transferable;

	private static Cursor nextCursor;
	
	public DragHandler(JComponent view, final AbstractPanelController<?, V> controller, final V parent, JComponent handle) {
		this.view = view;
		this.controller = controller;
		this.parent = parent;
		transferable = createTransferable();
		
		if (handle != null) {
			DragSource source = DragSource.getDefaultDragSource();
			source.createDefaultDragGestureRecognizer(handle, DnDConstants.ACTION_COPY_OR_MOVE, this);
		}
	}
	
	@Override
	public void dragExit(DragSourceEvent event) {
	}

	void setBackground(JComponent view, Color color) {
		view.setBackground(color);
		if (view instanceof Handle) {
			JComponent sibling = ((Handle<?>) view).getSiblingView();
			sibling.setBackground(color);
		}
	}
	
	@Override
	public void dragDropEnd(DragSourceDropEvent event) {
		JComponent lastComponent = controller.getLastHoveredComponent();
		if (lastComponent != null) {
			setBackground(lastComponent, ViewUtil.UNSELECTED_BACKGROUND_COLOR);
		}
	}

	@Override
	public void dragEnter(DragSourceDragEvent event) {
	}

	@Override
	public void dragOver(DragSourceDragEvent event) {
		if (isMac()) {
			// DragSourceContext.setCursor() is buggy for the Mac.
			// It's fixed in JDK 8(b86):
			// http://bugs.sun.com/view_bug.do?bug_id=7199783
			return;
		}
		
		if (nextCursor == null) {
			return;
		}
		
		event.getDragSourceContext().setCursor(nextCursor);
		nextCursor = null;
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
		
		Point offset = computeOffset(width, height);
		event.startDrag(null, image, offset, transferable, this);
	}
	
	private Point computeOffset(int width, int height) {
		if (isMac()) {
			// For some weird reason, the default drag image location
			// on Mac is shifted up.  We need to translate it back
			// down.
			return new Point(0, -height);
		}
		return new Point(0, 0);
	}

	private boolean isMac() {
		return System.getProperty("os.name").startsWith("Mac OS X");
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

	JComponent getPrimaryView(JComponent view) {
		if (view instanceof Handle) {
			return ((Handle<?>) view).getSiblingView();
		} else {
			return view;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void dragEnter(DropTargetDragEvent event) {
		JComponent lastComponent = controller.getLastHoveredComponent();
		if (lastComponent != null) {
			setBackground(lastComponent, ViewUtil.UNSELECTED_BACKGROUND_COLOR);
		}

		JComponent target = getPrimaryView(view);
		try {
			List<Integer> path = (List<Integer>) event.getTransferable().getTransferData(PathDataFlavor.instance);
			JComponent sourceView = controller.getChild(parent, path);
			List<Integer> targetPath = controller.getPath(parent, target);
			if (!controller.supportsDrop(parent, path, sourceView, targetPath, target) || isEquivalentLocation(path, targetPath, target)) {
				setCursor(DragSource.DefaultCopyNoDrop, target);
				return;
			}
			if (target instanceof CompositeSeparator || target instanceof CompositeFilterPanel) {
				// Move
				event.acceptDrag(DnDConstants.ACTION_MOVE);
				setCursor(DragSource.DefaultMoveDrop, view);
			} else {
				// Group
				event.acceptDrag(DnDConstants.ACTION_COPY);
				setCursor(DragSource.DefaultCopyDrop, view);
			}
		} catch (UnsupportedFlavorException e) {
			logger.error("Unexpected error", e);
		} catch (IOException e) {
			logger.error("Unexpected error", e);
		} finally {
			controller.setLastHoveredComponent(view);
		}
		
		target.setBackground(ViewUtil.SELECTED_BACKGROUND_COLOR);
	}
	
	private void setCursor(Cursor cursor, Component hoveredView) {
		if (isMac()) {
			// DragSourceContext.setCursor() is buggy for the Mac.
			// It's fixed in JDK 8(b86):
			// http://bugs.sun.com/view_bug.do?bug_id=7199783
			
			// Instead, we set the default cursor on the hovered
			// Component in order to customize the drag-over feedback.
			hoveredView.setCursor(cursor);
		} else {
			if (cursor == null) {
				cursor = Cursor.getDefaultCursor();
			}
			nextCursor = cursor;
		}
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
		setCursor(null, view.getParent());
		setCursor(null, view);
		if (view instanceof Handle) {
			JComponent sibling = ((Handle<?>) view).getSiblingView();
			setCursor(null, sibling);
		}
	}
	
	@Override
	public void dragOver(DropTargetDragEvent event) {
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent event) {
		JComponent target = getPrimaryView(view);
		setCursor(null, view);
		try {
			List<Integer> path = (List<Integer>) event.getTransferable().getTransferData(PathDataFlavor.instance);
			JComponent sourceView = controller.getChild(parent, path);
			if (sourceView == target) {
				return;
			}
			List<Integer> targetPath = controller.getPath(parent, target);
			if (targetPath == null) {
				return;
			}
			controller.handleDrop(parent, sourceView, path, target, targetPath);
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