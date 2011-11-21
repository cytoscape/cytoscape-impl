/*
 Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.internal.util.vizmap;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.io.internal.util.vizmap.model.AttributeType;
import org.cytoscape.io.internal.util.vizmap.model.DiscreteMappingEntry;
import org.cytoscape.io.internal.util.vizmap.model.Edge;
import org.cytoscape.io.internal.util.vizmap.model.Network;
import org.cytoscape.io.internal.util.vizmap.model.Node;
import org.cytoscape.io.internal.util.vizmap.model.Vizmap;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.Visualizable;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a utility interface used for converting collections of 
 * VisualStyle objects into serializable Vizmap objects and vice versa. 
 */
public class VisualStyleSerializer {

	private final CalculatorConverterFactory calculatorConverterFactory;
	private final VisualStyleFactory visualStyleFactory;
	private final VisualMappingManager visualMappingManager;
	private final RenderingEngineManager renderingEngineManager;
	private final VisualMappingFunctionFactory discreteMappingFactory;
	private final VisualMappingFunctionFactory continuousMappingFactory;
	private final VisualMappingFunctionFactory passthroughMappingFactory;

	private VisualLexicon lexicon;

	private static final Logger logger = LoggerFactory.getLogger(VisualStyleSerializer.class);

	public VisualStyleSerializer(final CalculatorConverterFactory calculatorConverterFactory,
								 final VisualStyleFactory visualStyleFactory,
								 final VisualMappingManager visualMappingManager,
								 final RenderingEngineManager renderingEngineManager,
								 final VisualMappingFunctionFactory discreteMappingFactory,
								 final VisualMappingFunctionFactory continuousMappingFactory,
								 final VisualMappingFunctionFactory passthroughMappingFactory) {
		this.calculatorConverterFactory = calculatorConverterFactory;
		this.visualStyleFactory = visualStyleFactory;
		this.visualMappingManager = visualMappingManager;
		this.renderingEngineManager = renderingEngineManager;
		this.discreteMappingFactory = discreteMappingFactory;
		this.continuousMappingFactory = continuousMappingFactory;
		this.passthroughMappingFactory = passthroughMappingFactory;
	}

	/**
	 * This method creates a serializable Vizmap object based on the provided collection of visual styles.
	 * @param styles The collection of VisualStyles that you wish to convert into a serializable object.
	 * @return A Vizmap object that contains a representation of the collection of visual styles.
	 */
	public Vizmap createVizmap(Collection<VisualStyle> styles) {
		Vizmap vizmap = new Vizmap();
		lexicon = renderingEngineManager.getDefaultVisualLexicon();

		if (styles != null) {
			for (VisualStyle vs : styles) {
				org.cytoscape.io.internal.util.vizmap.model.VisualStyle vsModel = new org.cytoscape.io.internal.util.vizmap.model.VisualStyle();
				vizmap.getVisualStyle().add(vsModel);

				vsModel.setName(vs.getTitle());

				vsModel.setNetwork(new Network());
				vsModel.setNode(new Node());
				vsModel.setEdge(new Edge());

				createVizmapProperties(vs, MinimalVisualLexicon.NETWORK, vsModel.getNetwork().getVisualProperty());
				createVizmapProperties(vs, MinimalVisualLexicon.NODE, vsModel.getNode().getVisualProperty());
				createVizmapProperties(vs, MinimalVisualLexicon.EDGE, vsModel.getEdge().getVisualProperty());
			}
		}

		return vizmap;
	}

	/**
	 * This method creates a collection of VisualStyle objects based on the provided Vizmap object.
	 * @param vizmap A Vizmap object containing a representation of VisualStyles.
	 * @return A collection of VisualStyle objects.
	 */
	public Set<VisualStyle> createVisualStyles(final Vizmap vizmap) {
		final Set<VisualStyle> styles = new HashSet<VisualStyle>();
		lexicon = renderingEngineManager.getDefaultVisualLexicon();

		if (lexicon == null) {
			logger.warn("Cannot create visual styles because there is no default Visual Lexicon");
			return styles;
		}

		if (vizmap != null) {
			final List<org.cytoscape.io.internal.util.vizmap.model.VisualStyle> vsModelList = vizmap.getVisualStyle();
			VisualStyle defStyle = visualMappingManager.getDefaultVisualStyle();
			final String DEFAULT_STYLE_NAME = defStyle.getTitle();

			for (org.cytoscape.io.internal.util.vizmap.model.VisualStyle vsModel : vsModelList) {
				final String styleName = vsModel.getName();
				// Each new style should be created from the default one:
				final VisualStyle vs;

				if (styleName.equals(DEFAULT_STYLE_NAME)) {
					// If loading the default style, do not create another one,
					// but just modify the current default object!
					vs = defStyle;
					// TODO: delete mappings?
				} else
					vs = visualStyleFactory.getInstance(styleName);

				// Set the visual properties and mappings:
				if (vsModel.getNetwork() != null)
					createVisualProperties(vs, CyNetwork.class, vsModel.getNetwork().getVisualProperty());
				if (vsModel.getNode() != null)
					createVisualProperties(vs, CyNode.class, vsModel.getNode().getVisualProperty());
				if (vsModel.getEdge() != null)
					createVisualProperties(vs, CyEdge.class, vsModel.getEdge().getVisualProperty());

				// Do not add the modified default style to the list!
				if (!vs.equals(defStyle)) 
					styles.add(vs);
			}
		}

		return styles;
	}

	/**
	 * This method creates a collection of VisualStyle objects based on the provided Properties object.
	 * Used to convert old (2.x) vizmap.props format to visual styles.
	 * @param vizmap A Properties object containing a representation of VisualStyles.
	 * @return A collection of VisualStyle objects.
	 */
	public Set<VisualStyle> createVisualStyles(Properties props) {
		// Convert properties to Vizmap:
		Vizmap vizmap = new Vizmap();
		List<org.cytoscape.io.internal.util.vizmap.model.VisualStyle> vizmapStyles = vizmap.getVisualStyle();

		// Group properties keys/values by visual style name:
		Map<String, Map<String, String>> styleNamesMap = new HashMap<String, Map<String, String>>();
		Set<String> propNames = props.stringPropertyNames();

		for (String key : propNames) {
			String value = props.getProperty(key);
			String styleName = CalculatorConverter.parseStyleName(key);

			if (styleName != null) {
				// Add each style name and its properties to a map
				Map<String, String> keyValueMap = styleNamesMap.get(styleName);

				if (keyValueMap == null) {
					keyValueMap = new HashMap<String, String>();
					styleNamesMap.put(styleName, keyValueMap);
				}

				keyValueMap.put(key, value);
			}
		}

		// Create a Visual Style for each style name:
		for (Entry<String, Map<String, String>> entry : styleNamesMap.entrySet()) {
			String styleName = entry.getKey();

			org.cytoscape.io.internal.util.vizmap.model.VisualStyle vs = new org.cytoscape.io.internal.util.vizmap.model.VisualStyle();
			vs.setName(styleName);
			vs.setNetwork(new Network());
			vs.setNode(new Node());
			vs.setEdge(new Edge());

			// Create and set the visual properties and mappings:
			Map<String, String> vsProps = entry.getValue();

			for (Entry<String, String> p : vsProps.entrySet()) {
				String key = p.getKey();
				String value = p.getValue();

				Set<CalculatorConverter> convs = calculatorConverterFactory.getConverters(key);

				for (CalculatorConverter c : convs) {
					c.convert(vs, value, props);
				}
			}

			vizmapStyles.add(vs);
		}

		return createVisualStyles(vizmap);
	}

	@SuppressWarnings("unchecked")
	private <K, V> void createVizmapProperties(VisualStyle vs,
											   VisualProperty<Visualizable> root,
											   List<org.cytoscape.io.internal.util.vizmap.model.VisualProperty> vpModelList) {
		Collection<VisualProperty<?>> vpList = lexicon.getAllDescendants(root);
		Iterator<VisualProperty<?>> iter = vpList.iterator();

		while (iter.hasNext()) {
			VisualProperty<V> vp = (VisualProperty<V>) iter.next();

			// NETWORK root includes NODES and EDGES, but we want to separate the CyNetwork properties!
			if (root == MinimalVisualLexicon.NETWORK && vp.getTargetDataType() != CyNetwork.class) continue;

			V defValue = vs.getDefaultValue(vp);
			VisualMappingFunction<?, V> mapping = vs.getVisualMappingFunction(vp);

			if (defValue != null || mapping != null) {
				org.cytoscape.io.internal.util.vizmap.model.VisualProperty vpModel = new org.cytoscape.io.internal.util.vizmap.model.VisualProperty();
				vpModel.setName(vp.getIdString());

				vpModelList.add(vpModel);

				if (defValue != null) {
					String sValue = vp.toSerializableString(defValue);
					if (sValue != null) vpModel.setDefault(sValue);
				}

				if (mapping instanceof PassthroughMapping<?, ?>) {
					PassthroughMapping<K, V> pm = (PassthroughMapping<K, V>) mapping;
					AttributeType attrType = toAttributeType(pm.getMappingColumnType());

					org.cytoscape.io.internal.util.vizmap.model.PassthroughMapping pmModel = new org.cytoscape.io.internal.util.vizmap.model.PassthroughMapping();
					pmModel.setAttributeName(pm.getMappingColumnName());
					pmModel.setAttributeType(attrType);

					vpModel.setPassthroughMapping(pmModel);

				} else if (mapping instanceof DiscreteMapping<?, ?>) {
					DiscreteMapping<K, V> dm = (DiscreteMapping<K, V>) mapping;
					AttributeType attrType = toAttributeType(dm.getMappingColumnType());

					org.cytoscape.io.internal.util.vizmap.model.DiscreteMapping dmModel = new org.cytoscape.io.internal.util.vizmap.model.DiscreteMapping();
					dmModel.setAttributeName(dm.getMappingColumnName());
					dmModel.setAttributeType(attrType);

					Map<K, V> map = dm.getAll();

					for (Map.Entry<?, V> entry : map.entrySet()) {
						DiscreteMappingEntry entryModel = new DiscreteMappingEntry();
						entryModel.setAttributeValue(entry.getKey().toString());
						entryModel.setValue(vp.toSerializableString(entry.getValue()));

						dmModel.getDiscreteMappingEntry().add(entryModel);
					}

					vpModel.setDiscreteMapping(dmModel);

				} else if (mapping instanceof ContinuousMapping<?, ?>) {
					ContinuousMapping<K, V> cm = (ContinuousMapping<K, V>) mapping;
					AttributeType attrType = toAttributeType(cm.getMappingColumnType());

					org.cytoscape.io.internal.util.vizmap.model.ContinuousMapping cmModel = new org.cytoscape.io.internal.util.vizmap.model.ContinuousMapping();
					cmModel.setAttributeName(cm.getMappingColumnName());
					cmModel.setAttributeType(attrType);

					List<ContinuousMappingPoint<K, V>> points = cm.getAllPoints();

					for (ContinuousMappingPoint<K, V> p : points) {
						org.cytoscape.io.internal.util.vizmap.model.ContinuousMappingPoint pModel = new org.cytoscape.io.internal.util.vizmap.model.ContinuousMappingPoint();

						String sValue = p.getValue().toString();
						BigDecimal value = new BigDecimal(sValue);
						V lesser = p.getRange().lesserValue;
						V equal = p.getRange().equalValue;
						V greater = p.getRange().greaterValue;

						pModel.setAttrValue(value);
						pModel.setLesserValue(vp.toSerializableString(lesser));
						pModel.setEqualValue(vp.toSerializableString(equal));
						pModel.setGreaterValue(vp.toSerializableString(greater));

						cmModel.getContinuousMappingPoint().add(pModel);
					}

					vpModel.setContinuousMapping(cmModel);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <K, V> void createVisualProperties(VisualStyle vs,
											   Class<? extends CyTableEntry> targetType,
											   List<org.cytoscape.io.internal.util.vizmap.model.VisualProperty> vpModelList) {
		for (org.cytoscape.io.internal.util.vizmap.model.VisualProperty vpModel : vpModelList) {
			String vpId = vpModel.getName();
			String defValue = vpModel.getDefault();

			VisualProperty<V> vp = (VisualProperty<V>) lexicon.lookup(targetType, vpId);

			if (vp != null) {
				// Default Value
				if (defValue != null) {
					V value = parseValue(defValue, vp);
					vs.setDefaultValue(vp, value);

					// TODO: dependencies
				}

				// Any mapping?
				if (vpModel.getPassthroughMapping() != null) {
					org.cytoscape.io.internal.util.vizmap.model.PassthroughMapping pmModel = vpModel
							.getPassthroughMapping();
					String attrName = pmModel.getAttributeName();

					try {
						PassthroughMapping<K, V> pm = (PassthroughMapping<K, V>) passthroughMappingFactory
								.createVisualMappingFunction(attrName, String.class, null, vp);

						vs.addVisualMappingFunction(pm);

					} catch (Exception e) {
						logger.error("Cannot create PassthroughMapping (style=" + vs.getTitle() + ", property=" +
									 vp.getIdString() + ")", e);
					}
				} else if (vpModel.getDiscreteMapping() != null) {
					org.cytoscape.io.internal.util.vizmap.model.DiscreteMapping dmModel = vpModel.getDiscreteMapping();
					String attrName = dmModel.getAttributeName();
					AttributeType attrType = dmModel.getAttributeType();

					try {
						Class<?> attrClass = null;

						switch (attrType) {
							case BOOLEAN:
								attrClass = Boolean.class;
								break;
							case FLOAT:
								attrClass = Double.class;
								break;
							case INTEGER:
								// TODO: what about Long attrs?
								attrClass = Integer.class;
								break;
							default:
								attrClass = String.class;
								break;
						}

						DiscreteMapping<K, V> dm = (DiscreteMapping<K, V>) discreteMappingFactory
								.createVisualMappingFunction(attrName, attrClass, null, vp);

						for (DiscreteMappingEntry entryModel : dmModel.getDiscreteMappingEntry()) {
							String sAttrValue = entryModel.getAttributeValue();
							String sValue = entryModel.getValue();

							if (sAttrValue != null && sValue != null) {
								Object attrValue = null;

								switch (attrType) {
									case BOOLEAN:
										attrValue = Boolean.parseBoolean(sAttrValue);
										break;
									case FLOAT:
										attrValue = Double.parseDouble(sAttrValue);
										break;
									case INTEGER:
										// TODO: what if it is a Long?
										attrValue = Integer.parseInt(sAttrValue);
										break;
									default:
										// Note: Always handle List type as String!
										attrValue = sAttrValue;
										break;
								}

								V vpValue = parseValue(sValue, vp);
								if (vpValue != null) dm.putMapValue((K) attrValue, vpValue);
							}
						}

						vs.addVisualMappingFunction(dm);

					} catch (Exception e) {
						logger.error("Cannot create DiscreteMapping (style=" + vs.getTitle() + ", property=" +
									 vp.getIdString() + ")", e);
					}
				} else if (vpModel.getContinuousMapping() != null) {
					org.cytoscape.io.internal.util.vizmap.model.ContinuousMapping cmModel = vpModel
							.getContinuousMapping();
					String attrName = cmModel.getAttributeName();

					try {
						ContinuousMapping<K, V> cm = (ContinuousMapping<K, V>) continuousMappingFactory
								.createVisualMappingFunction(attrName, Number.class, null, vp);

						for (org.cytoscape.io.internal.util.vizmap.model.ContinuousMappingPoint pModel : cmModel
								.getContinuousMappingPoint()) {

							// Should be numbers or colors
							V lesser = parseValue(pModel.getLesserValue(), vp);
							V equal = parseValue(pModel.getEqualValue(), vp);
							V greater = parseValue(pModel.getGreaterValue(), vp);

							BoundaryRangeValues<V> brv = new BoundaryRangeValues<V>(lesser, equal, greater);
							Double attrValue = pModel.getAttrValue().doubleValue();

							cm.addPoint((K) attrValue, brv);
						}

						vs.addVisualMappingFunction(cm);
					} catch (Exception e) {
						logger.error("Cannot create ContinuousMapping (style=" + vs.getTitle() + ", property=" +
									 vp.getIdString() + ")", e);
					}
				}
			}
		}
	}

	private void setDependency(VisualLexicon lexicon, VisualStyle vs, String key, String value) {
		// FIXME: should not be global, but per Visual Style
		if (key.contains("nodeSizeLocked")) {
			boolean b = Boolean.parseBoolean(value);
			lexicon.getVisualLexiconNode(MinimalVisualLexicon.NODE_WIDTH).setDependency(b);
			lexicon.getVisualLexiconNode(MinimalVisualLexicon.NODE_HEIGHT).setDependency(b);
		}
	}

	public <V> V parseValue(String sValue, VisualProperty<V> vp) {
		V value = null;

		if (sValue != null && vp != null) {
			value = vp.parseSerializableString(sValue);
		}

		return value;
	}

	private static AttributeType toAttributeType(Class<?> attrClass) {
		AttributeType attrType = AttributeType.STRING;

		if (attrClass == Boolean.class) {
			attrType = AttributeType.BOOLEAN;
		} else if (attrClass == Byte.class || attrClass == Short.class || attrClass == Integer.class ||
				   attrClass == Long.class) {
			attrType = AttributeType.INTEGER;
		} else if (Number.class.isAssignableFrom(attrClass)) {
			attrType = AttributeType.FLOAT;
		}

		return attrType;
	}
}
