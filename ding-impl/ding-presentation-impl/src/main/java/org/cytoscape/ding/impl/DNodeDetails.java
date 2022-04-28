package org.cytoscape.ding.impl;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.cytoscape.cg.model.NullCustomGraphics;
import org.cytoscape.ding.DNodeShape;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.CustomGraphicsInfo;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotNodeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LabelBackgroundShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.Justification;
import org.cytoscape.view.presentation.property.values.LabelBackgroundShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.presentation.property.values.Position;


public class DNodeDetails implements NodeDetails {

	private static final float NESTED_IMAGE_SCALE_FACTOR = 0.6f;
	
	private final CyServiceRegistrar registrar;
	
	// These images will be used when a view is not available for a nested network.
	private static BufferedImage DEFAULT_NESTED_NETWORK_IMAGE;
	private static BufferedImage RECURSIVE_NESTED_NETWORK_IMAGE;
	
	// Used to detect recursive rendering of nested networks.
	private static int nestedNetworkPaintingDepth = 0;
	
	static {
		// Initialize image icons for nested networks
		try {
			DEFAULT_NESTED_NETWORK_IMAGE   = ImageIO.read(DNodeDetails.class.getClassLoader().getResource("images/default_network.png"));
			RECURSIVE_NESTED_NETWORK_IMAGE = ImageIO.read(DNodeDetails.class.getClassLoader().getResource("images/recursive_network.png"));
		} catch (IOException e) {
			e.printStackTrace();
			DEFAULT_NESTED_NETWORK_IMAGE = null;
			RECURSIVE_NESTED_NETWORK_IMAGE = null;
		}
	}
	
	public DNodeDetails(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	private static int clamp(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}
	
	@Override
	public boolean isSelected(View<CyNode> nodeView) {
		return Boolean.TRUE.equals(nodeView.getVisualProperty(BasicVisualLexicon.NODE_SELECTED));
	}
	
	static Paint getTransparentColor(Paint p, Integer trans) {
		if(trans == null)
			return p;
		int alpha = clamp(trans, 0, 255);
		if (p instanceof Color && ((Color)p).getAlpha() != alpha) {
			final Color c = (Color) p;
			return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
		} else {
			return p;
		}
	}
	
	@Override
	public Color getColorLowDetail(CyNetworkViewSnapshot netView, View<CyNode> nodeView) {
		if (isSelected(nodeView))
			return getSelectedColorLowDetail(netView, nodeView);
		else
			return getUnselectedColorLowDetail(netView, nodeView);
	}
	
	private Color getUnselectedColorLowDetail(CyNetworkViewSnapshot netView, View<CyNode> nodeView) {
		Paint paint = nodeView.getVisualProperty(NODE_FILL_COLOR);
		if(paint instanceof Color)
			return (Color) paint;
		
		paint = netView.getViewDefault(NODE_FILL_COLOR);
		if(paint instanceof Color)
			return (Color) paint;
		
		return (Color) NODE_FILL_COLOR.getDefault();
	}

	private Color getSelectedColorLowDetail(CyNetworkViewSnapshot netView, View<CyNode> nodeView) {
		Paint paint = nodeView.getVisualProperty(NODE_SELECTED_PAINT);
		if(paint instanceof Color)
			return (Color) paint;
		
		paint = netView.getViewDefault(NODE_SELECTED_PAINT);
		if(paint instanceof Color)
			return (Color) paint;
		
		return (Color) NODE_SELECTED_PAINT.getDefault();
	}

	@Override
	public Paint getFillPaint(View<CyNode> nodeView) {
		if (isSelected(nodeView))
			return getSelectedPaint(nodeView);
		else
			return getUnselectedPaint(nodeView);
	}

	private Paint getSelectedPaint(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_SELECTED_PAINT);
	}

	public Paint getUnselectedPaint(View<CyNode> nodeView) {
		Paint paint = nodeView.getVisualProperty(NODE_FILL_COLOR);
		Integer trans = nodeView.getVisualProperty(NODE_TRANSPARENCY);
		return getTransparentColor(paint, trans);
	}

	@Override
	public double getWidth(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_WIDTH);
	}
	
	@Override
	public double getHeight(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_HEIGHT);
	}
	
	@Override
	public float getBorderWidth(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_BORDER_WIDTH).floatValue();
	}

	@Override
	public Stroke getBorderStroke(View<CyNode> nodeView) {
		float borderWidth = getBorderWidth(nodeView);
		LineType lineType = nodeView.getVisualProperty(NODE_BORDER_LINE_TYPE);
		return DLineType.getDLineType(lineType).getStroke(borderWidth);
	}
	
	@Override
	public Paint getBorderPaint(View<CyNode> nodeView) {
		Paint paint = nodeView.getVisualProperty(NODE_BORDER_PAINT);
		Integer trans = nodeView.getVisualProperty(NODE_BORDER_TRANSPARENCY);
		return getTransparentColor(paint, trans);
	}

	@Override
	public String getLabelText(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_LABEL);
	}

	@Override
	public String getTooltipText(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_TOOLTIP);
	}

	@Override
	public Font getLabelFont(View<CyNode> nodeView, boolean forPdf) {
		Font font = nodeView.getVisualProperty(NODE_LABEL_FONT_FACE);
		Number size = nodeView.getVisualProperty(NODE_LABEL_FONT_SIZE);
		return computeFont(font, size, forPdf);
	}
	
	public static Font computeFont(Font font, Number size, boolean forPdf) {
		if(font == null)
			return null;
		if(size != null)
			font = font.deriveFont(size.floatValue());
		
		// The iText PDF library won't render italic/bold unless the style bits are explicitly set.
		// Unfortunately we have to use this font name hack to guess if the font is bold or italic.
		if(forPdf) {
			String name = font.getName().toLowerCase();
			boolean italic = name.contains("italic") || name.contains("oblique");
			boolean bold   = name.contains("bold");
			if(italic && bold)
				font = font.deriveFont(Font.ITALIC | Font.BOLD);
			else if(italic)
				font = font.deriveFont(Font.ITALIC);
			else if(bold)
				font = font.deriveFont(Font.BOLD);
		}
		
		return font;
	}

	@Override
	public Paint getLabelPaint(View<CyNode> nodeView) {
		Paint paint = nodeView.getVisualProperty(NODE_LABEL_COLOR);
		Integer trans = nodeView.getVisualProperty(NODE_LABEL_TRANSPARENCY);
		return getTransparentColor(paint, trans);
	}
	
	@Override
	public Paint getLabelBackgroundPaint(View<CyNode> nodeView) {
		Paint paint = nodeView.getVisualProperty(NODE_LABEL_BACKGROUND_COLOR);
		Integer trans = nodeView.getVisualProperty(NODE_LABEL_BACKGROUND_TRANSPARENCY);
		return getTransparentColor(paint, trans);
	}
	
	@Override
	public byte getLabelBackgroundShape(View<CyNode> nodeView) {
		return getLabelBackgroundShape(nodeView.getVisualProperty(NODE_LABEL_BACKGROUND_SHAPE));
	}
	
	public static byte getLabelBackgroundShape(LabelBackgroundShape lbs) {
		if(lbs == LabelBackgroundShapeVisualProperty.RECTANGLE) {
			return GraphGraphics.SHAPE_RECTANGLE;
		} else if(lbs == LabelBackgroundShapeVisualProperty.ROUND_RECTANGLE) {
			return GraphGraphics.SHAPE_ROUNDED_RECTANGLE;
		} else {
			return GraphGraphics.SHAPE_NONE;
		}
	}
	
	@Override
	public byte getShape(View<CyNode> nodeView) {
		return DNodeShape.getDShape(nodeView.getVisualProperty(NODE_SHAPE)).getNativeShape();
	}

	@Override
	public double getLabelRotation(View<CyNode> nodeView) {
		Double dAngle = nodeView.getVisualProperty(NODE_LABEL_ROTATION);
		return dAngle == null ? 0.0 : dAngle;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private CustomGraphicsInfo getCustomGraphicsInfo(VisualProperty<CyCustomGraphics> cgVP, View<CyNode> node) {
		CyCustomGraphics<CustomGraphicLayer> cg = (CyCustomGraphics<CustomGraphicLayer>) node.getVisualProperty(cgVP);
		if(cg == null || cg == NullCustomGraphics.getNullObject())
			return null;
		
		VisualProperty<Double> sizeVP = DVisualLexicon.getAssociatedCustomGraphicsSizeVP(cgVP);
		Double size = node.getVisualProperty(sizeVP);
		
		VisualProperty<ObjectPosition> positionVP = DVisualLexicon.getAssociatedCustomGraphicsPositionVP(cgVP);
		ObjectPosition position = node.getVisualProperty(positionVP);
		
		CustomGraphicsInfo info = new CustomGraphicsInfo(cgVP);
		info.setCustomGraphics(cg);
		info.setSize(size);
		info.setPosition(position);
		return info;
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public Map<VisualProperty<CyCustomGraphics>, CustomGraphicsInfo> getCustomGraphics(View<CyNode> nodeView) {
		Map<VisualProperty<CyCustomGraphics>, CustomGraphicsInfo> cgInfoMap = null;
		
		for(VisualProperty<CyCustomGraphics> cgVP : DVisualLexicon.getCustomGraphicsVisualProperties()) {
			CustomGraphicsInfo info = getCustomGraphicsInfo(cgVP, nodeView);
			if(info != null) {
				if(cgInfoMap == null) {
					cgInfoMap = new TreeMap<>(Comparator.comparing(VisualProperty::getIdString));
				}
				cgInfoMap.put(cgVP, info);
			}
		}
		return cgInfoMap;
	}
	
	@Override
	public Position getLabelTextAnchor(View<CyNode> nodeView) {
		ObjectPosition pos = nodeView.getVisualProperty(NODE_LABEL_POSITION);
		return pos == null ? null : pos.getAnchor();
	}

	@Override
	public Position getLabelNodeAnchor(View<CyNode> nodeView) {
		ObjectPosition pos = nodeView.getVisualProperty(NODE_LABEL_POSITION);
		return pos == null ? null : pos.getTargetAnchor();
	}

	@Override
	public float getLabelOffsetVectorX(View<CyNode> nodeView) {
		ObjectPosition pos = nodeView.getVisualProperty(NODE_LABEL_POSITION);
		return pos == null ? 0.0f : (float) pos.getOffsetX();
	}

	@Override
	public float getLabelOffsetVectorY(View<CyNode> nodeView) {
		ObjectPosition pos = nodeView.getVisualProperty(NODE_LABEL_POSITION);
		return pos == null ? 0.0f : (float) pos.getOffsetY();
	}

	@Override
	public Justification getLabelJustify(View<CyNode> nodeView) {
		ObjectPosition pos = nodeView.getVisualProperty(NODE_LABEL_POSITION);
		return pos == null ? null : pos.getJustify();
	}
	
	@Override
	public double getLabelWidth(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_LABEL_WIDTH);
	}
	
	////// Transparencies /////////////
	
	public Integer getTransparency(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_TRANSPARENCY);
	}

	public Integer getLabelTransparency(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_LABEL_TRANSPARENCY);
	}

	@Override
	public Integer getBorderTransparency(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_BORDER_TRANSPARENCY);
	}

	public Boolean getNestedNetworkImgVisible(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_NESTED_NETWORK_IMAGE_VISIBLE);
	}

	public Double getNodeDepth(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_DEPTH);
	}

	@Override
	public double getXPosition(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_X_LOCATION);
	}

	@Override
	public double getYPosition(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_Y_LOCATION);
	}
	
	@Override
	public double getZPosition(View<CyNode> nodeView) {
		return nodeView.getVisualProperty(NODE_Z_LOCATION);
	}
	
	
	@Override
	public TexturePaint getNestedNetworkTexturePaint(CyNetworkViewSnapshot netView, View<CyNode> nodeView) {
		if(registrar == null)
			return null;
		
		++nestedNetworkPaintingDepth;
		try {
			boolean nestedNetworkVisible = getNestedNetworkImgVisible(nodeView);
			if(!Boolean.TRUE.equals(nestedNetworkVisible)) {
				return null;
			}
			
			SnapshotNodeInfo nodeInfo = netView.getNodeInfo(nodeView);
			CyNode modelNode = netView.getMutableNetworkView().getModel().getNode(nodeInfo.getModelSUID());
			
			if (modelNode == null || nestedNetworkPaintingDepth > 1 ||  modelNode.getNetworkPointer() == null)
				return null;

			final double IMAGE_WIDTH  = getWidth(nodeView)  * NESTED_IMAGE_SCALE_FACTOR;
			final double IMAGE_HEIGHT = getHeight(nodeView) * NESTED_IMAGE_SCALE_FACTOR;

			CyNetworkView nestedNetworkView = getNestedNetworkView(netView, nodeView);

			// Do we have a node w/ a self-reference?
			if (netView.getMutableNetworkView() == nestedNetworkView) {
				if (RECURSIVE_NESTED_NETWORK_IMAGE == null)
					return null;

				final Rectangle2D rect = new Rectangle2D.Double(-IMAGE_WIDTH / 2, -IMAGE_HEIGHT / 2, IMAGE_WIDTH, IMAGE_HEIGHT);
				return new TexturePaint(RECURSIVE_NESTED_NETWORK_IMAGE, rect);
			}
			
			if (nestedNetworkView != null) {
				DingRenderer dingRenderer = registrar.getService(DingRenderer.class);
				
				DRenderingEngine re = dingRenderer.getRenderingEngine(netView.getMutableNetworkView());
				double scaleFactor = re.getGraphLOD().getNestedNetworkImageScaleFactor();
				
				DRenderingEngine nestedRe = dingRenderer.getRenderingEngine(nestedNetworkView);
				if(nestedRe == null)
					return null; // not a Ding network
				
				return nestedRe.getSnapshot(IMAGE_WIDTH * scaleFactor, IMAGE_HEIGHT * scaleFactor);
			} else {
				if (DEFAULT_NESTED_NETWORK_IMAGE == null || getWidth(nodeView) == -1 || getHeight(nodeView) == -1)
					return null;

				Rectangle2D rect = new Rectangle2D.Double(-IMAGE_WIDTH / 2, -IMAGE_HEIGHT / 2, IMAGE_WIDTH, IMAGE_HEIGHT);
				return new TexturePaint(DEFAULT_NESTED_NETWORK_IMAGE, rect);
			}
		} finally {
			--nestedNetworkPaintingDepth;
		}
	}

	public CyNetworkView getNestedNetworkView(CyNetworkViewSnapshot netView, View<CyNode> nodeView) {
		if(registrar == null)
			return null;
		
		SnapshotNodeInfo nodeInfo = netView.getNodeInfo(nodeView);
		CyNode modelNode = netView.getMutableNetworkView().getModel().getNode(nodeInfo.getModelSUID());
		
		if(modelNode.getNetworkPointer() == null)
			return null;
		
		CyNetworkViewManager netViewMgr = registrar.getService(CyNetworkViewManager.class);
		Iterator<CyNetworkView> viewIterator = netViewMgr.getNetworkViews(modelNode.getNetworkPointer()).iterator();
		
		if(viewIterator.hasNext())
			return viewIterator.next();
		
		return null;
	}

}
