package net.sf.graphiti.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.graphiti.model.DOMNode;
import net.sf.graphiti.model.DocumentConfiguration;
import net.sf.graphiti.model.Edge;
import net.sf.graphiti.model.Graph;
import net.sf.graphiti.model.GraphitiDocument;
import net.sf.graphiti.model.PropertyBean;
import net.sf.graphiti.model.SkipDOMNode;
import net.sf.graphiti.model.Vertex;
import net.sf.graphiti.ontology.OntologyFactory;
import net.sf.graphiti.ontology.nodes.ParserNode;
import net.sf.graphiti.ontology.nodes.ParserParameterNode;
import net.sf.graphiti.ontology.nodes.parameters.ConstantParameter;
import net.sf.graphiti.ontology.nodes.parameters.ParserFixedParameter;
import net.sf.graphiti.ontology.nodes.parameters.PropertyBeanParameter;
import net.sf.graphiti.ontology.nodes.parameters.edges.EdgeParameterNode;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GenericGraphFileParser {

	private static final String IS_PORT = "isPort";

	private List<DocumentConfiguration> configurations;

	private OntologyFactory factory;

	private GraphitiDocument graphitiDocument;

	private Logger log;

	private HashMap<Node, Object> nodeToObj = new HashMap<Node, Object>();

	private HashMap<ParserNode, List<PropertyBean>> ontDomInstances = new HashMap<ParserNode, List<PropertyBean>>();

	/**
	 * Creates a generic graph parser using the given document configurations.
	 * 
	 * @param configurations
	 *            Available configurations.
	 */
	public GenericGraphFileParser(List<DocumentConfiguration> configurations) {
		this.configurations = configurations;
	}

	private void addOntDomInstance(ParserNode node, PropertyBean bean) {
		List<PropertyBean> beans;
		if ((beans = ontDomInstances.get(node)) == null) {
			beans = new ArrayList<PropertyBean>();
			ontDomInstances.put(node, beans);
		}
		beans.add(bean);
	}

	private PropertyBean getElementFromClass(String ontClass, String refVal) {
		for (ParserNode refType : ontDomInstances.keySet()) {
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

	private PropertyBean getReference(ParserParameterNode node, String refVal) {
		for (ParserNode refType : node.isReferenceTo()) {
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
	 * Parses the given InputStream using the stream semantic defined in the
	 * configuration file
	 * 
	 * @param file
	 *            The file to parse.
	 * @return The new GraphitiDocument
	 */
	public GraphitiDocument parse(IFile file) {
		for (DocumentConfiguration config : configurations) {
			try {
				InputStream is = file.getContents(false);
				factory = config.getOntologyFactory();
				log = Logger.getLogger(GenericGraphFileParser.class);

				return parseWithConfiguration(config, is);
			} catch (IncompatibleConfigurationFile e) {
				// Could not parse the file, trying with another ontology
				continue;
			} catch (NullPointerException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Parses all the parameters (including constant ones) defined in the
	 * ontology node <code>ontNode</code>.
	 * 
	 * @param ontNode
	 *            the node as defined in the ontology
	 * @param domNode
	 *            the implementation in the DOM
	 * @param element
	 *            parent element of this parameter
	 */
	private void parseAllParameter(ParserNode ontNode, Node domNode,
			DOMNode element) {
		Set<ParserParameterNode> attributesNodes = ontNode.hasAttributeNode();
		// parses all the constant nodes
		for (ParserParameterNode attNode : attributesNodes) {
			if (attNode
					.hasOntClass(OntologyFactory.getClassConstantParameter())) {
				ConstantParameter constant = (ConstantParameter) attNode;
				String parameterName = constant.hasParameter().hasName();
				element.setValue(parameterName, constant.hasValue());
			}
		}

		// parses the nodes having a DOM implementation
		NamedNodeMap attributes = domNode.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			parseCorrespondingParameter(attributesNodes, attributes.item(i),
					element);
		}
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
	private void parseCorrespondingNode(Set<ParserNode> ontNodes, Node domNode,
			DOMNode parentElement) {
		// if there is nodes defined in the Ontology for this DOM implementation
		// find the corresponding node
		// and parse it
		if (ontNodes != null) {
			for (ParserNode ontNode : ontNodes) {
				if (ontNode.hasName().equals(domNode.getNodeName())) {
					boolean correspond = true ;
					for(ParserFixedParameter fixParam : ontNode.hasFixedParameter()){
						if(domNode.getAttributes().getNamedItem(fixParam.hasName()) != null){
							correspond &= domNode.getAttributes().getNamedItem(fixParam.hasName()).getNodeValue().equals(fixParam.hasValue());
						}else{
							correspond = false ;
						}
					}
					if (ontNode.hasOntClass(OntologyFactory
							.getClassParserParameterNode()) && correspond) {
						parseParameter((ParserParameterNode) ontNode, domNode,
								parentElement);
					}
					
					if(correspond){
						parseNode(ontNode, domNode, parentElement);
						return;
					}
					
				}
			}
		}

		// no corresponding ont element has been found or ontNodes was null
		// creates and parses a dummyElement
		DOMNode domElement = new DOMNode(domNode.getNodeName());
		domElement.setNodeValue(domNode.getNodeValue());
		NamedNodeMap attributes = domNode.getAttributes();
		for (int i = 0; attributes != null && i < attributes.getLength(); i++) {
			parseCorrespondingParameter((Set<ParserParameterNode>) null,
					attributes.item(i), domElement);
		}

		// parsing the dummy element children
		NodeList children = domNode.getChildNodes();
		for (int i = 0; children != null && i < children.getLength(); i++) {
			parseCorrespondingNode(null, children.item(i), domElement);
		}

		// adding the dummy element to the parent element
		parentElement.addElement(domElement);
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
	private void parseCorrespondingParameter(Set<ParserParameterNode> ontNodes,
			Node attribute, DOMNode parentElement) {
		// if there is available parameters defined in the ontology for this DOM
		// element
		if (ontNodes != null) {
			for (ParserParameterNode ontNode : ontNodes) {
				if (ontNode.hasName() != null
						&& ontNode.hasName().equals(attribute.getNodeName())) {

					parseParameter(ontNode, attribute, parentElement);
					return;
				}
			}
		}

		// else parse the element in a dummy attribute
		DOMNode domAttribute = new DOMNode(attribute.getNodeName());
		domAttribute.setNodeValue(attribute.getNodeValue());
		parentElement.addAttribute(domAttribute);
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
	private void parseNode(ParserNode ontNode, Node domNode,
			DOMNode parentElement) {
		DOMNode element;

		// creates the element according to the type defined in the ontology
		if (ontNode.hasOntClass(OntologyFactory.getClassGraphNode())) {
			element = new Graph(graphitiDocument);
		} else if (ontNode.hasOntClass(OntologyFactory.getClassVertexNode())) {
			element = new Vertex(graphitiDocument);
			if (parentElement instanceof GraphitiDocument) {
				Graph newGraph = ((GraphitiDocument) parentElement).getGraph();
				if (newGraph.getValue(Graph.PARAMETER_ID) == null) {
					newGraph.setValue(Graph.PARAMETER_ID, parentElement
							.getValue(Graph.PARAMETER_ID));
				}
				parentElement = newGraph;
			}
		} else if (ontNode.hasOntClass(OntologyFactory.getClassEdgeNode())) {
			element = new Edge(graphitiDocument);
			if (parentElement instanceof GraphitiDocument) {
				if (((GraphitiDocument) parentElement).getGraph() == null) {
					Graph newGraph = new Graph((GraphitiDocument) parentElement);
					((GraphitiDocument) parentElement).setGraph(newGraph);
					parentElement = newGraph;
				} else {
					parentElement = ((GraphitiDocument) parentElement)
							.getGraph();
				}
			}
		} else if (ontNode.hasOntClass(OntologyFactory.getClassSkipNode())) {
			// The ontology node is a SkipNode
			element = new SkipDOMNode(parentElement);
		} else {
			element = new DOMNode(domNode.getNodeName());
		}

		// adding element and value to the parentElement
		element.setNodeName(domNode.getNodeName());
		element.setNodeValue(domNode.getNodeValue());
		parentElement.addElement(element);

		while (element instanceof SkipDOMNode) {
			element = ((SkipDOMNode) element).getTrueNode();
		}

		nodeToObj.put(domNode, element);

		// parsing parameters
		parseAllParameter(ontNode, domNode, element);

		// parsing this element children using the children defined in the
		// ontology
		Set<ParserNode> childNodes = ontNode.hasChildrenNode();
		NodeList children = domNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			parseCorrespondingNode(childNodes, children.item(i), element);
		}

		if ((parentElement instanceof GraphitiDocument)
				&& (element instanceof Graph)) {
			((GraphitiDocument) parentElement).setGraph((Graph) element);
		} else if ((parentElement instanceof Graph)
				&& (element instanceof Vertex)) {
			((Graph) parentElement).addVertex((Vertex) element);
		} else if ((parentElement instanceof Graph)
				&& (element instanceof Edge)) {
			treatEdge((Edge) element, (Graph) parentElement);
			((Graph) parentElement).addEdge((Edge) element);
		}
		addOntDomInstance(ontNode, element);
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
	private void parseParameter(ParserParameterNode ontNode, Node attribute,
			PropertyBean parentElement) {

		// if this parameter is a propertyBeanParameter or an IdParameter
		if (ontNode.hasOntClass(OntologyFactory.getClassIdParameter())
				|| ontNode.hasOntClass(OntologyFactory
						.getClassPropertyBeanParameter())) {
			if (attribute != null) {
				String value = attribute.getNodeValue();
				if (value == null) {
					value = attribute.getTextContent();
				}
				if (ontNode.hasOntClass(OntologyFactory.getClassIdParameter())) {
					parentElement.setValue(Graph.PARAMETER_ID, value);
				} else {
					PropertyBeanParameter beanNode = (PropertyBeanParameter) ontNode;
					parentElement.setValue(beanNode.hasParameter().hasName(),
							value);
				}
			}
		}
		if (ontNode.isReference()) {
			PropertyBean ref = getReference(ontNode, attribute.getNodeValue());
			if (parentElement instanceof Edge
					&& ontNode.hasOntClass(OntologyFactory
							.getClassEdgeParameterNode())) {
				Edge edge = (Edge) parentElement;
				String nodeName = attribute.getNodeName();
				String nodeValue = attribute.getNodeValue();
				edge.setValue(nodeName, nodeValue);
				setEdgeConnection(ref, (EdgeParameterNode) ontNode, edge);
			}
		}
	}

	/**
	 * Parses the given InputStream using the stream semantic defined in the
	 * configuration file
	 * 
	 * @param inputFile
	 *            The file to parse.
	 * @throws IncompatibleConfigurationFile
	 *             If the given file cannot be parsed with the given
	 *             configuration.
	 */
	private GraphitiDocument parseWithConfiguration(
			DocumentConfiguration config, InputStream inputFile)
			throws IncompatibleConfigurationFile {
		try {
			// When parsing, will ignore useless spaces and comments.
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory
					.newInstance();
			builderFactory.setIgnoringComments(true);
			builderFactory.setIgnoringElementContentWhitespace(true);

			// Creates the DOM
			DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(inputFile);
			Node mlRootNode = doc.getFirstChild();

			// Iterates over the parser root nodes
			for (ParserNode rootNode : factory.getParserRootNodes()) {
				if (!rootNode.hasName().equals(mlRootNode.getNodeName())) {
					throw (new IncompatibleConfigurationFile(
							"RootNode has name " + mlRootNode.getNodeName()
									+ " instead of " + rootNode.hasName()));
				} else {
					graphitiDocument = new GraphitiDocument(config);
					NamedNodeMap attributes = mlRootNode.getAttributes();
					for (int i = 0; attributes != null
							&& i < attributes.getLength(); i++) {
						parseCorrespondingParameter(
								(Set<ParserParameterNode>) null, attributes
										.item(i), graphitiDocument);
					}
					Node nextNode = mlRootNode.getFirstChild();
					while (nextNode != null) {
						parseCorrespondingNode(rootNode.hasChildrenNode(),
								nextNode, graphitiDocument);
						nextNode = nextNode.getNextSibling();
					}

					log.info("Parsing completed");
					return graphitiDocument;
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// The document could not be parser using this configuration
		throw new IncompatibleConfigurationFile();
	}

	private void setEdgeConnection(PropertyBean ref,
			EdgeParameterNode connectionType, Edge edge) {
		Vertex connection;
		if (ref == null || ref instanceof Graph) {
			connection = new Vertex(edge.getParentDocument());
			connection.setValue(IS_PORT, true);
		} else {
			connection = (Vertex) ref;
		}
		if (connectionType.hasOntClass(OntologyFactory
				.getClassEdgeSourceConnection())) {
			edge.setSource(connection);
		} else if (connectionType.hasOntClass(OntologyFactory
				.getClassEdgeTargetConnection())) {
			edge.setTarget(connection);
		} else if (connectionType.hasOntClass(OntologyFactory
				.getClassEdgeConnection())) {
			if (edge.getSource() == null) {
				edge.setSource(connection);
			} else if (edge.getTarget() == null) {
				edge.setTarget(connection);
			}
		}
	}

	private void treatEdge(Edge edge, Graph parentGraph) {
		if (edge.getSource().getValue(IS_PORT) != null
				&& (Boolean) edge.getSource().getValue(IS_PORT)) {
			Vertex trueSource = (Vertex) getElementFromClass(OntologyFactory
					.getClassVertexNode(), (String) edge
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
					.getClassVertexNode(), (String) edge
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