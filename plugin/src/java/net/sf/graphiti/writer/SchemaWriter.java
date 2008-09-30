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
package net.sf.graphiti.writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.graphiti.model.Edge;
import net.sf.graphiti.model.Graph;
import net.sf.graphiti.model.Vertex;
import net.sf.graphiti.ontology.AttributeRestriction;
import net.sf.graphiti.ontology.Choice;
import net.sf.graphiti.ontology.ComplexType;
import net.sf.graphiti.ontology.DocumentFragment;
import net.sf.graphiti.ontology.EdgeElement;
import net.sf.graphiti.ontology.EdgeType;
import net.sf.graphiti.ontology.Element;
import net.sf.graphiti.ontology.GraphType;
import net.sf.graphiti.ontology.OntologyFactory;
import net.sf.graphiti.ontology.OntologyIndividual;
import net.sf.graphiti.ontology.Parameter;
import net.sf.graphiti.ontology.ParameterSource;
import net.sf.graphiti.ontology.ParameterValue;
import net.sf.graphiti.ontology.Sequence;
import net.sf.graphiti.ontology.SequenceType;
import net.sf.graphiti.ontology.Translation;
import net.sf.graphiti.ontology.Type;
import net.sf.graphiti.ontology.VertexElement;
import net.sf.graphiti.ontology.VertexType;
import net.sf.graphiti.ontology.XMLAttribute;
import net.sf.graphiti.ontology.XMLSchemaType;
import net.sf.graphiti.parsers.NotCompatibleException;
import net.sf.graphiti.writer.ContentWriter.Checkpoint;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class fills a DOM document from a given graph according to the schema
 * defined in the ontology.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SchemaWriter {

	private ContentWriter contentWriter;

	private List<Edge> edgeBasket;

	private Graph graph;

	private Logger log;

	private List<Vertex> vertexBasket;

	/**
	 * Creates a new schema writer for the given configuration.
	 * 
	 * @param configuration
	 *            The configuration to use.
	 */
	public SchemaWriter(Graph graph) {
		contentWriter = new ContentWriter();
		edgeBasket = new ArrayList<Edge>(graph.edgeSet());
		this.graph = graph;
		log = Logger.getLogger(SchemaWriter.class);
		log.setLevel(Level.ALL);
		vertexBasket = new ArrayList<Vertex>(graph.vertexSet());
	}

	/**
	 * Checks that the given context will satisfy the given parameter value.
	 * 
	 * @param paValue
	 *            The parameter value.
	 * @param context
	 *            The context
	 */
	private void checkParameterValue(ParameterValue paValue, Object context)
			throws EmptyBasketException {
		if (paValue != null) {
			Parameter parameter = paValue.ofParameter();
			if (parameter != null) {
				String parameterName = parameter.hasName();
				Object value = null;

				ParameterSource source = paValue.hasSource();
				if (source == null || source.isCurrentElement()) {
					Type objectType = parameter.appliesTo();
					if (objectType instanceof EdgeType) {
						value = ((Edge) context).getValue(parameterName);
					} else if (objectType instanceof GraphType) {
						value = ((Graph) context).getValue(parameterName);
					} else if (objectType instanceof VertexType) {
						value = ((Vertex) context).getValue(parameterName);
					}
				} else {
					if (source.isParentElement()) {
					} else if (source.isEdgeSource()) {
						Vertex vertex = ((Edge) context).getSource();
						value = vertex.getValue(parameterName);
					} else if (source.isEdgeTarget()) {
						Vertex vertex = ((Edge) context).getTarget();
						value = vertex.getValue(parameterName);
					}
				}

				String expectedValue = paValue.hasValue();
				if (!expectedValue.equals(value)) {
					throw new EmptyBasketException();
				}
			}
		}
	}

	/**
	 * Picks an element in the list that satisfies the constraints set on the
	 * given element.
	 * 
	 * @param list
	 * @param element
	 * @return
	 * @throws EmptyBasketException
	 *             If the list provided is empty.
	 */
	private Object pick(List<?> list, Element element)
			throws EmptyBasketException {
		Set<ParameterValue> values = element.hasParameterValues();
		for (Object context : list) {
			for (ParameterValue paValue : values) {
				checkParameterValue(paValue, context);
			}

			list.remove(context);
			return context;
		}

		throw new EmptyBasketException();
	}

	/**
	 * Writes the given graph using the given ontology document element.
	 * 
	 * @param ontDocElement
	 *            The ontology document element.
	 * @return The DOM document created.
	 */
	public Document write(XMLSchemaType ontDocElement) {
		try {
			if (ontDocElement
					.hasOntClass(OntologyFactory.getClassComplexType())) {
				writeComplexTypeOccurs((ComplexType) ontDocElement, graph);
			} else {
				writeElement((Element) ontDocElement, graph);
			}
		} catch (EmptyBasketException e) {
			e.printStackTrace();
		}

		return contentWriter.commit();
	}

	private void writeAttributeRestrictions(Element ontElement, Object context) {
		for (AttributeRestriction attr : ontElement.hasAttributeRestriction()) {
			contentWriter.setAttributeRestriction(attr, context);
		}
	}

	/**
	 * Set the attributes defined by the ontology element with the given
	 * context.
	 * 
	 * @param ontElement
	 *            The ontology element.
	 * @param context
	 *            The context.
	 * @throws EmptyBasketException
	 */
	private void writeAttributes(Element ontElement, Object context)
			throws EmptyBasketException {
		Set<XMLAttribute> attributes = ontElement.hasAttributes();
		for (XMLAttribute ontAttribute : attributes) {
			contentWriter.setAttribute(ontAttribute, context);
		}
	}

	/**
	 * Writes a branch of the given {@link Choice} with the given context.
	 * 
	 * @param choice
	 *            The choice.
	 * @param context
	 *            The context.
	 * @throws NotCompatibleException
	 */
	private void writeChoice(Choice choice, Object context)
			throws EmptyBasketException {
		Checkpoint checkpoint = contentWriter.getCheckpoint();
		for (OntologyIndividual type : choice.hasElements()) {
			try {
				writeSchemaType((XMLSchemaType) type, context);
				return;
			} catch (EmptyBasketException e) {
				contentWriter.loadCheckpoint(checkpoint);
			}
		}

		throw new EmptyBasketException();
	}

	/**
	 * Writes the given complex type with the given context.
	 * 
	 * @param type
	 *            A {@link ComplexType}.
	 * @param context
	 *            The context.
	 * @throws NotCompatibleException
	 */
	private void writeComplexType(ComplexType type, Object context)
			throws EmptyBasketException {
		if (type instanceof SequenceType) {
			writeSequence((SequenceType) type, context);
		} else if (type instanceof Choice) {
			writeChoice((Choice) type, context);
		} else {
			log.debug("parseComplexType: type = All");
		}
	}

	/**
	 * Writes the given complex type a specified number of times (between
	 * minOccurs and maxOccurs).
	 * 
	 * @param type
	 *            A {@link ComplexType}.
	 * @param context
	 *            The context.
	 * @throws EmptyBasketException
	 */
	private void writeComplexTypeOccurs(ComplexType type, Object context)
			throws EmptyBasketException {
		int minOccurs = type.minOccurs();
		int maxOccurs = type.maxOccurs();

		// min occurs
		int i = 0;
		for (; i < minOccurs; i++) {
			writeComplexType(type, context);
		}

		// max occurs
		if (maxOccurs > -1) {
			for (; i < maxOccurs; i++) {
				writeComplexType(type, context);
			}
		} else {
			try {
				while (true) {
					writeComplexType(type, context);
				}
			} catch (EmptyBasketException e) {
			}
		}
	}

	/**
	 * Writes the given ontology document fragment a specified number of times
	 * (between minOccurs and maxOccurs).
	 * 
	 * @param ontElement
	 *            An ontology {@link DocumentFragment} that describes an XML
	 *            element in the DOM result.
	 */
	private void writeDocumentFragmentOccurs(DocumentFragment docFragment) {
		int minOccurs = docFragment.minOccurs();
		org.w3c.dom.Element domDocFragment = docFragment.hasXMLContents();

		// min occurs
		for (int i = 0; i < minOccurs; i++) {
			contentWriter.addDocumentFragment(domDocFragment);
		}
	}

	/**
	 * Writes the given ontology element with the given context.
	 * 
	 * @param ontElement
	 *            An ontology {@link Element} that describes an XML element in
	 *            the DOM result.
	 * @param context
	 *            The context.
	 * @throws EmptyBasketException
	 */
	private void writeElement(Element ontElement, Object context)
			throws EmptyBasketException {
		XMLSchemaType type = ontElement.hasSchemaType();

		// new context
		if (ontElement instanceof VertexElement) {
			context = pick(vertexBasket, ontElement);
		} else if (ontElement instanceof EdgeElement) {
			context = pick(edgeBasket, ontElement);
		} else {
			Set<ParameterValue> values = ontElement.hasParameterValues();
			// check that the element satisfies parameter values
			for (ParameterValue paValue : values) {
				checkParameterValue(paValue, context);
			}
		}

		// start the element
		contentWriter.elementStart(ontElement, context);

		// write attributes and attribute restrictions
		writeAttributeRestrictions(ontElement, context);
		writeAttributes(ontElement, context);

		// children
		if (type != null) {
			writeSchemaType(type, context);
		}

		// end the element
		contentWriter.elementEnd(ontElement, context);
	}

	/**
	 * Writes the given ontology element with the given context a specified
	 * number of times (between minOccurs and maxOccurs).
	 * 
	 * @param ontElement
	 *            An ontology {@link Element} that describes an XML element in
	 *            the DOM result.
	 * @param context
	 *            The context.
	 * @throws EmptyBasketException
	 */
	private void writeElementOccurs(Element ontElement, Object context)
			throws EmptyBasketException {
		int minOccurs = ontElement.minOccurs();
		int maxOccurs = ontElement.maxOccurs();
		Checkpoint checkpoint = null;

		// min occurs
		int i;
		for (i = 0; i < minOccurs; i++) {
			writeElement(ontElement, context);
		}

		// max occurs
		try {
			if (maxOccurs > -1) {
				for (; i < maxOccurs; i++) {
					checkpoint = contentWriter.getCheckpoint();
					writeElement(ontElement, context);
				}
			} else {
				while (true) {
					checkpoint = contentWriter.getCheckpoint();
					writeElement(ontElement, context);
				}
			}
		} catch (EmptyBasketException e) {
			contentWriter.loadCheckpoint(checkpoint);
		}
	}

	/**
	 * Writes the given schema type with the given context.
	 * 
	 * @param type
	 *            An {@link XMLSchemaType}.
	 * @param context
	 *            The context.
	 */
	private void writeSchemaType(XMLSchemaType type, Object context)
			throws EmptyBasketException {
		if (type instanceof ComplexType) {
			writeComplexTypeOccurs((ComplexType) type, context);
		} else if (type instanceof Element) {
			writeElementOccurs((Element) type, context);
		} else if (type instanceof DocumentFragment) {
			writeDocumentFragmentOccurs((DocumentFragment) type);
		} else if (type instanceof Translation) {
			writeTranslation((Translation) type, context);
		}
	}

	/**
	 * Writes the given sequence with the given context.
	 * 
	 * @param sequence
	 *            A {@link Sequence}.
	 * @param context
	 *            The context.
	 */
	private void writeSequence(SequenceType sequence, Object context)
			throws EmptyBasketException {
		for (XMLSchemaType type : sequence.hasSequence().hasElements()) {
			writeSchemaType(type, context);
		}
	}

	/**
	 * Writes the given translation with the given context.
	 * 
	 * @param translation
	 *            A {@link Translation}.
	 * @param context
	 *            The context.
	 */
	private void writeTranslation(Translation translation, Object context)
			throws EmptyBasketException {
		contentWriter.addTranslation(translation, context);
	}
}
