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
package net.sf.graphiti.ontology.elements.impl;

import java.util.Set;

import net.sf.graphiti.ontology.OntologyFactory;
import net.sf.graphiti.ontology.OntologyNodeImpl;
import net.sf.graphiti.ontology.attributeRestrictions.AttributeRestriction;
import net.sf.graphiti.ontology.elements.Element;
import net.sf.graphiti.ontology.elements.ParserParameterNode;
import net.sf.graphiti.ontology.parameterValues.ParameterValue;

import com.hp.hpl.jena.ontology.Individual;

/**
 * @author mwipliez
 * 
 */
public class ElementImpl extends OntologyNodeImpl implements Element {

	public ElementImpl(Individual individual) {
		super(individual);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<ParserParameterNode> hasAttributeNode() {
		return (Set<ParserParameterNode>) listIndividuals(OntologyFactory
				.getPropertyParserNodeHasAttributeNode());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<AttributeRestriction> hasAttributeRestriction() {
		return (Set<AttributeRestriction>) listIndividuals(OntologyFactory
				.getPropertyElementHasAttributeRestriction());
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<Element> hasChildrenNode() {
		return (Set<Element>) listIndividuals(OntologyFactory
				.getPropertyParserNodeHasChildNode());
	}

	@Override
	public String hasName() {
		return getStringProperty(OntologyFactory.getPropertyElementHasName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<ParameterValue> hasParameterValue() {
		return (Set<ParameterValue>) listIndividuals(OntologyFactory
				.getPropertyElementHasParameterValue());
	}

	@Override
	public Element hasPrecedenceNode() {
		return (Element) getIndividualProperty(OntologyFactory
				.getPropertyParserNodeHasPrecedenceNode());
	}

	public String toString() {
		return super.toString() + " | OntologyElement: hasName: " + hasName()
				+ ", hasAttributeNode: " + hasAttributeNode();
	}
}