package org.eclipse.stem.graphgenerators.impl;

/*******************************************************************************
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018
 * IBM Corporation, BfR, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors: Armin Weiser, Matthias Filter
 * Bundesinstitut für Risikobewertung
 * FG 43 - Epidemiologie und Zoonosen
 * Diedersdorfer Weg 1, 12277 Berlin
 *
 * IBM Corporation - initial API and implementation
 *******************************************************************************/

import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.stem.core.STEMURI;
import org.eclipse.stem.core.Utility;
import org.eclipse.stem.core.common.DublinCore;
import org.eclipse.stem.core.common.Identifiable;
import org.eclipse.stem.core.graph.Edge;
import org.eclipse.stem.core.graph.Graph;
import org.eclipse.stem.core.graph.GraphFactory;
import org.eclipse.stem.core.graph.Node;
import org.eclipse.stem.core.model.Model;
import org.eclipse.stem.definitions.Activator;
import org.eclipse.stem.definitions.adapters.spatial.geo.InlineLatLongDataProvider;
import org.eclipse.stem.definitions.adapters.spatial.geo.LatLong;
import org.eclipse.stem.definitions.adapters.spatial.geo.LatLong.Segment;
import org.eclipse.stem.definitions.adapters.spatial.geo.LatLong.SegmentBuilder;
import org.eclipse.stem.definitions.adapters.spatial.geo.LatLongProvider;
import org.eclipse.stem.definitions.adapters.spatial.geo.LatLongProviderAdapterFactory;
import org.eclipse.stem.definitions.edges.EdgesFactory;
import org.eclipse.stem.definitions.edges.MigrationEdge;
import org.eclipse.stem.definitions.edges.MigrationEdgeLabel;
import org.eclipse.stem.definitions.edges.MigrationEdgeLabelValue;
import org.eclipse.stem.definitions.edges.impl.EdgesFactoryImpl;
import org.eclipse.stem.definitions.labels.AreaLabel;
import org.eclipse.stem.definitions.labels.CommonBorderRelationshipLabel;
import org.eclipse.stem.definitions.labels.LabelsFactory;
import org.eclipse.stem.definitions.labels.PopulationLabel;
import org.eclipse.stem.definitions.labels.PopulationLabelValue;
import org.eclipse.stem.definitions.labels.impl.CommonBorderRelationshipLabelImpl;
import org.eclipse.stem.definitions.nodes.NodesFactory;
import org.eclipse.stem.definitions.nodes.Region;
import org.eclipse.swt.graphics.Point;

public class PajekNetGraphGeneratorImplOld {
	
	static double RESCALE = 100000.0;

	private File pajekNETFileURI;
	private double scalingFactor;
	
	public PajekNetGraphGeneratorImplOld(File pajekNETFileURI, double scalingFactor) {
		this.pajekNETFileURI = pajekNETFileURI;
		this.scalingFactor = scalingFactor;
	}	

	public Graph getGraph() {
		final Graph graph = GraphFactory.eINSTANCE.createGraph();
		final DublinCore dc = graph.getDublinCore();
		dc.populate();
		dc.setTitle("Pajek Import");
		dc.setSource(this.getClass().getSimpleName());
		Calendar c = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat(
				"E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		String valid = formatter.format(c.getTime());
		dc.setValid(valid);

		List<LinkedHashMap<String, Double>> vPopulationListNodes = new ArrayList<LinkedHashMap<String, Double>>();
		List<EdgeContainer> edgeContainerList = new ArrayList<EdgeContainer>();

		Node nodeHolder[] = new Node[0];

		Scanner input;
		try {
			input = new Scanner(pajekNETFileURI);

			boolean doNodes = false, doEdges = false;
			int numNodes = 0;
			String nodeName[] = new String[0];

			while (input.hasNext()) {
				String nextLine = input.nextLine();
				StreamTokenizer tok = new StreamTokenizer(new StringReader(
						nextLine.trim()));
				tok.parseNumbers();
				tok.wordChars('_', '_'); // otherwise s_size wouldn't be
											// detected
				int ttype = getNextToken(tok);
				if (ttype == StreamTokenizer.TT_WORD) {
					// e.g. Vertices 26
					if (tok.sval.equalsIgnoreCase("vertices")) {
						doNodes = true;
						doEdges = false;
						ttype = getNextToken(tok);
						if (ttype == StreamTokenizer.TT_NUMBER) {
							numNodes = (int) tok.nval;
							nodeHolder = new Node[numNodes];
							nodeName = new String[numNodes];
							numNodes = 0;
						} else {
							Activator
									.logError(
											"Pajek importing "
													+ pajekNETFileURI.getName()
													+ ": vertices keyword without valid number of nodes... Not processable!",
											null);
							input.close();
							return null;
						}
					} else if (tok.sval.equalsIgnoreCase("arcs")
							|| tok.sval.equalsIgnoreCase("arcslist")
							|| tok.sval.equalsIgnoreCase("edges")) {
						doNodes = false;
						doEdges = true;
					} else {
						doNodes = false;
						doEdges = false;
					}
				} else if (doNodes && ttype == StreamTokenizer.TT_NUMBER) { // Nodes
																			// =
																			// Vertices
					// default values
					double x = 0, y = 0; // z = 0
					String shape = "box";
					double nodeArea = 0;
//					Color shapeColor = null;
					LinkedHashMap<String, Double> vPopulation = new LinkedHashMap<String, Double>();

					String populationIdentifier = "";
					String parameter;

					// vertex/node number
					if (tok.nval != numNodes + 1) {
						Activator
								.logError(
										"Pajek importing: "
												+ pajekNETFileURI.getName()
												+ ": usually successive numbering of vertices/nodes... Please check! Exiting!",
										null);
						input.close();
						return null;
					} else {
						nodeName[numNodes] = "" + (int) tok.nval;
					}

					// label
					ttype = getNextToken(tok);
					if (ttype == StreamTokenizer.TT_WORD || ttype == '"')
						nodeName[numNodes] = tok.sval;
					else if (ttype == StreamTokenizer.TT_NUMBER) {
						nodeName[numNodes] = "" + (int) tok.nval;
					} else {
						Activator
								.logError(
										"Pajek importing: "
												+ pajekNETFileURI.getName()
												+ ": a label for the node/vertex is obligatory... Exiting!",
										null);
						input.close();
						return null;
					}

					// coordinates of vertex/node, at least x and y, z is not
					// used at the moment
					ttype = getNextToken(tok);
					if (ttype == StreamTokenizer.TT_NUMBER) {
						x = tok.nval;
					}
					/*
					 * else { Activator.logError("Pajek importing: " +
					 * pajekNETFileURI.getName() +
					 * ": coordinates for the node/vertex are necessary... Exiting!"
					 * , null); input.close(); return null; }
					 */
					ttype = getNextToken(tok);
					if (ttype == StreamTokenizer.TT_NUMBER) {
						y = tok.nval;
					}

					ttype = getNextToken(tok);
					if (ttype == StreamTokenizer.TT_NUMBER) {
//						z = tok.nval;
					} else {
						tok.pushBack();
					}

					// shape
					ttype = getNextToken(tok);
					if (ttype == StreamTokenizer.TT_WORD) {
						parameter = tok.sval.toLowerCase();
						if (parameter.equals("ellipse")
								|| parameter.equals("box")
								|| parameter.equals("diamond")
								|| parameter.equals("triangle")
								|| parameter.equals("cross")
								|| parameter.equals("empty")) {
							shape = parameter;
						} else {
							tok.pushBack();
						}
					}

					/*
					 * other interesting parameters: - s_size - default size -
					 * bc - boundary color of vertex
					 */
					while (ttype != StreamTokenizer.TT_EOF) {
						ttype = getNextToken(tok);
						if (ttype == StreamTokenizer.TT_WORD) {
							parameter = tok.sval.toLowerCase();
							if (parameter.equals("s_size")) {
								ttype = getNextToken(tok);
								if (ttype == StreamTokenizer.TT_NUMBER) {
									nodeArea = tok.nval;
								} else {
									Activator
											.logError(
													"Pajek importing: "
															+ pajekNETFileURI
																	.getName()
															+ ": size of node has wrong format... Exiting!",
													null);
									input.close();
									return null;
								}
							} else if (parameter.equals("bc")) {
								ttype = getNextToken(tok);
								if (ttype == StreamTokenizer.TT_WORD) {
//									shapeColor = getColor(tok.sval);
								} else {
									Activator
											.logError(
													"Pajek importing: "
															+ pajekNETFileURI
																	.getName()
															+ ": shapeColor value has wrong format... Exiting!",
													null);
									input.close();
									return null;
								}
							} else if (parameter.equals("popcount")) {
								ttype = getNextToken(tok);
								if (ttype == StreamTokenizer.TT_NUMBER) {
									// populationCount = tok.nval;
									vPopulation.put(populationIdentifier,
											tok.nval);
								} else {
									Activator
											.logError(
													"Pajek importing: "
															+ pajekNETFileURI
																	.getName()
															+ ": popCount value has wrong format... Exiting!",
													null);
									input.close();
									return null;
								}
							} else if (parameter.equals("popid")) {
								ttype = getNextToken(tok);
								if (ttype == StreamTokenizer.TT_WORD) {
									populationIdentifier = tok.sval;
								} else {
									Activator
											.logError(
													"Pajek importing: "
															+ pajekNETFileURI
																	.getName()
															+ ": popCount value has wrong format... Exiting!",
													null);
									input.close();
									return null;
								}
							} else {
								ttype = getNextToken(tok);
							}
						}
					}

					// Thats it. At the moment we are not interested in more
					// parameters, maybe later...

					// scale x,y,z by scaling factor
					x *= scalingFactor;
					y *= scalingFactor;
//					z *= scalingFactor;

					// Creating node:

					final Region regionNode = createRegionNode("PAJNET;"
							+ nodeName[numNodes] + ";", x, y, graph);
					double area = nodeArea * scalingFactor * scalingFactor;
					final AreaLabel areaLabel = LabelsFactory.eINSTANCE
							.createAreaLabel();
					areaLabel.getCurrentAreaValue().setArea(area);
					regionNode.getLabels().add(areaLabel);

					final LatLong nodeSegments = createNodePolygon(x, y, area,
							shape);
					final String spatialURIString = InlineLatLongDataProvider
							.createSpatialInlineURIString(nodeSegments);
					regionNode.getDublinCore().setSpatial(spatialURIString);

					nodeHolder[numNodes] = regionNode;
					numNodes++;

					// vPop
					vPopulationListNodes.add(vPopulation);
				}

				// Edges
				else if (doEdges && ttype == StreamTokenizer.TT_NUMBER) { // Edges
																			// =
																			// Arcs
					int a, b = 0;
					int borderLength = 0;

					double[] migrationRates = new double[2];
					migrationRates[0] = 0;
					migrationRates[1] = 0;
					String populationIdentifier = "";
					LinkedHashMap<String, double[]> vPopulation = new LinkedHashMap<String, double[]>();
//					Color edgeColor = null;

					// initial vertex number
					a = (int) tok.nval;

					// terminal vertex number
					ttype = getNextToken(tok);
					if (ttype == StreamTokenizer.TT_NUMBER)
						b = (int) tok.nval;
					else {
						Activator
								.logError(
										"Pajek importing: "
												+ pajekNETFileURI.getName()
												+ ": wrong arcs format. Cannot identify terminal node with initial node "
												+ a + " ... Exiting!", null);
						input.close();
						return null;
					}

					// value of arc from a to b, we interpret it as
					// borderLength, usually in Pajek format it is thickness of
					// arc
					ttype = getNextToken(tok);
					if (ttype == StreamTokenizer.TT_NUMBER) {
						borderLength = (int) tok.nval;
					} else if (ttype == StreamTokenizer.TT_EOF) {
						// borderLength is not defined ; no problem for
						// MigrationEdges
					} else {
						tok.pushBack();
						/*
						 * Activator.logError("Pajek importing: " +
						 * pajekNETFileURI.getName() +
						 * ": wrong arcs format. Cannot identify value of arc between node "
						 * + a + " and node " + b + " ... Exiting!", null);
						 * input.close(); return null;
						 */
					}
					if (borderLength < 0)
						borderLength *= -1;

					while (ttype != StreamTokenizer.TT_EOF) {
						ttype = getNextToken(tok);
						if (ttype == StreamTokenizer.TT_WORD) {
							String parameter = tok.sval.toLowerCase();
							if (parameter.equals("c")) {
								ttype = getNextToken(tok);
								if (ttype == StreamTokenizer.TT_WORD) {
//									edgeColor = getColor(tok.sval);
								} else {
									Activator
											.logError(
													"Pajek importing: "
															+ pajekNETFileURI
																	.getName()
															+ ": color of edge has wrong format... Exiting!",
													null);
									input.close();
									return null;
								}
							} else if (parameter.equals("rateab")) {
								ttype = getNextToken(tok);
								if (ttype == StreamTokenizer.TT_NUMBER) {
									// migrationRateAB = tok.nval;
									migrationRates[0] = tok.nval;
									vPopulation.put(populationIdentifier,
											migrationRates);
								} else {
									Activator
											.logError(
													"Pajek importing: "
															+ pajekNETFileURI
																	.getName()
															+ ": migration rate AB of edge has wrong format... Exiting!",
													null);
									input.close();
									return null;
								}
							} else if (parameter.equals("rateba")) {
								ttype = getNextToken(tok);
								if (ttype == StreamTokenizer.TT_NUMBER) {
									// migrationRateBA = tok.nval;
									migrationRates[1] = tok.nval;
									vPopulation.put(populationIdentifier,
											migrationRates);
								} else {
									Activator
											.logError(
													"Pajek importing: "
															+ pajekNETFileURI
																	.getName()
															+ ": migration rate BA of edge has wrong format... Exiting!",
													null);
									input.close();
									return null;
								}
							} else if (parameter.equals("popid")) {
								ttype = getNextToken(tok);
								if (ttype == StreamTokenizer.TT_WORD) {
									populationIdentifier = tok.sval;
								} else {
									Activator
											.logError(
													"Pajek importing: "
															+ pajekNETFileURI
																	.getName()
															+ ": population identifier of edge has wrong format... Exiting!",
													null);
									input.close();
									return null;
								}
							}
						}
					}
					// vPop
					edgeContainerList.add(new EdgeContainer(a, b, borderLength,
							vPopulation));

				} else {
					Activator
							.logError(
									"Pajek importing: "
											+ pajekNETFileURI.getName()
											+ ": very special Pajek format?!? Not processable!",
									null);
					input.close();
					return null;
				}

			} // input has next

			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		

		// nodes
		for (int i = 0; i < nodeHolder.length; i++) { // System.err.println("node "
														// + i + ": URI: "+
														// nodeHolder[i].getURI());
			Map<String, Double> vPopulation = vPopulationListNodes.get(i);
			if (vPopulationListNodes.get(i) != null)
				// System.err.println("vPop node size: " +
				// vPopulationListNodes.get(i).size());
				for (Map.Entry<String, Double> entry : vPopulation.entrySet()) {
					String populationID = entry.getKey();
					if (populationID.length() > 0) {
						double populationCount = entry.getValue();
						// Create a new population label
						PopulationLabel newLabel = LabelsFactory.eINSTANCE
								.createPopulationLabel();
						newLabel.setPopulationIdentifier(populationID);
						newLabel.setURIOfIdentifiableToBeLabeled(nodeHolder[i]
								.getURI());
						newLabel.setNode(nodeHolder[i]);
//						URI newURI = createPopulationLabelURI(nodeHolder[i],
//								populationID);
						// graph.getNodeLabels().put(newURI, newLabel);
						initializeLabel(newLabel, populationID, populationCount);
					}
				}
			graph.putNode(nodeHolder[i]);
		}

		// edges
		for (EdgeContainer edgeContainer : edgeContainerList) {
			int a = edgeContainer.a;
			int b = edgeContainer.b;
			int borderLength = edgeContainer.borderLength;
			LinkedHashMap<String, double[]> vPopulation = edgeContainer.vPopulation;

			if (vPopulation.size() == 0) {
				createCommonBorderEdge(graph, nodeHolder[a - 1],
						nodeHolder[b - 1], borderLength);
			} else {
				for (Map.Entry<String, double[]> entry : vPopulation.entrySet()) {
					String populationID = entry.getKey();
					if (populationID.length() > 0) {
						double[] migrationRates = entry.getValue();
						createMigrationEdge(graph, nodeHolder[a - 1],
								nodeHolder[b - 1], entry.getKey(),
								migrationRates[0], migrationRates[1]);
					}
				}
			}
		}

		assert graph.sane();
		return graph;
	}

	private int getNextToken(StreamTokenizer tok) throws IOException {
		int ttype = 0;
		while ((ttype = tok.nextToken()) != StreamTokenizer.TT_WORD
				&& ttype != StreamTokenizer.TT_NUMBER
				&& ttype != StreamTokenizer.TT_EOF && ttype != '"') {
			;
		}
		return ttype;
	}	

	/**
	 * 
	 * @param x
	 * @param y
	 * @param area
	 * @return
	 */
	private LatLong createNodePolygon(final double x, double y, double area,
			String shape) {
		final LatLong retValue = new LatLong();

		final SegmentBuilder sb = new SegmentBuilder();
		// ellipse box diamond triangle cross empty
		if (shape.toLowerCase().equals("triangle")) {
			double areax2 = Math.sqrt(4 * area / Math.sqrt(3)) / 2;
			double areay = areax2 / Math.sqrt(3);
			sb.add(x - areax2, y - areay);
			sb.add(x + areax2, y - areay);
			sb.add(x, y + 2 * areay);
			sb.add(x - areax2, y - areay);
		} else if (shape.toLowerCase().equals("cross")) {
			double area2 = Math.sqrt(2 * area / Math.sqrt(3)) / 2;
			sb.add(x + area2, y + area2);
			sb.add(x - area2, y - area2);
			sb.add(x - area2, y + area2);
			sb.add(x + area2, y - area2);
			sb.add(x + area2, y + area2);
		} else if (shape.toLowerCase().equals("ellipse")) { // circle
			double r = Math.sqrt(area / Math.PI);
			int numSegs = 20;
			for (int i = 0; i <= numSegs; i++) {
				sb.add(x + r * Math.cos(2 * Math.PI * i / numSegs), y + r
						* Math.sin(2 * Math.PI * i / numSegs));
			}
		} else { // box,diamond,empty,default
			double area2 = Math.sqrt(area) / 2;
			// We just make a square...
			sb.add(x - area2, y - area2);
			sb.add(x - area2, y + area2);
			sb.add(x + area2, y + area2);
			sb.add(x + area2, y - area2);
			sb.add(x - area2, y - area2);
		}
		retValue.add(sb.toSegment());

		return retValue;
	} // createNodePolygon

	/**
	 * @param x
	 *            the x coord of the node in the lattice
	 * @param y
	 *            the y coord of the node in the lattice
	 * @return a node
	 */
	private Region createRegionNode(String uriPrefix, double x, double y,
			Graph graph) {
		final Region node = NodesFactory.eINSTANCE.createRegion();
		final DublinCore dc = node.getDublinCore();
		dc.setTitle(createNodeTitle(x, y));
		node.setURI(createRegionNodeURI(uriPrefix, x, y, graph));
		return node;
	} // createRegionNode

	/**
	 * @param x
	 *            the x of the node in the lattice
	 * @param y
	 *            the y of the node in the lattice
	 * @return a title for a node
	 */
	private String createNodeTitle(double x, double y) {

		final StringBuilder sb = new StringBuilder("Node[");
		sb.append(String.valueOf(((long) (x * 100)) / 100));
		sb.append(", ");
		sb.append(String.valueOf(((long) (y * 100)) / 100));
		sb.append("]");
		return sb.toString();
	} // createNodeTitle

	/**
	 * @param x
	 *            the x of the region in the lattice
	 * @param y
	 *            the y of the region in the lattice
	 * @return a URI for the region node
	 */
	private URI createRegionNodeURI(String uriPrefix, double x, double y,
			Graph graph) {
		String nodeUriSuffix = uriPrefix
				+ String.valueOf(((long) (x * 100)) / 100) + ";"
				+ String.valueOf(((long) (y * 100)) / 100);

		String nodeUriString = Node.URI_TYPE_NODE_SEGMENT + "/"
				+ graph.getURI().lastSegment() + "/" + nodeUriSuffix;

		URI uri = STEMURI.createURI(nodeUriString);
		return uri;
	} // createRegionNodeURI

	/**
	 * 
	 * @param graph
	 * @param nodeA
	 * @param nodeB
	 * @param borderLength
	 */
	private void createCommonBorderEdge(final Graph graph, final Node nodeA,
			final Node nodeB, int borderLength) {

		// create the edge
		final Edge edge = CommonBorderRelationshipLabelImpl
				.createCommonBorderRelationship(nodeA, nodeB, borderLength);

		URI edgeURI = edge.getURI();
		// the autogenerated edge uri is not correct. Need to replace it
		String sEdge = edgeURI.toString();
		int last = sEdge.lastIndexOf("/");
		String sEdge1 = sEdge.substring(0, last);
		String sEdge2 = sEdge.substring(last, sEdge.length());
		sEdge = sEdge1 + "/relationship/commonborder" + sEdge2;
		URI newURI = URI.createURI(sEdge);
		edge.setURI(newURI);

		// now we need to set the uri for the label
		CommonBorderRelationshipLabel label = (CommonBorderRelationshipLabel) edge
				.getLabel();
		label.setURI(createEdgeLabelURI(nodeA.getURI(), nodeB.getURI()));

		final DublinCore dc = edge.getDublinCore();
		dc.setTitle(createEdgeTitle(nodeA, nodeB));

		graph.putEdge(edge);
	} // createCommonBorderEdge

	/**
	 * @param x
	 *            the x of the node in the lattice
	 * @param y
	 *            the y of the node in the lattice
	 * @return a title for a node
	 */
	private String createEdgeTitle(Node nodeA, Node nodeB) {
		String nA = nodeA.getDublinCore().getTitle();
		String nB = nodeB.getDublinCore().getTitle();
		final StringBuilder sb = new StringBuilder("Edge[(");
		sb.append(nA);
		sb.append(")<-->(");
		sb.append(nB);
		sb.append(")]");
		return sb.toString();
	} // createEdgeTitle

	/**
	 * 
	 * @param uriA
	 * @param uriB
	 * @return
	 */
	private static URI createEdgeLabelURI(URI uriA, URI uriB) {
		String sA = uriA.lastSegment();
		String sB = uriB.lastSegment();

		// MigrationEdgeLabelItemProvider
		// uses _ as a special character to separate the two nodes
		// so we need to replace it.
		sA = sA.replace('_', '.');
		sB = sB.replace('_', '.');
		String uriString = sA + "_" + sB;

		// System.out.println("createEdgeLabelURI() : FINALLY, uri = "+uriString);

		URI uri = STEMURI.createURI(uriString);
		return uri;
	} // createEdgeLabelURI

	private void createMigrationEdge(final Graph graph, final Node nodeA,
			final Node nodeB, String populationIdentifier,
			double migrationRateAB, double migrationRateBA) {
		URI sourceURI = nodeA.getURI();
		URI targetURI = nodeB.getURI();

		EdgesFactory ef = EdgesFactoryImpl.init();
		MigrationEdge mEdge1 = ef.createMigrationEdge();
		MigrationEdge mEdge2 = ef.createMigrationEdge();

		// The URI of the edge 1
		URI edgeURI1 = mEdge1.getURI();
		// the autogenerated edge uri is not correct. Need to replace it
		String s1 = edgeURI1.toString();
		int last = s1.lastIndexOf("/");
		String sEdge1 = s1.substring(0, last);
		String sEdge2 = s1.substring(last, s1.length());
		s1 = sEdge1 + "/relationship/migration" + sEdge2;
		URI newURI = URI.createURI(s1);
		mEdge1.setURI(newURI);

		// The URI of the edge 2
		URI edgeURI2 = mEdge2.getURI();
		// the autogenerated edge uri is not correct. Need to replace it
		String s2 = edgeURI2.toString();
		int last2 = s2.lastIndexOf("/");
		sEdge1 = s2.substring(0, last2);
		sEdge2 = s2.substring(last, s2.length());
		s2 = sEdge1 + "/relationship/migration" + sEdge2;
		URI newURI2 = URI.createURI(s2);
		mEdge2.setURI(newURI2);

		// ADD The URIs of the nodes connected by these two directed edges
		// A => B
		mEdge1.setNodeAURI(sourceURI);
		mEdge1.setNodeBURI(targetURI);
		// B => A
		mEdge2.setNodeBURI(sourceURI);
		mEdge2.setNodeAURI(targetURI);

		// now we need to set the uri for the label
		MigrationEdgeLabel label1 = mEdge1.getLabel();
		MigrationEdgeLabel label2 = mEdge2.getLabel();

		// System.out.println("BEFORE: mEdgeLabel1 URI was "+label1.getURI().toString());
		// System.out.println("BEFORE: mEdgeLabel2 URI was "+label2.getURI().toString());

		label1.setURI(createEdgeLabelURI(sourceURI, targetURI));
		label2.setURI(createEdgeLabelURI(targetURI, sourceURI));

		MigrationEdgeLabelValue melv1 = label1.getCurrentValue();
		melv1.setMigrationRate(migrationRateAB);
		MigrationEdgeLabelValue melv2 = label2.getCurrentValue();
		melv2.setMigrationRate(migrationRateBA);

		final DublinCore dc1 = mEdge1.getDublinCore();
		dc1.setTitle(createMigrationEdgeTitle(sourceURI, targetURI));

		final DublinCore dc2 = mEdge2.getDublinCore();
		dc2.setTitle(createMigrationEdgeTitle(targetURI, sourceURI));

		mEdge1.setPopulationIdentifier(populationIdentifier);
		mEdge2.setPopulationIdentifier(populationIdentifier);

		graph.putEdge(mEdge1);
		graph.putEdge(mEdge2);
	}

	/**
	 * @param x
	 *            the x of the node in the lattice
	 * @param y
	 *            the y of the node in the lattice
	 * @return a title for a node
	 */
	public static String createMigrationEdgeTitle(URI uriA, URI uriB) {
		String sA = uriA.lastSegment();
		String sB = uriB.lastSegment();
		final StringBuilder sb = new StringBuilder("MigrationEdge[(");
		sb.append(sA);
		sb.append(")<-->(");
		sb.append(sB);
		sb.append(")]");

		return sb.toString();
	} // createEdgeTitle		

	private void initializeLabel(PopulationLabel lab,
			String populationIdentifier, double populationCount) {
		PopulationLabelValue plv = lab.getCurrentPopulationValue();
		plv.setCount(populationCount);

		// Set the valid year to the start year of the sequencer
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		lab.setValidYear(year);

	}

	public boolean containsPoint(double x, double y, Segment datapoints) {
		int i, j;
		boolean isInside = false;
		double[] points = new double[2 * datapoints.size()];

		for (int k = 0, pointsIndex = 0; k < datapoints.size(); k++, pointsIndex += 2) {
			points[pointsIndex] = datapoints.latitude(k);
			points[pointsIndex + 1] = datapoints.longitude(k);
		}

		int numOfPoints = points.length / 2;

		for (i = 0, j = (numOfPoints - 1) * 2; i < points.length - 1;) {
			if ((((points[i + 1] <= y) && (y < points[j + 1])) || ((points[j + 1] <= y) && (y < points[i + 1])))
					&& (x < (points[j] - points[i]) * (y - points[i + 1])
							/ (points[j + 1] - points[i + 1]) + points[i])) {
				isInside = !isInside;
			}
			j = i;
			i += 2;
		}
		return isInside;
	}

	public static Set<LatLong> getSpatials(IProject project, URI location) {
		// U
		Map<String, LatLong> latLongs = new HashMap<String, LatLong>();
		if (project != null) {

			IContainer modelFolder = project.getFolder("models");
			IResource[] models = null;
			try {
				models = modelFolder.members();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (models != null) {
				for (IResource r : models) {
					// ignore system files
					if (r.getName().startsWith("."))
						continue;

					try {
						URI uri = URI.createURI(r.getLocationURI().toString());
						Identifiable id = Utility.getIdentifiable(uri);
						Graph g = ((Model) id).getCanonicalGraphNoDecorate(
								STEMURI.createURI(""), null, null);
						if (id instanceof Model)
							latLongs.putAll(getGraphSpatials(g, location));

					} catch (Exception e) {
						// Skip bad file
					}
				}
			}

			IContainer graphsFolder = project.getFolder("graphs");
			IResource[] graphs = null;
			try {
				graphs = graphsFolder.members();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (graphs != null) {
				for (IResource r : graphs) {
					// ignore system files
					if (r.getName().startsWith("."))
						continue;

					try {
						URI uri = URI.createURI(r.getLocationURI().toString());
						Identifiable id = Utility.getIdentifiable(uri);
						if (id instanceof Graph)
							latLongs.putAll(getGraphSpatials((Graph) id,
									location));

					} catch (Exception e) {
						// Skip bad file
					}
				}
			}
		}
		Set<LatLong> retVal = new HashSet<LatLong>();
		retVal.addAll(latLongs.values());
		return retVal;
	}

	private static Map<String, LatLong> getGraphSpatials(Graph g, URI location) {

		Map<String, LatLong> latlongs = new HashMap<String, LatLong>();
		for (Node n : g.getNodes().values()) {
			URI uriKey = n.getURI();

			// get spatial information of containing node
			if (location != null && !location.toString().trim().equals("")) {
				if (uriKey.toString().equals(location.toString())) {
					// URI spatialURI =
					// URI.createURI(n.getDublinCore().getSpatial().replace("stemspatial:",""));
					final LatLongProvider latLongProvider = (LatLongProvider) LatLongProviderAdapterFactory.INSTANCE
							.adapt(n, LatLongProvider.class);
					final LatLong result = latLongProvider.getLatLong();
					latlongs.put(uriKey.toString(), result);
				}
			}

		}

		return latlongs;
	}// getGraphSpatials

	public static double getArea(Polygon p) {
		double area = 0;
		int[] x = p.xpoints;
		int[] y = p.ypoints;

		for (int i = 0; i < x.length - 1; i++) {
			area += (((double) x[i] * (double) y[i + 1]) - ((double) x[i + 1] * (double) y[i]));
		}
		area /= 2.0;
		return area > 0 ? area : -area;
	}

	public static Point getInteriorCentroid(Polygon p) {
		double maxRange = -1.0;
		double cx = 0;
		double cy = 0;
		Rectangle r = p.getBounds();
		final double STEP = 100.0;

		double deltax = Math.abs((double) r.width);
		double deltay = Math.abs((double) r.height);

		deltax /= STEP;
		deltay /= STEP;

		double x = r.getMinX();
		double y = r.getMinY();

		for (int i = 0; i <= (int) STEP; i++) {
			y = r.getMinY();
			for (int j = 0; j <= (int) STEP; j++) {
				if (p.contains(x, y)) {
					double range = getSqrdEdgeRange(x, y, p);

					if (range >= maxRange) {
						maxRange = range;
						cx = x;
						cy = y;

					}
				}
				y += deltay;
			}
			x += deltax;
		}

		x = cx - deltax;
		y = cy - deltay;

		double y0 = y;
		deltax /= STEP;
		deltay /= STEP;
		for (int i = 0; i <= (int) (2.0 * STEP); i++) {
			y = y0;
			for (int j = 0; j <= (int) (2.0 * STEP); j++) {
				if (p.contains(x, y)) {
					double range = getSqrdEdgeRange(x, y, p);

					if (range >= maxRange) {
						maxRange = range;
						cx = x;
						cy = y;

					}
				}
				y += deltay;
			}
			x += deltax;
		}

		int ix = (int) Math.round(cx);
		int iy = (int) Math.round(cy);
		Point center = new Point(ix, iy);

		return center;
	}// end of get inner centroid

	public static double getSqrdEdgeRange(double qx, double qy, Polygon p) {
		double range = Double.MAX_VALUE;
		int[] ix = p.xpoints;
		int[] iy = p.ypoints;

		for (int i = 0; i < ix.length; i++) {
			double x = ix[i];
			double y = iy[i];
			double dx = qx - x;
			double dy = qy - y;
			double r = (dx * dx) + (dy * dy);
			if (r <= range)
				range = r;
		}
		return range;

	}

	public class EdgeContainer {
		int a;
		int b;
		int borderLength;
		LinkedHashMap<String, double[]> vPopulation;

		public EdgeContainer(int a, int b, int borderLength,
				LinkedHashMap<String, double[]> vPopulation) {
			this.a = a;
			this.b = b;
			this.borderLength = borderLength;
			this.vPopulation = vPopulation;
		}

	}

} // PajekNetGraphGeneratorImpl
