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
package net.sf.graphiti.ontology.impl;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Implementation of an ontology model.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class OntologyModelImpl extends OntologyBaseImpl {

	private OntModel model;

	/**
	 * Creates a new {@link OntologyModelImpl}.
	 * 
	 * @param model
	 *            The {@link OntModel} this {@link OntologyModelImpl} is based
	 *            on.
	 */
	public OntologyModelImpl(OntModel model) {
		super(null);
		this.model = model;
	}

	/**
	 * Lists all the individuals of the given class of this model.
	 * 
	 * @param clasz
	 *            A class name.
	 * @return A {@link Set} of {@link Object}.
	 */
	@Override
	public Set<?> listIndividuals(String clasz) {
		OntClass individuals = model.getOntClass(clasz);
		ExtendedIterator it = model.listIndividuals(individuals);
		Set<Individual> individualsSet = new HashSet<Individual>();
		while (it.hasNext()) {
			Individual ind = (Individual) it.next();
			if (model.isInBaseModel(ind)) {
				individualsSet.add(ind);
			}
		}

		return convertIndividuals(individualsSet.iterator());
	}

}
