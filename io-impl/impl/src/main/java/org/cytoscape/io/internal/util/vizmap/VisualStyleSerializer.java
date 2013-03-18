package org.cytoscape.io.internal.util.vizmap;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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
import org.cytoscape.io.internal.util.vizmap.model.Dependency;
import org.cytoscape.io.internal.util.vizmap.model.DiscreteMappingEntry;
import org.cytoscape.io.internal.util.vizmap.model.Edge;
import org.cytoscape.io.internal.util.vizmap.model.Network;
import org.cytoscape.io.internal.util.vizmap.model.Node;
import org.cytoscape.io.internal.util.vizmap.model.Vizmap;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.Visualizable;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
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
	private final RenderingEngineManager renderingEngineManager;
	private final VisualMappingFunctionFactory discreteMappingFactory;
	private final VisualMappingFunctionFactory continuousMappingFactory;
	private final VisualMappingFunctionFactory passthroughMappingFactory;

	private VisualLexicon lexicon;

	private static final Logger logger = LoggerFactory.getLogger(VisualStyleSerializer.class);

	public VisualStyleSerializer(final CalculatorConverterFactory calculatorConverterFactory,
								 final VisualStyleFactory visualStyleFactory,
								 final RenderingEngineManager renderingEngineManager,
								 final VisualMappingFunctionFactory discreteMappingFactory,
								 final VisualMappingFunctionFactory continuousMappingFactory,
								 final VisualMappingFunctionFactory passthroughMappingFactory) {
		this.calculatorConverterFactory = calculatorConverterFactory;
		this.visualStyleFactory = visualStyleFactory;
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
	public Vizmap createVizmap(final Collection<VisualStyle> styles) {
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

				createVizmapProperties(vs, BasicVisualLexicon.NETWORK, vsModel.getNetwork().getVisualProperty());
				createVizmapProperties(vs, BasicVisualLexicon.NODE, vsModel.getNode().getVisualProperty());
				createVizmapProperties(vs, BasicVisualLexicon.EDGE, vsModel.getEdge().getVisualProperty());
				
				// Create Dependencies
				createDependencies(vs, vsModel);
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

			for (org.cytoscape.io.internal.util.vizmap.model.VisualStyle vsModel : vsModelList) {
				final String styleName = vsModel.getName();
				// Each new style should be created from the default one:
				final VisualStyle vs = visualStyleFactory.createVisualStyle(styleName);

				// Set the visual properties and mappings:
				if (vsModel.getNetwork() != null)
					createVisualProperties(vs, CyNetwork.class, vsModel.getNetwork().getVisualProperty());
				if (vsModel.getNode() != null)
					createVisualProperties(vs, CyNode.class, vsModel.getNode().getVisualProperty());
				if (vsModel.getEdge() != null)
					createVisualProperties(vs, CyEdge.class, vsModel.getEdge().getVisualProperty());

				// Restore dependency
				restoreDependencies(vs, vsModel);
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
	public Set<VisualStyle> createVisualStyles(final Properties props) {
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createVizmapProperties(final VisualStyle vs, final VisualProperty<Visualizable> root,
			final List<org.cytoscape.io.internal.util.vizmap.model.VisualProperty> vpModelList) {
		
		final Collection<VisualProperty<?>> vpList = lexicon.getAllDescendants(root);
		final Iterator<VisualProperty<?>> iter = vpList.iterator();

		while (iter.hasNext()) {
			final VisualProperty vp = iter.next();
			
			try {
				// NETWORK root includes NODES and EDGES, but we want to separate the CyNetwork properties!
				if (root == BasicVisualLexicon.NETWORK && vp.getTargetDataType() != CyNetwork.class)
					continue;
	
				Object defValue = vs.getDefaultValue(vp);
				final VisualMappingFunction<?, ?> mapping = vs.getVisualMappingFunction(vp);
	
				if (defValue != null || mapping != null) {
					org.cytoscape.io.internal.util.vizmap.model.VisualProperty vpModel = new org.cytoscape.io.internal.util.vizmap.model.VisualProperty();
					vpModel.setName(vp.getIdString());
	
					vpModelList.add(vpModel);
	
					if (defValue != null) {
						String sValue = vp.toSerializableString(defValue);
						
						if (sValue != null)
							vpModel.setDefault(sValue);
					}
	
					if (mapping instanceof PassthroughMapping<?, ?>) {
						PassthroughMapping<?, ?> pm = (PassthroughMapping<?, ?>) mapping;
						AttributeType attrType = toAttributeType(pm.getMappingColumnType());
	
						org.cytoscape.io.internal.util.vizmap.model.PassthroughMapping pmModel = new org.cytoscape.io.internal.util.vizmap.model.PassthroughMapping();
						pmModel.setAttributeName(pm.getMappingColumnName());
						pmModel.setAttributeType(attrType);
	
						vpModel.setPassthroughMapping(pmModel);
	
					} else if (mapping instanceof DiscreteMapping<?, ?>) {
						DiscreteMapping<?, ?> dm = (DiscreteMapping<?, ?>) mapping;
						AttributeType attrType = toAttributeType(dm.getMappingColumnType());
	
						org.cytoscape.io.internal.util.vizmap.model.DiscreteMapping dmModel = new org.cytoscape.io.internal.util.vizmap.model.DiscreteMapping();
						dmModel.setAttributeName(dm.getMappingColumnName());
						dmModel.setAttributeType(attrType);
	
						Map<?, ?> map = dm.getAll();
	
						for (Map.Entry<?, ?> entry : map.entrySet()) {
							final Object value = entry.getValue();
							if (value == null)
								continue;

							try {
								DiscreteMappingEntry entryModel = new DiscreteMappingEntry();
								entryModel.setAttributeValue(entry.getKey().toString());
								entryModel.setValue(vp.toSerializableString(value));
								dmModel.getDiscreteMappingEntry().add(entryModel);
							} catch (Exception e) {
								logger.warn("Could not add Discrete Mapping entry: " + value, e);
							}
						}
	
						vpModel.setDiscreteMapping(dmModel);
	
					} else if (mapping instanceof ContinuousMapping<?, ?>) {
						final ContinuousMapping<?,?> cm = (ContinuousMapping<?, ?>) mapping;
						AttributeType attrType = toAttributeType(cm.getMappingColumnType());
	
						org.cytoscape.io.internal.util.vizmap.model.ContinuousMapping cmModel = new org.cytoscape.io.internal.util.vizmap.model.ContinuousMapping();
						cmModel.setAttributeName(cm.getMappingColumnName());
						cmModel.setAttributeType(attrType);
	
						List<?> points = cm.getAllPoints();
	
						for (Object point : points) {
							ContinuousMappingPoint<?, ?> continuousPoint = (ContinuousMappingPoint<?, ?>) point;
							org.cytoscape.io.internal.util.vizmap.model.ContinuousMappingPoint pModel = new org.cytoscape.io.internal.util.vizmap.model.ContinuousMappingPoint();
	
							Object originalValue = continuousPoint.getValue();
							
							final String sValue = originalValue.toString();
							final BigDecimal value = new BigDecimal(sValue);
							pModel.setAttrValue(value);
							
							Object lesser = continuousPoint.getRange().lesserValue;
							Object equal = continuousPoint.getRange().equalValue;
							Object greater = continuousPoint.getRange().greaterValue;
							
							pModel.setLesserValue(vp.toSerializableString(lesser));
							pModel.setEqualValue(vp.toSerializableString(equal));
							pModel.setGreaterValue(vp.toSerializableString(greater));
	
							cmModel.getContinuousMappingPoint().add(pModel);
						}
	
						vpModel.setContinuousMapping(cmModel);
					}
				}
			} catch (final Exception e) {
				logger.error("Cannot save visual property: " + (vp != null ? vp.getDisplayName() : ""), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <K, V> void createVisualProperties(VisualStyle vs,
											   Class<? extends CyIdentifiable> targetType,
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
				}

				// Any mapping?
				if (vpModel.getPassthroughMapping() != null) {
					org.cytoscape.io.internal.util.vizmap.model.PassthroughMapping pmModel = vpModel
							.getPassthroughMapping();
					final String attrName = pmModel.getAttributeName();
					final AttributeType attrType = pmModel.getAttributeType();
					final Class<?> columnDataType;
					
					if (attrType == AttributeType.BOOLEAN)
						columnDataType = Boolean.class;
					else if (attrType == AttributeType.FLOAT)
						columnDataType = Double.class;
					else if (attrType == AttributeType.INTEGER)
						columnDataType = Integer.class;
					else if (attrType == AttributeType.LONG)
						columnDataType = Long.class;
					else if (attrType == AttributeType.LIST)
						columnDataType = List.class;
					else
						columnDataType = String.class;
					
					try {
						PassthroughMapping<K, V> pm = (PassthroughMapping<K, V>) passthroughMappingFactory
								.createVisualMappingFunction(attrName, columnDataType, vp);

						vs.addVisualMappingFunction(pm);

					} catch (Throwable e) {
						logger.error("Cannot create PassthroughMapping (style=" + vs.getTitle() + ", property=" +
									 vp.getIdString() + ")", e);
					}
				} else if (vpModel.getDiscreteMapping() != null) {
					org.cytoscape.io.internal.util.vizmap.model.DiscreteMapping dmModel = vpModel.getDiscreteMapping();
					String attrName = dmModel.getAttributeName();
					AttributeType attrType = dmModel.getAttributeType();
					
					try {
						Class<?> attrClass = null;

						// TODO refactor attr type assignment
						switch (attrType) {
							case BOOLEAN:
								attrClass = Boolean.class;
								break;
							case FLOAT:
								attrClass = Double.class;
								break;
							case INTEGER:
								attrClass = Integer.class;
								break;
							case LONG:
								attrClass = Long.class;
								break;
							default:
								attrClass = String.class;
								break;
						}

						DiscreteMapping<K, V> dm = (DiscreteMapping<K, V>) discreteMappingFactory
								.createVisualMappingFunction(attrName, attrClass, vp);

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
										attrValue = Integer.parseInt(sAttrValue);
										break;
									case LONG:
										attrValue = Long.parseLong(sAttrValue);
										break;
									default:
										// Note: Always handle List type as String!
										attrValue = sAttrValue;
										break;
								}

								V vpValue = parseValue(sValue, vp);
								
								if (vpValue != null)
									dm.putMapValue((K) attrValue, vpValue);
							}
						}

						vs.addVisualMappingFunction(dm);

					} catch (Throwable e) {
						logger.error("Cannot create DiscreteMapping (style=" + vs.getTitle() + ", property=" +
									 vp.getIdString() + ")", e);
					}
				} else if (vpModel.getContinuousMapping() != null) {
					org.cytoscape.io.internal.util.vizmap.model.ContinuousMapping cmModel = vpModel
							.getContinuousMapping();
					String attrName = cmModel.getAttributeName();

					try {
						ContinuousMapping<K, V> cm = (ContinuousMapping<K, V>) continuousMappingFactory
								.createVisualMappingFunction(attrName, Number.class, vp);

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
					} catch (Throwable e) {
						logger.error("Cannot create ContinuousMapping (style=" + vs.getTitle() + ", property=" +
									 vp.getIdString() + ")", e);
					}
				}
			}
		}
	}
	
	private void createDependencies(final VisualStyle visualStyle,
			org.cytoscape.io.internal.util.vizmap.model.VisualStyle vsModel) {
		// Create serializable Dependency
		final Set<VisualPropertyDependency<?>> dependencies = visualStyle.getAllVisualPropertyDependencies();

		final List<Dependency> nodeDep = vsModel.getNode().getDependency();
		final List<Dependency> edgeDep = vsModel.getEdge().getDependency();
		final List<Dependency> networkDep = vsModel.getNetwork().getDependency();

		Collection<VisualProperty<?>> nodeVisualProperties = lexicon.getAllDescendants(BasicVisualLexicon.NODE);
		Collection<VisualProperty<?>> edgeVisualProperties = lexicon.getAllDescendants(BasicVisualLexicon.EDGE);

		for (VisualPropertyDependency<?> vpDep : dependencies) {
			try {
				final Dependency newDependency = new Dependency();
				newDependency.setName(vpDep.getIdString());
				newDependency.setValue(vpDep.isDependencyEnabled());
	
				final VisualProperty<?> parent = vpDep.getParentVisualProperty();
				
				if (nodeVisualProperties.contains(parent))
					nodeDep.add(newDependency);
				else if (edgeVisualProperties.contains(parent))
					edgeDep.add(newDependency);
				else
					networkDep.add(newDependency);
			} catch (final Exception e) {
				logger.error("Cannot save dependency: " + (vpDep != null ? vpDep.getDisplayName() : ""), e);
			}
		}
	}
	
	private void restoreDependencies(final VisualStyle visualStyle, org.cytoscape.io.internal.util.vizmap.model.VisualStyle vsModel) {
		final Node nodeSection = vsModel.getNode();
		final Edge edgeSection = vsModel.getEdge();
		final Network networkSection = vsModel.getNetwork();

		final Set<Dependency> dependencyStates = new HashSet<Dependency>();

		if (nodeSection != null)
			dependencyStates.addAll(nodeSection.getDependency());
		if (edgeSection != null)
			dependencyStates.addAll(edgeSection.getDependency());
		if (networkSection != null)
			dependencyStates.addAll(networkSection.getDependency());

		final Set<VisualPropertyDependency<?>> availableDependencies = visualStyle.getAllVisualPropertyDependencies();
		
		for (final Dependency dep : dependencyStates) {
			final String depName = dep.getName();
			final Boolean enabled = dep.isValue();

			for (final VisualPropertyDependency<?> vsDependency : availableDependencies) {
				if (vsDependency.getIdString().equalsIgnoreCase(depName))
					vsDependency.setDependency(enabled);
			}
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
		} else if (attrClass == Byte.class || attrClass == Short.class || attrClass == Integer.class) {
			attrType = AttributeType.INTEGER;
		} else if (attrClass == Long.class) {
			attrType = AttributeType.LONG;
		} else if (Number.class.isAssignableFrom(attrClass)) {
			attrType = AttributeType.FLOAT;
		}

		return attrType;
	}
}
