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
import java.util.UUID;

import org.cytoscape.io.internal.util.vizmap.model.AttributeType;
import org.cytoscape.io.internal.util.vizmap.model.Cell;
import org.cytoscape.io.internal.util.vizmap.model.ColumnStyleAssociation;
import org.cytoscape.io.internal.util.vizmap.model.Dependency;
import org.cytoscape.io.internal.util.vizmap.model.DiscreteMappingEntry;
import org.cytoscape.io.internal.util.vizmap.model.Edge;
import org.cytoscape.io.internal.util.vizmap.model.Network;
import org.cytoscape.io.internal.util.vizmap.model.Node;
import org.cytoscape.io.internal.util.vizmap.model.TableColumnStyle;
import org.cytoscape.io.internal.util.vizmap.model.Vizmap;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.Visualizable;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.vizmap.StyleAssociation;
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

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

/**
 * This is a utility interface used for converting collections of 
 * VisualStyle objects into serializable Vizmap objects and vice versa. 
 */
public class VisualStyleSerializer {

	private final CalculatorConverterFactory calculatorConverterFactory;
	private final CyServiceRegistrar serviceRegistrar;

	private static final Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");

	public VisualStyleSerializer(final CalculatorConverterFactory calculatorConverterFactory,
								 final CyServiceRegistrar serviceRegistrar) {
		this.calculatorConverterFactory = calculatorConverterFactory;
		this.serviceRegistrar = serviceRegistrar;
	}

	/**
	 * This method creates a serializable Vizmap object based on the provided collection of visual styles.
	 * @param styles The collection of VisualStyles that you wish to convert into a serializable object.
	 * @return A Vizmap object that contains a representation of the collection of visual styles.
	 */
	public Vizmap createVizmap(Collection<VisualStyle> networkStyles, Collection<VisualStyle> tableStyles, Collection<StyleAssociation> columnStyleAssociations) {
		final Vizmap vizmap = new Vizmap();
		final RenderingEngineManager renderingEngineManager = serviceRegistrar.getService(RenderingEngineManager.class);
		
		if (networkStyles != null) {
			VisualLexicon lexicon = renderingEngineManager.getDefaultVisualLexicon();
			
			for (VisualStyle style : networkStyles) {
				var vsModel = new org.cytoscape.io.internal.util.vizmap.model.VisualStyle();
				vizmap.getVisualStyle().add(vsModel);

				vsModel.setName(style.getTitle());

				vsModel.setNetwork(new Network());
				vsModel.setNode(new Node());
				vsModel.setEdge(new Edge());

				createVizmapProperties(style, lexicon, BasicVisualLexicon.NETWORK, vsModel.getNetwork().getVisualProperty());
				createVizmapProperties(style, lexicon, BasicVisualLexicon.NODE, vsModel.getNode().getVisualProperty());
				createVizmapProperties(style, lexicon, BasicVisualLexicon.EDGE, vsModel.getEdge().getVisualProperty());
				
				// Create Dependencies
				createDependencies(style, vsModel, lexicon);
			}
		}
		
		var idMap = new ColStyleIdMap();
		VisualLexicon tableLexicon = renderingEngineManager.getDefaultTableVisualLexicon();
		
		if (tableStyles != null) {
			for (VisualStyle style : tableStyles) {
				var id = idMap.getId(style);
				var vsModel = createTableStyle(style, tableLexicon, id);
				vizmap.getTableColumnStyle().add(vsModel);
			}
		}
		
		if(columnStyleAssociations != null) {
			for(var styleAssociation : columnStyleAssociations) {
				var style = styleAssociation.columnVisualStyle();
				if(!idMap.contains(style)) {
					var id = idMap.getId(style);
					var vsModel = createTableStyle(style, tableLexicon, id);
					vsModel.setAssociated(true); // Means the style is only associated.
					vizmap.getTableColumnStyle().add(vsModel);
				}
				
				var associationModel = new ColumnStyleAssociation();
				vizmap.getColumnStyleAssociation().add(associationModel);
				
				associationModel.setColumnName(styleAssociation.colName());
				associationModel.setNetworkStyleName(styleAssociation.networkVisualStyle().getTitle());
				associationModel.setTableColumnStyleId(idMap.getId(style));
				associationModel.setTableType(styleAssociation.tableType().getSimpleName());
			}
		}
		
		return vizmap;
	}
	
	
	private TableColumnStyle createTableStyle(VisualStyle style, VisualLexicon lexicon, String id) {
		var vsModel = new TableColumnStyle();

		vsModel.setName(style.getTitle());
		vsModel.setId(id);
		
		vsModel.setCell(new Cell());

		createVizmapProperties(style, lexicon, BasicTableVisualLexicon.CELL, vsModel.getCell().getVisualProperty());
		
		// Create Dependencies
		createDependencies(style, vsModel, lexicon);
		
		return vsModel;
	}
	

	/**
	 * This method creates a collection of VisualStyle objects based on the provided Vizmap object.
	 * @param vizmap A Vizmap object containing a representation of VisualStyles.
	 * @return A collection of VisualStyle objects.
	 */
	public Set<VisualStyle> createNetworkVisualStyles(final Vizmap vizmap) {
		final Set<VisualStyle> styles = new HashSet<>();
		
		final RenderingEngineManager renderingEngineManager = serviceRegistrar.getService(RenderingEngineManager.class);
		VisualLexicon lexicon = renderingEngineManager.getDefaultVisualLexicon();
		if (lexicon == null) {
			logger.warn("Cannot create visual styles because there is no default Visual Lexicon");
			return styles;
		}

		if (vizmap != null) {
			VisualStyleFactory visualStyleFactory = serviceRegistrar.getService(VisualStyleFactory.class);
			for (var vsModel : vizmap.getVisualStyle()) {
				// Each new style should be created from the default one:
				VisualStyle vs = visualStyleFactory.createVisualStyle(vsModel.getName());

				// Set the visual properties and mappings:
				if (vsModel.getNetwork() != null)
					createVisualProperties(vs, lexicon, CyNetwork.class, vsModel.getNetwork().getVisualProperty());
				if (vsModel.getNode() != null)
					createVisualProperties(vs, lexicon, CyNode.class, vsModel.getNode().getVisualProperty());
				if (vsModel.getEdge() != null)
					createVisualProperties(vs, lexicon, CyEdge.class, vsModel.getEdge().getVisualProperty());

				// Restore dependency
				restoreDependencies(vs, vsModel);
				styles.add(vs);
			}
		}

		return styles;
	}
	
	/**
	 * This method creates a collection of VisualStyle objects based on the provided Vizmap object.
	 * @param vizmap A Vizmap object containing a representation of VisualStyles.
	 * @return A collection of VisualStyle objects.
	 */
	public Map<String,VisualStyle> createTableVisualStyles(final Vizmap vizmap) {
		final Map<String,VisualStyle> styles = new HashMap<>();
		
		final RenderingEngineManager renderingEngineManager = serviceRegistrar.getService(RenderingEngineManager.class);
		VisualLexicon lexicon = renderingEngineManager.getDefaultTableVisualLexicon();
		if (lexicon == null) {
			logger.warn("Cannot create visual styles because there is no default Visual Lexicon");
			return styles;
		}

		if (vizmap != null) {
			VisualStyleFactory visualStyleFactory = serviceRegistrar.getService(VisualStyleFactory.class);
			
			for(var vsModel : vizmap.getTableColumnStyle()) {
				VisualStyle vs = visualStyleFactory.createVisualStyle(vsModel.getName());
				
				if (vsModel.getCell() != null) {
					createVisualProperties(vs, lexicon, CyColumn.class, vsModel.getCell().getVisualProperty());
				}
				
				// Restore dependency
				restoreDependencies(vs, vsModel);
				
				// The "id" field was introduced in cytoscape 3.10. 
				// Session files created with Cytoscape 3.9 won't have this field, in that case use a dummy UUID.
				String id = vsModel.getId();
				if(id == null) {
					id = UUID.randomUUID().toString();
				}
				styles.put(id, vs);
			}
		}

		return styles;
	}
	
	
	public Set<StyleAssociation> createStyleAssociations(final Vizmap vizmap, Set<VisualStyle> networkStyles, Map<String,VisualStyle> columnStyles) { 
		final Set<StyleAssociation> associations = new HashSet<>();
		
		if (vizmap != null) {
			for(var colAssociationModel : vizmap.getColumnStyleAssociation()) {
				var colName = colAssociationModel.getColumnName();
				
				var colStyleId = colAssociationModel.getTableColumnStyleId();
				var colStyle = columnStyles.get(colStyleId);
				
				var tableType = getTableType(colAssociationModel.getTableType());
				
				var netStyle = getNetworkStyle(networkStyles, colAssociationModel.getNetworkStyleName());
				
				if(colName != null && netStyle != null && tableType != null && colStyle != null) {
					associations.add(new StyleAssociation(netStyle, tableType, colName, colStyle));
				}
			}
			
			// Remove columnStyles from the map that are not directly associated.
			for(var tableStyleModel : vizmap.getTableColumnStyle()) {
				if(Boolean.TRUE.equals(tableStyleModel.isAssociated())) {
					columnStyles.remove(tableStyleModel.getId());
				}
			}
			
		}
		
		return associations;
	}
	
	public Class<? extends CyIdentifiable> getTableType(String name) {
		if(name == null)
			return null;
		if(name.equals("CyNode"))
			return CyNode.class;
		if(name.equals("CyEdge"))
			return CyEdge.class;
		return null;
	}
	
	public VisualStyle getNetworkStyle(Collection<VisualStyle> styles, String name) {
		for(var style : styles) {
			if(style.getTitle().equals(name)) {
				return style;
			}
		}
		return null;
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
		var vizmapStyles = vizmap.getVisualStyle();

		// Group properties keys/values by visual style name:
		Map<String, Map<String, String>> styleNamesMap = new HashMap<>();
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

			var vs = new org.cytoscape.io.internal.util.vizmap.model.VisualStyle();
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

		return createNetworkVisualStyles(vizmap);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createVizmapProperties(final VisualStyle vs, VisualLexicon lexicon, final VisualProperty<Visualizable> root,
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
					var vpModel = new org.cytoscape.io.internal.util.vizmap.model.VisualProperty();
					vpModel.setName(vp.getIdString());
					vpModelList.add(vpModel);
	
					try {
						if (defValue != null) {
							String sValue = vp.toSerializableString(defValue);
							if (sValue != null)
								vpModel.setDefault(sValue);
						}
					} catch (final Exception e) {
						System.out.println("CCE in VisualStyleSerielizer");
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
//								System.out.println("DiscreteMapEntry: " + entry.getKey().toString() + " = " + value);
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
	private <K, V> void createVisualProperties(VisualStyle vs, VisualLexicon lexicon,
											   Class<? extends CyIdentifiable> targetType,
											   List<org.cytoscape.io.internal.util.vizmap.model.VisualProperty> vpModelList) {
		for (var vpModel : vpModelList) {
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
					org.cytoscape.io.internal.util.vizmap.model.PassthroughMapping pmModel = vpModel.getPassthroughMapping();
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
						VisualMappingFunctionFactory pmFactory = serviceRegistrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
						PassthroughMapping<K, V> pm = (PassthroughMapping<K, V>) pmFactory.createVisualMappingFunction(attrName, columnDataType, vp);
						vs.addVisualMappingFunction(pm);
					} catch (Throwable e) {
						logger.error("Cannot create PassthroughMapping (style=" + vs.getTitle() + ", property=" + vp.getIdString() + ")", e);
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

						VisualMappingFunctionFactory dmFactory = serviceRegistrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
						DiscreteMapping<K, V> dm = (DiscreteMapping<K, V>) dmFactory.createVisualMappingFunction(attrName, attrClass, vp);

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
						logger.error("Cannot create DiscreteMapping (style=" + vs.getTitle() + ", property=" + vp.getIdString() + ")", e);
					}
				} else if (vpModel.getContinuousMapping() != null) {
					org.cytoscape.io.internal.util.vizmap.model.ContinuousMapping cmModel = vpModel.getContinuousMapping();
					String attrName = cmModel.getAttributeName();

					try {
						VisualMappingFunctionFactory cmFactory = serviceRegistrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
						ContinuousMapping<K, V> cm = (ContinuousMapping<K, V>) cmFactory.createVisualMappingFunction(attrName, Number.class, vp);

						for (org.cytoscape.io.internal.util.vizmap.model.ContinuousMappingPoint pModel : cmModel.getContinuousMappingPoint()) {

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
						logger.error("Cannot create ContinuousMapping (style=" + vs.getTitle() + ", property=" + vp.getIdString() + ")", e);
					}
				}
			}
		}
	}
	
	private void createDependencies(final VisualStyle visualStyle,
			org.cytoscape.io.internal.util.vizmap.model.VisualStyle vsModel, VisualLexicon lexicon) {
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
	
	private void createDependencies(final VisualStyle visualStyle, TableColumnStyle vsModel, VisualLexicon lexicon) {
		// Create serializable Dependency
		final Set<VisualPropertyDependency<?>> dependencies = visualStyle.getAllVisualPropertyDependencies();

		final List<Dependency> colDep = vsModel.getCell().getDependency();

		Collection<VisualProperty<?>> colVisualProperties = lexicon.getAllDescendants(BasicTableVisualLexicon.CELL);

		for (VisualPropertyDependency<?> vpDep : dependencies) {
			try {
				final Dependency newDependency = new Dependency();
				newDependency.setName(vpDep.getIdString());
				newDependency.setValue(vpDep.isDependencyEnabled());
	
				final VisualProperty<?> parent = vpDep.getParentVisualProperty();
				
				if (colVisualProperties.contains(parent))
					colDep.add(newDependency);
			} catch (final Exception e) {
				logger.error("Cannot save dependency: " + (vpDep != null ? vpDep.getDisplayName() : ""), e);
			}
		}
	}
	
	private void restoreDependencies(final VisualStyle visualStyle, org.cytoscape.io.internal.util.vizmap.model.VisualStyle vsModel) {
		final Node nodeSection = vsModel.getNode();
		final Edge edgeSection = vsModel.getEdge();
		final Network networkSection = vsModel.getNetwork();

		final Set<Dependency> dependencyStates = new HashSet<>();

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
	
	private void restoreDependencies(final VisualStyle visualStyle, TableColumnStyle vsModel) {
		final Cell colSection = vsModel.getCell();

		final Set<Dependency> dependencyStates = new HashSet<>();

		if (colSection != null)
			dependencyStates.addAll(colSection.getDependency());

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
