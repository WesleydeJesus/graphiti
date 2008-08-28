/*
 * Copyright (c) 2008, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package net.sf.graphiti.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.graphiti.model.DOMNode;
import net.sf.graphiti.model.Configuration;
import net.sf.graphiti.model.Edge;
import net.sf.graphiti.model.Graph;
import net.sf.graphiti.model.InfoDOMNode;
import net.sf.graphiti.model.PropertyBean;
import net.sf.graphiti.model.SkipDOMNode;
import net.sf.graphiti.model.Vertex;
import net.sf.graphiti.ontology.OntologyFactory;
import net.sf.graphiti.ontology.parameterValues.ParameterValue;
import net.sf.graphiti.ontology.parameters.Parameter;
import net.sf.graphiti.ontology.xmlDescriptions.attributeRestrictions.AttributeRestriction;
import net.sf.graphiti.ontology.xmlDescriptions.xmlAttributes.XMLAttribute;
import net.sf.graphiti.ontology.xmlDescriptions.xmlAttributes.edgeAttributes.EdgeAttribute;
import net.sf.graphiti.ontology.xmlDescriptions.xmlAttributes.otherAttributes.OtherAttribute;
import net.sf.graphiti.ontology.xmlDescriptions.xmlSchemaTypes.XMLSchemaType;
import net.sf.graphiti.ontology.xmlDescriptions.xmlSchemaTypes.complexTypes.Sequence;
import net.sf.graphiti.ontology.xmlDescriptions.xmlSchemaTypes.elements.DocumentElement;
import net.sf.graphiti.ontology.xmlDescriptions.xmlSchemaTypes.elements.Element;
import net.sf.graphiti.ontology.xmlDescriptions.xmlSchemaTypes.elements.TextContentElement;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class provides a generic graph file parser.
 * 
 * @author Jonathan Piat
 * @author Matthieu Wipliez
 * 
 */
public class GenericGraphFileParserBackup {

	private static final String IS_PORT = "isPort";

	private Configuration configuration;

	private Logger log;

	private HashMap<Node, Object> nodeToObj = new HashMap<Node, Object>();

	private HashMap<Element, List<PropertyBean>> ontDomInstances = new HashMap<Element, List<PropertyBean>>();

	/**
	 * Creates a generic graph parser using the given document configuration
	 * tree.
	 * 
	 * @param configuration
	 *            The root of the {@link Configuration} tree.
	 */
	public GenericGraphFileParserBackup(Configuration configuration) {
		this.configuration = configuration;
	}

	private void addOntDomInstance(Element node, PropertyBean bean) {
		List<PropertyBean> beans;
		if ((beans = ontDomInstances.get(node)) == null) {
			beans = new ArrayList<PropertyBean>();
			ontDomInstances.put(node, beans);
		}
		beans.add(bean);
	}

	private PropertyBean getElementFromClass(String ontClass, String refVal) {
		for (Element refType : ontDomInstances.keySet()) {
			if (refType.hasOntClass(ontClass)) {
				List<PropertyBean> references = ontDomInstances.get(refType);
				if (references != null) {
					for (int i = 0; i < references.size(); i++) {
						PropertyBean ref = references.get(i);
						if (ref.getValue(Graph.PARAMETER_ID) instanceof String
								&& ref.getValue(Graph.PARAMETER_ID).equals(
										refVal)) {
							return ref;
						}
					}
				}
			}
		}
		return null;
	}

	private PropertyBean getReference(EdgeAttribute node, String refVal) {
		for (Element refType : node.isReferenceTo()) {
			List<PropertyBean> references = ontDomInstances.get(refType);
			if (references != null) {
				for (int i = 0; i < references.size(); i++) {
					PropertyBean ref = references.get(i);
					if (ref.getValue(Graph.PARAMETER_ID) instanceof String
							&& ref.getValue(Graph.PARAMETER_ID).equals(refVal)) {
						return ref;
					}
				}
			}
		}
		return null;
	}

	/**
	 * This methods checks that the given DOM node called <code>domNode</code>
	 * is defined by the given ontology node called <code>ontNode</code>. Checks
	 * include:
	 * <ol>
	 * <li>name equality</li>
	 * <li>DOM node attributes match the fixed parameters defined (if any). A
	 * fixed parameter is something like &lt;Vertex kind="invisible"&gt; or
	 * &lt;Graph kind="directed"&gt;.</li>
	 * </ol>
	 * 
	 * @param ontNode
	 * @param domNode
	 * @param parentElement
	 * @return
	 */
	private boolean isElementDefined(Element ontNode, Node domNode,
			DOMNode parentElement) {
		boolean correspond = false;

		// If the DOM node has the same name as this ontology node
		if (ontNode.hasName().equals(domNode.getNodeName())) {
			// We apply attribute restrictions (if it has any).
			Iterator<AttributeRestriction> it = ontNode
					.hasAttributeRestriction().iterator();
			NamedNodeMap attributes = domNode.getAttributes();

			correspond = true;
			while (it.hasNext() && correspond) {
				AttributeRestriction attrRestrict = it.next();
				String attrRestrictName = attrRestrict.hasName();
				Node node = attributes.getNamedItem(attrRestrictName);

				if (node == null) {
					// The DOM has no attribute with the same name as our
					// attribute restriction.
					correspond = false;
				} else {
					// The DOM node has an attribute that matches our attribute
					// restriction. It corresponds if the value is the same.
					String attrRestrictValue = attrRestrict.hasValue();
					correspond &= node.getNodeValue().equals(attrRestrictValue);
					if (correspond) {
						parentElement.setValue(attrRestrictName,
								attrRestrictValue);
					}
				}
			}

			if (ontNode.hasOntClass(OntologyFactory.getClassXMLAttribute())
					&& correspond) {
				parseAttribute((XMLAttribute) ontNode, domNode, parentElement);
			}
		}

		return correspond;
	}

	/**
	 * Tries to parse file with every configuration in the {@link Configuration}
	 * tree. This method tries first by the most specialized ontologies, ie by
	 * the leafs of the tree.
	 * 
	 * @param file
	 *            The file to parse.
	 * @param configuration
	 *            A {@link Configuration}. When called by {@link #parse(IFile)},
	 *            this is the root of the document configuration tree.
	 * @return True if <code>file</code> could be parsed with
	 *         <code>configuration</code>, <code>false</code> otherwise.
	 */
	private Graph parse(Document document, Configuration configuration) {
		List<Configuration> children = configuration
				.getConfigurationList(false);
		if (children.isEmpty()) {
			// We have a leaf, try to parse
			return parseWithConfiguration(document, configuration);
		} else {
			// We try the children
			Iterator<Configuration> it = children.iterator();
			Graph isParsed = null;
			while (it.hasNext() && (isParsed == null)) {
				isParsed = parse(document, it.next());
			}

			// And then ourselves
			if (isParsed == null) {
				isParsed = parseWithConfiguration(document, configuration);
			}

			return isParsed;
		}
	}

	/**
	 * Parses the given {@link IFile} with the semantics defined in the
	 * configuration file.
	 * 
	 * @param file
	 *            The file to parse.
	 * @return The new {@link GraphitiDocument}.
	 * @throws IncompatibleConfigurationFile
	 *             If the given file could not be parsed with any of the
	 *             document configurations.
	 */
	public Graph parse(IFile file) throws IncompatibleConfigurationFile {
		try {
			InputStream is = file.getContents(false);
			log = Logger.getLogger(GenericGraphFileParserBackup.class);

			// When parsing, will ignore useless spaces and comments.
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory
					.newInstance();
			builderFactory.setIgnoringComments(true);
			builderFactory.setIgnoringElementContentWhitespace(true);

			// Creates the DOM
			DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
			Document document = docBuilder.parse(is);

			// Parses the DOM
			Graph graph = parse(document, configuration);
			if (graph != null) {
				return graph;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		// could not parse
		throw new IncompatibleConfigurationFile();
	}

	/**
	 * Parses a the DOM implementation of a parameter defined in the ontology
	 * 
	 * @param ontNode
	 *            the ontology node
	 * @param attribute
	 *            the DOM node to parse
	 * @param parentElement
	 *            the parent OntologyNode
	 */
	private void parseAttribute(XMLAttribute ontNode, Node attribute,
			PropertyBean parentElement) {
		PropertyBean trueParentElement;
		// if the element to write is a SkipDOMNode this means that this node
		// attributes values come from the upper level
		if (parentElement instanceof SkipDOMNode) {
			trueParentElement = ((SkipDOMNode) parentElement).getTrueNode();
		} else {
			trueParentElement = parentElement;
		}
		// if this parameter is an EdgeAttribute
		if (ontNode.hasOntClass(OntologyFactory.getClassEdgeAttribute())) {
			PropertyBean ref = getReference((EdgeAttribute) ontNode, attribute
					.getNodeValue());
			if (trueParentElement instanceof Edge
					&& ontNode.hasOntClass(OntologyFactory
							.getClassEdgeAttribute())) {
				Edge edge = (Edge) trueParentElement;
				String nodeName = attribute.getNodeName();
				String nodeValue = attribute.getNodeValue();
				edge.setValue(nodeName, nodeValue);
				setEdgeConnection(ref, (EdgeAttribute) ontNode, edge);
			}
		} else {
			// if this parameter is an OtherAttribute
			if (attribute != null) {
				String value = attribute.getNodeValue();
				if (value == null) {
					value = attribute.getTextContent();
				}
				// if
				// (ontNode.hasOntClass(OntologyFactory.getClassIdParameter()))
				// {
				// parentElement.setValue(Graph.PARAMETER_ID, value);
				// } else {
				OtherAttribute beanNode = (OtherAttribute) ontNode;
				trueParentElement.setValue(beanNode.hasParameter().hasName(),
						value);
				// }
			}
		}
	}

	/**
	 * Find the ontology attribute corresponding to the given DOM element
	 * 
	 * @param ontNodes
	 *            the available attributes for this element
	 * @param attribute
	 *            the DOM element
	 * @param parentElement
	 *            the parent node
	 */
	private void parseAttributeOrStoreDOMAttribute(Set<XMLAttribute> ontNodes,
			Node attribute, DOMNode parentElement) {
		if (ontNodes != null) {
			// We iterate over the ontology nodes to see if the DOM attribute is
			// defined.
			for (XMLAttribute ontNode : ontNodes) {
				String name = ontNode.hasName();
				if (attribute.getNodeName().equals(name)) {
					// This attribute is defined by the ontology, we parse it
					// and return.
					parseAttribute(ontNode, attribute, parentElement);
					return;

				}
			}
		}
		// Either we were given no ontology nodes, or the attribute we are
		// dealing with is not defined by the ontology. Then we just store it as
		// a DOMNode.
		DOMNode domAttribute = new DOMNode(attribute.getNodeName());
		domAttribute.setNodeValue(attribute.getNodeValue());
		parentElement.addDOMAttribute(domAttribute);
	}

	/**
	 * Parses all the attributes defined by
	 * <code>ontElement.hasAttributes()</code>.
	 * 
	 * @param ontElement
	 *            the node as defined in the ontology
	 * @param domNode
	 *            the implementation in the DOM
	 * @param element
	 *            parent element of this parameter
	 */
	private void parseAttributes(Element ontElement, Node domNode,
			DOMNode element) {
		Set<XMLAttribute> attributesNodes = ontElement.hasAttributes();

		// parses the nodes having a DOM implementation
		NamedNodeMap attributes = domNode.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			parseAttributeOrStoreDOMAttribute(attributesNodes, attributes
					.item(i), element);
		}
	}

	/**
	 * Parses the given DOM node "domNode" as described in the ontology's
	 * instance ontNode
	 * 
	 * @param ontNode
	 *            the node as defined in the ontology
	 * @param domNode
	 *            the implementation of the ontNode in the DOM
	 * @param parentElement
	 *            parent element of this node
	 */
	private void parseElement(Element ontNode, Node domNode,
			DOMNode parentElement) {
		DOMNode element;

		// creates the element according to the type defined in the ontology
		if (ontNode.hasOntClass(OntologyFactory.getClassDocumentElement())) {
			element = null;
		} else if (ontNode.hasOntClass(OntologyFactory.getClassGraphElement())) {
			element = new Graph(configuration);
		} else if (ontNode.hasOntClass(OntologyFactory.getClassVertexElement())) {
			element = new Vertex();
			// if (parentElement instanceof GraphitiDocument) {
			// Graph newGraph = ((GraphitiDocument) parentElement).getGraph();
			// if (newGraph.getValue(Graph.PARAMETER_ID) == null) {
			// newGraph.setValue(Graph.PARAMETER_ID, parentElement
			// .getValue(Graph.PARAMETER_ID));
			// }
			// parentElement = newGraph;
			// }
		} else if (ontNode.hasOntClass(OntologyFactory.getClassEdgeElement())) {
			element = new Edge();
			// if (parentElement instanceof GraphitiDocument) {
			// if (((GraphitiDocument) parentElement).getGraph() == null) {
			// Graph newGraph = new Graph((GraphitiDocument) parentElement);
			// ((GraphitiDocument) parentElement).setGraph(newGraph);
			// parentElement = newGraph;
			// } else {
			// parentElement = ((GraphitiDocument) parentElement)
			// .getGraph();
			// }
			// }
		} else if (ontNode.hasOntClass(OntologyFactory.getClassSkipElement())) {
			// The ontology node is a SkipElement
			element = new SkipDOMNode(parentElement);
		} else if (ontNode.hasOntClass(OntologyFactory
				.getClassTextContentElement())) {
			// Sets parameter values associated with this element
			element = new InfoDOMNode();
			setParameter((TextContentElement) ontNode, parentElement, domNode);
		} else {
			element = new DOMNode(domNode.getNodeName());
		}

		// adding element and value to the parentElement
		// element.setNodeName(domNode.getNodeName());
		// element.setNodeValue(domNode.getNodeValue());

		// if the element is a skipNode the DOMELementdon't need to be parsed
		// has they will parsed later
		if (!(element instanceof SkipDOMNode)) {
			parentElement.addDOMElement(element);
			nodeToObj.put(domNode, element);
			// Sets parameter values associated with this element
			setParameterValues(ontNode, element);
		} else {
			parentElement.addDOMElement(element);
			nodeToObj.put(domNode, ((SkipDOMNode) element).getTrueNode());
			// Sets parameter values associated with this element
			setParameterValues(ontNode, ((SkipDOMNode) element).getTrueNode());
		}
		// Parses recursively attributes and elements
		parseAttributes(ontNode, domNode, element);
		parseElements(ontNode, domNode, element);

		if (element instanceof SkipDOMNode) {
			element = ((SkipDOMNode) element).getTrueNode();
		}
		// if ((parentElement instanceof GraphitiDocument)
		// && (element instanceof Graph)) {
		// ((GraphitiDocument) parentElement).setGraph((Graph) element);
		// } else if ((parentElement instanceof Graph)
		// && (element instanceof Vertex)) {
		// ((Graph) parentElement).addVertex((Vertex) element);
		// } else if ((parentElement instanceof Graph)
		// && (element instanceof Edge)) {
		// treatEdge((Edge) element, (Graph) parentElement);
		// ((Graph) parentElement).addEdge((Edge) element);
		// }

		// TODO: find out about this...
		addOntDomInstance(ontNode, element);
	}

	/**
	 * Find the ontology node corresponding to the given DOM element
	 * 
	 * @param ontNodes
	 *            available node in the ontology for this element
	 * @param domNode
	 *            the element to parse
	 * @param parentElement
	 *            the parent node
	 */
	private void parseElementOrStoreDOMNode(Set<Element> ontNodes,
			Node domNode, DOMNode parentElement) {
		if (ontNodes != null) {
			// We iterate over the ontology nodes to see if the DOM element is
			// defined.
			for (Element ontNode : ontNodes) {
				if (isElementDefined(ontNode, domNode, parentElement)) {
					parseElement(ontNode, domNode, parentElement);
					return;
				}
			}
		}

		// Either we were given no ontology nodes, or the element we are
		// dealing with is not defined by the ontology. Then we just store it as
		// a DOMNode.
		DOMNode domElement = new DOMNode(domNode.getNodeName());
		domElement.setNodeValue(domNode.getNodeValue());
		NamedNodeMap attributes = domNode.getAttributes();
		for (int i = 0; attributes != null && i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			parseAttributeOrStoreDOMAttribute(null, attribute, domElement);
		}

		// parsing the dummy element children
		NodeList children = domNode.getChildNodes();
		for (int i = 0; children != null && i < children.getLength(); i++) {
			parseElementOrStoreDOMNode(null, children.item(i), domElement);
		}

		// adding the dummy element to the parent element
		parentElement.addDOMElement(domElement);
	}

	/**
	 * Parses all the elements defined by
	 * <code>ontElement.hasElementChildren()</code>.
	 * 
	 * @param ontElement
	 *            the node as defined in the ontology
	 * @param domNode
	 *            the implementation in the DOM
	 * @param element
	 *            parent element of this parameter
	 */
	private void parseElements(Element ontElement, Node domNode, DOMNode element) {
		XMLSchemaType type = ontElement.hasSchemaType();
		Set<Element> childNodes = new HashSet<Element>();
		if (type != null) {
			if (type.hasOntClass(OntologyFactory.getClassElement())) {
				childNodes.add((Element) type);
			} else if (type.hasOntClass(OntologyFactory.getClassSequence())) {
				Sequence sequence = (Sequence) type;
				for (XMLSchemaType seqElt : sequence.hasElements()) {
					if (seqElt.hasOntClass(OntologyFactory.getClassElement())) {
						childNodes.add((Element) seqElt);
					}
				}
			}
		}

		// parses the nodes having a DOM implementation
		NodeList children = domNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			parseElementOrStoreDOMNode(childNodes, children.item(i), element);
		}
	}

	/**
	 * Parses the given DOM using the semantics defined in the configuration
	 * file.
	 * 
	 * @param document
	 *            The DOM to parse.
	 * @return True if document could be parsed, false otherwise.
	 */
	private Graph parseWithConfiguration(Document document,
			Configuration configuration) {
		Node docElement = document.getDocumentElement();
		Graph graph = new Graph(configuration);

		// Retrieves the document element
		OntologyFactory factory = configuration.getOntologyFactory();
		DocumentElement ontDocElement = factory.getDocumentElement();
		if (ontDocElement == null) {
			return null;
		} else {
			if (isElementDefined(ontDocElement, docElement, graph)) {
				// parseElement(ontDocElement, docElement, graph);
				log.info("Parsing completed");
				return graph;
			} else {
				return null;
			}
		}
	}

	private void setEdgeConnection(PropertyBean ref,
			EdgeAttribute connectionType, Edge edge) {
		// Vertex connection;
		// if (ref == null || ref instanceof Graph) {
		// connection = new Vertex(edge.getParentDocument());
		// connection.setValue(IS_PORT, true);
		// } else {
		// connection = (Vertex) ref;
		// }
		// if (connectionType.hasOntClass(OntologyFactory
		// .getClassEdgeSourceConnection())) {
		// edge.setSource(connection);
		// } else if (connectionType.hasOntClass(OntologyFactory
		// .getClassEdgeTargetConnection())) {
		// edge.setTarget(connection);
		// } else if (connectionType.hasOntClass(OntologyFactory
		// .getClassEdgeConnection())) {
		// if (edge.getSource() == null) {
		// edge.setSource(connection);
		// } else if (edge.getTarget() == null) {
		// edge.setTarget(connection);
		// }
		// }
	}

	/**
	 * This method is called on an TextContentElement. A Parameter is obtained
	 * from it using referencesParameter, and we set the value of the parameter
	 * name to the domElement text content.
	 * 
	 * @param ontNode
	 * @param parent
	 * @param domElement
	 */
	private void setParameter(TextContentElement ontNode, DOMNode parent,
			Node domElement) {
		Parameter parameter = ontNode.referencesParameter();
		String parameterName = parameter.hasName();
		parent.setValue(parameterName, domElement.getTextContent());
	}

	/**
	 * Iterates through the parameter values associated with this ontology node,
	 * and for each of them, calls {@link DOMNode#setValue(String, Object)} with
	 * the parameter name and value. Example of a parameter value is "Port"
	 * associated to the parameter with the name "type".
	 * 
	 * @param ontNode
	 * @param element
	 */
	private void setParameterValues(Element ontNode, DOMNode element) {
		Set<ParameterValue> attributesNodes = ontNode.hasParameterValues();
		for (ParameterValue attNode : attributesNodes) {
			ParameterValue constant = (ParameterValue) attNode;
			String parameterName = constant.ofParameter().hasName();
			element.setValue(parameterName, constant.hasValue());
		}
	}

	private void treatEdge(Edge edge, Graph parentGraph) {
		if (edge.getSource().getValue(IS_PORT) != null
				&& (Boolean) edge.getSource().getValue(IS_PORT)) {
			Vertex trueSource = (Vertex) getElementFromClass(OntologyFactory
					.getClassVertexElement(), (String) edge
					.getValue(Edge.SRC_PORT_NAME));
			if (trueSource != null) {
				edge.setSource(trueSource);
			} else {
				edge.getSource().setValue(PropertyBean.PROPERTY_NAME,
						(String) edge.getValue(Edge.SRC_PORT_NAME));
				edge.getSource().setValue(Graph.PARAMETER_ID,
						(String) edge.getValue(Edge.SRC_PORT_NAME));
				parentGraph.addVertex(edge.getSource());
			}
		}

		if (edge.getTarget().getValue(IS_PORT) != null
				&& (Boolean) edge.getTarget().getValue(IS_PORT)) {
			Vertex trueTarget = (Vertex) getElementFromClass(OntologyFactory
					.getClassVertexElement(), (String) edge
					.getValue(Edge.DST_PORT_NAME));
			if (trueTarget != null) {
				edge.setTarget(trueTarget);
			} else {
				edge.getTarget().setValue(PropertyBean.PROPERTY_NAME,
						(String) edge.getValue(Edge.SRC_PORT_NAME));
				edge.getTarget().setValue(Graph.PARAMETER_ID,
						(String) edge.getValue(Edge.DST_PORT_NAME));
				parentGraph.addVertex(edge.getTarget());
			}
		}
	}
}